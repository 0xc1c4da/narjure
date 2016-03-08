(ns narjure.actor.active-concept-collator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-inference-tick)
(declare process-active-concept)
(declare active-concept-collator-actor)

(defsfn active-concept-collator-actor
        "state is collection of active concepts"
        []
        (register! :active-concept-collator @self)
        (set-state! [])
        (loop []
          (receive [msg]
                   :inference-tick-msg (set-state! (process-inference-tick @state))
                   [:active-concept-msg active-concept] (process-active-concept active-concept @state)
                   :else (! :logger [:log-msg :log-debug :active-concept-collator "unhandled msg"]))
          (recur)))

(defn process-inference-tick [state]
  ;(! :logger [:log-msg :log-debug :active-concept-collator "process-inference-tick"])
  [])

(defn process-active-concept [_ _]
  (! :logger [:log-msg :log-debug :active-concept-collator "process-active-concept"]))