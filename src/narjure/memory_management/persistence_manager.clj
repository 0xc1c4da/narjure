(ns narjure.memory-management.persistence-manager
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :persistence-manager)

(defn concept-state-request-handler
  "Processes :concept-state-request-msg:
    serialises concept state and returns
    to requesting actor"
  [from [msg]]
  ()
  (debug aname "process-concept-state-request-msg"))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ _ :as message]]
  (case type
    :concept-state-request-msg (concept-state-request-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def persistence-manager (gen-server
                         (reify Server
                           (init [_] (register! aname @self))
                           (terminate [_ cause] (info (str aname " terminated.")))
                           (handle-cast [_ from id message] (msg-handler from message)))))
