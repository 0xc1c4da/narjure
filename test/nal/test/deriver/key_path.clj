(ns nal.test.deriver.key-path
  (:require [clojure.test :refer :all]
            [nal.deriver.key-path :refer :all]))

(deftest test-path
  (is (= '(--> :any :any) (path '(--> A B))))
  (is (= '(--> (- :any :any) :any) (path '(--> (- B G) S))))
  (is (= '(==> (--> :any :any) (--> :any :any))
         (path '(==> (--> $X S) (--> $X P))))))

(deftest test-rule-path
  (is (= [:any :and :any] (rule-path 'A 'B)))
  (is (= '((--> :any :any) :and (--> :any :any))
         (rule-path '(--> A B) '(--> B C)))))

(deftest test-cart
  (is (= '((1 2 3) (1 2 4) (2 2 3) (2 2 4)) (cart [[1 2] [2] [3 4]]))))

(deftest test-path-invariants
  (is (= '((==> (--> :any :any) (--> :any :any))
            (==> (--> :any :any) :any)
            (==> :any (--> :any :any))
            (==> :any :any)
            :any)
         (path-invariants '(==> (--> :any :any) (--> :any :any))))))

(deftest test-all-paths
  (is
    (= '([(==> (--> :any :any) (--> :any :any)) :and (--> (- :any :any) :any)]
          [(==> (--> :any :any) (--> :any :any)) :and (--> :any :any)]
          [(==> (--> :any :any) (--> :any :any)) :and :any]
          [(==> (--> :any :any) :any) :and (--> (- :any :any) :any)]
          [(==> (--> :any :any) :any) :and (--> :any :any)]
          [(==> (--> :any :any) :any) :and :any]
          [(==> :any (--> :any :any)) :and (--> (- :any :any) :any)]
          [(==> :any (--> :any :any)) :and (--> :any :any)]
          [(==> :any (--> :any :any)) :and :any]
          [(==> :any :any) :and (--> (- :any :any) :any)]
          [(==> :any :any) :and (--> :any :any)]
          [(==> :any :any) :and :any]
          [:any :and (--> (- :any :any) :any)]
          [:any :and (--> :any :any)]
          [:any :and :any])
       (all-paths
         '(==> (--> :any :any) (--> :any :any))
         '(--> (- :any :any) :any)))))
