(ns narjure.actor.general-inferencer-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-do-inference)
(declare general-inferencer-actor)

(defsfn general-inferencer-actor
        "state is inference rule trie or equivalent"
        []
        (register! :general-inferencer @self)
        (set-state! {:trie 0})
        (loop []
          (receive [msg]
                   [:do-inference-msg inference-package] (set-state! (process-do-inference inference-package @state))
                   :else (! :logger [:log-msg :log-debug :general-inferencer (str "unhandled msg:" msg)]))
          (recur)))

(defn process-do-inference [_ _]
  (! :logger [:log-msg :log-debug :general-inferencer "process-do-inference"]))
