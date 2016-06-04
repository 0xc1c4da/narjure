(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]]
    [immutant.scheduling :refer :all]
    [narjure.global-atoms :refer :all]
    [narjure.memory-management
     [concept-manager :refer [concept-manager]]
     [event-buffer :refer [event-buffer]]
     [task-dispatcher :refer [task-dispatcher]]]
    [narjure.general-inference
     [concept-selector :refer [concept-selector]]
     [event-selector :refer [event-selector]]
     [general-inferencer :refer [general-inferencer]]]
    [narjure.perception-action
     [operator-executor :refer [operator-executor]]
     [sentence-parser :refer [sentence-parser]]
     [derived-load-reducer :refer [derived-load-reducer]]
     [task-creator :refer [task-creator]]]
    [narjure.narsese :refer [parse2]]
    [taoensso.timbre :refer [info set-level!]]
    [narjure.bag :as b])
  (:refer-clojure :exclude [promise await])
  (:import (ch.qos.logback.classic Level)
           (org.slf4j LoggerFactory)
           (java.util.concurrent TimeUnit))
  (:gen-class))

(def inference-tick-interval 25)
(def system-tick-interval 10)
(def sentence-tick-interval 500)

(defn inference-tick []
  (cast! (whereis :concept-selector) [:inference-tick-msg])
  (cast! (whereis :event-selector) [:inference-tick-msg]))

(defn system-tick []
  (cast! (whereis :task-creator) [:system-time-tick-msg])
  (cast! (whereis :derived-load-reducer) [:system-time-tick-msg]))

(defn sentence-tick []
  (cast! (whereis :sentence-parser) [:narsese-string-msg (format "<%s-->%s>.:|10|:" (rand-nth ["a" "b" "c" "d" "e" "f" "g"]) (rand-nth ["h" "p" "j" "k" "l" "m" "n"]))]))

(defn prn-ok [msg] (info (format "\t[OK] %s" msg)))

(defn start-timers []
  (info "Initialising system timers...")
  (schedule inference-tick {:in    inference-tick-interval
                            :every inference-tick-interval})
  (prn-ok :inference-timer)

  (schedule system-tick {:every system-tick-interval})
  (prn-ok :system-timer)

  ;uncomment following two line to auto generate input sentences
  (schedule sentence-tick {:every sentence-tick-interval})
  (prn-ok :sentence-timer)

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

; supervisor test code
(def child-specs
  #(list
    ["1" :permanent 5 5 :sec 100 (derived-load-reducer)]
    ["2" :permanent 5 5 :sec 100 (general-inferencer)]
    ["3" :permanent 5 5 :sec 100 (concept-selector)]
    ["4" :permanent 5 5 :sec 100 (event-selector)]
    ["5" :permanent 5 5 :sec 100 (event-buffer)]
    ["6" :permanent 5 5 :sec 100 (concept-manager)]
    ["7" :permanent 5 5 :sec 100 (task-dispatcher)]
    ["8" :permanent 5 5 :sec 100 (task-creator)]
    ["9" :permanent 5 5 :sec 100 (operator-executor)]
    ["10" :permanent 5 5 :sec 100 (sentence-parser)]
    ))

(def sup (atom '()))

(defn run []
  (setup-logging)
  (info "NARS initialising...")
  (start-timers)

  ; reset global bags
  (reset! c-bag (b/default-bag max-concepts))
  (reset! e-bag (b/default-bag max-events))

  (reset! sup (spawn (supervisor :all-for-one child-specs)))

  ; update user with status
  (info "NARS initialised."))

(defn shutdown []
  (info "Shutting down actors...")

  ; cancel schedulers
  (stop)

  (shutdown! @sup)
  (join @sup)

  (info "System shutdown complete."))

; call main function
(run)
