(ns narjure.bag
  (:require [clojure.data.priority-map :refer [priority-map-keyfn]]))

(defprotocol Bag
  (put-el [this item])
  (get-el
    [this]
    [this k])
  (remove-el [this k]))

;TODO must be discussed
(defn randomize-priority [priority]
  (* (rand) priority))

(defn assoc-to-bag
  "Set some random priority for a new item and slice map."
  [col {:keys [key priority] :as v} capacity]
  (let [ncol (->> (randomize-priority priority)
                  (assoc v :rand-priority)
                  (assoc col key))]
    (if (> (count ncol) capacity)
      (let [[k] (last ncol)]
        (dissoc ncol k))
      ncol)))

(defrecord DefaultBag [capacity queue]
  Bag
  (put-el [_ item]
    (DefaultBag. capacity (assoc-to-bag queue item capacity)))
  (get-el [_] (peek queue))
  (get-el [_ key] (queue key))
  (remove-el [_ key] (DefaultBag. capacity (dissoc queue key))))

(defn default-bag
  ([] (default-bag 100))
  ([capacity]
   (DefaultBag. capacity (priority-map-keyfn :rand-priority))))

#_(comment
  (defrecord DefaultBag [capacity index queue]
    Bag
    (put-in [_ priority item]
      (DefaultBag. capacity (assoc queue item priority)))
    (get-out [_])
    (count-eml [_] ()))

  (defn default-bag []
    (DefaultBag. 100 (priority-map)))

  (defprotocol LimitedTable
    (select [_ k])
    (insert [_ k v])
    (count-elms [_]))

  (defrecord LimitedMap [col cap]
    LimitedTable
    (select [_ k] [(col k) (LimitedMap. (dissoc col k) cap)])
    (insert [_ k v]
      (let [ncol (if (>= (count col) cap)
                   (into {} (butlast col))
                   col)]
        (LimitedMap. (assoc ncol k v) cap)))
    (count-elms [_] (count col)))


  (defn get-level [{:keys [levels]} {:keys [p]}]
    (int (dec (Math/ceil (* p levels)))))

  (defn select-level [{:keys [levels sel-factor]} {:keys [p]}]
    (int (* (Math/pow (rand) sel-factor) levels)))

  (defrecord PriorityTable [col cap levels sel-factor]
    LimitedTable
    (select [_ k])
    (insert [_ k v])
    (count-elms [_] (count col))))
