(ns narjure.actor.cross-modal-integrator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [narjure.defaults :refer [serial-no]])
  (:refer-clojure :exclude [promise await])
  )

(declare process-system-time)
(declare process-percept-sentence)
(declare cross-modal-integrator-actor)

(defsfn cross-modal-integrator-actor
        "state is system-time and collection of precepts from current duration window"
        []
        (register! :cross-modal-integrator @self)
        (set-state! {:time 0 :percepts []})
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:percept-sentence-msg percept-sentence] (set-state! (process-percept-sentence percept-sentence @state))
                   :else (! :logger [:log-msg :log-debug :cross-modal-integrator (str "unhandled msg:" msg)]))
          (recur)))

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug :cross-modal-integrator "process-system-time"])
  {:time time :percepts (state :percepts)})

(defn process-percept-sentence [_ _]
  (! :logger [:log-msg :log-debug :cross-modal-integrator "process-percept-sentence"]))

