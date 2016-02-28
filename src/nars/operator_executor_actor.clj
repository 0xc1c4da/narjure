(ns nars.operator-executor-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare operator-executor-actor)

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug (str "process-system-time")])
  {:time time})

(defn process-operator-execution-req [_ _]
  (! :logger [:log-msg :log-debug (str "process-operator-execution-req")]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In operator-executor :else" msg)]))

(defn operator-executor-actor
        "state is system-time"
        [in-state]
        (register! :operator-executor @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:operator-execution-req-msg operator-execution-req] (process-operator-execution-req operator-execution-req @state)
                   :else (process-unhandled-msg msg))
          (recur)))



