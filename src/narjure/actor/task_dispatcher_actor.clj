(ns narjure.actor.task-dispatcher-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare c-map)
(declare process-task)
(declare process-forget-concept)
(declare process-concept-count)
(declare task-dispatcher-actor)

(defsfn task-dispatcher-actor
      "concept-map is atom {:term :actor-ref} shared between
       task-dispatcher and concept-creator
      "
      []

      (register! :task-dispatcher @self)
      (def concept-creator (whereis :concept-creator))
      (loop []
        (receive [msg]
                 [:task-msg input-task] (process-task input-task c-map concept-creator)
                 [:forget-concept-msg forget-concept] (process-forget-concept forget-concept)
                 :concept-count (process-concept-count c-map)
                 :else   (! :logger [:log-msg :log-debug :task-dispatcher (str "unhandled msg:" msg)]))
        (recur)))

(def c-map (atom {}))

(defn process-task

      "Takes inut task and checks to see if concept exists,
       if not sends task to concept-creator else posts task to
       relevant concept actor
      "
      [input-task c-map concept-creator]
      (let [term (input-task :term)]
        (if (contains? @c-map term)
          (! (@c-map term) :task-msg input-task)
          (! concept-creator [:create-concept-msg @self input-task c-map])))
  ;(! :logger [:log-msg :log-debug :task-dispatcher (str "process-task" input-task)])
      )

(defn process-forget-concept [forget-concept]
      (! :logger [:log-msg :log-debug :task-dispatcher "process-forget-concept"]))

(defn process-concept-count [c-map]
  ;(println (str (keys @c-map)))
  (! :logger [:log-msg :log-info :task-dispatcher (str "Concept count[" (count @c-map) "]")])
  )
