(ns narjure.memory-management.event-buffer
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.memory-management.concept-manager :refer [c-bag]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.bag :as b]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :event-buffer)
(def max-events 100)
(def e-bag (atom (b/default-bag max-events)))
(def display (atom '()))

(defn event-handler
  ""
  [from [_ task]]
  ;todo
  (try
    (swap! e-bag b/add-element {:id task :priority ((:budget task) 0) :task task})
    (catch Exception e (debuglogger display (str "event add error " (.toString e)))))
  #_(debug aname "In create-concepts"))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor: registers actor and sets actor state"
  [aname actor-ref]
  (register! aname actor-ref)
  (set-state! {}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger display message)
  (case type
    :event-msg (event-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn event-buffer []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))
