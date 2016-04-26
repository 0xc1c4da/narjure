(ns narjure.memory-management.forgettable-concept-collator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :forgettable-concept-collator)

(defn concept-limit-handler
  "Processes :concept-limit-msg:
    selects lowest ranked concept in forgettable
    concept bag and shuts it down"
  [from [msg]]
  #_(debug aname "Process-free-memory-msg"))

(defn forgettable-concept-handler
  "Processes :forgettable-concept-msg:
    Adds concept to forgettable concept bag"
  [from [msg concept]]
  (debug aname "process-forgettable-concept-msg"))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor:
      registers actor and sets actor state"
  [aname actor-ref]
  (register! aname actor-ref)
  (set-state! {:state 0}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (case type
    :concept-limit-msg (concept-limit-handler from message)
    :forgettable-concept-msg (forgettable-concept-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def forgettable-concept-collator (gen-server
                         (reify Server
                           (init [_] (initialise aname @self))
                           (terminate [_ cause] #_(info (str aname " terminated.")))
                           (handle-cast [_ from id message] (msg-handler from message)))))
