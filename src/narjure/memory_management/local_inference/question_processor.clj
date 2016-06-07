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

(defn decrease-budget [question]
  (let [budget (:budget question)
        solution (:solution question)]
    ;todo improve budget function here
    (info (str "decrease-budget " question))
    (let [new-budget [(* (- 1.0 (second (:truth solution))) (first budget)) (second budget)]] ;TODO dependent on solution confidence
      (info (str "in let"))
      (assoc question :budget new-budget))))

(defn user? [task]
  (= (:source task) :input))

(defn process-question [state question tasks old-item]
  ;group-by :task-type tasks
  (let [beliefs (filter #(= (:task-type %) :belief) tasks)]
    (info (str "task bag: " tasks))
    ;filter beliefs matching concept content
    ;project to task time
    ;select best ranked
    (let [projected-beliefs (map #(project-eternalize-to (:occurrence question) % @nars-time) (filter #(= (:statement %) (:statement question)) beliefs))]
    (if (not-empty projected-beliefs)
      ;select best solution
      (let [solution (apply max-key confidence projected-beliefs)
            answerered-question (assoc question :solution solution)]
        (info (str "at if)"))
        (if (or (= (:solution question) nil)
                (> (second (:truth (project-eternalize-to (:occurrence question) solution @nars-time)))
                   (second (:truth (project-eternalize-to (:occurrence question) (:solution question) @nars-time)))))
          ;update budget and tasks
          (let [result (decrease-budget answerered-question)]
            (add-to-tasks state result old-item)
            ;if answer to user question ouput answer
            (when (and (user? question)
                       (= (:statement question) (:id @state)))
              (info (str "result: " result))
              (output-task [:answer-to (str (narsese-print (:statement question)) "?")] (:solution result))))

          (add-to-tasks state question old-item)        ;it was not better, we just add the question and dont replace the solution
        ))
      ;was empty so just add
      (add-to-tasks state question old-item)))))
