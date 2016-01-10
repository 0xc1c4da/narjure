(ns nal.test.args-processing
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.args-processing :refer :all]
            [clojure.core.logic :refer [run run* ==]]))

(deftest test-replace
  (is (= '([1 (nil 2 3 4)]
            [2 (1 nil 3 4)]
            [3 (1 2 nil 4)]
            [4 (1 2 3 nil)])
         (run* [q1 q2] (replaceo [1 2 3 4] q1 q2))))
  (is (= '([1 (_0 2 3) _0] [2 (1 _0 3) _0] [3 (1 2 _0) _0])
         (run* [q1 q2 q3] (replaceo [1 2 3] q1 q2 q3))))
  (is (= '((nil 2)) (run* [q] (replaceo [1 2] 1 q))))
  (is (= '((1 2 nil 4)) (run* [q] (replaceo [1 2 3 4] 3 q))))
  (is (= '(4) (run* [q] (replaceo [1 2] 1 [4 2] q))))
  (is (= '() (run* [q] (replaceo [1 2] 2 [4 2] q)))))

(deftest test-include1
  (is (= '(true) (run 1 [q] (== true q) (include1o [1 3] [1 2 3 4]))))
  (is (= '() (run 1 [q] (== true q) (include1o [1 3 5] [1 2 3 4])))))

(deftest test-include
  (is (= '(_0) (run* [q] (includeo [1 2 3] [1 2 3 4]))))
  (is (= '() (run* [q] (includeo [1 2 5] [1 2 3 4]))))
  (is (= '() (run* [q] (includeo [1 2 5] q)))))

(deftest test-same-set
  (is (= '((1 3 2) (2 1 3) (3 1 2) (2 3 1) (3 2 1))
         (run* [q] (same-seto [1 2 3] q)))))

(deftest test-same
  (is (= '((1 2 3) (1 3 2) (2 1 3) (3 1 2) (2 3 1) (3 2 1))
         (run* [q] (sameo [1 2 3] q)))))
