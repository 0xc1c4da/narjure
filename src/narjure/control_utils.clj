(ns narjure.control-utils
  (:require
    [narjure.bag :as b]
    [clojure.math.numeric-tower :as math]))

(def selection-parameter 3)
(defn selection-fn
  ""
  [bag]
  (* (math/expt (rand) selection-parameter) (b/count-elements bag)))

(defn forget-element [el]                                   ;TODO put in control-utils
  (let [budget (:budget (:id el))
        new-priority (* (:priority el) (second budget))
        new-budget  [new-priority (second budget)]]
    (assoc el :priority new-priority
              :id (assoc (:id el) :budget new-budget))))