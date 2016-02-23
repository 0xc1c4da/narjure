(ns nal.deriver
  (:require [clojure.walk :as w]
            [clojure.core.match :refer [match]]
            [clojure.string :as s]
            [clojure.set :refer [map-invert]]
            [clojure.core.unify :as u]))

(declare generate-matching)

(defmacro walk
  "Macro that helps to replace elements during walk. The first argument
  is collection, rest of the arguments are cond-like
  expressions. Default result of cond is element itself.
  el is reserved name for current element of collection."
  [coll & conditions]
  (let [el (gensym)
        replace-el (fn [coll] (w/postwalk #(if (= 'el %) el %) coll))]
    `(w/postwalk
       (fn [~el] (cond ~@(replace-el conditions)
                       :else ~el))
       ~coll)))

(defn path
  "Generates premises \"path\" by replacing terms with :any"
  [statement]
  (if (coll? statement)
    (let [[fst & tail] statement]
      (conj (map path tail) fst))
    :any))

(def operators
  #{'&| '--> '<-> '==> 'retro-impl 'pred-impl 'seq-conj 'inst 'prop 'inst-prop
    'int-image 'ext-image '=/> '=|> '& '| '<=> '</> '<|> '- 'int-dif '|| '&&})

(defn infix->prefix
  [premise]
  (if (coll? premise)
    (let [[f s & tail] premise]
      (map infix->prefix
           (if (operators s)
             (concat [s f] tail)
             premise)))
    premise))

(defn rule-path [p1 p2]
  "Generates detailed pattern for the rule."
  [(path p1) :and (path p2)])

(defn cart [colls]
  "Cartesian product."
  (if (empty? colls)
    '(())
    (for [x (first colls)
          more (cart (rest colls))]
      (cons x more))))

(defn path-invariants
  "Generates all pathes that will match with path from args."
  [path]
  (if (coll? path)
    (let [[op & args] path
          args-inv (map path-invariants args)]
      (concat (cart (concat [[op]] args-inv)) [:any]))
    [path]))

(defn all-paths
  "Generates all pathes for pair of premises."
  [p1 p2]
  ;(println p1 p2)
  (let [paths1 (path-invariants (path p1))
        paths2 (path-invariants (path p2))]
    (map vec (cart [paths1 [:and] paths2]))))

(defn options
  "Generates map from rest of the rule's args."
  [args]
  (when-not (empty? args)
    (into {} (map vec (partition 2 args)))))

(defn- neg-symbol?
  [el]
  (and (not= el '-->) (symbol? el) (s/starts-with? (str el) "--")))

(defn- trim-negation
  [el]
  (symbol (apply str (drop 2 (str el)))))

(defn neg [el] (list '-- el))

(defn replace-negation
  "Replaces negations's \"new notation\"."
  [statement]
  (cond
    (neg-symbol? statement) (neg (trim-negation statement))
    (or (vector? statement) (and (seq? statement) (not= '-- (first statement))))
    (:st
      (reduce
        (fn [{:keys [prev st] :as ac} el]
          (if (= '-- el)
            (assoc ac :prev true)
            (->> [(cond prev (neg el)
                        (coll? el) (replace-negation el)
                        (neg-symbol? el) (neg (trim-negation el))
                        :else el)]
                 (concat st)
                 (assoc ac :prev false :st))))
        {:prev false :st '()}
        statement))
    :else statement))

(defn get-conclusions
  "Parses conclusions from the rule."
  [c opts]
  (if (and (seq? c) (some #{:post} c))
    (map (fn [[c _ post]] {:conclusion c :post post}) (partition 3 c))
    [{:conclusion c :post (:post opts)}]))

(defn rule [data]
  "Generates rule from #R statement."
  (let [[p1 p2 _ c & other] (replace-negation data)]
    (let [p1 (infix->prefix p1)
          p2 (infix->prefix p2)
          c (infix->prefix c)
          opts (options other)
          conclusions (get-conclusions c opts)]
      {:p1          p1
       :p2          p2
       :conclusions conclusions
       :full-path   (rule-path p1 p2)
       :pre         (:pre opts)})))

(defn rule->map
  "Adds rule to map of rules, conjoin rule to set of rules that
  matches to pattern. Rules paths are keys in this map."
  [ac {:keys [p1 p2 full-path] :as rule}]
  (-> ac
      (update-in [full-path :rules] conj rule)
      (assoc-in [full-path :pattern] [p1 p2])
      (assoc-in [full-path :all] (all-paths p1 p2))))

(defn add-possible-paths
  "Selects all rules that will match the same path as current rule and adds
  these rules to the set of rules that matches path.
  For instance:
  current rule's path [[--> [- :any :any] :any] :and [--> [:any :any]]]

  so, if we find rule with path [[--> :any :any] :and [--> [:any :any]]],
  it matches to current's rule path too, hence it should be added to the set
  of rules that matches [[--> [- :any :any] :any] :and [--> [:any :any]]] path."
  [ac [k {:keys [all]}]]
  (let [rules (mapcat :rules (vals (select-keys ac all)))]
    (-> ac
        (update-in [k :rules] concat rules)
        (update-in [k :rules] set))))

(defn rules-map
  "Generates map from list of #R satetments, whetre key is path, and value is
  another map with keys pattern ans rules. Pattern is will be used to match
  values from the premises, rules will be used to generate deriver."
  [ruleset]
  (let [rules (reduce rule->map {} ruleset)]
    (-> (reduce add-possible-paths rules rules)
        generate-matching)))

(defn check-duplication
  "Checks if there are rules with same premises and preconditions but with
  different conclusions, merges them if they exist."
  [rules]
  (vals (reduce (fn [ac {:keys [p1 p2 pre conclusions] :as r}]
                  (let [k [p1 p2 pre]]
                    (if (ac k)
                      (update-in ac [k :conclusions] concat conclusions)
                      (assoc ac k r))))
                {} rules)))

(defn allow-backward?
  "Return true if rule allows backward inference."
  [{:keys [conclusions]}]
  (some #{:allow-backward} (:post (first conclusions))))

(defn question?
  "Return true if rule allows only question as task."
  [{:keys [pre]}]
  (some #{:question?} pre))

;equivalences, implications, conjunctions - sets of operators that are use in
; permutation for :order-for-all-same postcondititon
(def equivalences #{'<=> '</> '<|>})
(def implications #{'==> 'pred-impl '=|> 'retro-impl})
(def conjunctions #{'&& '&| 'seq-conj})

(defn contains-op?
  "Checks if statement contains operators from set."
  [statement s]
  (cond
    (symbol? statement) (s statement)
    (keyword? statement) false
    :default (some (complement nil?)
                   (map #(contains-op? % s) statement))))

(defn order-for-all-same?
  "Return true if rule contains order-for-all-same postcondition"
  [{:keys [conclusions]}]
  (some #{:order-for-all-same} (:post (first conclusions))))

(defn replace-op
  "Replaces operator from the set s to op in statement"
  [statement s op]
  (walk statement (s el) op))

(defn permute-op
  "Makes permuatation of operators from s in statement."
  [statement s]
  (if (contains-op? statement s)
    (map #(replace-op statement s %) s)
    [statement]))

(defn generate-all-orders
  "Permutes all operators in statement with :order-for-all-same precondition."
  [{:keys [p1 p2 conclusions full-path pre] :as rule}]
  (let [{:keys [conclusion] :as c1} (first conclusions)
        statements (->> (permute-op [p1 p2 conclusion full-path pre] equivalences)
                        (mapcat (fn [st] (permute-op st conjunctions)))
                        (mapcat (fn [st] (permute-op st implications)))
                        set)]
    (map (fn [[p1 p2 c full-path pre]]
           (assoc rule :p1 p1
                       :p2 p2
                       :full-path full-path
                       :conclusions [(assoc c1 :conclusion c)]
                       :pre pre))
         statements)))

(defn for-questions
  "Filters rules that are allowed for question task."
  [rules]
  (set (concat (filter allow-backward? rules)
               (filter question? rules))))

(defn check-orders [r]
  (if (order-for-all-same? r)
    (generate-all-orders r)
    [r]))

;todo expand :list/A
(defn get-list
  [statement]
  (cond
    (and (keyword? statement) (s/starts-with? (str statement) ":list"))
    statement
    (coll? statement) (some identity (map get-list statement))
    :default nil))

(defn generate-all-lists [r])

(defn check-list [r]
  (if-let [list-name (get-list r)]
    (generate-all-lists r)
    [r]))

(defmacro defrules
  "Define rules. Rules must be #R statements."
  ;TODO exception on duplication of the rule
  [name & rules]
  `(let [rules# (->> (quote ~rules)
                     (map rule)
                     (mapcat check-orders)
                     check-duplication)
         judgement-rules# (remove question? rules#)
         question-rules# (for-questions rules#)]
     (println "Q rules:" (count question-rules#))
     (println "J rules:" (count judgement-rules#))
     (def ~name {:judgement (rules-map judgement-rules#)
                 :question  (rules-map question-rules#)})))

(defn symbol-ordering-keyfn [sym]
  (if (symbol? sym)
    (try (Integer/parseInt (apply str (drop 1 (str sym))))
         (catch Exception _ -100))
    -1))

(defn check-conditions [syms]
  (->> (keep
         (fn [[alias sym]]
           (let [aliases (filter (fn [[a v]]
                                   (and (< (symbol-ordering-keyfn alias)
                                           (symbol-ordering-keyfn a)) (= v sym)))
                                 (dissoc syms alias))]
             (mapcat (fn [[a]] `(= ~alias ~a)) aliases)))
         syms)
       (filter not-empty)))

(defn not-operator?
  "CHecks if element is not operator"
  [el] (re-matches #"[akxA-Z$]" (-> el str first str)))

(def operator? (complement not-operator?))

(defn replace-symbols
  "Replaces elements from statement if finds them in sym-map."
  [conclusion sym-map]
  (let [sym-map (map-invert sym-map)]
    (walk conclusion
      (sym-map el) (sym-map el))))

(defn find-and-replace-symbols
  "Replaces all terms in statemnt to placeholders that will be used in pattern
  matching or unification. Return vector, where the first element is map from
  placeolder to term and the second is statement with replaced terms.
  Form instance:

  (find-and-replace-symbols '[--> [- A B] C] \"x\")
  ;[{x0 A, x1 B, x2 C} [(quote -->) [(quote -) x0 x1] x2]]
  "
  [statement prefix]
  (let [cnt (volatile! 0)
        sym-map (volatile! {})
        get-sym #(symbol (str prefix %))
        result (walk statement
                 (or (and (symbol? el) (not-operator? el))
                     ;TODO remove when :list/A will be expanded
                     (= :list/A el))
                 (let [s (get-sym @cnt)]
                   (vswap! cnt inc)
                   (vswap! sym-map assoc s el)
                   s)
                 (symbol? el) el)]
    [@sym-map result]))

(defn main-pattern [premise]
  (second (find-and-replace-symbols premise "x")))

(defn filter-preconditions [preconditions]
  (reduce (fn [ac condition]
            ;TODO should be refactored
            ;TODO preconditions
            ;:difference :union :intersection
            ;:shift-occurrence-forward :shift-occurrence-backward
            ;:substitute-if-unifies :not-implication-or-equivalence :substitute-if-unifies
            ;:measure-time :concurrent :substitute
            (cond
              (seq? condition)
              (cond
                (= :!= (first condition))
                (conj ac (concat (list `not=) (rest condition)))
                (= :set-ext? (first condition))
                (let [sec (second condition)]
                  (conj ac `(and (seq? ~sec) (= ~'ext-set (first ~sec)))))
                (= :set-int? (first condition))
                (let [sec (second condition)]
                  (conj ac `(and (seq? ~sec) (= ~'int-set (first ~sec)))))
                (= :difference (first condition))
                (conj ac `(and
                            (coll? ~(nth condition 1))
                            (coll? ~(nth condition 2))
                            (let [k# 1
                                  afop# (first ~(nth condition 1))
                                  asop# (first ~(nth condition 2))
                                  aops# (set ['~'ext-set '~'int-set])
                                  afcount# (count ~(nth condition 1))
                                  ascount# (count ~(nth condition 2))]
                              (and (aops# afop#) (aops# asop#) (= afop# asop#)
                                   (>= afcount# 2) (pos? (- afcount# ascount#))))))
                :else ac)
              :else ac))
          [] preconditions))

(defn sort-placeholders [tail]
  (sort-by symbol-ordering-keyfn tail))

(defn premises-pattern
  "Creates map with preconditions and conclusions regarding to the main pattern
   of rules branch.
   Example:

   the main pattern of riles branch is
   [[--> :any [- :any :any]] :and [--> :any :any]]

   before it will be applied to pattern matching it will be transformed to
   [[--> x1 [- x2 x3]] [--> x4 x5]]

   When whe want to use the pattern above to derive conclusions from some
   another rule, we have to map placeholders from pattern to terms in this rule.

   For rule with premises [[--> A B] [--> B C]] and conclusion [--> A C]
   map will be
   {x1 A, ['- x2 x3] B, x4 B, x5 C}

   From this map, conditions will be the list with one condition:
   [(= ['- x2 x3] x4)]

   and conclusion will be
   [--> x1 x4]."
  [pattern premise {:keys [conclusion]} preconditions]
  (let [[sym-map pat] (find-and-replace-symbols premise "?a")
        unification-map (u/unify pattern pat)
        sym-map (into {} (map (fn [[k v]] [(k unification-map) v]) sym-map))
        inverted-sym-map (map-invert sym-map)
        pre (walk (filter-preconditions preconditions)
              (inverted-sym-map el) (inverted-sym-map el)
              (seq? el)
              (let [[f & tail] el]
                (concat (list f) (sort-placeholders tail))))]
    {:conclusion (replace-symbols conclusion sym-map)
     :conditions (walk (concat (check-conditions sym-map) pre)
                   (and (coll? el) (= \a (first (str (first el)))))
                   (concat '() el)
                   (and (coll? el) (not ((conj reserved-operators 'quote)
                                          (first el))))
                   (vec el))}))


#_(defmacro premises-matcher
    "Generates function that will match premises and generate conclusion.
     Experimental.

     ((premises-matcher [[(quote -->) x0 [(quote -) x1 x2]] [(quote -->) x3 x4]]
                         [[--> A C] [--> C B]]
                         [A --> B])
        '[[--> robin [- cat bird]] [--> [- cat bird] animal]])
     ;=> [robin --> animal]
    "
    [pattern premises conclusion]
    (let [{:keys [conditions conclusion]}
          (premises-pattern pattern premises conclusion)]
      `(fn [x#]
         (match x# ~pattern
                (when (and ~@conditions)
                  ~(walk conclusion
                     (and (symbol? el) (operator? el)) `'~el))
                :else nil))))

(defn conds-priorities-map
  "Generates the map of priorities for coditions according to their frequency."
  [conds]
  (->> (mapcat (fn [[cnds k]]
                 (if (not-empty cnds)
                   (map (fn [c] [c k]) cnds)
                   [(list '() k)])) conds)
       (group-by first)
       (map (fn [[k v]] [k (- (count v))]))
       (into {})))

(defn sort-conds
  "Sorts conditions in conditions->conclusions map according to the map of
   priorities of conditions. If condition occurs frequntly it will have higher
   priority."
  [conds cpm]
  (map (fn [[cnds k]] [(sort-by cpm cnds) k]) conds))

(defn group-conditions
  "Groups conditions->conclusions map by first condition and remove it."
  [conds]
  (into {} (map (fn [[k v]] [k (map (fn [[k v]] [(drop 1 k) v]) v)])
                (group-by #(-> % first first) conds))))

(defn conditions->conclusions-map
  "Creates map from conditions to conclusions."
  [main rules]
  (->> rules
       (map (fn [[premises conclusions preconditions]]
              (premises-pattern main premises conclusions preconditions)))
       (group-by :conditions)
       (map (fn [[k v]] [k (map :conclusion v)]))))

(defrecord TrieNode [condition conclusions children])

(defn generate-trie
  "Generates trie of conditions from conditions->conclusions map."
  ([conds] (generate-trie true conds))
  ([cond conds]
   (let [grouped-conditions (group-conditions conds)
         reached-keys (map second (grouped-conditions nil))
         other (dissoc grouped-conditions nil)]
     (->TrieNode cond reached-keys
                 (map (fn [[cond conds]]
                        (generate-trie cond conds))
                      other)))))

(def reserved-operators
  #{`= `not= `seq? `first `and `let `pos? `neg `> `>= `< `<= `coll? `set
    `quote `count 'aops `-})

(defn quote-operators
  [statement]
  (walk statement
    (reserved-operators el) el
    (and (symbol? el) (operator? el)) `'~el
    (and (coll? el) (= 'quote (first el))
         (= 'quote (first (second el))))
    `(quote ~(second (second el)))
    ;TODO remove this condition!
    (#{'I 'X 'Y 'R} el) :a
    (and (coll? el) (= \a (first (str (first el)))))
    (concat '() el)
    (and (coll? el) (not (reserved-operators (first el)))) (vec el)))

(defn traverse-node
  [result {:keys [conclusions children condition]}]
  `(when ~(quote-operators condition)
     ~(when-not (zero? (count conclusions))
        `(vswap! ~result concat ~@(quote-operators conclusions)))
     ~@(map (fn [n] (traverse-node result n)) children)))

(defn traverse [trie]
  (let [results (gensym)]
    `(let [~results (volatile! [])]
       ~(traverse-node results trie)
       @~results)))

(defn match-rules
  [pattern rules]
  `(fn [x#]
     (match x# ~(quote-operators pattern)
            ~(traverse rules)
            :else nil)))

(defn gen-rules [pattern rules]
  (let [main (main-pattern pattern)
        rules (mapcat (fn [{:keys [p1 p2 conclusions pre]}]
                        (map #(vector [p1 p2] % pre) conclusions))
                      rules)
        cond-conclusions-m (conditions->conclusions-map main rules)
        cpm (conds-priorities-map cond-conclusions-m)
        sorted-conds (sort-conds cond-conclusions-m cpm)]
    (generate-trie sorted-conds)))

(defn generate-matching [rules]
  (->> rules
       (map (fn [[k {:keys [pattern rules] :as v}]]
              [k (->> (match-rules (main-pattern pattern)
                                   (gen-rules pattern rules))
                      eval
                      (assoc v :matcher))]))
       (into {})))

(def mall-paths (memoize all-paths))

(defn get-matcher-rec
  [rules [f & tail]]
  (if-let [r (rules f)]
    (:matcher r)
    (get-matcher-rec rules tail)))

(defn get-matcher [rules p1 p2]
  (println :m p1 p2)
  (let [paths (mall-paths p1 p2)]
    (get-matcher-rec rules paths)))

(def mget-matcher (memoize get-matcher))
(defn generate-conclusions [rules [p1 p2 :as premises]]
  ((mget-matcher rules p1 p2) premises))
;!s.contains("task(") && !s.contains("after(") && !s.contains("measure_time(") && !s.contains("Structural") && !s.contains("Identity") && !s.contains("Negation")
