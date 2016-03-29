(ns narjure.general_inference.active-concept-collator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare process active-concept-collator)

(def aname :active-concept-collator)

(defsfn active-concept-collator
  "State is collection of active concepts."
  []
  (register! aname @self)
  (set-state! [])
  (actor-loop aname process))

(defhandler process)

(defmethod process :inference-tick-msg [_ _]
  ;(debug aname "process-inference-tick")
  [])

(defmethod process :active-concept-msg [_ _]
  (debug aname "process-active-concept"))
