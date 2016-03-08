(ns narjure.actor.operator-executor-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [narjure.defaults :refer [serial-no]])
  (:refer-clojure :exclude [promise await])
  )

(declare process-system-time)
(declare process-operator-execution-req)
(declare operator-executor-actor)

(defsfn operator-executor-actor
        "state is system-time"
        []
        (register! :operator-executor @self)
        (set-state! {:time 0})
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:operator-execution-req-msg operator-execution-req] (process-operator-execution-req operator-execution-req @state)
                   :else (! :logger [:log-msg :log-debug :operator-executor (str "unhandled msg:" msg)]))
          (recur)))

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug :operator-executor "process-system-time"])
  {:time time})

(defn process-operator-execution-req [_ _]
  (! :logger [:log-msg :log-debug :operator-executor "process-operator-execution-req"]))



