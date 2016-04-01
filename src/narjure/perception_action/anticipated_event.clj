(ns narjure.perception-action.anticipated-event
  (:require
    [co.paralleluniverse.pulsar.actors :refer [!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare anticipated-event anticipated-event-handler system-time input-task)

(def aname :anticipated-event)

(defactor anticipated-event
  "State is system-time and collection of anticipated events."
  {:time 0 :anticipated-events {}}
  {:system-time-msg       system-time
   :anticipated-event-msg anticipated-event-handler
   :input-task-msg        input-task})

(defn system-time
  [[_ time] state]
  (debug aname "process-system-time")
  {:time time :anticipated-events (state :percepts)})

(defn anticipated-event-handler
  [_ _]
  #_(debug aname "process-anticipated-event"))

(defn input-task
  [[_ input-task] _]
  #_(debug aname "process-input-task")
  (! :task-dispatcher [:task-msg input-task]))

