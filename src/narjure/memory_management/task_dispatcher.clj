(ns narjure.memory-management.task-dispatcher
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def c-map (atom {}))

(def aname :task-dispatcher)

(defn task-handler
  "processes :task-msg and dispatches tasks to resepctive concepts
   or if concept, or any sub concepts, do not exist posts task to concept-creator"
  [from [_ task]]
  (let [term (task :term)]
    (if-let [concept (@c-map term)]
      (! concept :task-msg task)
      (cast! (whereis :concept-creator) [:create-concept-msg task c-map])))
  #_(debug aname (str "process-task" task)))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ :as message]]
  (case type
    :task-msg (task-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def task-dispatcher (gen-server
                       (reify Server
                         (init [_] (register! aname @self))
                         (terminate [_ cause] (info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))