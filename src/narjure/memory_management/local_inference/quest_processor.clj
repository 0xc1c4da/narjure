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

(defn decrease-budget [quest]
  (let [budget (:budget quest)
        solution (:solution quest)]
    ;todo improve budget function here
    (info (str "decrease-budget " quest))
    (let [new-budget [(* (- 1.0 (second (:truth solution))) (first budget)) (second budget)]] ;TODO dependent on solution confidence
      (info (str "in let"))
      (assoc quest :budget new-budget))))

(defn user? [task]
  (= (:source task) :input))

(defn process-quest [state quest tasks old-item]
  ;group-by :task-type tasks
  (let [goals (filter #(= (:task-type %) :goal) tasks)]
    (info (str "task bag: " tasks))
    ;filter beliefs matching concept content
    ;project to task time
    ;select best ranked
    (if (= (:statement quest) (:id @state))
      (let [projected-goals (map #(project-eternalize-to (:occurrence quest) % @nars-time) (filter #(= (:statement %) (:id @state)) goals))]
        (if (not-empty projected-goals)
          ;select best solution
          (let [solution (reduce #(max ((second (:truth %1)) (second (:truth %2)))) projected-goals)
                answerered-quest (assoc quest :solution solution)]
            (info (str "at if)"))
            (if (or (= (:solution quest) nil)
                    (> (second (:truth (project-eternalize-to (:occurrence quest) solution @nars-time)))
                       (second (:truth (project-eternalize-to (:occurrence quest) (:solution quest) @nars-time)))))
              ;update budget and tasks
              (let [result (decrease-budget answerered-quest)]
                (add-to-tasks state result old-item)
                ;if answer to user quest ouput answer
                (when (user? quest)
                  (info (str "result: " result))
                  (output-task [:solution-to (str (narsese-print (:statement quest)) "??")] (:solution result))))

              (add-to-tasks state quest old-item)        ;it was not better, we just add the question and dont replace the solution
              ))
          ;was empty so just add
          (add-to-tasks state quest old-item)
          ))
      (add-to-tasks state quest old-item)                ;it has other content, so we just add it for general inference purposes (quest derivation)
      )))
