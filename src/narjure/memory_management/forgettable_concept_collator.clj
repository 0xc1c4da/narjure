(ns narjure.memory-management.forgettable-concept-collator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :forgettable-concept-collator)

(defn concept-limit-handler
  "Processes :concept-limit-msg:
    selects lowest ranked concept in forgettable
    concept bag and shuts it down"
  [from [msg]]
  ()
  (debug aname "Process-free-memory-msg"))

(defn forgettable-concept-handler
  "Processes :forgettable-concept-msg:
    Adds concept to forgettable concept bag"
  [from [msg concept]]
  ()
  (debug aname "process-forgettable-concept-msg"))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ _ :as message]]
  (case type
    :concept-limit-msg (concept-limit-handler from message)
    :forgettable-concept-msg (forgettable-concept-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def forgettable-concept-collator (gen-server
                         (reify Server
                           (init [_] (register! aname @self))
                           (terminate [_ cause] (info (str aname " terminated.")))
                           (handle-cast [_ from id message] (msg-handler from message)))))
