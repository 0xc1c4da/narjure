(ns narjure.actor.concept
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-task)
(declare process-belief-req)
(declare process-inference-req)
(declare process-persistence-req)
(declare concept-actor)

(defsfn concept-actor
        "state is a map {:name :budget :activation-level :belief-tab :goal-tab :task-bag :term-bag}
        (this list may not be complete)"
        []
        (set-state! {})
        (loop []
          (receive [msg]
                   [:task-msg task] (set-state! (process-task task @state))
                   [:belief-request-msg belief-req] (set-state! (process-belief-req belief-req @state))
                   [:inference-request-msg inference-req] (set-state! (process-inference-req inference-req @state))
                   [:persistence-request-msg presistence-req] (set-state!  (process-persistence-req presistence-req @state))
                   :else   (! :logger [:log-msg :log-debug :concept (str "unhandled msg:" msg)]))
          (recur)))

(defn process-task [_ _]
  ;(! :logger [:log-msg :log-debug :concept "process-task"])
  )

(defn process-belief-req [_ _]
  (! :logger [:log-msg :log-debug :concept "process-belief-req"]))

(defn process-inference-req [_ _]
  (! :logger [:log-msg :log-debug :concept "process-inference-req"]))

(defn process-persistence-req [_ _]
  (! :logger [:log-msg :log-debug :concept "process-persistence-req"]))
