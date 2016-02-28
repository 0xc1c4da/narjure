(ns actortest.persistence-manager-actor)
(ns nars.persistence-manager-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare persistence-manager-actor)

(defn process-concept-state [_ _]
  (! :logger [:log-msg :log-debug (str "process-concept-state")]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In persistence-manager :else" msg)]))

(defn persistence-manager-actor
        "state is file system handles"
        [in-state]
        (register! :persistence-manager @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:concept-state-msg concept-state] (set-state! (process-concept-state concept-state @state))
                   :else (process-unhandled-msg msg))
          (recur)))