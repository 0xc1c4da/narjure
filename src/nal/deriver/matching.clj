(ns nal.deriver.matching
  (:require
    [nal.deriver.utils :refer [walk]]
    [clojure.core.match :refer [match]]
    [clojure.core.unify :as u]
    [clojure.set :refer [map-invert]]
    [clojure.string :as s]
    [nal.deriver
     [set-functions :refer [f-map not-empty-diff? not-empty-inter?]]
     [substitution :refer [munification-map substitute]]
     [preconditions :refer [sets compound-precondition
                            preconditions-transformations]]
     [normalization :refer [commutative-ops sort-commutative reducible-ops]
      :as n]
     [truth :as t]]))

;operators/functions that shouldn't be quoted
(def reserved-operators
  #{`= `not= `seq? `first `and `let `pos? `> `>= `< `<= `coll? `set `quote
    `count 'aops `- `not-empty-diff? `not-empty-inter? `walk `munification-map
    `substitute `sets `some `deref `do `vreset! `volatile! `fn `mapv `if
    `sort-commutative `n/reduce-ext-inter `n/reduce-symilarity
    `n/reduce-int-dif `n/reduce-and `n/reduce-ext-dif `n/reduce-image
    `n/reduce-int-inter `n/reduce-neg `n/reduce-or})

(defn not-operator?
  "Checks if element is not operator"
  [el] (re-matches #"[akxA-Z$]" (-> el str first str)))

(def operator? (complement not-operator?))

(defn quote-operators
  [statement]
  (walk statement
    (reserved-operators el) el
    (and (symbol? el) (or (operator? el) (#{'Y 'X} el))) `'~el
    (and (coll? el) (= 'quote (first el))
         (= 'quote (first (second el))))
    `(quote ~(second (second el)))
    (and (coll? el) (= \a (first (str (first el)))))
    (concat '() el)
    (and (coll? el)
         (let [f (first el)]
           (and (not (reserved-operators f))
                (not (fn? f)))))
    (vec el)))

(defn traverse-node
  [b1 b2 result {:keys [conclusions children condition]}]
  `(when ~(quote-operators condition)
     ~(when-not (zero? (count conclusions))
        `(vswap! ~result concat ~@(set (map (fn [concls]
                                              (mapv (fn [[c tf :as concls]]
                                                      (if (nil? tf)
                                                        concls
                                                        [c (list tf b1 b2)]))
                                                    concls))
                                            (quote-operators conclusions)))))
     ~@(map (fn [n] (traverse-node b1 b2 result n)) children)))

(defn traverse [b1 b2 tree]
  (let [results (gensym)]
    `(let [~results (volatile! [])]
       ~(traverse-node b1 b2 results tree)
       @~results)))

(defn match-rules
  [pattern rules]
  (let [b1 (gensym)
        b2 (gensym)]
    `(fn [[p1# ~b1] [p2# ~b2]]
       (match [p1# p2#] ~(quote-operators pattern)
         ~(traverse b1 b2 rules)
         :else nil))))

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

(defn main-pattern [premise]
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

(defn get-truth-fn [post]
  (first (filter #(and (keyword? %) (s/starts-with? (str %) ":t/")) post)))

(defn check-conditions [syms]
  (filter not-empty
          (keep
            (fn [[alias sym]]
              (let [aliases (filter (fn [[a v]]
                                      (and (< (symbol-ordering-keyfn alias)
                                              (symbol-ordering-keyfn a)) (= v sym)))
                                    (dissoc syms alias))]
                (mapcat (fn [[a]] `(= ~alias ~a)) aliases)))
            syms)))

(defn commutative? [st]
  (and (coll? st)
       (some commutative-ops st)))

(defn check-commutative [conclusion]
  (if (commutative? conclusion)
    `(sort-commutative ~(sort-commutative conclusion))
    conclusion))

(defn check-reduction [conclusion]
  (walk conclusion
    (and (coll? :el) (reducible-ops (first :el)) (<= 3 (count :el)))
    `(~(reducible-ops (first :el)) ~:el)))

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
    {:conclusion [(-> conclusion
                      (preconditions-transformations preconditions)
                      (replace-symbols sym-map)
                      check-commutative
                      check-reduction
                      )
                  (t/tvtypes (get-truth-fn post))]
     :conditions (walk (concat (check-conditions sym-map) pre)
                   (and (coll? el) (= \a (first (str (first el)))))
                   (concat '() el)
                   (and (coll? el) (not ((conj reserved-operators 'quote)
                                          (first el))))
                   (vec el))}))

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

(defn gen-rules [pattern rules]
  (let [main (main-pattern pattern)
        rules (mapcat (fn [{:keys [p1 p2 conclusions pre]}]
                        (map #(vector [p1 p2] % pre) conclusions))
                      rules)
        cond-conclusions-m (conditions->conclusions-map main rules)
        cpm (conds-priorities-map cond-conclusions-m)
        sorted-conds (sort-conds cond-conclusions-m cpm)]
    (generate-tree sorted-conds)))

(defn generate-matching [rules]
  (->> rules
       (map (fn [[k {:keys [pattern rules] :as v}]]
              (let [match-fn-code (match-rules (main-pattern pattern)
                                               (gen-rules pattern rules))]
                [k (assoc v :matcher (eval match-fn-code)
                            :matcher-code match-fn-code)])))
       (into {})))
