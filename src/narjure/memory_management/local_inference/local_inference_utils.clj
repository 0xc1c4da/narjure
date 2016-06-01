(ns narjure.memory-management.local-inference.local-inference-utils
  (:require
    [co.paralleluniverse.pulsar.actors
     :refer [! spawn gen-server register! cast! Server self
             shutdown! unregister! set-state! state whereis]]
    [taoensso.timbre :refer [debug info]]
    [narjure.bag :as b]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(defn add-to-tasks [state task]
  (set-state! (assoc state :tasks (b/add-element (:tasks state) {:id task :priority (:priority task) :task task}))))

(defn revisable? [t1 t2]
  (empty? (clojure.set/intersection (set (:evidence t1)) (set (:evidence t2)))))

(defn revise [t1 t2]
  nal.deriver.truth/revision (:truth t1) (:truth t2))