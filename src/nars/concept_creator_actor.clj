(ns nars.concept-creator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:require [nars.task-dispatcher-actor :refer [concept-map]])
  (:require [nars.concept :refer [concept-actor]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare concept-creator-actor)

(defn create-concept
  "TODO: update state for concept-actor to state initialiser
   TODO: need to create concepts for sub terms
   "
  [term task]
  ;(swap! concept-map assoc term "actor-ref")
  ;(swap! concept-map assoc term (spawn concept-actor {:term term}))
  )

(defn process-task
  "If concept-map does not contain :term then create concept actor for term
   then post task to task-dispatcher either way

     (if (not (contains? @concept-map term))\n    (create-concept term task))
  "
  [{term :term :as task}]
  ; if concept not exist then create - goes here

  ;(! :task-dispatcher [:task-msg task])
  (! :logger [:log-msg :log-debug "concept-creator - process-task"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In concept-creator :else" msg)]))

(defsfn concept-creator-actor
        ""
        []
        (register! :concept-creator @self)
        (loop []
          (receive [msg]
                   [:task-msg task] (process-task task)
                   :else (process-unhandled-msg msg))
          (recur)))

