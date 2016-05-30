(ns narjure.memory-management.concept-manager
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.bag :as b]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :concept-manager)
(def c-priority 0.5)
(def max-concepts 1000)
(def c-bag (atom (b/default-bag max-concepts)))

(defn make-general-concept
  "Create a concept, for the supplied term, and add to
   the concept bag"
  [term]
  (let [concept-ref (spawn (concept term))]
    (swap! c-bag b/add-element {:id term :priority c-priority :ref concept-ref}))
  #_(debug aname (str "Created concept: " term)))

(defn create-concept-handler
  "Create a concept for each term in statement, if they dont
   exist. Then post the task back to task-dispatcher."
  [from [_ {:keys [statement]  :as task}]]
  (doseq [term (:terms task)]
    (when-not (b/exists? @c-bag term)
      (make-general-concept term)))
  (cast! from [:task-msg task])
  #_(debug aname "In create-concepts"))

(defn persist-state-handler
  ""
  [from message]
  ;todo
  (info (str "in persist-state-handler"))
  )

(defn load-state-handler
  ""
  [from message]
  ;todo
  (info (str "in load-state-handler"))
  )

(defn budget-update-handler
  "all we have to do is re-adding the new item"
  [from message]
  (info (str "in budget-update-handler"))                   ;use add-element and not update here for the case that
  (swap! c-bag b/add-element message))                      ;it doesnt exist anymore

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor: registers actor and sets actor state"
  [aname actor-ref]
  ;(info (str "In initialise"))
  (register! aname actor-ref)
  (set-state! {}))

(defn clean-up
  "Send :exit message to all concepts"
  []
  ;todo
  (comment (doseq [{{actor-ref :ref} sym} (:elements-map @c-bag)]
             (info (str "actor-ref" actor-ref))
             (shutdown! actor-ref))))


(def display (atom '()))
(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger display message)
  (case type
    :create-concept-msg (create-concept-handler from message)
    :persist-state-msg (persist-state-handler from message)
    :load-state-msg (load-state-handler from message)
    :budget-update-msg (budget-update-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn concept-manager []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str "cleaning up")))
      (handle-cast [_ from id message] (msg-handler from message)))))

