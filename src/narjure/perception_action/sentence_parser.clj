(ns narjure.perception-action.sentence-parser
  (:require
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self whereis shutdown! unregister! set-state! state]]
    [narjure.narsese :refer [parse2]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]])
  (:refer-clojure :exclude [promise await]))

(def aname :sentence-parser)

(defn narsese-string-handler
  "Parses a narsese string and posts a :sentence-msg to task-creator"
  [from [msg string]]
  (let [sentence (parse2 string)]
    #_(info (str sentence))
    (cast! (:task-creator @state)  [:sentence-msg sentence])))

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
  (set-state! {:task-creator (whereis :task-creator)}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (case type
    :narsese-string-msg (narsese-string-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def sentence-parser (gen-server
                       (reify Server
                         (init [_] (initialise aname @self))
                         (terminate [_ cause] #_(info (str aname " terminated.")))
                         (handle-cast [_ from id message] (msg-handler from message)))))