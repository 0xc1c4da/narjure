(ns narjure.perception-action.task-creator
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare concept-state task-creator)

(defactor task-creator
  ""
  []
  {})
