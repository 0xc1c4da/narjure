(ns narjure.perception-action.sentence-parser
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self whereis]]
    [narjure.narsese :refer [parse]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :sentence-parser)

(defn narsese-string-handler
  "Parses a narsese string and posts a :sentence-msg to task-creator"
  [from [msg string]]
  (let [sentence (parse string)]
    (info (str sentence))
    (cast! (whereis :task-creator)  [:sentence-msg sentence])))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type _ :as message]]
  (case type
    ::narsese-string-msg (narsese-string-handler from message)
    :default (debug aname (str "unhandled msg: " type))))

(def sentence-parser (gen-server
                       (reify Server
                         (init [_] (register! aname @self))
                         (terminate [_ cause] (info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))