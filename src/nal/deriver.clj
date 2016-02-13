(ns nal.deriver
  (:require [narjure.narsese :refer [parser parse]]
            [clojure.walk :as w]))

(defn path [statement]
  (if (coll? statement)
    (let [[fst & tail] statement]
      (concat [fst] (map path tail)))
    :any))

(def operators
  #{(symbol "&/")
    (symbol "&|")
    (symbol "-->")
    (symbol "<->")
    (symbol "o--")
    (symbol "--o")
    (symbol "o-o")
    (symbol "==>")
    (symbol "=/>")
    (symbol "=|>")
    (symbol "=>")
    (symbol "<=>")
    (symbol "</>")
    (symbol "<|>")})

(defn infix->prefix
  [premise]
  (if (list? premise)
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
      (concat [:any] (cart (concat [[op]] args-inv))))
    [path]))

(defn all-paths [p1 p2]
  "Generates all pathes for pair of premises."
  (let [paths1 (path-invariants (path p1))
        paths2 (path-invariants (path p2))]
    (map vec (cart [paths1 [:and] paths2]))))

(defn options [args]
  (when-not (empty? args)
    (into {} (map vec (partition 2 args)))))

(defn replace-sets
  [statement]
  (letfn [(do-replace [el]
            (cond
              (vector? el) (concat '(int-set) el)
              (set? el) (concat '(ext-set) el)
              :default el))]
    (w/walk do-replace identity (do-replace statement))))

(defn rule
  [[p1 p2 _ c & other]]
  (let [transform (comp infix->prefix replace-sets)
        p1 (transform p1)
        p2 (transform p2)]
    {:p1        p1
     :p2        p2
     :c         (transform c)
     :full-path (rule-path p1 p2)
     :rest      (options other)}))

(defn rule->map
  "Adds rule to map of rules, conjoin rule to set of rules that
  matches to pattern. Rules paths are keys in this map."
  [ac {:keys [p1 p2 full-path] :as rule}]
  (-> ac
      (update-in [full-path :rules] conj rule)
      (assoc-in [full-path :all] (all-paths p1 p2))))

(defn add-possible-paths
  [ac [k {:keys [all]}]]
  (let [rules (mapcat :rules (vals (select-keys ac all)))]
    (-> ac
        (update-in [k :rules] concat rules)
        (update-in [k :rules] set))))

(defn rules-map [ruleset]
  (let [rules (reduce rule->map {} ruleset)]
    (reduce add-possible-paths rules rules)))

(defmacro defrules
  ;TODO exception on duplication of the rule
  [name & rules]
  `(def ~name (rules-map (map rule (quote ~rules)))))
