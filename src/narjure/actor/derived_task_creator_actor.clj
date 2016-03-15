(ns narjure.actor.derived-task-creator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-system-time)
(declare process-inference-result)
(declare derived-task-creator-actor)

(defsfn derived-task-creator-actor
        "state is system-time"
        []
        (register! :derived-task-creator @self)
        (set-state! {:time 0})
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:inference-result-msg inference-result] (process-inference-result inference-result @state)
                   :else (! :logger [:log-msg :log-debug :derived-task-creator (str "unhandled msg:" msg)]))
          (recur)))

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug :derived-task-creator "process-system-time"])
  {:time time})

(defn process-inference-result [_ _]
  (! :logger [:log-msg :log-debug :derived-task-creator "process-inference-result"]))

