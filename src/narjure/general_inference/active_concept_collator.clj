(ns narjure.general-inference.active-concept-collator
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare inference-tick active-concept active-concept-collator)

(defactor active-concept-collator
  "State is collection of active concepts."
  {:inference-tick-msg inference-tick
   :active-concept-msg active-concept})

(def aname :active-concept-collator)

(defn inference-tick [_ _]
  ;(debug aname "process-inference-tick")
  [])

(defn active-concept [_ _]
  (debug aname "process-active-concept"))
