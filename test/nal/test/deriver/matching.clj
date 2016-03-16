(ns nal.test.deriver.matching
  (:require [clojure.test :refer :all]
            [nal.deriver.matching :refer :all]))

(deftest test-quote-operators
  (are [a1 a2] (= a1 (quote-operators a2))
    `(let
        [~'G__57753
         (nal.deriver.preconditions/abs (- :t-occurrence :b-occurrence))]
        [:interval ~'G__57753])
    `(let
       [~'G__57753
        (nal.deriver.preconditions/abs (- :t-occurrence :b-occurrence))]
       [:interval ~'G__57753])

    `(let
       [~'G__57753
        (nal.deriver.preconditions/abs (- :t-occurrence :b-occurrence))]
       [(quote ~'ext-set) ~'G__57753])
    `(let
       [~'G__57753
        (nal.deriver.preconditions/abs (- :t-occurrence :b-occurrence))]
       [~'ext-set ~'G__57753])

    '[(quote ext-set) x1 x2]
    '(ext-set x1 x2)))
