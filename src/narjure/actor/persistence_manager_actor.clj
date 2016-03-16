(ns narjure.actor.persistence-manager-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-concept-state)
(declare persistence-manager-actor)

(defsfn persistence-manager-actor
        "state is file system handles"
        [in-state]
        (register! :persistence-manager @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:concept-state-msg concept-state] (set-state! (process-concept-state concept-state @state))
                   :else (! :logger [:log-msg :log-debug :persistence-manager (str "unhandled msg:" msg)]))
          (recur)))

(defn process-concept-state [_ _]
  (! :logger [:log-msg :log-debug :persistence-manager "process-concept-state"]))
