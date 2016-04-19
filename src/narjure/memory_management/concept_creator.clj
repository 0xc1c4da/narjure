(ns narjure.memory-management.concept-creator
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.memory-management.task-dispatcher :refer [c-map]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :concept-creator)
(def max-concepts 1000)

(defn create-concept
  "Create a concept for the supplied term in c-map and
   add a key value pair to c-map for the created concept.
   Updates the concept count and if it the count exceeds
   the specified limit a :concept-limit-msg is posted to
   :forgettable-concept-collator"
  [term c-map]
  (let [concept-ref (spawn (concept))]
    (swap! c-map assoc term concept-ref)
    (set-state! (update @state :concept-count inc))
    (cast! concept-ref [:set-content-msg term]))
  (if (> (:concept-count @state) max-concepts)
    (cast! (:forgettable-concept-collator @state) [:concept-limit-msg]))
  #_(debug aname (str "Created concept: " term)))

(defn task-handler
  "Create a concept for each term in statement plus one for
   occurrence time if they dont exist. Then post the task
   back to task-dispatcher. Note that occurrence time keys
   are keys, whilst term keys are terms, which can be numbers"
  [from [msg task c-map]]
  (doseq [term (get-in task [:statement :terms])]
    (if-not (contains? @c-map term)
      (create-concept term c-map)))
  (let [oc (keyword (str (:occurrence task)))]
    (if-not (contains? @c-map oc)
      (create-concept oc c-map)))
  (cast! from [:task-msg task])
  #_(debug aname "concept-creator - process-task"))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor: registers actor and sets actor state"
  [aname actor-ref]
  (register! aname actor-ref)
  (set-state! {:concept-count 0 :forgettable-concept-collator (whereis :forgettable-concept-collator)}))

(defn clean-up
  "Send :exit message to all concepts"
  [actor-ref]
  (doseq [actor-ref (vals @c-map)]
    (shutdown! actor-ref)))

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
                         (terminate [_ cause] (clean-up @self))
                         (handle-cast [_ from id message] (msg-handler from message)))))
