(ns narjure.general-inference.general-inferencer
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [nal.deriver :refer [inference]]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :general-inferencer)
(def display (atom '()))

(defn non-overlapping-evidence? [e1 e2]
  #_(info (str "evidence " (list e1) (list e2)))
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
            task-creator (whereis :task-creator)]
        (doseq [der derived]
          (cast! task-creator [:derived-sentence-msg der [0.5 0.5 0.0] evidence]))))
    (catch Exception e (debuglogger display (str "inference error " (.toString e))))))

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
  (debuglogger display message)
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
