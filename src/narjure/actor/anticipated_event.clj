(ns narjure.actor.anticipated-event
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self !]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare anticipated-event process)

(def aname :anticipated-event)

(defsfn anticipated-event
  "State is system-time and collection of anticipated events."
  []
  (register! aname @self)
  (set-state! {:time 0 :anticipated-events {}})
  (actor-loop aname process))

(defhandler process)

(defmethod process :system-time-msg
  [[_ time] state]
  (debug aname "process-system-time")
  {:time time :anticipated-events (state :percepts)})

(defmethod process :anticipated-event-msg
  [_ _]
  #_(debug aname "process-anticipated-event"))

(defmethod process :input-task-msg
  [[_ input-task] _]
  #_(debug aname "process-input-task")
  (! :task-dispatcher [:task-msg input-task]))

