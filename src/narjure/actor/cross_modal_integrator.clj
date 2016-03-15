(ns narjure.actor.cross-modal-integrator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare cross-modal-integrator process)

(def aname :cross-modal-integrator)

(defsfn cross-modal-integrator
  "State is system-time and collection of precepts from current duration window."
  []
  (register! :cross-modal-integrator @self)
  (set-state! {:time 0 :percepts []})
  (actor-loop aname process))

(defhandler process)

(defmethod process :system-time [[_ time] state]
  (debug aname "process-system-time")
  {:time time :percepts (state :percepts)})

(defmethod process :percept-sentence [_ _]
  (debug aname "process-percept-sentence"))

