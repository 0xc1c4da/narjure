(ns nars.forgettable-concept-collator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare process-forgetting-tick)
(declare process-forgettable-concept)
(declare process-unhandled-msg)
(declare forgettable-concept-collator-actor)

(defn process-forgetting-tick [_]
  (! :logger [:log-msg :log-debug (str "process-forgetting-tick")]))

(defn process-forgettable-concept [_ _]
  (! :logger [:log-msg :log-debug (str "process-forgettable-concept")]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In forgettable-concept-collator :else" msg)]))

(defn forgettable-concept-collator-actor
        "state is collection of forgettable concepts"
        [in-state]
        (register! :forgettable-concept-collator @self)
        (set-state! in-state)
        (loop []
          (receive [msg]
                   :forgetting-tick-msg (set-state! (process-forgetting-tick @state))
                   [:forgettable-concept-msg forgettable-concept] (process-forgettable-concept forgettable-concept @state)
                   :else (process-unhandled-msg msg))
          (recur)))
