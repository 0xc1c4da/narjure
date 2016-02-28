(ns nars.core
    (:require
      [co.paralleluniverse.pulsar
       [core :refer :all]
       [actors :refer :all]
       ])
  (:require [immutant.scheduling :refer :all])
  (:require [nars.active-concept-collator-actor :refer [active-concept-collator-actor]])
  (:require [nars.anticipated-event-actor :refer [anticipated-event-actor]])
  (:require [nars.concept-creator-actor :refer [concept-creator-actor]])
  (:require [nars.cross-modal-integrator-actor :refer [cross-modal-integrator-actor]])
  (:require [nars.derived-task-creator-actor :refer [derived-task-creator-actor]])
  (:require [nars.forgettable-concept-collator-actor :refer [forgettable-concept-collator-actor]])
  (:require [nars.general-inferencer-actor :refer [general-inferencer-actor]])
  (:require [nars.new-input-task-creator-actor :refer [new-input-task-creator-actor]])
  (:require [nars.operator-executor-actor :refer [operator-executor-actor]])
  (:require [nars.persistence-manager-actor :refer [persistence-manager-actor]])
  (:require [nars.sentence-parser-actor :refer [sentence-parser-actor]])
  (:require [nars.serialiser-actor :refer [serialiser-actor]])
  (:require [nars.system-time-actor :refer [system-time-actor]])
  (:require [nars.task-dispatcher-actor :refer [task-dispatcher-actor]])
  (:require [nars.logger :refer [logger]])

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

;(defn system-tick [actors] (map #(! % :system-time-msg) actors ))
(defn system-tick [] (! :system-time :system-time-tick-msg))
(defn inference-tick [] (! :active-concept-collator :inference-tick-msg))
(defn forgetting-tick [] (! :forgettable-concept-collator :forgetting-tick-msg))

(defn -main
  "Doc's go here"
  [& args]
  (create-system-actors)
  (! :logger [:log-msg :log-info "NARS initialising..."])
  (schedule system-tick (-> {:every system-tick-interval}))
  (schedule inference-tick (-> {:in inference-tick-interval :every inference-tick-interval}))
  (schedule forgetting-tick (-> {:in forgetting-tick-interval :every forgetting-tick-interval}))
  ;(! :task-dispatcher :task-msg {:term "a --> b" :other "other"})
  (! :logger [:log-msg :log-info (str "Press any key to exit")])
  ;(let [x (read-line)])
  ; block until key pressed
  (read-line)
  ;(stop job1)
  )

(-main)