(ns narjure.actor.sentence-parser-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [narjure.narsese :refer [parse]]
    [narjure.defaults :refer [serial-no]])
  (:refer-clojure :exclude [promise await])
  )

(declare process-system-time)
(declare process-narsese-string)
(declare sentence-parser-actor)

(defsfn sentence-parser-actor
        "Example format for using actor with  set-state! on recursion.
        Usage: (def actor (spawn actor [state]))
        This example uses a map for state {:id 0} and increments value on recursion
        state is system-time"
        []
        (register! :sentence-parser @self)
        (set-state! {:time 0})
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:narsese-string-msg string] (set-state! (process-narsese-string string @state))
                   :else (! :logger [:log-msg :log-debug :sentence-parser (str "unhandled msg:" msg)]))
          (recur)))

(defn process-system-time [time state]
  (! :logger [:log-msg :log-debug :sentence-parser "process-system-time"])
  {:time time})

(defn create-stamp-for-input-task [system-time serial-no]
  {:id serial-no :creation-time system-time :occurence-time system-time :trail [serial-no]})

(defn process-narsese-string
  "Parses a narsese string and converts tense to occurence time
   and adds a stamp {id: id :creation-time system-time :occurence-time occurence-time :trail []}
  "
  [string state]
  (let [task (assoc (parse string) :stamp (create-stamp-for-input-task (:time state) (swap! serial-no inc)))]
    (! :anticipated-event [:input-task-msg task])
    (! :logger [:log-msg :log-info :sentence-parser (str "process-narsese-string" task)])))
