(ns narjure.actor.forgettable-concept-collator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare forgettable-concept-collator process)

(def aname :forgettable-concept-collator)

(defsfn forgettable-concept-collator
  "State is collection of forgettable concepts."
  []
  (register! :forgettable-concept-collator @self)
  (set-state! [])
  (actor-loop aname process))

(defhandler process)

(defmethod process :forgetting-tick-msg [_ _]
  #_(debug aname "process-forgetting-tick"))

(defmethod process :forgettable-concept-msg [_ _]
  (debug aname "process-forgettable-concept"))
