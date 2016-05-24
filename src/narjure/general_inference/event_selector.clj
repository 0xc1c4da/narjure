(ns narjure.general-inference.event-selector
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.memory-management.event-buffer :refer [e-bag]]
    [narjure.bag :as b]
    [clojure.math.numeric-tower :as math]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :event-selector)

(def event-pairs 2)
(def selection-parameter 3)
(defn selection-fn
  ""
  []
  (* (math/expt (rand) selection-parameter) (b/count-elements @e-bag)))

(defn pairs-to-get
  ""
  [n, bag]
  ;if n >= count bag / 2
  ; get n pairs
  ;else
  ;count bag /2 round down
  )

(defn inference-tick-handler
  "Select n pairs of events events from event buffer for inference
   and post do-inference-msg to general inferencer"
  [from [msg]]
  ;todo
  (when (> (b/count-elements @e-bag) 1)
    (let [result1 (b/get-by-index @e-bag (selection-fn))
          result2 (b/get-by-index (second result1) (selection-fn))]
      (reset! e-bag (second result2))
      (cast! (:general-inferencer @state) [:do-inference-msg [(first result1) (first result2)]])))
  #_(debug aname "process-inference-tick-msg"))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor:
      registers actor and sets actor state"
  [aname actor-ref]
  (register! aname actor-ref)
  (set-state! {:general-inferencer (whereis :general-inferencer)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (case type
    :inference-tick-msg (inference-tick-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def event-selector (gen-server
                        (reify Server
                          (init [_] (initialise aname @self))
                          (terminate [_ cause] #_(info (str aname " terminated.")))
                          (handle-cast [_ from id message] (msg-handler from message)))))