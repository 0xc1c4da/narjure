(ns narjure.core
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [immutant.scheduling :refer :all]
    [narjure.actor.active-concept-collator :refer [active-concept-collator-actor]]
    [narjure.actor.anticipated-event :refer [anticipated-event-actor]]
    [narjure.actor.concept-creator :refer [concept-creator-actor]]
    [narjure.actor.cross-modal-integrator :refer [cross-modal-integrator-actor]]
    [narjure.actor.derived-task-creator :refer [derived-task-creator-actor]]
    [narjure.actor.forgettable-concept-collator :refer [forgettable-concept-collator-actor]]
    [narjure.actor.general-inferencer :refer [general-inferencer-actor]]
    [narjure.actor.new-input-task-creator :refer [new-input-task-creator-actor]]
    [narjure.actor.operator-executor :refer [operator-executor-actor]]
    [narjure.actor.persistence-manager :refer [persistence-manager-actor]]
    [narjure.actor.sentence-parser :refer [sentence-parser-actor]]
    [narjure.actor.serialiser :refer [serialiser-actor]]
    [narjure.actor.system-time :refer [system-time-actor]]
    [narjure.actor.task-dispatcher :refer [task-dispatcher-actor]]
    [narjure.actor.logger :refer [logger]])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(def system-tick-interval 2000)
(def inference-tick-interval 2500)
(def forgetting-tick-interval 3000)

(defn create-system-actors []
  (spawn active-concept-collator-actor :state)
  (spawn anticipated-event-actor :state)
  (spawn concept-creator-actor)
  (spawn cross-modal-integrator-actor :state)
  (spawn derived-task-creator-actor :state)
  (spawn forgettable-concept-collator-actor :state)
  (spawn general-inferencer-actor :state)
  (spawn new-input-task-creator-actor :state)
  (spawn operator-executor-actor :state)
  (spawn persistence-manager-actor :state)
  (spawn sentence-parser-actor :state)
  (spawn serialiser-actor :state)
  (spawn system-time-actor 0)
  (spawn task-dispatcher-actor)
  (spawn logger :log-debug)
  )

(defn system-tick [] (! :system-time :system-time-tick-msg))
(defn inference-tick [] (! :active-concept-collator :inference-tick-msg))
(defn forgetting-tick [] (! :forgettable-concept-collator :forgetting-tick-msg))

(defn start-nars
  "Doc's go here"
  [& args]
  (create-system-actors)
  (! :logger [:log-msg :log-info "NARS initialising..."])
  (schedule system-tick (-> {:every system-tick-interval}))
  (schedule inference-tick (-> {:in inference-tick-interval :every inference-tick-interval}))
  (schedule forgetting-tick (-> {:in forgetting-tick-interval :every forgetting-tick-interval}))
  ;(! :task-dispatcher :task-msg {:term "a --> b" :other "other"})
  (! :logger [:log-msg :log-info (str "NARS initialised.")])
  (join (whereis :taskdispatcher))
  (stop)
  )

(start-nars)