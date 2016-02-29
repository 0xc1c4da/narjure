(ns narjure.actor.concept-creator
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [narjure.actor.task-dispatcher :refer [concept-map]]
    [narjure.actor.concept :refer [concept-actor]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare concept-creator-actor)

(defn create-concept2
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

     (if (not (contains? @concept-map term))\n    (create-concept2 term task))
  "
  [{term :term :as task}]
  ; if concept not exist then create - goes here

  ;(! :task-dispatcher [:task-msg task])
  (! :logger [:log-msg :log-debug "concept-creator - process-task"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In concept-creator :else" msg)]))

(defn concept-creator-actor
  ""
  []
  (register! :concept-creator @self)
  (loop []
    (receive [msg]
             [:task-msg task] (process-task task)
             :else (process-unhandled-msg msg))
    (recur)))

