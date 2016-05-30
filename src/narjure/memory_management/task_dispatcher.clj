(ns narjure.memory-management.task-dispatcher
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! state set-state!]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.memory-management.concept-manager :refer [c-bag]]
    [narjure.bag :as b]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await])
  (:import (java.util.concurrent TimeUnit)))

(def aname :task-dispatcher)

(defn event?
  "return true if task is event otherwise false"
  [{:keys [occurrence]}]
  (not= occurrence :eternal))

(defn term-exists?
  ""
  [term]
  (b/exists? @c-bag term))

(defn task-handler
  "If concept, or any sub concepts, do not exist post task to concept-creator,
   otherwise, dispatch task to respective concepts. Also, if task is an event
   dispatch task to event buffer actor."
  [from [_ task]]
  (let [terms (:terms task)
        statement (:statement task)]
      (if (every? term-exists? terms)
        (doseq [term terms]
          (when-let [{c-ref :ref} ((:elements-map @c-bag) term)]
            (cast! c-ref [:task-msg task])))
        (cast! (:concept-manager @state) [:create-concept-msg task])))
  (when (event? task)
    #_(info (str "posting event"))
    (cast! (:event-buffer @state) [:event-msg task]))
  #_(debug aname (str "process-task" task)))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (do
    (unregister!)
    (shutdown!)))

(defn initialise
  "Initialises actor:
    registers actor and sets actor state"
  [aname actor-ref]
  (do
    (register! aname actor-ref)
    (set-state! {:concept-manager (whereis :concept-manager)
                 :event-buffer    (whereis :event-buffer)})))

(def display (atom '()))
(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger display message)
  (case type
    :task-msg (task-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn task-dispatcher []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))