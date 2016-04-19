(ns narjure.test.concept-creator
  (:require
    [clojure.test :refer :all]
    [co.paralleluniverse.pulsar.actors :refer [! spawn gen-server register! cast! Server self shutdown! unregister! set-state! state whereis]]
    [narjure.memory-management.concept :refer [concept]]
    [narjure.actor.utils :refer [defactor]]
    [taoensso.timbre :refer [debug info]]))

;;{term actor-ref}
(def c-map (atom {}))

(deftest concept-dictionary
  (is (= 0 (count @c-map)))
  (is (= {0 12345} (swap! c-map assoc 0 12345)))
  (is (= 1 (count @c-map)))
  (swap! c-map {})
  (is (= {:0 12345} (swap! c-map assoc :0 12345)))
  (is (= 1 (count @c-map)))
  (swap! c-map {})
  (dotimes [n 10]
    (swap! c-map assoc (keyword (str n)) n))
  (is (= 10 (count @c-map)))
  (is (= true (contains? @c-map (keyword (str 5)))))
  )
