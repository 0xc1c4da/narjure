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

(defn add-to-tasks [state task old-item]
  (info (str "old time: " old-item))
  (when (not= nil old-item)
    (let [[element bag] (b/get-by-id (:tasks @state) old-item)]
      (info (str "have element " element))
      (when (not= nil element)
        (info (str "in when"))
        (set-state! (assoc @state :tasks bag)))))
  (set-state! (assoc @state :tasks (b/add-element (:tasks @state) {:id task :priority (first (:budget task))}))))

(defn revisable? [t1 t2]
  (empty? (clojure.set/intersection (set (:evidence t1)) (set (:evidence t2)))))

(defn create-revised-task
  "create a revised task with the provided sentence, truth and default value"
  [sentence truth evidence]
  ;todo budget should be updated
  (assoc sentence :truth truth :evidence evidence))

(defn revise [t1 t2]
  (let [revised-truth (nal.deriver.truth/revision (:truth t1) (:truth t2))
        evidence (make-evidence (:evidence t1) (:evidence t2))]
    (create-revised-task t1 revised-truth evidence)))