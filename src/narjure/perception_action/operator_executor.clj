(ns narjure.perception-action.operator-executor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare operator-executor process system-time operator-execution-req)

(defactor operator-executor
  "State is system-time"
  {:time 0}
  {:system-time-msg            system-time
   :operator-execution-req-msg operator-execution-req})

(def aname :operator-executor)

(defn system-time [[_ time] _]
  (debug aname "process-system-time")
  {:time time})

(defn operator-execution-req [_ _]
  (debug aname "process-operator-execution-req"))
