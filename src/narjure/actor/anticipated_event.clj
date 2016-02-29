(ns narjure.actor.anticipated-event
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare anticipated-event-actor)

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug "process-system-time"])
  {:time time})

(defn process-input-task [_ _]
  (! :logger [:log-msg :log-debug "process-input-task"]))

(defn process-anticipated-event [_ _]
  (! :logger [:log-msg :log-debug "process-anticipated-event"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In anticipated-event :else" msg)]))

(defsfn anticipated-event-actor
        "state is system-time and collection of anticipated events"
        [in-state]
        (register! :anticipated-event @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:input-task-msg input-task] (set-state! (process-input-task input-task @state))
                   [:anticipated-event-msg anticipated-event] (set-state! (process-anticipated-event anticipated-event @state))
                   :else (process-unhandled-msg msg))
          (recur)))