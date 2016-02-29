(ns nars.task-dispatcher-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [nars.logger :refer [logger]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare process-task)
(declare process-forget-concept)
(declare process-unhandled-msg)
(declare task-dispatcher-actor)

(defn process-task
  "Takes inut task and checks to see if concept exists,
  if not sends task to concept-creator else posts task to
  relevant concept actor"
  [{term :term} input-task & concept-map]

  (if (contains? concept-map term)
    (! (term concept-map) :task-msg input-task)
    (! :concept-creator :task-msg input-task))

  (! :logger [:log-msg :log-debug "In task-dispatcher :task-msg"]))

(defn process-forget-concept [forget-concept concept-map]
  (! :logger [:log-msg :log-debug "In task-dispatcher :forget-concept-msg"]))

(defn process-unhandled-msg [msg]
  (! :logger [:log-msg :log-debug (str "In task-dipatcher :else" msg)]))

; {term actor-ref}
(def concept-map {atom {}})

(defsfn task-dispatcher-actor
        "concept-map is atom {:term :actor-ref} shared between task-dispatcher and concept-creator"
        []
        (register! :task-dispatcher @self)
        (loop []
          (receive [msg]
                   [:task-msg input-task] (process-task input-task concept-map)
                   [:forget-concept-msg forget-concept] (process-forget-concept forget-concept concept-map)
                   :else (process-unhandled-msg msg))
          (recur)))
