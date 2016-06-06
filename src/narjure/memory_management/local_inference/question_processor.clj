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
    (when (= (:statement question) (:id @state))
      (let [projected-beliefs (map #(project-eternalize-to (:occurrence question) % @nars-time) (filter #(= (:statement %) (:id @state)) beliefs))]
      (when (not-empty projected-beliefs)
        ;select best solution
        (let [solution (reduce #(max ((second (:truth %1)) (second (:truth %2)))) projected-beliefs)
              answerered-question (assoc question :solution solution)]
          (info (str "at if)"))
          (if (or (= (:solution question) nil) (> (second (:truth solution)) (second (:truth (:solution question)))))

            ;update budget and tasks
            (let [result (decrease-budget answerered-question)]
              (add-to-tasks state result old-item)
              ;if answer to user question ouput answer
              (when (user? question)
                (info (str "result: " result))
                (output-task [:solution-to (str (narsese-print (:statement question)) "?")] (:solution result))
                ;(cast! (:message-display @state) [:task-msg task'])
                )))))))
    (add-to-tasks state question old-item)))
