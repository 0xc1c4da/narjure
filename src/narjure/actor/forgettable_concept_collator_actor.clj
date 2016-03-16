(ns narjure.actor.forgettable-concept-collator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-forgetting-tick)
(declare process-forgettable-concept)
(declare forgettable-concept-collator-actor)

(defsfn forgettable-concept-collator-actor
        "state is collection of forgettable concepts"
        []
        (register! :forgettable-concept-collator @self)
        (set-state! [])
        (loop []
          (receive [msg]
                   :forgetting-tick-msg (set-state! (process-forgetting-tick @state))
                   [:forgettable-concept-msg forgettable-concept] (process-forgettable-concept forgettable-concept @state)
                   :else (! :logger [:log-msg :log-debug :forgettable-concept-collator (str "unhandled msg:" msg)]))
          (recur)))

(defn process-forgetting-tick [_]
  ;(! :logger [:log-msg :log-debug :forgettable-concept-collator "process-forgetting-tick"])
  )

(defn process-forgettable-concept [_ _]
  (! :logger [:log-msg :log-debug :forgettable-concept-collator "process-forgettable-concept"]))
