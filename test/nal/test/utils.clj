(ns nal.test.utils
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.utils :refer :all]
            [clojure.core.logic :refer [run run*]]))

(deftest test-u-not
  (is (= '(-2) (run* [q] (u-not 3 q)))))

(deftest test-u-and
  (is (= '(6) (run* [q] (u-and [1 2 3] q))))
  (is (= '(1) (run* [q] (u-and [1] q)))))

(deftest test-u-or
  (is (= '(1) (run* [q] (u-or [7 1] q)))))

(deftest test-u-w2c
  (is (= '(3/4) (run* [q] (u-w2c 3 q)))))

(deftest test-subtract
  (is (= '([1 2 4]) (run* [q] (subtract [1 1 2 3 4] [1 3 5] q))))
  (is (= '([]) (run* [q] (subtract [] [] q)))))
