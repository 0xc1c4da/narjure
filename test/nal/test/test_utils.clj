(ns nal.test.test-utils
  (:require [clojure.test :refer [is]]
            [clojure.core.logic :refer [run run*]]))

(defmacro trun [result lvars & body]
  `(is (= ~result (run 1 ~lvars ~@body))))

(defmacro trun* [result lvars & body]
  `(is (= ~result (run* ~lvars ~@body))))
