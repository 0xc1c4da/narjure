(ns nal.test.test-utils
  (:require [clojure.test :refer [is are]]))

(defmacro both-equal [& body]
  `(are [arg1# arg2#] (= arg1# arg2#) ~@body))
