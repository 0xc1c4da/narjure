(ns narjure.memory-management.persistence-manager
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare concept-state persistence-manager)

(defactor persistence-manager
  "State is file system handles"
  [in-state]
  {:concept-state-msg concept-state})

(defn concept-state [_ _]
  (debug :persistence-manager "process-concept-state"))
