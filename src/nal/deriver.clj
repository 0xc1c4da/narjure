(ns nal.deriver
  (:require
    [nal.deriver.utils :refer [walk]]
    [nal.deriver.key-path :refer [mall-paths]]
    [nal.deriver.rules :refer [rule]]))

;todo what is "after("?
;!s.contains("task(") && !s.contains("after(") && !s.contains("measure_time(") && !s.contains("Structural") && !s.contains("Identity") && !s.contains("Negation")

(defn get-matcher-rec
  [rules [f & tail]]
  (if-let [r (rules f)]
    (:matcher r)
    (get-matcher-rec rules tail)))

(defn get-matcher [rules p1 p2]
  (let [paths (mall-paths p1 p2)]
    (get-matcher-rec rules paths)))

(def mget-matcher (memoize get-matcher))
(defn generate-conclusions [rules [p1 _ :as t1] [p2 _ :as t2]]
  ((mget-matcher rules p1 p2) t1 t2))
