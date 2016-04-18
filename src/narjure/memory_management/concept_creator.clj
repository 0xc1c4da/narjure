(ns narjure.memory-management.concept-creator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :concept-creator)

(defn create-concept
  ;TODO: update state for concept-actor to state initialiser
  ;TODO: Create required sub-term concepts and propogate budget
  [task c-map]
  (let [{term :term} task]
    (swap! c-map assoc term (spawn concept))
    #_(debug aname (str "Created concept: " term))))

(defn task-handler
  "When concept-map does not contain :term, create concept actor for term
   then post task to task-dispatcher either way."
  [from [msg task c-map]]
  (let [term (task :term)]
    ; when concept not exist then create - goes here
    (when (not (contains? @c-map term))
      (create-concept task c-map)))

  (cast! from [:task-msg task])
  #_(debug aname "concept-creator - process-task"))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ _ :as message]]
  (case type
    :create-concept-msg (task-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def concept-creator (gen-server
                       (reify Server
                         (init [_] (register! aname @self))
                         (terminate [_ cause] (info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))
