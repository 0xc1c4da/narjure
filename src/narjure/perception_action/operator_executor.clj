(ns narjure.perception-action.operator-executor
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state!]]
    [co.paralleluniverse.pulsar [core :refer [defsfn]]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all]
    [narjure.global-atoms :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :operator-executor)

(defn operator-execution-handler
  "Processes an :operator-execution-msg:
    executes operation with optionally supplied parameters
    if feedback msg required posts :sentence-msg to task creator"
  [from [msg operationgoal]]
  (let [feedback (assoc operationgoal :task-type :belief
                                      :occurrence @nars-time
                                      :budget [0.9 0.8 0.5])]
    (output-task :execution operationgoal)
    (cast! (whereis :task-creator) [:derived-sentence-msg [feedback (:budget feedback) (:evidence feedback)]]) ;derived-sentence cause we keep evidence trail
    ))

(def display (atom '()))
(def search (atom ""))

(defn initialise
  "Initialises actor:
      registers actor and sets actor state"
  [aname actor-ref]
  (reset! display '())
  (register! aname actor-ref)
  (set-state! {:task-creator (whereis :task-creator)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger search display message)
  (case type
    :operator-execution-msg (operator-execution-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn operator-executor []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause])
      (handle-cast [_ from id message] (msg-handler from message)))))
