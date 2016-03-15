(ns narjure.actor.task-dispatcher
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self ! whereis]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(declare task-dispatcher process)

(def aname :task-dispatcher)
(def c-map (atom {}))

(defsfn task-dispatcher
  "concept-map is atom {:term :actor-ref} shared between
         task-dispatcher and concept-creator"
  []
  (register! aname @self)
  (actor-loop aname process))

(defhandler process)

(defmethod process :task-msg
  [[_ input-task] _]
  (let [concept-creator (whereis :concept-creator)
        term (input-task :term)]
    (if-let [concept (@c-map term)]
      (! concept :task-msg input-task)
      (! concept-creator [:create-concept-msg @self input-task c-map])))
  #_(debug aname (str "process-task" input-task)))

(defmethod process :forget-concept-msg [[_ forget-concept] _]
  (debug aname "process-forget-concept"))

(defmethod process :concept-count-msg [_ _]
  (info aname (format "Concept count[%s]" (count @c-map))))
