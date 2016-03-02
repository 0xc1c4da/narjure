(ns nal.deriver.matching
  (:require [nal.deriver.utils :refer [walk]]
            [clojure.core.match :refer [match]]
            [clojure.core.unify :as u]
            [clojure.set :refer [map-invert]]
            [nal.deriver.truth :as t]
            [clojure.string :as s]
            [nal.deriver.set-functions :refer [f-map not-empty-diff?]]))

(def reserved-operators
  #{`= `not= `seq? `first `and `let `pos? `> `>= `< `<= `coll? `set
    `quote `count 'aops `- `not-empty-diff?})

(defn not-operator?
  "Checks if element is not operator"
  [el] (re-matches #"[akxA-Z$]" (-> el str first str)))

(def operator? (complement not-operator?))

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
        (and (coll? el)
             (let [f (first el)]
               (and (not (reserved-operators f))
                    (not (fn? f)))))
        (vec el)))

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

(defn symbol-ordering-keyfn [sym]
  (if (symbol? sym)
    (try (Integer/parseInt (s/join (drop 1 (str sym))))
         (catch Exception _ -100))
    -1))

(defn sort-placeholders [tail]
  (sort-by symbol-ordering-keyfn tail))

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
                  (conj ac `(and (coll? ~sec) (= ~'ext-set (first ~sec)))))
                (= :set-int? (first condition))
                (let [sec (second condition)]
                  (conj ac `(and (coll? ~sec) (= ~'int-set (first ~sec)))))
                (= :difference (first condition))
                (concat ac [`(coll? ~(nth condition 1))
                            `(coll? ~(nth condition 2))
                            `(let [k# 1
                                   afop# (first ~(nth condition 1))
                                   asop# (first ~(nth condition 2))
                                   aops# (set ['~'ext-set '~'int-set])]
                               (and (aops# afop#) (= afop# asop#)
                                    (not-empty-diff? ~(nth condition 1)
                                                     ~(nth condition 2))))])
                (= :union (first condition))
                (concat ac [`(coll? ~(nth condition 1))
                            `(coll? ~(nth condition 2))
                            `(let [k# 1
                                   afop# (first ~(nth condition 1))
                                   asop# (first ~(nth condition 2))
                                   aops# (set ['~'ext-set '~'int-set])]
                               (and (aops# afop#) (= afop# asop#)))])
                :else ac)
              :else ac))
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

(defn apply-preconditions [conclusion preconditions]
  (reduce (fn [conclusion precondition]
            (if (seq? precondition)
              (let [cond-name (first precondition)]
                (cond
                  (#{:difference :union} cond-name)
                  (let [[_ el1 el2 el3] precondition]
                    (walk conclusion (= el el3)
                          `(~(f-map cond-name) ~el1 ~el2)))
                  :default conclusion))
              conclusion))
          conclusion preconditions))

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
    {:conclusion [(-> conclusion
                      (apply-preconditions preconditions)
                      (replace-symbols sym-map))
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

(defrecord TrieNode [condition conclusions children])

(defn group-conditions
  "Groups conditions->conclusions map by first condition and remove it."
  [conds]
  (into {} (map (fn [[k v]] [k (map (fn [[k v]] [(drop 1 k) v]) v)])
                (group-by #(-> % first first) conds))))

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
    (generate-trie sorted-conds)))

(defn generate-matching [rules]
  (->> rules
       (map (fn [[k {:keys [pattern rules] :as v}]]
              (let [match-fn-code (match-rules (main-pattern pattern)
                                               (gen-rules pattern rules))
                    ]
                [k (assoc v :matcher (eval match-fn-code)
                            :matcher-code match-fn-code)])))
       (into {})))
