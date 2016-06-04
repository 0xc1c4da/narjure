(ns narjure.perception-action.derived-load-reducer
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self whereis shutdown! unregister! set-state! state]]
    [narjure.narsese :refer [parse2]]
    ;[narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]
    [narjure.bag :as b]
    [narjure.control-utils :refer :all]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :derived-load-reducer)
(def display (atom '()))
(def search (atom ""))

(def max-derived-sentences 200)
(def max-selections 50)
(def bag (atom (b/default-bag max-derived-sentences)))

(defn system-time-tick-handler
  "select n sentences from input bag and post to :task-creator"
  []
  (doseq [n (range (min max-selections (b/count-elements @bag)))]
    ;(info (str "selecting: " (min max-selections (b/count-elements @bag))))
    (let [[element bag'] (b/get-by-index @bag (selection-fn @bag))]
      (reset! bag bag')
      (cast! (whereis :task-creator) [:derived-sentence-msg (:sentence element )])
      ;(info (str "selected:" (:sentence element)))
      )))

(defn derived-sentence-handler
  "adds sentence to input-bag and selects n senetences on system-time-tick"
  [from [msg sentence budget evidence]]
  ;(info (str "adding: " (b/add-element @bag {:priority (first budget) :sentence [sentence budget evidence]})))
  (swap! bag b/add-element {:id sentence :priority (first budget) :sentence [sentence budget evidence]}))

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
  (set-state! {}))

(def display (atom '()))
(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger search display message)
  (case type
    :derived-sentence-msg (derived-sentence-handler from message)
    :system-time-tick-msg (system-time-tick-handler)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn derived-load-reducer []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))