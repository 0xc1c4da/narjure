(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]]
    [immutant.scheduling :refer :all]
    [narjure.memory-management
     [concept-creator :refer [concept-creator]]
     [forgettable-concept-collator :refer [forgettable-concept-collator]]
     [persistence-manager :refer [persistence-manager]]
     [task-dispatcher :refer [task-dispatcher]]]
    [narjure.general-inference
     [active-concept-collator :refer [active-concept-collator]]
     [general-inferencer :refer [general-inferencer]]]
    [narjure.perception-action
     [operator-executor :refer [operator-executor]]
     [sentence-parser :refer [sentence-parser]]
     [task-creator :refer [task-creator]]]
    [taoensso.timbre :refer [info set-level!]])
  (:refer-clojure :exclude [promise await])
  (:import (ch.qos.logback.classic Level)
           (org.slf4j LoggerFactory))
  (:gen-class))

;co.paralleluniverse.actors.JMXActorMonitor
(def actors-names
  #{:active-concept-collator
    :concept-creator
    :forgettable-concept-collator
    :general-inferencer
    :operator-executor
    :persistence-manager
    :sentence-parser
    :task-creator
    :task-dispatcher})

(defn create-system-actors
  "Spawns all actors which self register!"
  []
  (spawn active-concept-collator)
  ;;(register! :concept-creator (spawn concept-creator))
  (spawn concept-creator)
  (spawn forgettable-concept-collator)
  (spawn general-inferencer)
  (spawn operator-executor)
  (spawn persistence-manager :state)
  (spawn sentence-parser)
  (spawn task-creator)
  (spawn task-dispatcher))

(defn check-actor [actor-name]
  (info (if (whereis actor-name) "\t[OK]" "\t[FAILED]") (str actor-name)))

(defn check-actors-registered []
  (info "Checking all services are registered...")
  (doseq [actor-name actors-names]
    (check-actor actor-name))
  (info "All services registered."))

(def inference-tick-interval 2500)
(def system-tick-interval 2000)

(defn inference-tick []
  (cast! (whereis :active-concept-collator) [:inference-tick-msg]))

(defn system-tick []
  (cast! (whereis :task-creator) [:system-time-tick-msg]))

(defn prn-ok [msg] (info (format "\t[OK] %s" msg)))

(defn start-timers []
  (info "Initialising system timers...")
  (schedule inference-tick {:in    inference-tick-interval
                            :every inference-tick-interval})
  (prn-ok :system-timer)

  (schedule system-tick {:every system-tick-interval})
  (prn-ok :inference-timer)

  (info "System timer initialisation complete."))

(defn disable-third-party-loggers []
  (doseq [logger ["co.paralleluniverse.actors.JMXActorMonitor"
                  "org.quartz.core.QuartzScheduler"
                  "co.paralleluniverse.actors.LocalActorRegistry"
                  "co.paralleluniverse.actors.ActorRegistry"
                  "org.projectodd.wunderboss.scheduling.Scheduling"]]
    (.setLevel (LoggerFactory/getLogger logger) Level/OFF)))

(defn setup-logging []
  (set-level! :debug)
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
  (do
    (info "Beginning test...")
    (time
      (loop [n 0]
        (when (< n 100000)
          ; select approximately 90% from existing concepts
          (let [n1 (if (< (rand) 0.01) n (rand-int (/ n 10)))]
            (cast! (whereis :task-dispatcher) [:task-msg {:term  (format "a --> %d" n1) :other "other"}])
            (when (== (mod n 10000) 0)
              (info (format "processed [%s] messages" n))))
          (recur (inc n))))))
  ; allow delay for all actors to process their queues
  (Thread/sleep 100)
  (info "Test complete.")
  ; *** End test code

  (comment
    (shutdown! (whereis :sentence-parser))
    (shutdown! (whereis :active-concept-collator))
    (shutdown! (whereis :concept-creator))
    (shutdown! (whereis :task-dispatcher))
    (shutdown! (whereis :task-creator))
    (shutdown! (whereis :operator-executor))
    (shutdown! (whereis :persistence-manager))
    (shutdown! (whereis :forgettable-concept-collator))
    (shutdown! (whereis :general-inferencer)))

  ; shutdown all actors so they terminate cleanly
  (doseq [actor-name actors-names]
    (let [actor-ref (whereis actor-name)]
      (shutdown! actor-ref)
      (join actor-ref)))

  ; cancel schedulers
  (stop))

; call main function
(defn run []
  (future (start-nars)))
(run)