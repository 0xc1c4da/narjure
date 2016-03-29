(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]]
    [immutant.scheduling :refer :all]
    [narjure.actor
     [active-concept-collator :refer [active-concept-collator]]
     [anticipated-event :refer [anticipated-event]]
     [concept-creator :refer [concept-creator]]
     [cross-modal-integrator :refer [cross-modal-integrator]]
     [derived-task-creator :refer [derived-task-creator]]
     [forgettable-concept-collator :refer [forgettable-concept-collator]]
     [general-inferencer :refer [general-inferencer]]
     [operator-executor :refer [operator-executor]]
     [persistence-manager :refer [persistence-manager]]
     [sentence-parser :refer [sentence-parser]]
     [system-time :refer [system-time]]
     [task-dispatcher :refer [task-dispatcher]]]
    [taoensso.timbre :refer [info set-level!]])
  (:refer-clojure :exclude [promise await])
  (:import (ch.qos.logback.classic Level)
           (org.slf4j LoggerFactory))
  (:gen-class))


;co.paralleluniverse.actors.JMXActorMonitor
(def actors-names
  #{:active-concept-collator
    :anticipated-event
    :concept-creator
    :cross-modal-integrator
    :derived-task-creator
    :forgettable-concept-collator
    :general-inferencer
    :operator-executor
    :persistence-manager
    :sentence-parser
    :system-time
    :task-dispatcher})

(defn create-system-actors
  "Spawns all actors which self register!"
  []
  (spawn active-concept-collator)
  (spawn anticipated-event)
  (spawn concept-creator)
  (spawn cross-modal-integrator)
  (spawn derived-task-creator)
  (spawn forgettable-concept-collator)
  (spawn general-inferencer)
  (spawn operator-executor)
  (spawn persistence-manager :state)
  (spawn sentence-parser)
  (spawn system-time)
  (spawn task-dispatcher))

(defn check-actor [actor-name]
  (info (if (whereis actor-name) "\t[OK]" "\t[FAILED]") (str actor-name)))

(defn check-actors-registered []
  (info "Checking all services are registered...")
  (doseq [actor-name actors-names]
    (check-actor actor-name))
  (info "All services registered."))

(def inference-tick-interval 2500)
(def forgetting-tick-interval 3000)
(def system-tick-interval 2000)

(defn inference-tick []
  (! :active-concept-collator [:inference-tick-msg]))

(defn forgetting-tick []
  (! :forgettable-concept-collator [:forgetting-tick-msg]))

(defn system-tick []
  (! :system-time [:system-time-tick-msg]))

(defn prn-ok [msg] (info (format "\t[OK] %s" msg)))

(defn start-timers []
  (info "Initialising system timers...")
  (schedule inference-tick {:in    inference-tick-interval
                            :every inference-tick-interval})
  (prn-ok :system-timer)

  (schedule forgetting-tick {:in    forgetting-tick-interval
                             :every forgetting-tick-interval})
  (prn-ok :forgetting-timer)

  (schedule system-tick {:every system-tick-interval})
  (prn-ok :inference-timer)

  (info "System timer initialisation complete."))

(def disable-third-party-loggers []
  (doseq [logger ["co.paralleluniverse.actors.JMXActorMonitor"
                  "org.quartz.core.QuartzScheduler"
                  "co.paralleluniverse.actors.LocalActorRegistry"
                  "co.paralleluniverse.actors.ActorRegistry"
                  "org.projectodd.wunderboss.scheduling.Scheduling"]]
    (.setLevel (LoggerFactory/getLogger logger) Level/OFF)))

(defn setup-logging []
  (set-level! :info)
  (disable-third-party-loggers))

(defn start-nars [& _]
  (setup-logging)
  (info "NARS initialising...")

  ; spawn all actors except concepts
  (create-system-actors)
  ; allow delay for all actors to be initialised
  (sleep 1 :sec)
  (check-actors-registered)
  (start-timers)

  ; update user with status
  (info "NARS initialised.")

  ; *** Test code
  (let [task-dispatcher (whereis :task-dispatcher)]
    (info "Beginning test...")
    (time
      (loop [n 0]
        (when (< n 1000000)
          ; select approximately 90% from existing concepts
          (let [n1 (if (< (rand) 0.01) n (rand-int (/ n 10)))]
            (! task-dispatcher [:task-msg {:term  (format "a --> %d" n1)
                                           :other "other"}])
            (when (== (mod n 100000) 0)
              (info (format "processed [%s] messages" n))))
          (recur (inc n))))))
  ; allow delay for all actors to process their queues
  (Thread/sleep 100)
  (info "Test complete.")
  ; *** End test code

  ; join all actors so the terminate cleanly
  (doseq [actor-name actors-names]
    (join (whereis actor-name)))

  ; cancel schedulers
  (stop))

; call main function
(defn run []
  (future (start-nars)))
