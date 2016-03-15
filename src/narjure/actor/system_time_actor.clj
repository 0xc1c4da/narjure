(ns narjure.actor.system-time-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-system-time-tick)
(declare system-time-actor)

(defsfn system-time-actor
        "state is system-time"
        []
        (register! :system-time @self)
        (set-state! 0)
        (loop []
          (receive [msg]
                   :system-time-tick-msg (set-state! (process-system-time-tick @state))
                   :else (! :logger [:log-msg :log-debug :system-time (str "unhandled msg:" msg)]))
          (recur)))

(defn process-system-time-tick [state]
  ;(! :logger [:log-msg :log-debug :system-time (str "process-system-time-tick " state)])
  (inc state))
