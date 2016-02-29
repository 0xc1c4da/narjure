(ns nal.deriver.key-path)

(defn path
  "Generates premises \"path\" by replacing terms with :any"
  [statement]
  (if (coll? statement)
    (let [[fst & tail] statement]
      (conj (map path tail) fst))
    :any))

(defn rule-path
  "Generates detailed pattern for the rule."
  [p1 p2]
  [(path p1) :and (path p2)])

(defn cart
  "Cartesian product."
  [colls]
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

(def mall-paths (memoize all-paths))
