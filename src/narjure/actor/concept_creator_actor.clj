(ns narjure.actor.concept-creator-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [narjure.actor.concept :refer [concept-actor]]
    )
  (:refer-clojure :exclude [promise await])
  )

(declare create-concept)
(declare process-task)
(declare concept-creator-actor)

(defsfn concept-creator-actor
        ""
        []

        (register! :concept-creator @self)
        (loop []
          (receive [msg]
                   [:create-concept-msg from task c-map] (process-task from task c-map)
                   :else (! :logger [:log-msg :log-debug :concept-creator (str "unhandled msg:" msg)]))
          (recur)))

(defn create-concept
        "TODO: update state for concept-actor to state initialiser
         TODO: Create required sub-term concepts and propogate budget
         "
        [task c-map]
        (let [{term :term} task]
          (swap! c-map assoc term (spawn concept-actor))
          ;(! :logger [:log-msg :log-debug :concept-creator (str "Created concept: " term)])
          )
        )

(defn process-task
        "When concept-map does not contain :term, create concept actor for term
         then post task to task-dispatcher either way
        "
        [from task c-map]
        (let [term (task :term)]
          ; when concept not exist then create - goes here
          (when (not (contains? @c-map term))
            (create-concept task c-map)))

        (! from [:task-msg task])
        ;(! :logger [:log-msg :log-debug :concept-creator "concept-creator - process-task"])
        )
