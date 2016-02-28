(ns nal.deriver
  (:require
    [clojure.core.match :refer [match]]
    [clojure.string :as s]
    [clojure.set :refer [map-invert]]
    [clojure.core.unify :as u]
    [nal.deriver.truth :as t]
    [nal.deriver.utils :refer [walk]]
    [nal.deriver.key-path :refer [mall-paths all-paths rule-path]]
    [nal.deriver.rules :refer [rule]]))

;todo commutative:	<-> <=> <|> & | && ||
;todo not commutative: --> ==> =/> =\> </> &/ - ~

(def commutative-ops #{'<-> '<=> '<|> '| '|| 'conj 'ext-inter})
(declare generate-matching reserved-operators)

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

;http://pastebin.com/3zLX7rPx
(defn allow-backward?
  "Return true if rule allows backward inference."
  [{:keys [conclusions]}]
  (some #{:allow-backward} (:post (first conclusions))))

(defn allow-swapping? [{:keys [pre conclusions]}]
  (and (not (some #{:question? :judgement? :goal? :measure-time :t/belief-structural-deduction
                    :t/structural-deduction :t/belief-structural-difference :t/identity
                    :t/negation}
                  (flatten (concat pre (:post (first conclusions))))))
       (not (some commutative-ops
                  (flatten (:conclusion (first conclusions)))))))

;todo what is "after("?
;!s.contains("task(") && !s.contains("after(") && !s.contains("measure_time(") && !s.contains("Structural") && !s.contains("Identity") && !s.contains("Negation")

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

(defn check-orders [r]
  (if (order-for-all-same? r)
    (generate-all-orders r)
    [r]))

;todo expand :list/B
(defn get-list
  [prefix statement]
  (cond
    (and (keyword? statement) (s/starts-with? (str statement) prefix))
    statement
    (coll? statement) (some identity (map #(get-list prefix %) statement))
    :default nil))

(defn gen-symbols [prefix n]
  (map #(symbol (str prefix (inc %))) (range n)))

(defn replace-list-elemets [statement list-name sym n]
  (walk statement
        (and (coll? el) (some #{list-name} el))
        (mapcat (fn [e] (if (= list-name e)
                          (concat '() (gen-symbols sym n))
                          (list e))) el)))

(defn generate-all-lists [list-name sym r]
  (mapcat #(let [st (replace-list-elemets r list-name sym %)]
            (if-let [from-name (get-list ":from" st)]
              (map (fn [idx]
                     (walk st (= from-name el) (symbol (str sym idx))))
                   (range 1 (inc %)))
              [st]))
          (range 1 6)))

(defn check-list [r]
  (if-let [list-name (get-list ":list" r)]
    (let [sym (apply str (drop 6 (str list-name)))]
      (generate-all-lists list-name sym r))
    [r]))

(defn generate-backward-rule
  [{:keys [p1 p2 conclusions] :as rule}]
  (mapcat (fn [{:keys [conclusion post]}]
            (conj (map
                    (fn [r] (update r :pre conj :question?))
                    [(assoc rule :p1 conclusion
                                 :conclusions [{:conclusion p1
                                                :post       post}]
                                 :full-path (rule-path conclusion p2))
                     (assoc rule :p2 conclusion
                                 :conclusions [{:conclusion p2
                                                :post       post}]
                                 :full-path (rule-path p1 conclusion))])
                  rule))
          conclusions))

(defn generate-backward-rules
  [rules]
  (mapcat (fn [rule]
            (if (allow-backward? rule)
              (generate-backward-rule rule)
              [rule]))
          rules))

(defn check-swapping
  [{:keys [p1 p2] :as rule}]
  (if (allow-swapping? rule)
    [rule (assoc rule :p1 p2
                      :p2 p1
                      :full-path (rule-path p2 p1))]
    [rule]))
(defmacro defrules
  "Define rules. Rules must be #R statements."
  ;TODO exception on duplication of the rule
  [name & rules]
  `(let [rules# (->> (quote ~rules)
                     (mapcat check-list)
                     (mapcat rule)
                     (mapcat check-orders)
                     (mapcat check-swapping)
                     generate-backward-rules
                     check-duplication)
         judgement-rules# (remove question? rules#)
         question-rules# (filter question? rules#)]
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

(defn get-truth-fn [post]
  (first (filter #(and (keyword? %) (s/starts-with? (str %) ":t/")) post)))

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
  [pattern premise {:keys [post conclusion]} preconditions]
  (let [[sym-map pat] (find-and-replace-symbols premise "?a")
        unification-map (u/unify pattern pat)
        sym-map (into {} (map (fn [[k v]] [(k unification-map) v]) sym-map))
        inverted-sym-map (map-invert sym-map)
        pre (walk (filter-preconditions preconditions)
                  (inverted-sym-map el) (inverted-sym-map el)
                  (seq? el)
                  (let [[f & tail] el]
                    (concat (list f) (sort-placeholders tail))))]
    {:conclusion [(replace-symbols conclusion sym-map) (t/tvtypes (get-truth-fn post))]
     :conditions (walk (concat (check-conditions sym-map) pre)
                       (and (coll? el) (= \a (first (str (first el)))))
                       (concat '() el)
                       (and (coll? el) (not ((conj reserved-operators 'quote)
                                              (first el))))
                       (vec el))}))

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
        (#{'X 'Y 'R} el) :a
        (and (coll? el) (= \a (first (str (first el)))))
        (concat '() el)
        (and (coll? el) (not (reserved-operators (first el)))) (vec el)))

(defn traverse-node
  [b1 b2 result {:keys [conclusions children condition]}]
  `(when ~(quote-operators condition)
     ~(when-not (zero? (count conclusions))
        `(vswap! ~result concat ~@(map (fn [concls]
                                         (mapv (fn [[c tf :as concls]]
                                                 (if (nil? tf)
                                                   concls
                                                   [c (list tf b1 b2)]))
                                               concls))
                                       (quote-operators conclusions))))
     ~@(map (fn [n] (traverse-node b1 b2 result n)) children)))

(defn traverse [b1 b2 trie]
  (let [results (gensym)]
    `(let [~results (volatile! [])]
       ~(traverse-node b1 b2 results trie)
       @~results)))

(defn match-rules
  [pattern rules]
  (let [b1 (gensym)
        b2 (gensym)]
    `(fn [[p1# ~b1] [p2# ~b2]]
       (match [p1# p2#] ~(quote-operators pattern)
              ~(traverse b1 b2 rules)
              :else nil))))

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

(defn get-matcher-rec
  [rules [f & tail]]
  (if-let [r (rules f)]
    (:matcher r)
    (get-matcher-rec rules tail)))

(defn get-matcher [rules p1 p2]
  (let [paths (mall-paths p1 p2)]
    (get-matcher-rec rules paths)))

(def mget-matcher (memoize get-matcher))
(defn generate-conclusions [rules [p1 _ :as t1] [p2 _ :as t2]]
  ((mget-matcher rules p1 p2) t1 t2))
;!s.contains("task(") && !s.contains("after(") && !s.contains("measure_time(") && !s.contains("Structural") && !s.contains("Identity") && !s.contains("Negation")
