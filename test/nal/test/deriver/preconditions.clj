(ns nal.test.deriver.preconditions
  (:require [clojure.test :refer :all]
            [nal.deriver.preconditions :refer :all]))

(deftest test-compount-precondition
  (are [c1 c2] (= c1 (compound-precondition c2))
    `[(and
        (coll? ~'A)
        (= ~'ext-set (first ~'A)))]
    '(:set-ext? A)

    `[(and
        (coll? ~'A)
        (= ~'int-set (first ~'A)))]
    '(:set-int? A)

    '[(clojure.core/not= A B)]
    '(:!= A B)

    '[(nal.deriver.substitution/munification-map "$" A B)]
    '(:substitute-if-unifies "$" A B)

    `[(if (coll? ~'A)
        (nil?
          (nal.deriver.preconditions/implications-and-equivalences
            (first ~'A)))
        true)]
    '(:not-implication-or-equivalence A)))

(deftest test-sets-preconditions
  (are [c1 c2] (= c1 (count (compound-precondition c2)))
    4 '(:difference A B)
    3 '(:union A B)
    4 '(:intersection A B)))
