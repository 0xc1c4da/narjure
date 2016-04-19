(ns narjure.memory-management.concept
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :as t])
  (:refer-clojure :exclude [promise await]))

(declare concept task-req belief-req inference-req persistence-req)

(defactor concept
          "State is a map
          {:name :budget :activation-level :belief-tab :goal-tab :task-bag :term-bag}
          (this list may not be complete)."
          {:task-msg            task-req
           :belief-req-msq      belief-req
           :inference-req-msq   inference-req
           :persistence-req-msg persistence-req})

(defn debug [msg] (t/debug :concept msg))

(defn task-req [_ _]
  #_(debug "process-task"))

(defn belief-req [_ _]
  (debug "process-belief-req"))

(defn inference-req [_ _]
  (debug "process-inference-req"))

(defn persistence-req [_ _]
  (debug "process-persistence-req"))
