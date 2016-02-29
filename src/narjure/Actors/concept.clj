(ns narsjure.actors.concept
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare process-task)
(declare process-belief-req)
(declare process-inference-req)
(declare process-persistence-req)
(declare process-state)
(declare process-unhandled-msg)
(declare concept-actor)

(defn process-task [_ _]
  (! :logger [:log-msg :log-debug "process-task"]))

(defn process-belief-req [_ _]
  (! :logger [:log-msg :log-debug "process-belief-req"]))

(defn process-inference-req [_ _]
  (! :logger [:log-msg :log-debug "process-inference-req"]))

(defn process-persistence-req [_ _]
  (! :logger [:log-msg :log-debug "process-persistence-req"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In concept :else" msg)]))

(defsfn concept-actor
        "state is a map {:name :budget :activation-level :belief-tab :goal-tab :task-bag :term-bag}
        (this list may not be complete)"
        [in-state]
        (set-state! in-state)
        (loop []
          (receive [msg]
                   [:task-msg task] (set-state! (process-task task @state))
                   [:belief-request-msg belief-req] (set-state! (process-belief-req belief-req @state))
                   [:inference-request-msg inference-req] (set-state! (process-inference-req inference-req @state))
                   [:persistence-request-msg presistence-req] (set-state!  (process-persistence-req presistence-req @state))
                   :else (process-unhandled-msg msg))
          (recur)))

