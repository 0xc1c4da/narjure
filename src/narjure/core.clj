(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]]
    [immutant.scheduling :refer :all]
    [narjure.memory-management
     [concept-manager :refer [concept-manager c-bag max-concepts]]
     [event-buffer :refer [event-buffer e-bag max-events]]
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

  ; reset global bags
  (reset! c-bag (b/default-bag max-concepts))
  (reset! e-bag (b/default-bag max-events))

  ; update user with status
  (info "NARS initialised."))

(defn shutdown-nars [& _]
  (info "Shutting down actors...")

  ; cancel schedulers
  (stop)

  ; shutdown all concepts
  (doseq [id-item (:elements-map @c-bag)]
    (let [item (vals id-item)
          actor-ref (:ref item)] (shutdown! actor-ref)))

  ; shutdown all actors so they terminate cleanly
  (doseq [actor-name actors-names]
    (let [ref (whereis actor-name 100 TimeUnit/MILLISECONDS)]
      (when (not= nil ref)
        (println (str "shutdown " actor-name))
        (unregister! ref)
        (shutdown! ref))))


  ;;wait for concepts to shutdown
  (Thread/sleep 5000)
  (info "System shutdown complete."))

(defn run []
  (start-nars))

(defn shutdown []
  (shutdown-nars))

(defn reset []
  (info (str "reseting Actors System..."))
  (shutdown)
  (sleep 5 :sec)
  (run))

; call main function
(run)
