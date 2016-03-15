(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [immutant.scheduling :refer :all]
    [narjure.actor.active-concept-collator-actor :refer [active-concept-collator-actor]]
    [narjure.actor.anticipated-event-actor :refer [anticipated-event-actor]]
    [narjure.actor.concept-creator-actor :refer [concept-creator-actor]]
    [narjure.actor.cross-modal-integrator-actor :refer [cross-modal-integrator-actor]]
    [narjure.actor.derived-task-creator-actor :refer [derived-task-creator-actor]]
    [narjure.actor.forgettable-concept-collator-actor :refer [forgettable-concept-collator-actor]]
    [narjure.actor.general-inferencer-actor :refer [general-inferencer-actor]]
    [narjure.actor.operator-executor-actor :refer [operator-executor-actor]]
    [narjure.actor.persistence-manager-actor :refer [persistence-manager-actor]]
    [narjure.actor.sentence-parser-actor :refer [sentence-parser-actor]]
    [narjure.actor.system-time-actor :refer [system-time-actor]]
    [narjure.actor.task-dispatcher-actor :refer [task-dispatcher-actor]]
    [narjure.actor.logger :refer [logger]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(defn create-system-actors
  "spawns all actors which self register!
  "
  []
  (spawn active-concept-collator-actor)
  (spawn anticipated-event-actor)
  (spawn concept-creator-actor)
  (spawn cross-modal-integrator-actor)
  (spawn derived-task-creator-actor)
  (spawn forgettable-concept-collator-actor)
  (spawn general-inferencer-actor)
  (spawn operator-executor-actor)
  (spawn persistence-manager-actor :state)
  (spawn sentence-parser-actor)
  (spawn system-time-actor)
  (spawn task-dispatcher-actor)
  (spawn logger :log-info)
  )

(defn check-actors-registered
  "
  "
  []

  (defn check-actor [actor-name]
    (if (whereis actor-name)
      (println "\t[OK]" (str actor-name))
      (println "\t[FAILED]" (str actor-name)))
    )

  (println "\nChecking all services are registered...")
  (check-actor :logger)
  (check-actor :active-concept-collator)
  (check-actor :anticipated-event)
  (check-actor :concept-creator)
  (check-actor :cross-modal-integrator)
  (check-actor :derived-task-creator)
  (check-actor :forgettable-concept-collator)
  (check-actor :general-inferencer)
  (check-actor :operator-executor)
  (check-actor :persistence-manager)
  (check-actor :sentence-parser)
  (check-actor :system-time)
  (check-actor :task-dispatcher))
  (println "\nAll services registered.")

(defn start-timers
  "
  "
  []
  (println "\nInitialising system timers...")
  (def inference-tick-interval 2500)
  (defn inference-tick [] (! :active-concept-collator :inference-tick-msg))
  (schedule inference-tick (-> {:in inference-tick-interval :every inference-tick-interval}))
  (println "\t[OK] ":system-timer "")

  (def forgetting-tick-interval 3000)
  (defn forgetting-tick [] (! :forgettable-concept-collator :forgetting-tick-msg))
  (schedule forgetting-tick (-> {:in forgetting-tick-interval :every forgetting-tick-interval}))
  (println "\t[OK] ":forgetting-timer "")

  (def system-tick-interval 2000)
  (defn system-tick [] (! :system-time :system-time-tick-msg))
  (schedule system-tick (-> {:every system-tick-interval}))
  (println "\t[OK] ":inference-timer "")

  (println "\nSystem timer initialisation complete.")
  )

(defn start-nars
  "
  "
  [& args]

  (println  "NARS initialising...")
  ; spawn all actors except concepts
  (create-system-actors)
  (Thread/sleep 1000) ; allow delay for all actors to be initialised
  (check-actors-registered)
  (start-timers)

  ; update user with status
  (! :logger [:log-msg :log-info :anon "NARS initialised."])


  ; *** Test code
  (def task-dispatcher (whereis :task-dispatcher))
  (! :logger [:log-msg :log-info :anon "Beginning test..."])
  (time
    (loop [n 0]
      (when (< n 1000000)
        (let [n1 (if (< (rand) 0.01) n (rand-int (/ n 10)))] ; select approximately 90% from existing concepts
          (! task-dispatcher [:task-msg {:term (format "a --> %d" n1) :other "other"}])
          (when (== (mod n 100000) 0)
            (! :logger [:log-msg :log-info :anon (str "processed [" n "] messages")])
            ))
        (recur (inc n)))))
  (Thread/sleep 100) ; allow delay for all actors to process their queues
  (! :logger [:log-msg :log-info :anon "Test complete."])
  ; *** End test code

  ; join all actors so the terminate cleanly
  (join (whereis :active-concept-collator))
  (join (whereis :anticipated-event))
  (join (whereis :concept-creator))
  (join (whereis :cross-modal-integrator))
  (join (whereis :derived-task-creator))
  (join (whereis :forgettable-concept-collator))
  (join (whereis :general-inferencer))
  (join (whereis :new-input-task-creator))
  (join (whereis :operator-executor))
  (join (whereis :persistence-manager))
  (join (whereis :sentence-parser))
  (join (whereis :serialiser))
  (join (whereis :system-time))
  (join (whereis :task-dispatcher))
  (join (whereis :logger))

  ; cancel schedulers
  (stop)
  )

; call main function
(start-nars)