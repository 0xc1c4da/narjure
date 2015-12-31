(ns nal.core
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run*]]))


(deftest test-revision
  (is (= [nil [0.8 0.8333333333333334]]
         (first
           (run 1 [R] (revision [nil [1, 0.8]] [nil [0, 0.5]] R))))))

(deftest test-choice
  (is (= [nil [1 0.8]]
         (first
           (run 1 [R] (choice [nil [1, 0.8]], [nil [0, 0.5]], R)))))
  (is (= [nil [0.8, 0.9]]
         (first
           (run 1 [R] (choice [nil [1, 0.5]], [nil [0.8, 0.9]], R))))))
