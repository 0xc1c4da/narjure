(ns narjure.perception-action.task-creator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self whereis state set-state! shutdown! unregister!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [clojure.set :as set]
    [narjure.global-atoms :refer :all]
    [narjure.defaults :refer :all]
    [nal.term_utils :refer :all]
    [narjure.debug-util :refer :all]
    [nal.deriver.projection-eternalization :refer [eternalize]])
  (:refer-clojure :exclude [promise await]))

(def aname :task-creator)

(defn sentence [_ _]
  (debug aname "process-sentence"))

;(def nars-time (atom 0))

(defn system-time-tick-handler
  "inc :time value in global atom for each system-time-tick-msg"
  []
  (swap! nars-time inc))

;(def nars-id (atom -1))

(defn get-id
  "inc the task :id in actor state and returns the value"
  []
  (set-state! (update @state :id inc))
  (@state :id))

(defn get-id []
  (swap! nars-id inc))

(defn get-time
  "return the current time from actor state"
  []
  (@state :time))

(defn initialise
  "Initialises actor:
      registers actor and sets actor state"
  [aname actor-ref]
  (register! aname actor-ref)
  (set-state! {:task-dispatcher (whereis :task-dispatcher)}))

(defn create-new-task
  "create a new task with the provided sentence and default values
   convert tense to occurrence time if applicable"
  [sentence time id syntactic-complexity]
  (let [occurrence (:occurrence sentence)
        toc (case occurrence
              :eternal :eternal
              (+ occurrence time))
        content (:statement sentence)
        task-type (:task-type sentence)]
    {:truth (:truth sentence)
     :desire (:desire sentence)
     :budget (task-type budgets)
     :creation time
     :occurrence toc
     :source :input
     :id id
     :evidence (list id)
     :sc syntactic-complexity
     :terms (termlink-subterms content)
     :solution nil
     :task-type task-type
     :statement content}))

(defn create-derived-task
  "Create a derived task with the provided sentence, budget and occurence time
   and default values for the remaining parameters"
  [sentence budget time id evidence syntactic-complexity]
  (let [content (:statement sentence)]
    {:truth      (:truth sentence)
     :desire     (:desire sentence)
     :budget     budget
     :creation   time
     :occurrence (:occurrence sentence)
     :source     :derived
     :id         id
     :evidence   evidence
     :sc         syntactic-complexity
     :terms      (termlink-subterms content)
     :solution   nil
     :task-type  (:task-type sentence)
     :statement  content}))

(defn event? [sentence] (not= :eternal (:occurrence sentence)))

(defn sentence-handler
  "Processes a :sentence-msg"
  [from [_ sentence]]
  (let [syntactic-complexity (syntactic-complexity (:statement sentence))]
    (when (< syntactic-complexity max-term-complexity)
      (let [new-task (create-new-task
                       sentence
                       @nars-time
                       (get-id)
                       syntactic-complexity)]
        (cast! (:task-dispatcher @state) [:task-msg new-task])
        (when (event? sentence)
          (cast! (:task-dispatcher @state) [:task-msg (eternalize new-task)]))))))

(defn derived-sentence-handler
  "processes a :derived-sentence-msg"
  [from [msg [sentence budget evidence]]]
  (let [syntactic-complexity (syntactic-complexity (:statement sentence))]
       (when (< syntactic-complexity max-term-complexity)
         (let [derived-task (create-derived-task
                              sentence
                              budget
                              @nars-time
                              (get-id)
                              evidence
                              syntactic-complexity)]
           (cast! (:task-dispatcher @state) [:task-msg derived-task])
           (when (event? sentence)
             (cast! (:task-dispatcher @state) [:task-msg (eternalize derived-task)]))))))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(def display (atom '()))
(def search (atom ""))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (when (not= type :system-time-tick-msg) (debuglogger search display message))
  (case type
    :sentence-msg (sentence-handler from message)
    :derived-sentence-msg (derived-sentence-handler from message)
    :system-time-tick-msg (system-time-tick-handler)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn task-creator []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))
