(ns narjure.memory_management.concept
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state!]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :as t])
  (:refer-clojure :exclude [promise await]))

(declare concept process)

(defsfn concept
  "State is a map
  {:name :budget :activation-level :belief-tab :goal-tab :task-bag :term-bag}
  (this list may not be complete)."
  []
  (set-state! {})
  (actor-loop :concept process))

(defhandler process)

(defn debug [msg] (t/debug :concept msg))

(defmethod process :task-msg [_ _]
  #_(debug "process-task"))

(defmethod process :belief-req [_ _]
  (debug "process-belief-req"))

(defmethod process :inference-req [_ _]
  (debug "process-inference-req"))

(defmethod process :persistence-req [_ _]
  (debug "process-persistence-req"))
