(ns narjure.actor.operator-executor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare operator-executor process)

(def aname :operator-executor)

(defsfn operator-executor
  "state is system-time"
  []
  (register! aname @self)
  (set-state! {:time 0})
  (actor-loop aname process))

(defhandler process)

(defmethod process :system-time-msg [[_ time] _]
  (debug aname "process-system-time")
  {:time time})

(defmethod process :operator-execution-req-msg [_ _]
  (debug aname "process-operator-execution-req"))



