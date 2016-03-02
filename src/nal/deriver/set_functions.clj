(ns nal.deriver.set-functions
  (:require [clojure.set :as set]))

;todo performance
(defn difference [[op & set1] [_ & set2]]
  [(into [op] (sort (set/difference (set set1) (set set2)))) :diff])

(defn union [[op & set1] [_ & set2]]
  [(into [op] (sort (set/union (set set1) (set set2)))) :union])

(def f-map {:difference difference
            :union      union})

(defn not-empty-diff? [[_ & set1] [_ & set2]]
  (not-empty (set/difference (set set1) (set set2))))
