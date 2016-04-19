(ns narjure.memory-management.concept-creator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self shutdown! unregister! set-state! state]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :concept-creator)

(defn create-concept
  ;TODO: update state for concept-actor to state initialiser
  ;TODO: Create required sub-term concepts
  [term c-map]
  (swap! c-map assoc term (spawn concept))
  #_(debug aname (str "Created concept: " term))
  )

(defn task-handler
  "When concept-map does not contain :term, create concept actor for term
   then post task to task-dispatcher either way."
  [from [msg task c-map]]
  (doseq [term (get-in task [:statement :terms])]
    (if-not (contains? @c-map term)
      (create-concept term c-map)))
  (cast! from [:task-msg task])
  #_(debug aname "concept-creator - process-task"))

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
    :create-concept-msg (task-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def concept-creator (gen-server
                       (reify Server
                         (init [_] (initialise aname @self))
                         (terminate [_ cause] #_(info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))
