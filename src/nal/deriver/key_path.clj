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

(declare mpath-invariants)

(defn path-invariants
  "Generates all pathes that will match with path from args."
  [path]
  (if (coll? path)
    (let [[op & args] path
          args-inv (map mpath-invariants args)]
      (concat (cart (concat [[op]] args-inv)) [:any]))
    [path]))

(def mpath-invariants (memoize path-invariants))

;todo I have to think about this
(defn path-kfn
  [[f _ l]]
  (- (+ (* 3 (if (seq? l) (count (flatten l)) 0))
        (* 3 (if (seq? f) (count (flatten f)) 0)))))

(defn all-paths
  "Generates all pathes for pair of premises."
  [p1 p2]
  (let [paths1 (mpath-invariants p1)
        paths2 (mpath-invariants p2)]
    (sort-by
      path-kfn
      (map vec (cart [paths1 [:and] paths2])))))

(def mall-paths (memoize all-paths))
