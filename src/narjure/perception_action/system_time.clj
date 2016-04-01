(ns narjure.perception-action.system-time
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare system-time system-time-tick)

(defactor system-time
  "State is system-time."
  0
  {:system-time-tick-msg system-time-tick})

(defn system-time-tick [_ state]
  ;(debug :system-time (str "process-system-time-tick " state))
  (inc state))
