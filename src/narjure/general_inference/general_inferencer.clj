(ns narjure.general-inference.general-inferencer
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [nal.deriver :refer [inference]]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all]
    [nal.term_utils :refer [syntactic-complexity]]
    [nal.deriver.truth :refer [expectation]])
  (:refer-clojure :exclude [promise await]))

(def aname :general-inferencer)
(def display (atom '()))
(def search (atom ""))

(defn non-overlapping-evidence? [e1 e2]
  (empty? (clojure.set/intersection (set e1) (set e2))))

(def max-evidence 10)

(defn make-evidence [e1 e2]
  (take max-evidence (interleave e1 e2)))

(defn do-inference-handler
  "Processes :do-inference-msg:
    generated derived results, budget and occurrence time for derived tasks.
    Posts derived sentences to task creator"
  [from [msg [task belief]]]
  (try
    (when (non-overlapping-evidence? (:evidence task) (:evidence belief))
      (let [derived (inference task belief)
            evidence (make-evidence (:evidence task) (:evidence belief))
            derived-load-reducer (whereis :derived-load-reducer)]
        (doseq [der derived]
          #_(cast! derived-load-reducer [:derived-sentence-msg der [(* (if (= nil (:truth task))
                                                                       1.0
                                                                       (expectation (:truth task)))
                                                                     (/ 1.0 (syntactic-complexity (:statement task)))) 0.8 0.0] evidence]))))
    (catch Exception e (debuglogger search display (str "inference error " (.toString e))))))

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
  (set-state! {:state 0}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger search display message)
  (case type
    :do-inference-msg (do-inference-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(defn general-inferencer []
  (gen-server
    (reify Server
      (init [_] (initialise aname @self))
      (terminate [_ cause] #_(info (str aname " terminated.")))
      (handle-cast [_ from id message] (msg-handler from message)))))
