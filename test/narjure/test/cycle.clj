(ns narjure.test.cycle
  (:require [clojure.test :refer :all]
            [narjure.cycle :refer :all]))

(def b1 {:truth          [1.0 0.9]
         :evidental-base #{1}
         :statement      '[inheritance bird swimmer]})
(def b2 {:truth          [0.1 0.6]
         :evidental-base #{2}
         :statement      '[inheritance bird swimmer]})

(deftest test-local-inference
  (is (= [0.8714285714285714 0.9130434782608696]
         (:truth (local-inference b1 b2))))
  (is (= [1.0 0.9]
         (:truth (local-inference b1 (assoc b2 :evidental-base #{1 2}))))))
