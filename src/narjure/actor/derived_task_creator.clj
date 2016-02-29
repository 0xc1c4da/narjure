(ns narjure.actor.derived-task-creator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare derived-task-creator-actor)

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug "process-system-time"])
  {:time time})

(defn process-inference-result [_ _]
  (! :logger [:log-msg :log-debug "process-inference-result"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In derived-task-creator :else" msg)]))

(defn derived-task-creator-actor
  "state is system-time"
  [in-state]
  (register! :derived-task-creator @self)
  (set-state! in-state)
  (loop []
    (receive [msg]
             [:system-time-msg time] (set-state! (process-system-time time @state))
             [:inference-result-msg inference-result] (process-inference-result inference-result @state)
             :else (process-unhandled-msg msg))
    (recur)))

