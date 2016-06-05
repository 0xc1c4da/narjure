(ns narjure.memory-management.local-inference.question-processor
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [narjure.bag :as b]
    [narjure.debug-util :refer :all]
    [narjure.control-utils :refer :all]
    [narjure.global-atoms :refer :all]
    [narjure.memory-management.local-inference.local-inference-utils :refer [add-to-tasks]]
    [nal.deriver.truth :refer [t-or confidence frequency]]
    [nal.deriver.projection-eternalization :refer [project-eternalize-to]])
  (:refer-clojure :exclude [promise await]))

(defn decrease-budget [solution task]
  ;todo
  task)

(defn user? [task]
  (= (:source :input)))

(defn process-question [state task tasks]
  ;group-by :task-type tasks
  (let [beliefs (filter #(= (:task-type %) :belief) tasks)]

    ;filter beliefs matching concept content
    ;project to task time
    ;select best ranked
    (let [projected-beliefs (map #(project-eternalize-to (:occurrence task) % @nars-time) (filter #(= (:statement %) (:id @state)) beliefs))]
      (when (not-empty projected-beliefs)
        ;select best solution
        (let [solution (reduce #(max (second (:truth %))) projected-beliefs)]
          (assoc task :solution solution)
          ;update budget and tasks
          (let [task' (decrease-budget solution task)]
            ;if answer to user question ouput answer
            (when (user? task)
              (output-task "" solution)
              ;(cast! (:message-display @state) [:task-msg task'])
              )
            (add-to-tasks state task'))))))
  )
