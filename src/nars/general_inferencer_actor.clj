(ns nars.general-inferencer-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare process-do-inference)
(declare process-unhandled-msg)
(declare general-inferencer-actor)

(defn process-do-inference [_ _]
  (! :logger [:log-msg :log-debug "process-do-inference"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In general-inferencer :else" msg)]))

(defsfn general-inferencer-actor
        "state is inference rule trie or equivalent"
        [in-state]
        (register! :general-inferencer @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:do-inference-msg inference-package] (set-state! (process-do-inference inference-package @state))
                   :else (process-unhandled-msg msg))
          (recur)))
