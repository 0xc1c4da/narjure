(ns narjure.actor.cross-modal-integrator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare process-system-time)
(declare process-percept-sentence)
(declare process-unhandled-msg)
(declare cross-modal-integrator-actor)

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug "process-system-time"])
  {:time time})

(defn process-percept-sentence [_ _]
  (! :logger [:log-msg :log-debug "process-percept-sentence"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In cross-modal-integrator :else" msg)]))

(defsfn cross-modal-integrator-actor
        "state is system-time and collection of precepts from current duration window"
        [in-state]
        (register! :cross-modal-integrator @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:percept-sentence-msg percept-sentence] (set-state! (process-percept-sentence percept-sentence @state))
                   :else (process-unhandled-msg msg))
          (recur)))


