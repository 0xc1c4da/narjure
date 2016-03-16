(ns nal.deriver.matching
  (:require
    [nal.deriver.utils :refer [walk operator? not-operator?]]
    [clojure.core.match :refer [match]]
    [clojure.core.unify :as u]
    [clojure.set :refer [map-invert intersection]]
    [clojure.string :as s]
    [nal.deriver
     [set-functions :refer [f-map not-empty-diff? not-empty-inter?]]
     [substitution :refer [munification-map substitute]]
     [preconditions :refer [sets compound-precondition conclusion-transformation
                            implications-and-equivalences get-terms abs
                            preconditions-transformations]]
     [normalization :refer [commutative-ops sort-commutative reducible-ops]
      :as n]
     [truth :as t]]))

;operators/functions that shouldn't be quoted
(def reserved-operators
  #{`= `not= `seq? `first `and `let `pos? `> `>= `< `<= `coll? `set `quote
    `count 'aops `- `not-empty-diff? `not-empty-inter? `walk `munification-map
    `substitute `sets `some `deref `do `vreset! `volatile! `fn `mapv `if
    `sort-commutative `n/reduce-ext-inter `n/reduce-symilarity `complement
    `n/reduce-int-dif `n/reduce-and `n/reduce-ext-dif `n/reduce-image
    `n/reduce-int-inter `n/reduce-neg `n/reduce-or `nil? `not `or `abs
    `implications-and-equivalences `get-terms `empty? `intersection
    `n/reduce-seq-conj})

(defn quote-operators
  [statement]
  (walk statement
    (and
      (not (reserved-operators :el))
      (symbol? :el)
      (or (operator? :el) (#{'Y 'X} :el))) `'~:el
    (and (coll? :el)
         ((complement map?) :el)
         (let [f (first :el)]
           (and (not (reserved-operators f))
                (not (fn? f)))))
    (vec :el)))

(defn form-conclusion
  "Formation of cocnlusion in terms of task and truth/desire functions"
  [{:keys [t1 t2 task-type]}
   {c  :statement tf :t-function pj :p/judgement df :d-function
    sc :shift-conditions}]
  (let [conclusion-type (if pj :judgement task-type)
        conclusion {:statement  c
                    :task-type  conclusion-type
                    :occurrence :t-occurrence}
        conclusion (case conclusion-type
                     :judgement (assoc conclusion :truth (list tf t1 t2))
                     :goal (assoc conclusion :desire (list df t1 t2))
                     conclusion)]
    (if sc
      (conclusion-transformation sc conclusion)
      conclusion)))

(defn traverse-node
  "Generates code for precondition node."
  [vars result {:keys [conclusions children condition]}]
  `(when ~(quote-operators condition)
     ~(when-not (zero? (count conclusions))
        `(vswap! ~result concat
                 ~@(set (map #(mapv (partial form-conclusion vars) %)
                             (quote-operators conclusions)))))
     ~@(map (fn [n] (traverse-node vars result n)) children)))

(defn traversal
  "Walk through preconditions tree and generates code for matcher."
  [vars tree]
  (let [results (gensym)]
    `(let [~results (volatile! [])]
       ~(traverse-node vars results tree)
       @~results)))

(defn replace-occurrences
  "Reblaces occurrences keywords from matcher's code by generated symbols."
  [code]
  (let [t-occurrence (gensym) b-occurrence (gensym)]
    (walk code
      (= :el :t-occurrence) t-occurrence
      (= :el :b-occurrence) b-occurrence)))

(defn match-rules
  "Generates code of function that will match premises. Generated function
  should be called with task and beleif as arguments."
  [rules pattern task-type]
  (let [t1 (gensym) t2 (gensym)
        task (gensym) belief (gensym)
        truth-kw (if (= :goal task-type) :desire :truth)]
    (replace-occurrences
      `(fn [{p1# :statement ~t1 ~truth-kw :t-occurrence :occurrence :as ~task}
            {p2# :statement ~t2 :truth :b-occurrence :occurrence :as ~belief}]
         (match [p1# p2#] ~(quote-operators pattern)
           ~(traversal {:t1        t1
                        :t2        t2
                        :task      task
                        :belief    belief
                        :task-type task-type}
                       rules)
           :else nil)))))

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
                   s))]
    [@sym-map result]))

(defn symbols->placeholders
  "Replaces "
  [premise]
  (second (find-and-replace-symbols premise "x")))

(defn symbol-ordering-keyfn
  [sym]
  (if (symbol? sym)
    (try (Integer/parseInt (s/join (drop 1 (str sym))))
         (catch Exception _ -100))
    -1))

(defn sort-placeholders
  "Sorts placeholder for preconditions. If we have two precondition like
  (!= x0 x2) and (!= x2 x0) they are equal but we can not check this easely.
  So this function sort [x2 x0] to [x0 x2], so we can reason that
  (!= x0 x2) and (!= x2 x0) are equal."
  [tail]
  (sort-by symbol-ordering-keyfn tail))

(defn apply-preconditions
  "Generates code for preconditions."
  [preconditions]
  (reduce (fn [ac condition]
            (if (seq? condition)
              (concat ac (compound-precondition condition))
              ac))
          [] preconditions))

(defn replace-symbols
  "Replaces elements from statement if finds them in sym-map."
  [conclusion sym-map]
  (let [sym-map (map-invert sym-map)]
    (walk conclusion
      (sym-map el) (sym-map el))))

