(ns narjure.actor.persistence-manager
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare concept-state persistence-manager)

(def aname :persistence-manager)

(defsfn persistence-manager
  "state is file system handles"
  [in-state]
  (register! aname @self)
  (set-state! in-state)
  (actor-loop aname concept-state))

(defn concept-state [_ _]
  (debug aname "process-concept-state"))
