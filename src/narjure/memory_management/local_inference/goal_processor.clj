(ns narjure.memory-management.local-inference.goal-processor
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [narjure.bag :as b]
    [narjure.debug-util :refer :all]
    [narjure.control-utils :refer :all]
    [narjure.perception-action.task-creator :refer [nars-time]]
    [nal.deriver.truth :refer [t-or expectation]]
    [nal.deriver.projection-eternalization :refer [project-eternalize-to]])
  (:refer-clojure :exclude [promise await]))

(defn decrease-budget [task]
  ;todo
  task)

(defn increase-budget [task]
  ;todo
  task)

(defn revisable? [t1 t2]
  (empty? (clojure.set/intersection (set (:evidence t1)) (set (:evidence t2)))))

(defn revise [t1 t2]
  nal.deriver.truth/revision (:truth t1) (:truth t2))

(defn add-to-tasks [state task]
  (set-state! (assoc state :tasks (b/add-element (:tasks state) task))))

(defn operation? [task]
  ;todo
  (= (:operator task) true))

(def decision-threshold 0.5)

(defn execute? [task]
  (> (expectation (:truth task)) decision-threshold))

(defn process-goal [state task tasks]
  ;group-by :task-type tasks
  (let [goals (filter #(= (:task-type %) :goal) tasks)
        beliefs (filter #(= (:task-type %) :belief) tasks)
        questions (filter #(= (:task-type %) :question ) tasks)]

    ;filter beliefs matching concept content
    ;project to task time
    ;select best ranked
    (let [projected-beliefs (map #(project-eternalize-to (:occurrence task) % @nars-time) (filter #(= (:statement %) (:id @state)) beliefs))]
      (when (not-empty projected-beliefs)
        (let [belief (reduce #(max (confidence %)) projected-beliefs)]
          ;update budget and tasks
          (decrease-budget task)
          ;update budget and tasks
          (increase-budget belief))))

    ;filter beliefs matching concept content
    ;(project to task time
    (let [projected-goals (map #(project-eternalize-to (:occurrence task) % @nars-time) (filter #(= (:statement %) (:id @state)) goals))]
      ;revise task with revisable goals
      (doseq [revisable (filter #(revisable? task %) goals)]
        ;revise goals and add to tasks
        (add-to-tasks state (revise revisable task))))
    ;add task to tasks
    (add-to-tasks state task)

    ; check to see if revised or task is answer to quest
    ;todo

    ;if operation project goal to current time
    ; if above decision threshold then execute
    (when (operation? task)
      (project-eternalize-to @nars-time task @nars-time)
      (when (execute? task)
        (cast! (:operator-executor @state) [:operator-execution-msg task]))))
  )
