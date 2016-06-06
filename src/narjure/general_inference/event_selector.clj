(ns narjure.general-inference.event-selector
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.global-atoms :refer :all]
    [narjure.bag :as b]
    [clojure.math.numeric-tower :as math]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all]
    [narjure.control-utils :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :event-selector)

(def event-pairs 2)

(defn pairs-to-get
  ""
  [n, bag]
  ;if n >= count bag / 2
  ; get n pairs
  ;else
  ;count bag /2 round down
  )

(def display (atom '()))
(def search (atom ""))

(defn inference-tick-handler
  "Select n pairs of events events from event buffer for inference
   and post do-inference-msg to general inferencer"
  [from [msg]]
  ;todo
  (try
    (when (> (b/count-elements @e-bag) 1)
     (let [[result1 bag1] (b/get-by-index @e-bag ((partial selection-fn @e-bag)))
           [result2 bag2] (b/get-by-index bag1 ((partial selection-fn bag1)))
           bag3 (b/add-element bag2 (forget-element result1))
           bag4 (b/add-element bag3 (forget-element result2))]
       (reset! e-bag bag4)
       (debuglogger search display ["selected events:" result1 "§" result2 "§§"])
       (cast! (:general-inferencer @state) [:do-inference-msg [(:id result1) (:id result2)]])))
    (catch Exception e (debuglogger search display (str "event select error " (.toString e))))))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor:
      registers actor and sets actor state"
  [aname actor-ref]
  (reset! display '())
  (register! aname actor-ref)
  (set-state! {:general-inferencer (whereis :general-inferencer)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  ;(debuglogger display message) same as in concept_selector
  (case type
    :inference-tick-msg (inference-tick-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn event-selector []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))