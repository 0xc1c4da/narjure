(ns narjure.actor.sentence-parser
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare sentence-parser-actor)

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug "process-system-time"])
  {:time time})

(defn process-narsese-string [_ _]
  (! :logger [:log-msg :log-debug "process-narsese-string"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In sentence-parser :else" msg)]))

(defsfn sentence-parser-actor
        "Example format for using actor with  set-state! on recursion.
        Usage: (def actor (spawn actor [state]))
        This example uses a map for state {:id 0} and increments value on recursion
        state is system-time"
        [in-state]
        (register! :sentence-parser @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:narsese-string-msg string] (set-state! (process-narsese-string string @state))
                   :else (process-unhandled-msg msg))
          (recur)))
