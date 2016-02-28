(ns nars.serialiser-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare process-input-task)
(declare process-derived-task)
(declare process-unhandled-msg)
(declare serialiser-actor)

(defn process-input-task [_ _]
  (! :logger [:log-msg :log-debug (str "process-input-task")]))

(defn process-derived-task [_ _]
  (! :logger [:log-msg :log-debug (str "process-derived-task")]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In serialiser :else" msg)]))

(defsfn serialiser-actor
        "state is serial number (long)"
        [in-state]
        (register! :serialiser @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:input-task-msg input-task] (set-state! (process-input-task input-task @state))
                   [:derived-task-msg derived-task] (set-state! (process-derived-task derived-task @state))
                   :else (process-unhandled-msg msg))
          (recur)))
