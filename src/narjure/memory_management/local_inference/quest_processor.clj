(ns narjure.memory-management.local-inference.quest-processor
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

(defn process-quest [state task tasks]
  ;group-by :task-type tasks
  (let [goals (filter #(= (:task-type %) :goal) tasks)]

    ;filter beliefs matching concept content
    ;project to task time
    ;select best ranked
    (let [projected-goals (map #(project-eternalize-to (:occurrence task) %  @nars-time) (filter #(= (:statement %) (:id @state)) goals))]
      (when (not-empty projected-goals)
        ;select best solution
        (let [solution (reduce #(max (second (:truth %))) projected-goals)]
          (assoc task :solution solution)
          ;update budget and tasks
          (let [task' (decrease-budget solution task)]
            ;if answer to user question ouput answer
            (when (user? task)
              (cast! (:message-display @state) [:task-msg task']))
            (add-to-tasks state task'))))))
  )
