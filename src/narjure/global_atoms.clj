(ns narjure.global-atoms
  (:require
    [narjure.bag :as b]))

(def max-concepts 1000000) ;do not make too small (less than 50) as causes cyclic issue between task-dispatcher and concept-manager
(def c-bag (atom (b/default-bag max-concepts)))

(def max-events 10)
(def e-bag (atom (b/default-bag max-events)))

(def nars-time (atom 0))

(def nars-id (atom -1))

(def output-display (atom '()))
(def output-search (atom ""))