(defn find-kv-by-prefix [prefix coll]
  (first (filter #(and (keyword? %) (s/starts-with? (str %) prefix)) coll)))

(defn get-truth-fn [post] (find-kv-by-prefix ":t/" post))

(defn get-desire-fn [post] (find-kv-by-prefix ":d/" post))

(defn get-aliases
  "Filter map of symbols and keep only aliases of symbol that have lower
  order-key (to avoid preconditions with swapped symbols, like (= x1 x2)
  (= x2 x1)."
  [symbols-map alias]
  (let [sym (symbols-map alias)]
    (->> (dissoc symbols-map alias)
         (filter (fn [[a v]]
                   (and (< (symbol-ordering-keyfn alias)
                           (symbol-ordering-keyfn a))
                        (= v sym))))
         keys)))

(defn aliases->conditins
  [symbols-map alias]
  (mapcat #(list `= alias %) (get-aliases symbols-map alias)))

(defn check-conditions [syms]
  (->> (keys syms)
       (keep (partial aliases->conditins syms))
       (filter not-empty)))

(defn commutative? [st]
  (and (coll? st) (some commutative-ops st)))

(defn check-commutative [conclusion]
  (if (commutative? conclusion)
    `(sort-commutative ~(sort-commutative conclusion))
    conclusion))

(defn check-reduction [conclusion]
  (walk conclusion
    (and (coll? :el) (reducible-ops (first :el)) (<= 3 (count :el)))
    `(~(reducible-ops (first :el)) ~:el)))

(defn find-shift-precondition
  [preconditions]
  (first
    (filter
      #(and (coll? %)
            (#{:shift-occurrence-backward
               :shift-occurrence-forward}
              (first %))) preconditions)))

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
        pre (walk (apply-preconditions preconditions)
              (inverted-sym-map el) (inverted-sym-map el)
              (seq? el)
              (let [[f & tail] el]
                (if-not (#{`munification-map `not-empty-diff?} f)
                  (concat (list f) (sort-placeholders tail))
                  el)))]
    {:conclusion {:statement        (-> conclusion
                                        (preconditions-transformations preconditions)
                                        (replace-symbols sym-map)
                                        check-commutative
                                        check-reduction)
                  :shift-conditions (replace-symbols
                                      (find-shift-precondition preconditions)
                                      sym-map)
                  :t-function       (t/tvtypes (get-truth-fn post))
                  :d-function       (t/dvtypes (get-desire-fn post))
                  :p/judgement      (some #{:p/judgement} post)}
     :conditions (remove nil?
                         (walk (concat (check-conditions sym-map) pre)
                           (and (coll? el) (= \a (first (str (first el)))))
                           (concat '() el)
                           (and (coll? el) (not ((conj reserved-operators 'quote)
                                                  (first el))))
                           (vec el)))}))

(defn conditions->conclusions-map
  "Creates map from conditions to conclusions."
  [main rules]
  (->> rules
       (map (fn [[premises conclusions preconditions]]
              (premises-pattern main premises conclusions preconditions)))
       (group-by :conditions)
       (map (fn [[k v]] [k (map :conclusion v)]))))

(defrecord TreeNode [condition conclusions children])

(defn group-conditions
  "Groups conditions->conclusions map by first condition and remove it."
  [conds]
  (into {} (map (fn [[k v]]
                  [k (map (fn [[k v]]
                            [(drop 1 k) (set v)]) v)])
                (group-by #(-> % first first) conds))))

(defn generate-tree
  "Generates tree of conditions from conditions->conclusions map."
  ([conds] (generate-tree true conds))
  ([cond conds]
   (let [grouped-conditions (group-conditions conds)
         reached-keys (map second (grouped-conditions nil))
         other (dissoc grouped-conditions nil)]
     (->TreeNode cond reached-keys
                 (map (fn [[cond conds]]
                        (generate-tree cond conds))
                      other)))))

(defn conds-priorities-map
  "Generates the map of priorities for coditions according to their frequency."
  [conds]
  (->> (mapcat (fn [[cnds k]]
                 (if (not-empty cnds)
                   (map (fn [c] [c k]) cnds)
                   [(list '() k)])) conds)
       (group-by first)
       (map (fn [[k v]] [k (+ (- (count v)) (rand 0.4))]))
       (into {})))

(defn sort-conds
  "Sorts conditions in conditions->conclusions map according to the map of
   priorities of conditions. If condition occurs frequntly it will have higher
   priority."
  [conds cpm]
  (map (fn [[cnds k]] [(sort-by cpm cnds) k]) conds))

(defn gen-rules
  "Prepeares data for generation of conditions tree and then generates tree."
  [main-pattern rules]
  (let [rules (mapcat (fn [{:keys [p1 p2 conclusions pre]}]
                        (map #(vector [p1 p2] % pre) conclusions))
                      rules)
        cond-conclusions-m (conditions->conclusions-map main-pattern rules)
        cpm (conds-priorities-map cond-conclusions-m)
        sorted-conds (sort-conds cond-conclusions-m cpm)]
    (generate-tree sorted-conds)))

(defn generate-matching
  "Generates code for rule matcher."
  [rules task-type]
  (->> rules
       (map (fn [[k {:keys [pattern rules] :as v}]]
              (let [main-pattern (symbols->placeholders pattern)
                    match-fn-code (-> main-pattern
                                      (gen-rules rules)
                                      (match-rules main-pattern task-type))]
                [k (assoc v :matcher (eval match-fn-code)
                            :matcher-code match-fn-code)])))
       (into {})))
