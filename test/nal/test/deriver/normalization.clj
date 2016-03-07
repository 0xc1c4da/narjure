(ns nal.test.deriver.normalization
  (:require [clojure.test :refer :all]
            [nal.deriver.normalization :refer :all]
            [nal.test.test-utils :refer [both-equal]]))

(deftest test-infix->prefix
  (both-equal
     '(--> A B) (infix->prefix '(A --> B))
     '(--> (- B G) S) (infix->prefix '((B - G) --> S))
     '(==> (--> $X S) (--> $X P)) (infix->prefix '(($X --> S) ==> ($X --> P)))))

(deftest test-neg-symbol?
  (are [el] (true? el)
    (#'nal.deriver.normalization/neg-symbol? '--)
    (#'nal.deriver.normalization/neg-symbol? '--A))
  (are [el] (false? el)
    (#'nal.deriver.normalization/neg-symbol? '-->)
    (#'nal.deriver.normalization/neg-symbol? 'B)))

(deftest test-trim-negation
  (is (= 'A (#'nal.deriver.normalization/trim-negation '--A))))

(deftest test-neg-el
  (is (= '(-- A) (neg 'A))))

(deftest test-replace-negation
  (both-equal
     '(-- A) (replace-negation '--A)
     '(A (-- (--> A B))) (replace-negation '(A -- (--> A B)))
     '(--> A B) (replace-negation '(--> A B))
     '((-- A)) (replace-negation '[-- A])
     '(-- A) (replace-negation '(-- A))))
