(ns nal.test.core
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]))

(deftest test-inference
  (are [a1 a2] (= (set a1) (set (apply inference a2)))

    '([[==> [&| [--> [ext-set tim] [int-set driving]]] [--> [ext-set tim] [int-set dead]]] [1.0 0.81]])

    ['[[--> [ext-set tim] [int-set drunk]] [1 0.9]]
     '[[==> [&| [--> [ind-var X] [int-set drunk]] [--> [ind-var X] [int-set driving]]] [--> [ind-var X] [int-set dead]]] [1 0.9]]]


    '([[--> a1 [ext-image m _ a2 a3]] [1 0.9]])
    '[[[--> [* a1 a2 a3] m] [1 0.9]] [a1 [1 0.9]]]

    []
    '[[a1 [1 0.9]] [[--> [* a1 a2 a3] m] [1 0.9]]]

    '[[a1 [1 0.44751381215469616]]]
    '[[[conj a1 a2 a3] [1 0.9]] [a1 [1 0.9]]]

    []
    '[[[==> [--> M S] [--> M P]] [1 0.9]] [[--> M S] [1 0.9]]]

    '([[==>
        [conj [--> [ext-set A] [int-set Y]] [--> [ind-var X] [int-set B]]]
        [--> [ind-var X] P]]
       [1 0.44751381215469616]]
       [[conj
         [--> [dep-var Y] [int-set B]]
         [==> [--> [ext-set A] [int-set Y]] [--> [dep-var Y] P]]]
        [1.0 0.81]])
    ['[[==> [--> [ext-set A] [int-set Y]] [--> [ext-set A] P]] [1 0.9]]
     '[[--> [ext-set A] [int-set B]] [1 0.9]]]))
