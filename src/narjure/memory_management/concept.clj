(ns narjure.memory-management.concept
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :as t])
  (:refer-clojure :exclude [promise await]))

(defn debug [msg] (t/debug :concept msg))

(defn task-handler
  ""
  [from message]
  ;todo
  )

(defn belief-request-handler
  ""
  [from message]
  ;todo
  )

(defn inference-request-handler
  ""
  [from message]
  ;todo
  )

(defn concept-state-handler
  "Sends a copy of the actor state to requesting actor"
  [from _]
  (let [concept-state @state]
    (cast! from [:concept-state-msg concept-state])))

(defn set-concept-state-handler
  "set concept state to value passed in message"
  [from [_ new-state]]
  (set-state! (merge @state new-state)))

(defn task-budget-update-handler
  ""
  [from message]
  ;todo
  )

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor: registers actor and sets actor state"
  [name]
  (set-state! {:name name
               :budget [0.0 0.0]
               :satisfaction [0.0 0.0]
               :tasks {}
               :termlinks {}
               :active-concept-collator (whereis :active-concept-collator)
               :general-inferencer (whereis :general-inferencer)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (case type
    :task-msg (task-handler from message)
    :belief-request-msg (belief-request-handler from message)
    :inference-request-msg (inference-request-handler from message)
    :concept-state-request-msg (concept-state-handler from message)
    :set-concept-state-msg (set-concept-state-handler from message)
    :task-budget-update-msg (task-budget-update-handler from message)
    :shutdown (shutdown-handler from message)
    (debug (str "unhandled msg: " type))))

(defn concept [name]
  (gen-server
    (reify Server
      (init [_] (initialise name))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))