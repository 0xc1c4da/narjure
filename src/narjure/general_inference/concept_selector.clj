(ns narjure.general-inference.concept-selector
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.memory-management.concept-manager :refer [c-bag]]
    [narjure.bag :as b]
    [clojure.math.numeric-tower :as math]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all]
    [narjure.control-utils :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :concept-selector)
(def inference-pairs 200)

(def display (atom '()))
(def search (atom ""))

(defn inference-tick-handler
  "Select n concepts for inference and post
   inference-request-message to each selected
   concept"
  [from [msg]]
  ;todo
  ; (dotimes [n (min (b/count-elements @c-bag) 1)]
  ;one concept for inference is enough for now ^^
  (try (doseq [_ (range selection-parameter)]
     (when (> (b/count-elements @c-bag) 0)
       (let [selected (first (b/get-by-index @c-bag (selection-fn @c-bag)))
             ref (:ref selected)]
         (cast! ref [:inference-request-msg (:id selected)])
         ;(info (str "Concept selected: " [:id (:id selected) :priority (:priority selected)]))
         (debuglogger search display (str "Concept selected: " [:id (:id selected) :priority (:priority selected)])))))
       (catch Exception e (debuglogger search display (str "concept select error " (.toString e))))))

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
  (set-state! {:state 0}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  ;(debuglogger display message) since tick is uninteresting we use what is selected
  (case type
    :inference-tick-msg (inference-tick-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn concept-selector []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))
