(ns narjure.memory-management.concept
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [narjure.global-atoms :refer :all]
    [narjure.bag :as b]
    [narjure.debug-util :refer :all]
    [narjure.control-utils :refer :all]
    [narjure.memory-management.local-inference.belief-processor :refer [process-belief]]
    [narjure.memory-management.local-inference.goal-processor :refer [process-goal]]
    [narjure.memory-management.local-inference.quest-processor :refer [process-quest]]
    [narjure.memory-management.local-inference.question-processor :refer [process-question]]
    [nal.deriver.truth :refer [t-or confidence frequency]]
    [nal.deriver.projection-eternalization :refer [project-eternalize-to]])
  (:refer-clojure :exclude [promise await]))

(def max-tasks 10)
(def display (atom '()))
(def search (atom ""))

(defn task-handler
  ""
  [from [_ task]]
  (debuglogger search display ["task processed:" task])
  (try
    (let [tasks (apply vector (for [x (:priority-index (:tasks @state))]
                   (:id x)))
          old-item {:id task :priority (first (:budget task))}]
      (case (:task-type task)
        :belief (process-belief state task tasks old-item)
        :goal (process-goal state task tasks old-item)
        :question (process-question state task tasks old-item)
        :quest (process-quest state task tasks old-item)))
    (catch Exception e (debuglogger search display (str "local inference error " (.toString e)))))

  (try
    (let [concept-state @state
          task-bag (:tasks concept-state)
          newbag (b/add-element task-bag {:id task :priority (first (:budget task))})]
      (let [newtermlinks (merge (apply merge (for [tl (:terms task)] ;prefer existing termlinks strengths
                                               {tl [0.5 0.5]})) (:termlinks concept-state))]
        (set-state! (merge concept-state {                  ;:tasks     newbag
                                          :termlinks (select-keys newtermlinks
                                                                  (filter #(b/exists? @c-bag %) (keys newtermlinks))) ;only these keys which exist in concept bag
                                          }))))
    (catch Exception e (debuglogger search display (str "task add error " (.toString e)))))
  )

(defn belief-request-handler
  ""
  [from [_ task]]
  ;todo get a belief which has highest confidence when projected to task time
  (try (let [tasks (:priority-index (:tasks @state))
             projected-beliefs (map #(project-eternalize-to (:occurrence task) (:id %) @nars-time)
                                (filter #(and (= (:statement (:id %)) (:id @state))
                                              (= (:task-type (:id %)) :belief)) tasks))]
     (when (not-empty projected-beliefs)
       (let [belief (reduce #(max (confidence %)) projected-beliefs)]
         (debuglogger search display ["selected belief:" belief "§"])
         (cast! (:general-inferencer @state) [:do-inference-msg [task belief]])
         )))
       (catch Exception e (debuglogger search display (str "belief request error " (.toString e))))))

(defn update-concept-budget []
  "Update the concept budget"
  (let [concept-state @state
        budget (:budget concept-state)
        tasks (:priority-index (:tasks concept-state))
        priority-sum (reduce t-or (for [x tasks] (:priority x)))
        state-update (assoc concept-state :budget (assoc budget :priority priority-sum))]
    (set-state! (merge concept-state state-update))
    ;update c-bag directly instead of message passing
    (swap! c-bag b/add-element {:id (:id @state) :priority priority-sum :ref @self}))
  )

(defn inference-request-handler
  ""
  [from message]
  (let [concept-state @state
        task-bag (:tasks concept-state)]
    ; and sending budget update message to concept mgr
    (try
      (when (> (b/count-elements task-bag) 0)
        (let [[result1 bag1] (b/get-by-index task-bag (selection-fn task-bag))
              bag2 (b/add-element bag1 (forget-element result1))]
          (set-state! (merge concept-state {:tasks bag2}))
          (update-concept-budget)
          (debuglogger search display ["selected inference task:" result1])
          ;now search through termlinks, get the endpoint concepts, and form a bag of them
          (let [initbag (b/default-bag 10)
                resbag (reduce (fn [a b] (b/add-element a b)) initbag (for [[k v] (:termlinks @state)]
                                                                        {:priority (:priority (first (b/get-by-id @c-bag k)))
                                                                         :id       k}))
                ;now select an element from this bag
                [beliefconcept bag1] (b/get-by-index resbag (selection-fn resbag))]
            ;and create a belief request message
            (when-let [{c-ref :ref} ((:elements-map @c-bag) (:id beliefconcept))]
              (cast! c-ref [:belief-request-msg (:id result1)])
              ))))
      (catch Exception e (debuglogger search display (str "inference request error " (.toString e)))))
    )
  )

(defn update-concept-budget2 []
  "Update the concept budget"
  (let [concept-state @state
        budget (:budget concept-state)
        tasks (:priority-index (:tasks concept-state))
        priority-sum (reduce t-or (for [x tasks] (:priority x)))
        state-update (assoc concept-state :budget (assoc budget :priority priority-sum))]
    (set-state! (merge concept-state state-update))

    ;update c-bag directly instead of message passing
    ;(reset! (:c-bag @state) (b/add-element @(:c-bag @state) {:id (:id @state) :priority priority-sum :ref @self}))

    (let [concept-state-new @state]
      (cast! (whereis :concept-manager) [:budget-update-msg
                                         {:id       (:id concept-state-new)
                                          :priority priority-sum
                                          :ref      @self}])))
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
  (set-state! {})
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
  (debuglogger search display message)
  (when (> debug-messages 0)
    (swap! lense-taskbags
           (fn [dic]
             (assoc dic (:id @state) (:tasks @state))))
    (swap! lense-termlinks
           (fn [dic]
             (assoc dic (:id @state) (:termlinks @state)))))
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