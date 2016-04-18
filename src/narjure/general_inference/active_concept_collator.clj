(ns narjure.general-inference.active-concept-collator
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register!]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :active-concept-collator)

(defn active-concept [_ _]
  (debug aname "process-active-concept"))

(defn active-concept-handler
  "Processes :active-cocnept-msg:
    Add concept to concepts bags: general or temproal respectively"
  [from [msg concept]]
  ()
  (debug aname "process-active-concept-msg"))

(defn inference-tick-handler
  "Processes :inference-tick-msg:
    Executes cocnept selection process for general inference.
    Both temporal and general concept are selected"
  [from [msg]]
  ()
  (debug aname "process-inference-tick-msg"))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ _ :as message]]
  (case type
    :active-concept-msg (active-concept-handler from message)
    :inference-tick-msg (inference-tick-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def active-concept-collator (gen-server
                          (reify Server
                            (init [_] (register! aname @self))
                            (terminate [_ cause] (info (str aname " terminated.")))
                            (handle-cast [_ from id message] (msg-handler from message)))))
