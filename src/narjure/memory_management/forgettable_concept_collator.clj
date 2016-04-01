(ns narjure.memory-management.forgettable-concept-collator
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare forgettable-concept-collator forgetting-tick forgettable-concept)

(defactor forgettable-concept-collator
  "State is collection of forgettable concepts."
  {:forgetting-tick-msg     forgetting-tick
   :forgettable-concept-msg forgettable-concept})

(def aname :forgettable-concept-collator)

(defn forgetting-tick [_ _]
  #_(debug aname "process-forgetting-tick"))

(defn forgettable-concept [_ _]
  (debug aname "process-forgettable-concept"))
