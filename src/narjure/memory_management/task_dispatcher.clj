(ns narjure.memory-management.task-dispatcher
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(declare task-dispatcher task forget-concept concept-count)

(def c-map (atom {}))

(defactor task-dispatcher
  "Concept-map is atom {:term :actor-ref} shared between
         task-dispatcher and concept-creator"
  {:task-msg           task
   :forget-concept-msg forget-concept
   :concept-count-msg  concept-count})

(def aname :task-dispatcher)

(defn task
  [[_ input-task] _]
  (let [concept-creator (whereis :concept-creator)
        term (input-task :term)]
    (if-let [concept (@c-map term)]
      (! concept :task-msg input-task)
      (! concept-creator [:create-concept-msg @self input-task c-map])))
  #_(debug aname (str "process-task" input-task)))

(defn forget-concept [[_ forget-concept] _]
  (debug aname "process-forget-concept"))

(defn concept-count [_ _]
  (info aname (format "Concept count[%s]" (count @c-map))))
