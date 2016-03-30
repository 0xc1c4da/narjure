(ns narjure.perception-action.cross-modal-integrator
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare cross-modal-integrator system-time percept-sentence)

(defactor cross-modal-integrator
  "State is system-time and collection of precepts from current duration window."
  {:time 0 :percepts []}
  {:system-time-msg      system-time
   :percept-sentence-msg percept-sentence})

(def aname :cross-modal-integrator)

(defn system-time [[_ time] state]
  (debug aname "process-system-time")
  {:time time :percepts (state :percepts)})

(defn percept-sentence [_ _]
  (debug aname "process-percept-sentence"))
