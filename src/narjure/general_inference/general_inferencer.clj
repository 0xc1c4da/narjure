(ns narjure.general-inference.general-inferencer
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :general-inferencer)

(defn do-inference-handler
  "Processes :do-inference-msg:
    generated derived results, budget and occurrence time for derived tasks.
    Posts derived sentences to task creator"
  [from [msg task belief]]
  ()
  (debug aname "process-do-inference-msg"))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ _ :as message]]
  (case type
    :do-inference-msg (do-inference-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def general-inferencer (gen-server
                                    (reify Server
                                      (init [_] (register! aname @self))
                                      (terminate [_ cause] (info (str aname " terminated.")))
                                      (handle-cast [_ from id message] (msg-handler from message)))))
