(ns nal.deriver
  (:require [clojure.walk :as w]
            [clojure.core.match :refer [match]]
            [clojure.string :as s]
            [clojure.set :refer [map-invert]]
            [clojure.core.unify :as u]))

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
  #{'&| '--> '<-> '==> 'retro-impl 'seq-conj 'inst 'prop 'inst-prop 'int-image
    'ext-image '=/> '=|> '& '| '<=> '</> '<|> '- 'int-dif '|| '&&})

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

(defn all-paths [p1 p2]
  "Generates all pathes for pair of premises."
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
    (reduce add-possible-paths rules rules)))

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
(def implications #{'==> '=/> '=|> 'retro-impl})
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


(defn check-conditions [syms]
  (let [get-n #(if (symbol? %)
                (Integer/parseInt (apply str (drop 1 (str %))))
                -1)]
    (->> (keep (fn [[alias sym]]
                 (let [aliases (filter (fn [[a v]]
                                         (and (< (get-n alias)
                                                 (get-n a)) (= v sym)))
                                       (dissoc syms alias))]
                   (mapcat (fn [[a]] `(= ~alias ~a)) aliases)))
               syms)
         (filter not-empty))))

(defn not-operator?
  "CHecks if element is not operator"
  [el] (re-matches #"[xA-Z$]" (-> el str first str)))

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
                 (and (symbol? el) (not-operator? el))
                 (let [s (get-sym @cnt)]
                   (vswap! cnt inc)
                   (vswap! sym-map assoc s el)
                   s)
                 (symbol? el) `(quote ~el))]
    [@sym-map result]))

(defn main-pattern [premise]
  (second (find-and-replace-symbols premise "x")))

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
  [pattern premise conclusion]
  (let [[sym-map pat] (find-and-replace-symbols premise "?a")
        unification-map (u/unify pattern pat)
        sym-map (into {} (map (fn [[k v]] [(k unification-map) v]) sym-map))]
    {:conclusion (replace-symbols conclusion sym-map)
     :conditions (walk (check-conditions sym-map)
                   (and (coll? el) (not (#{'clojure.core/= 'quote} (first el))))
                   (vec el))}))

(defmacro premises-matcher
  "Generates function that will match premises and generate conclusion.
   Experimental.

   ((premises-matcher [[(quote -->) x0 [(quote -) x1 x2]] [(quote -->) x3 x4]]
                       [[--> A C] [--> C B]
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
