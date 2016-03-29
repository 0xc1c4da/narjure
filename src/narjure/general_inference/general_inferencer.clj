(ns narjure.general_inference.general-inferencer
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [register! set-state! self]]]
    [narjure.actor.utils :refer [actor-loop]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare general-inferencer do-inference)

(def aname :general-inferencer)

(defsfn general-inferencer
  "state is inference rule trie or equivalent"
  []
  (register! aname @self)
  (set-state! {:trie 0})
  (actor-loop aname do-inference))

(defn do-inference [_ _]
  (debug aname "process-do-inference"))


