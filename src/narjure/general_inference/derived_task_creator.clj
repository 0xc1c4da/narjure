(ns narjure.general-inference.derived-task-creator
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare derived-task-creator system-time inference-result)

(defactor derived-task-creator
  "State is system-time."
  {:time 0}
  {:system-time-msg      system-time
   :inference-result-msg inference-result})

(def aname :derived-task-creator)

(defn system-time [[_ time] _]
  (debug aname "process-system-time")
  {:time time})

(defn inference-result [_ _]
  (debug aname "process-inference-result"))
