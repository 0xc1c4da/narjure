(ns narjure.test.bag
  (:require [clojure.test :refer :all]
            [narjure.bag :refer :all]))

(defn abc-bag
  ([] (abc-bag 3))
  ([cap] (-> (default-bag cap)
             (put-el {:key :a :priority 0.7})
             (put-el {:key :b :priority 0.7})
             (put-el {:key :c :priority 0.7}))))

(def a-bag (put-el (default-bag) {:key :a :priority 0.7}))

(deftest test-bag
  (is (= 3 (count-els (abc-bag))))
  (is (= 2 (count (-> (default-bag)
                      (put-el {:key :a :priority 0.7})
                      (put-el {:key :b :priority 0.7})
                      (put-el {:key :a :priority 0.7})
                      :queue))))
  (is (= 2 (count-els (abc-bag 2))))
  (is (nil? (get-el a-bag :b))))
