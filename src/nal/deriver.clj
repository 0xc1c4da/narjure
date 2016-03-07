(ns nal.deriver
  (:require
    [nal.deriver.utils :refer [walk]]
    [nal.deriver.key-path :refer [mall-paths all-paths path mpath-invariants]]
    [nal.deriver.rules :refer [rule]]))

;!s.contains("task(") && !s.contains("measure_time(") && !s.contains("Structural") && !s.contains("Identity") && !s.contains("Negation")
(defn child? [parent child]
  (when-not (= child parent)
    (let [[f _ l] child
          [pf _ pl] parent
          fp-inv (set (mpath-invariants pf))
          fl-inv (set (mpath-invariants pl))]
      (and (fp-inv f) (fl-inv l)))))

(defn remove-children [paths]
  (reduce #(remove (partial child? %2) %1) paths paths))

(defn get-matcher [rules p1 p2]
  (let [paths (->> (mall-paths p1 p2)
                   (filter rules)
                   remove-children)
        matchers (->> (filter rules paths)
                      (select-keys rules)
                      vals
                      (map :matcher))]
    (case (count matchers)
      0 (constantly [])
      1 (first matchers)
      (fn [t1 t2] (mapcat #(% t1 t2) matchers)))))

(def mget-matcher (memoize get-matcher))
(def mpath (memoize path))
(defn generate-conclusions [rules [p1 _ :as t1] [p2 _ :as t2]]
  (let [matcher (mget-matcher rules (mpath p1) (mpath p2))]
    (matcher t1 t2)))
