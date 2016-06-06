(ns narjure.memory-management.local-inference.belief-processor
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
    [narjure.memory-management.local-inference.local-inference-utils :refer :all]
    [nal.deriver.truth :refer [t-or confidence frequency]]
    [nal.deriver.projection-eternalization :refer [project-eternalize-to]])
  (:refer-clojure :exclude [promise await]))

(defn decrease-budget [task]
  ;todo
  task)

(defn increase-budget [task]
  ;todo
  task)

(defn expired? [anticipation]
  (> @nars-time (:expiry anticipation)))

(defn create-negative-confirmation-task [anticipation]
  (assoc anticipation :task-type :belief :truth (nal.deriver.truth/negation (:truth anticipation) 0)))

(defn confirmable-observable? [task]
  ;todo check state for observable
  (not= (:occurrence task) :eternal))

(defn create-anticipation-task [task]
  (assoc task :task-type :anticipation :expiry (+ (:occurrence task) 100)))

(defn process-belief [state task tasks old-item]
  ;group-by :task-type tasks
  (let [goals (filter #(= (:task-type %) :goal) tasks)
        beliefs (filter #(= (:task-type %) :belief) tasks)
        anticipations (filter #(= (:task-type %) :anticipation) tasks)
        questions (filter #(= (:task-type %) :question ) tasks)]
    
    ;filter goals matching concept content
    ;project-to task time
    ;select best ranked
    (let [projected-goals (map #(project-eternalize-to (:occurrence task) %  @nars-time) (filter #(= (:statement %) (:id @state)) goals))]
      (when (not-empty projected-goals)
        (let [goal (reduce #(max (confidence %)) projected-goals)]
          ;update budget and tasks
          (decrease-budget goal)
          ;update budget and tasks
          (increase-budget task))))

    ;filter beliefs matching concept content
    ;(project-to task time
    (let [projected-beliefs (map #(project-eternalize-to (:occurrence task) % @nars-time) (filter #(= (:statement %) (:id @state)) beliefs))]
      (when (= (:source task) :input)
        (doseq [projected-anticipation (map #(project-eternalize-to (:occurrence task) % @nars-time) anticipations)]
          ;revise anticpation and add to tasks
          (add-to-tasks state (revise projected-anticipation task) nil)))
      (doseq [revisable (filter #(revisable? task %) projected-beliefs)]
        ;revise beliefs and add to tasks
        (add-to-tasks state (revise task revisable) nil)))

    ;add task to bag
    (add-to-tasks state task old-item)

    ; check to see if revised or task is answer to question
    ;todo

    ;generate neg confirmation for expired anticipations
    ;and add to tasks
    (doseq [anticipation anticipations]
      (when (expired? anticipation)
        (let [neg-confirmation (create-negative-confirmation-task anticipation)]
          ;add to tasks
          (add-to-tasks state neg-confirmation nil))))

    ;when task is confirmable and observabnle
    ;add an anticipation tasks to tasks
    (when (confirmable-observable? task)
      (let [anticipated-task (create-anticipation-task task)]
        (add-to-tasks state anticipated-task nil))))
  )
