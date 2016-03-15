(ns narjure.actor.system-time
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare system-time system-time-tick)

(def aname :system-time)

(defsfn system-time
  "state is system-time"
  []
  (register! aname @self)
  (set-state! 0)
  (actor-loop aname system-time-tick))

(defn system-time-tick [_ state]
  ;(debug aname (str "process-system-time-tick " state))
  (inc state))
