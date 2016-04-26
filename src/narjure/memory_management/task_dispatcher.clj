(ns narjure.memory-management.task-dispatcher
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! state set-state!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def c-map (atom {}))

(def aname :task-dispatcher)

(defn task-handler
  "processes :task-msg and dispatches tasks to resepctive concepts
   or if concept, or any sub concepts, do not exist posts task to concept-creator"
  [from [_ task]]
  (doseq [term (get-in task [:statement :terms])]
    (if-let [concept (@c-map term)]
      (cast! concept [:task-msg task])
      (cast! (:concept-creator @state) [:create-concept-msg task c-map]))
    )
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
    (set-state! {:concept-creator (whereis :concept-creator)})))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (case type
    :task-msg (task-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def task-dispatcher (gen-server
                       (reify Server
                         (init [_] (initialise aname @self))
                         (terminate [_ cause] #_(info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))