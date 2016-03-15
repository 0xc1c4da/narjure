(ns narjure.actor.sentence-parser
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self !]]]
    [narjure.narsese :refer [parse]]
    [narjure.actor.utils :refer [actor-loop defhandler]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(declare sentence-parser process)

(def aname :sentence-parser)

(def serial-no (atom 0))

(defsfn sentence-parser
  []
  (register! aname @self)
  (set-state! {:time 0})
  (actor-loop aname process))

(defhandler process)

(defmethod process :system-time-msg [[_ time] _]
  (debug aname "process-system-time")
  {:time time})

(defn parse-task
  "Parses a narsese string."
  [string system-time]
  (assoc (parse string)
    :stamp
    {:id              (swap! serial-no inc)
     :creation-time   system-time
     :occurrence-time system-time
     :trail           [serial-no]}))

(defmethod process :narsese-string-msg
  [[_ string] {time :time}]
  (let [task (parse-task string time)]
    (! :anticipated-event [:input-task-msg task])
    (info aname (str "process-narsese-string" task))))
