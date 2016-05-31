(ns narjure.memory-management.concept
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [narjure.bag :as b]
    [narjure.debug-util :refer :all]
    [narjure.control-utils :refer :all]
    [narjure.perception-action.task-creator :refer [nars-time]]
    [narjure.memory-management.local-inference.belief-processor :refer [process-belief]]
    [narjure.memory-management.local-inference.goal-processor :refer [process-goal]]
    [narjure.memory-management.local-inference.quest-processor :refer [process-quest]]
    [narjure.memory-management.local-inference.question-processor :refer [process-question]]
    [nal.deriver.truth :refer [t-or]])
  (:refer-clojure :exclude [promise await]))

(def max-tasks 100)
(def display (atom '()))

(defn task-handler
  ""
  [from [_ task]]
  (try
    (let [tasks (:elements-map (:tasks @state))]
      (case (:task-type task)
        :belief (process-belief task tasks)
        :goal (process-goal task tasks)
        :question (process-question task tasks)
        :quest (process-quest task tasks)))
    (catch Exception e (debuglogger display (str "local inference error " (.toString e)))))

  (comment
    ;add task to bag
    (try
      (let [concept-state @state
            task-bag (:tasks concept-state)
            newbag (b/add-element task-bag {:id task :priority (first (:budget task)) :task task})]
        (set-state! (merge concept-state {:tasks newbag})))
      (catch Exception e (debuglogger display (str "task add error " (.toString e)))))
    )
  )

(defn belief-request-handler
  ""
  [from message]
  ;todo
  )


(defn update-concept-budget []
  "Update the concept budget"
  (let [concept-state @state
        budget (:budget concept-state)
        tasks (:priority-index (:tasks concept-state))
        priority-sum (reduce t-or (for [x tasks] (:priority x)))
        state-update (assoc concept-state :budget (assoc budget :priority priority-sum))]
    (set-state! (merge concept-state state-update))
    (let [concept-state-new @state]
      (cast! (whereis :concept-manager) [:budget-update-msg
                                         {:id       (:id concept-state-new)
                                          :priority priority-sum
                                          :ref      @self}]))))

(defn inference-request-handler
  ""
  [from message]
  (let [concept-state @state
        task-bag (:tasks concept-state)]
    ;TODO get termlink and targets, this code so far is for forgetting task
    ; and sending budget update message to concept mgr
    (try
      (when (> (b/count-elements task-bag) 0)
       (let [[result1 bag1] (b/get-by-index task-bag ((partial selection-fn task-bag)))
             bag2 (b/add-element bag1 (forget-element result1))]
         (set-state! (merge concept-state {:tasks bag2}))
         (update-concept-budget)
         (debuglogger display ["selected inference task:" result1])))
      (catch Exception e (debuglogger display (str "inference request error " (.toString e)))))
    )
  )

(defn concept-state-handler
  "Sends a copy of the actor state to requesting actor"
  [from _]
  (let [concept-state @state]
    (cast! from [:concept-state-msg concept-state])))

(defn set-concept-state-handler
  "set concept state to value passed in message"
  [from [_ new-state]]
  (set-state! (merge @state new-state)))

(defn task-budget-update-handler
  ""
  [from message]
  ;todo change task bag item priority before
  (update-concept-budget)
  )

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor: registers actor and sets actor state"
  [name]
  (set-state! {:id name
               :budget {:priority 0 :quality 0}
               :tasks (b/default-bag max-tasks)
               :termlinks {}
               :concept-manager (whereis :concept-manager)
               :general-inferencer (whereis :general-inferencer)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger display message)
  (case type
    :task-msg (task-handler from message)
    :belief-request-msg (belief-request-handler from message)
    :inference-request-msg (inference-request-handler from message)
    :concept-state-request-msg (concept-state-handler from message)
    :set-concept-state-msg (set-concept-state-handler from message)
    :task-budget-update-msg (task-budget-update-handler from message)
    :shutdown (shutdown-handler from message)
    (debug (str "unhandled msg: " type))))

(defn concept [name]
  (gen-server
    (reify Server
      (init [_] (initialise name))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))