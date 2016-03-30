(ns narjure.general-inference.general-inferencer
  (:require
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug]])
  (:refer-clojure :exclude [promise await]))

(declare general-inferencer do-inference)

(defactor general-inferencer
  "State is inference rule trie or equivalent."
  {:trie 0}
  {:do-inference-msg do-inference})

(defn do-inference [_ _]
  (debug :general-inferencer "process-do-inference"))


