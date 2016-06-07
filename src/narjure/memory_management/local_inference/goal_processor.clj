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
    [narjure.global-atoms :refer :all]
    [narjure.memory-management.local-inference.local-inference-utils :refer :all]
    [nal.deriver.truth :refer [t-or frequency confidence expectation]]
    [nal.deriver.projection-eternalization :refer [project-eternalize-to]])
  (:refer-clojure :exclude [promise await]))

(defn decrease-budget [task]
  ;todo
  task)

(defn increase-budget [task]
  ;todo
  task)


(defn operation? [task]
  (let [st (:statement task)]
    (if (and (= (first st) '-->)
                    (coll? (second st))
                    (= (first (second st)) 'ext-set))
      (let [op (nth st 2)]
        (and (clojure.string/starts-with? (name op) "op_")))
      false)))

(def decision-threshold 0.5)

(defn execute? [task]
  (> (expectation (:truth task)) decision-threshold))

(defn process-goal [state task tasks old-item]
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
    (let [projected-goals (map #(project-eternalize-to (:occurrence task) % @nars-time) (filter #(= (:statement %) (:statement task)) goals))]
      ;revise task with revisable goals
      (doseq [revisable (filter #(revisable? task %) projected-goals)]
        ;revise goals and add to tasks
        (add-to-tasks state (revise revisable task) nil))
      )

    ;add task to bag
    (add-to-tasks state task old-item)


    ; check to see if revised or task is answer to quest and increase budget accordingly
    ;todo
    ;check whether it is fullfilled by belief and decrease budget accordingly


    ;best operation project goal to current time
    ; if above decision threshold then execute
    (let [projected-goals (map #(project-eternalize-to @nars-time % @nars-time) (filter #(= (:statement %) (:statement task)) goals))]
     (when (not-empty projected-goals)
       (let [goal (reduce #(max (confidence %)) projected-goals)]
         (when (and (operation? goal)
                    (= (:statement goal) (:id @state)))   ;execution really only in concept which is responsible for this goal!
           (when (execute? goal)
             (cast! (whereis :operator-executor) [:operator-execution-msg goal]))))))

    ))
