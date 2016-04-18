(ns narjure.perception-action.operator-executor
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register!]]
    [co.paralleluniverse.pulsar [core :refer [defsfn]]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :operator-executor)

(defn operator-execution-handler
  "Processes an :operator-execution-msg:
    executes operation with optionally supplied parameters
    if feedback msg required posts :sentence-msg to task creator"
  [from [msg operator & params]]
  ()
  (debug aname "process-operator-execution-request-msg"))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ _ :as message]]
  (case type
    :operator-execution-msg (operator-execution-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def operator-executor (gen-server
                       (reify Server
                         (init [_] (register! aname @self))
                         (terminate [_ cause] (info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))
