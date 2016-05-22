(ns nal.deriver.set-functions
  (:require [clojure.set :as set]))

;todo performance
(defn difference [[op & set1] [_ & set2]]
  (into [op] (sort-by hash (set/difference (set set1) (set set2)))))

(defn union [[op & set1] [_ & set2]]
  (into [op] (sort-by hash (set/union (set set1) (set set2)))))

(defn intersection [[op & set1] [_ & set2]]
  (into [op] (sort-by hash (set/intersection (set set1) (set set2)))))

(def f-map {:difference   difference
            :union        union
            :intersection intersection})

(defn not-empty-diff? [[_ & set1] [_ & set2]]
  (not-empty (set/difference (set set1) (set set2))))

(defn not-empty-inter? [[_ & set1] [_ & set2]]
  (not-empty (set/intersection (set set1) (set set2))))
