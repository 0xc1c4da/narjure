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

(deftest test-reduce-ops
  (are [a1 a2] (= a1 (reduce-ops a2))
    1 '[ext-inter 1]
    '[ext-inter 1] '[ext-inter [ext-inter 1] [ext-inter 1]]
    '[ext-inter 2 1] '[ext-inter [ext-inter 1] [ext-inter 2]]
    '[ext-inter 2 1] '[ext-inter [ext-inter 1] 2]
    '[ext-inter 2 1] '[ext-inter 1 [ext-inter 2]]
    '[int-set 2 1] '[ext-inter [int-set 1] [int-set 2]]

    2 '[| 2]
    '[| 1] '[| [| 1] [| 1]]
    '[| 2 1] '[| [| 1] [| 2]]
    '[| 2 1] '[| [| 1] 2]
    '[| 2 1] '[| 1 [| 2]]
    '[int-set 3 2 1] '[| [int-set 1] [int-set 2 3]]
    '[int-set 2 4 1] '[| [int-set 1 4] [int-set 2]]

    '[ext-set 2 4 1] '[- [ext-set 5 6 2 4 1] [ext-set 5 6]]
    '[int-set 2 4 1] '[int-dif [int-set 5 6 2 4 1] [int-set 5 6]]

    '[<-> 1 2] '[<-> [ext-set 1] [ext-set 2]]
    '[<-> 1 2] '[<-> [int-set 1] [int-set 2]]

    '[* 1 2] '[* [* 1] 2]
    '[* 1 3 2] '[* [* 1 3] 2]
    '[* 1 3 2 4] '[* [* 1 3] 2 4]

    1 '[ext-image [* 1 2] 2]
    '[ext-image [* 1 2] 3] '[ext-image [* 1 2] 3]
    1 '[int-image [* 1 2] 2]
    '[int-image [* 1 2] 2 4] '[int-image [* 1 2] 2 4]

    1 '[-- [-- 1]]
    '[-- 1] '[-- 1]

    1 '[conj 1]
    1 '[conj 1 1]
    '[conj 3 2 4 1] '[conj [conj 2 4] [conj 1 3]]
    '[conj 3 2 4] '[conj [conj 2 4] 3]
    '[conj 3 2 4] '[conj 2 [conj 3 4]]

    1 '[|| 1]
    1 '[|| 1 1]
    '[|| 3 2 4 1] '[|| [|| 2 4] [|| 1 3]]
    '[|| 3 2 4] '[|| [|| 2 4] 3]
    '[|| 3 2 4] '[|| 2 [|| 3 4]]))
