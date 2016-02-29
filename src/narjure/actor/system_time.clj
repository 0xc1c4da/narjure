(ns narjure.actor.system-time
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare system-time-actor)

(defn process-system-time-tick [state]
  (! :logger [:log-msg :log-debug (str "process-system-time-tick " state)])
  (inc state))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In system-time :else" msg)]))

(defsfn system-time-actor
        "state is system-time"
        [in-state]
        (register! :system-time @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   :system-time-tick-msg (set-state! (process-system-time-tick @state))
                   :else (process-unhandled-msg msg))
          (recur)))


