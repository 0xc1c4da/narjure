(ns narjure.perception-action.task-creator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self whereis state set-state!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :task-creator)

(defn sentence [_ _]
  (debug aname "process-sentence"))

(defn system-time-tick-handler
  "inc :time value in actor state for each system-time-tick-msg"
  []
  (debug aname "process-system-time-tick")
  (set-state! (update @state :time inc)))

(defn get-id
  "inc the task :id in actor state and returns the value"
  []
  (set-state! (update @state :id inc))
  (@state :id))

(defn get-time
  "return the current time from actor state"
  []
  (@state :time))

(defn initialise
  "Initialises actor:
      registers actor and
      sets actor state to: {:time 0 :id -1}"
  [actor-ref]
  (register! aname actor-ref)
  (set-state! {:time 0 :id -1}))

(defn create-new-task
  "create a new task with the provided sentence and default values
   convert tense to occurrence time if applicable"
  [sentence time id]
  ({:truth '(0.9 0.9)
    :desire nil
    :budget '(0.5 0.5)
    :creation time
    :occurence 0
    :source :input
    :id id
    :evidence '(0)
    :sc 0
    :terms '(cat animal)
    :solution nil
    :statement sentence
    :term '(inh cat animal)}))

(defn create-derived-task
  "Create a derived task with the provided sentence, budget and occurence time
   and default values for the remaining parameters"
  [sentence budget occurrence time id]
  ({:truth '(0.9 0.9)
    :desire nil
    :budget budget
    :creation time
    :occurence occurrence
    :source :derived
    :id id
    :evidence '(0)
    :sc 0
    :terms '(cat animal)
    :solution nil
    :statement sentence }))

(defn sentence-handler
  "Processes a :sentence-msg"
  [from [_ sentence]]
  (cast! (whereis :task-dispatcher) [:task-msg (create-new-task sentence (:time @state) (get-id))]))

(defn derived-sentence-handler
  "processes a :derived-sentence-msg"
  [from [msg sentence budget occurrence]]
  (cast! (whereis :task-dispatcher) [:task-msg (create-derived-task sentence budget occurrence (:time @state) (get-id))]))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ :as message]]
  (case type
    :sentence-msg (sentence-handler from message)
    :derived-sentence-msg (derived-sentence-handler from message)
    :system-time-tick-msg (system-time-tick-handler)
    :default (debug aname (str "unhandled msg: " type))))

(def task-creator (gen-server
                    (reify Server
                      (init [_] (initialise @self))
                      (terminate [_ cause] (info (str aname " terminated.")))
                      (handle-cast [_ from id message] (msg-handler from message)))))