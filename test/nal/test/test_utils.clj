(ns nal.test.test-utils
  (:require [clojure.test :refer [is are]]
            [clojure.core.logic :refer [run run*]]))

(defmacro trun [result lvars & body]
  `(is (= ~result (run 1 ~lvars ~@body))))

(defmacro trun* [result lvars & body]
  `(is (= ~result (run* ~lvars ~@body))))

(defmacro both-equal [& body]
  `(are [arg1# arg2#] (= arg1# arg2#) ~@body))
