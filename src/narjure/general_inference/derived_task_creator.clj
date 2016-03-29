(ns narjure.general_inference.derived-task-creator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare derived-task-creator process)

(def aname :derived-task-creator)

(defsfn derived-task-creator
  "State is system-time."
  []
  (register! aname @self)
  (set-state! {:time 0})
  (actor-loop aname process))

(defn process-system-time [[_ time] _]
  (debug aname "process-system-time")
  {:time time})

(defn process-inference-result [_ _]
  (debug aname "process-inference-result"))
