(ns narjure.perception-action.sentence-parser
  (:require
    [co.paralleluniverse.pulsar.actors :refer [!]]
    [narjure.narsese :refer [parse]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(declare sentence-parser system-time narsese-string)

(def serial-no (atom 0))

(defactor sentence-parser
  {:time 0}
  {:system-time-msg    system-time
   :narsese-string-msg narsese-string})

(def aname :sentence-parser)

(defn system-time [[_ time] _]
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

(defn narsese-string
  [[_ string] {time :time :as state}]
  (let [task (parse-task string time)]
    (! :anticipated-event [:input-task-msg task])
    (info aname (str "process-narsese-string" task)))
  state)
