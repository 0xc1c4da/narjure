(ns nal.deriver
  (:require
    [nal.deriver.utils :refer [walk]]
    [nal.deriver.key-path :refer [mall-paths all-paths path]]
    [nal.deriver.rules :refer [rule]]))

;!s.contains("task(") && !s.contains("measure_time(") && !s.contains("Structural") && !s.contains("Identity") && !s.contains("Negation")

(defn get-matcher-rec
  [rules [f & tail]]
  (if-let [r (rules f)]
    (:matcher r)
    (if tail
      (get-matcher-rec rules tail)
      (throw (Exception. "There is no matching rule!")))))

(defn get-matcher [rules p1 p2]
  (let [paths (all-paths p1 p2)]
    (get-matcher-rec rules paths)))

(def mget-matcher (memoize get-matcher))
(defn generate-conclusions [rules [p1 _ :as t1] [p2 _ :as t2]]
  ((mget-matcher rules (path p1) (path p2)) t1 t2))
