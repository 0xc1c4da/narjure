(ns narjure.memory-management.local-inference.local-inference-utils
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [taoensso.timbre :refer [debug info]]
    [narjure.bag :as b]
    [narjure.perception-action.task-creator :refer :all]
    [nal.term_utils :refer :all]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def max-evidence 10)

(defn make-evidence [e1 e2]
  (take max-evidence (interleave e1 e2)))

(defn add-to-tasks [state task]
  (set-state! (assoc state :tasks (b/add-element (:tasks state) {:id task :priority (first (:budget task)) :task task}))))

(defn revisable? [t1 t2]
  (empty? (clojure.set/intersection (set (:evidence t1)) (set (:evidence t2)))))

(defn create-revised-task
  "create a revised task with the provided sentence, truth and default value"
  [sentence truth evidence]
  ;todo budget should be updated
  (assoc sentence :truth truth :id (get-id) :creation (get-time) :evidence evidence))

(defn revise [t1 t2]
  (let [revised-truth (nal.deriver.truth/revision (:truth t1) (:truth t2))
        evidence (make-evidence (:evidence t1) (:evidence t2))]
    (create-revised-task t1 revised-truth evidence)))