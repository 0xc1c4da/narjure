(ns nal.test.deriver.normalization
  (:require [clojure.test :refer :all]
            [nal.deriver.normalization :refer :all]))

(deftest test-infix->prefix
  (is (= '(--> A B) (infix->prefix '(A --> B))))
  (is (= '(--> (- B G) S) (infix->prefix '((B - G) --> S))))
  (is (= '(==> (--> $X S) (--> $X P))
         (infix->prefix '(($X --> S) ==> ($X --> P))))))

(deftest test-neg-symbol?
  (is (true? (#'nal.deriver.normalization/neg-symbol? '--)))
  (is (true? (#'nal.deriver.normalization/neg-symbol? '--A)))
  (is (false? (#'nal.deriver.normalization/neg-symbol? '-->)))
  (is (false? (#'nal.deriver.normalization/neg-symbol? 'B))))

(deftest test-trim-negation
  (is (= 'A (#'nal.deriver.normalization/trim-negation '--A))))

(deftest test-neg-el
  (is (= '(-- A) (neg 'A))))

(deftest test-replace-negation
  (is (= '(-- A) (replace-negation '--A)))
  (is (= '(A (-- (--> A B))) (replace-negation '(A -- (--> A B)))))
  (is (= '(--> A B) (replace-negation '(--> A B))))
  (is (= '((-- A)) (replace-negation '[-- A])))
  (is (= '(-- A) (replace-negation '(-- A)))))
