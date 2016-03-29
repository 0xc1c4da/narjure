(ns nal.deriver
  (:require
    [nal.deriver.utils :refer [walk]]
    [nal.deriver.key-path :refer [mall-paths all-paths mpath-invariants
                                  path-with-max-level]]
    [nal.deriver.rules :refer [rule]]))

(defn get-matcher [rules p1 p2]
  (let [matchers (->> (mall-paths p1 p2)
                      (filter rules)
                      (map rules)
                      (map (fn [el] (:matcher el))))]
    (case (count matchers)
      0 (constantly [])
      1 (first matchers)
      (fn [t1 t2] (mapcat #(% t1 t2) matchers)))))

(def mget-matcher (memoize get-matcher))
(def mpath (memoize path-with-max-level))
(defn generate-conclusions
  [rules {p1 :statement :as t1} {p2 :statement :as t2}]
  (let [matcher (mget-matcher rules (mpath p1) (mpath p2))]
    (matcher t1 t2)))
