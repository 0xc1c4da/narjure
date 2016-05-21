(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]]
    [immutant.scheduling :refer :all]
    [narjure.memory-management
     [concept-manager :refer [concept-manager c-bag]]
     [event-buffer :refer [event-buffer e-bag]]
     [task-dispatcher :refer [task-dispatcher]]]
    [narjure.general-inference
     [concept-selector :refer [concept-selector]]
     [event-selector :refer [event-selector]]
     [general-inferencer :refer [general-inferencer]]]
    [narjure.perception-action
     [operator-executor :refer [operator-executor]]
     [sentence-parser :refer [sentence-parser]]
     [task-creator :refer [task-creator]]]
    [taoensso.timbre :refer [info set-level!]]
    [narjure.bag :as b])
  (:refer-clojure :exclude [promise await])
  (:import (ch.qos.logback.classic Level)
           (org.slf4j LoggerFactory)
           (java.util.concurrent TimeUnit))
  (:gen-class))

;co.paralleluniverse.actors.JMXActorMonitor
(def actors-names
  #{:concept-selector
    :event-selector
    :concept-manager
    :general-inferencer
    :operator-executor
    :sentence-parser
    :task-creator
    :task-dispatcher
    :event-buffer})

(defn create-system-actors
  "Spawns all actors which self register!"
  []
  (spawn concept-selector)
  (spawn event-selector)
  (spawn event-buffer)
  (spawn concept-manager)
  (spawn general-inferencer)
  (spawn operator-executor)
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

(def inference-tick-interval 25)
(def system-tick-interval 1000)

(defn inference-tick []
  (cast! (whereis :concept-selector) [:inference-tick-msg])
  (cast! (whereis :event-selector) [:inference-tick-msg]))

(defn system-tick []
  (cast! (whereis :task-creator) [:system-time-tick-msg]))

(defn prn-ok [msg] (info (format "\t[OK] %s" msg)))

(defn start-timers []
  (info "Initialising system timers...")
  (schedule inference-tick {:in    inference-tick-interval
                            :every inference-tick-interval})
  (prn-ok :inference-timer)

  (schedule system-tick {:every system-tick-interval})
  (prn-ok :system-timer)

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

  ;(cast! (whereis :persistence-manager) [:restore-concept-state-msg "d:/clojure/snapshot1.nar"])
  ;(Thread/sleep  1000)
  ;(info (str "Concept count: " (count @c-bag)))

  ; *** Test code
  (comment
    (do
      (info "Beginning test...")
      (time
        (loop [n 0]
          (when (< n 100)
            ; select approximately 90% from existing concepts
            (let [n1 (if (< (rand) 0.01) n (rand-int (/ n 10)))]

              ;;(cast! (whereis :task-dispatcher) [:task-msg {:term  (format "a --> %d" n1) :other "other"}])
              (cast! (whereis :sentence-parser) [:narsese-string-msg (format "<a --> t%d>." n1)])
              (when (== (mod n 10) 0)
                (info (format "processed [%s] messages" n))))
            (recur (inc n)))))))

  (do
    (info "Beginning test...")
    (let [sentence-parser (whereis :sentence-parser)]
      (time
        (loop [n 0]
          (when (< n 10000)
            (cast! sentence-parser [:narsese-string-msg (format "<a --> term%d>." n)])
            (when (== (mod n 1000) 0)
              (info (format "processed [%s] messages" n)))
            (recur (inc n))))))
      )

  ; allow delay for all actors to process their queues
  (print "Processing ")
  (dotimes [n 5]
          (print ".")
          (flush)
          (Thread/sleep 1000))
  (println "")

  (info "Test complete.")
  ; *** End test code

  ; test persistence
  ;(info "Test persistence")
  (info (str "c-bag count: " (b/count-elements @c-bag)))
  (info (str "e-bag count: " (b/count-elements @e-bag)))
  ;(cast! (whereis :persistence-manager) [:persist-concept-state-msg "d:/clojure/snapshot1.nar"])

  (print "Processing ")
  (dotimes [n 5]
    (print ".")
    (flush)
    (Thread/sleep 1000))
  (println "")
  (info "Test complete.")


  (info "Shutting down actors...")

  ; cancel schedulers
  (stop)

  ; shutdown all actors so they terminate cleanly
  (doseq [actor-name actors-names]
    (shutdown! (whereis actor-name 10 TimeUnit/MILLISECONDS)))

  ;;wait for concepts to shutdown
  (Thread/sleep 5000)
  (info "System shutdown complete.")
)
; call main function
(defn run []
  (future (start-nars)))
(run)