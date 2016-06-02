(ns narjure.global-atoms
  (:require
    [narjure.bag :as b]))

(def max-concepts 1000) ;do not make too small (less than 500) as causes cyclic issue between task-dispatcher and concept-manager
(def c-bag (atom (b/default-bag max-concepts)))

(def max-events 10)
(def e-bag (atom (b/default-bag max-events)))