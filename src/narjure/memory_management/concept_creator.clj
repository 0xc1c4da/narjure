(ns narjure.memory-management.concept-creator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare concept-creator task)

(defactor concept-creator {:create-concept-msg task})

(def aname :concept-creator)

(defn create-concept
  ;TODO: update state for concept-actor to state initialiser
  ;TODO: Create required sub-term concepts and propogate budget
  [task c-map]
  (let [{term :term} task]
    (swap! c-map assoc term (spawn concept))
    #_(debug aname (str "Created concept: " term))))

(defn task
  "When concept-map does not contain :term, create concept actor for term
   then post task to task-dispatcher either way."
  [[_ from task c-map] _]
  (let [term (task :term)]
    ; when concept not exist then create - goes here
    (when (not (contains? @c-map term))
      (create-concept task c-map)))

  (! from [:task-msg task])
  #_(debug aname "concept-creator - process-task"))
