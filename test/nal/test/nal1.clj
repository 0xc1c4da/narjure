(ns nal.test.nal1
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run*]]
            [nal.test.test-utils :refer [trun*]]))

(deftest test-revision
  (trun* [[nil [0.8 0.8333333333333334]]]
        [R] (revision [nil [1 0.8]] [nil [0 0.5]] R)))

(deftest test-choice
  (trun* [[nil [1 0.8]]]
        [R] (choice [nil [1 0.8]] [nil [0 0.5]] R))
  (trun* [[nil [0.8 0.9]]]
        [R] (choice [nil [1 0.5]] [nil [0.8 0.9]] R)))

(deftest test-inference-nal1
  ;deduction
  (trun* [[1 0.81]]
        [q1] (inference ['(inheritance bird animal) [1 0.9]]
                        ['(inheritance robin bird) [1 0.9]]
                        ['(inheritance robin animal) q1]))
  ;induction
  (trun* [[1 0.44751381215469616]]
        [q1] (inference ['(inheritance robin animal) [1 0.9]]
                        ['(inheritance robin bird) [1 0.9]]
                        ['(inheritance bird animal) q1]))
  ;abduction
  (trun* [[1 0.44751381215469616]]
        [q1] (inference ['(inheritance bird animal) [1 0.9]]
                        ['(inheritance robin animal) [1 0.9]]
                        ['(inheritance robin bird) q1]))
  ;examplification
  (trun* [[1 0.44751381215469616]]
        [q1] (inference ['(inheritance robin bird) [1 0.9]]
                        ['(inheritance bird animal) [1 0.9]]
                        ['(inheritance animal robin) q1]))
  ;convension
  (trun*
    [[1 0.4186046511627907]]
    [q]
    (inference '((inheritance swan bird) [0.9 0.8]) 
               ['(inheritance bird swan) q])))
