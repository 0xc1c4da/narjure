(ns nal.test.deriver.truth
  (:require [nal.deriver.truth :refer :all]
            [clojure.test :refer :all]
            [nal.test.test-utils :refer [both-equal]]))

(deftest test-t-and
  (both-equal
    0.7 (t-and 1 0.7)
    0.8 (t-and 1 1 0.8)
    0.9 (t-and 1 1 1 0.9)))

(deftest test-t-or
  (both-equal
     0.94 (t-or 0.8 0.7)
     1.0 (t-or 1 0.7)
     1.0 (t-or 0.7 1)))

(deftest test-w2c
  (both-equal
     0.4117647058823529 (w2c 0.7)
     0.16666666666666669 (w2c 0.2)))

(deftest test-c2w
  (both-equal
     0.7241379310344827 (c2w 0.42)
     0.7241379310344827 (c2w 0.42)
     0.20048019207683077 (c2w 0.167)))

(deftest test-conversion
  (both-equal
     [1 0.4736842105263158] (conversion [1 0.9] [1 0.9])
     [1 0.4736842105263158] (conversion [0 0.9] [1 0.9])
     [1 0.3333333333333333] (conversion [1 0.9] [0.6 0.5])))

(deftest test-negation
  (both-equal
     [0.0 0.9] (negation [1 0.9] [1 0.9])
     [0.0 0.9] (negation [1 0.9] [0.6 0.9])
     [0.6 0.6] (negation [0.4 0.6] [0.6 0.9])))

(deftest test-contraposition
  (both-equal
     [0 0.4736842105263158] (contraposition [1 0.9] [0 0])
     [0 0.4736842105263158] (contraposition [0 0.9] [0 0])
     [0 0.37499999999999994] (contraposition [0 0.6] [0 0])))

(deftest test-revision
  (both-equal
     [1.0 0.9473684210526316] (revision [1 0.9] [1 0.9])
     [0.95 0.9090909090909091] (revision [1 0.9] [0.5 0.5])
     [0.95 0.9090909090909091] (revision [0.5 0.5] [1 0.9])))

(deftest tets-deduction
  (both-equal
     [1.0 0.81] (deduction [1 0.9] [1 0.9])
     [1.0 0.45] (deduction [1 0.9] [1 0.5])
     [1.0 0.45] (deduction [1 0.5] [1 0.9])))

(deftest test-analogy
  (both-equal
     [1.0 0.81] (analogy [1 0.9] [1 0.9])
     [1.0 0.27] (analogy [1 0.9] [1 0.3])
     [1.0 0.27] (analogy [1 0.3] [1 0.9])
     [0.54 0.24300000000000002] (analogy [0.6 0.3] [0.9 0.9])))

(deftest test-resemblance
  (both-equal
     [1.0 0.81] (resemblance [1 0.9] [1 0.9])
     [1.0 0.45] (resemblance [1 0.9] [1 0.5])
     [1.0 0.45] (resemblance [1 0.5] [1 0.9])
     [0.29700000000000004 0.5224799999999999] (resemblance [0.9 0.8] [0.33 0.7])))

(deftest test-abduction
  (both-equal
     [1 0.44751381215469616] (abduction [1 0.9] [1 0.9])
     [1 0.35064935064935066] (abduction [1 0.9] [1 0.6])
     [1 0.35064935064935066] (abduction [1 0.6] [1 0.9])
     [0.67 0.10714285714285712] (abduction [0.67 0.6] [1 0.2])))

(deftest test-induction
  (both-equal
     [1 0.44751381215469616] (induction [1 0.9] [1 0.9])
     [1 0.3103448275862069] (induction [1 0.9] [1 0.5])
     [1 0.3103448275862069] (induction [1 0.9] [1 0.5])
     [0.9 0.12280701754385964] (induction [0.4 0.7] [0.9 0.5])))

(deftest test-exemplification
  (both-equal
     [1 0.44751381215469616] (exemplification [1 0.9] [1 0.9])
     [1 0.3865030674846626] (exemplification [1 0.9] [1 0.7])
     [1 0.3865030674846626] (exemplification [1 0.7] [1 0.9])
     [1 0.04928506236689991] (exemplification [0.8 0.2] [0.6 0.54])))

(deftest test-comparison
  (both-equal
     [1.0 0.44751381215469616] (comparison [1 0.9] [1 0.9])
     [1.0 0.2647058823529412] (comparison [1 0.9] [1 0.4])
     [1.0 0.2647058823529412] (comparison [1 0.4] [1 0.9])
     [0.7346938775510206 0.07270029673590506] (comparison [0.8 0.2] [0.9 0.4])))

(deftest test-union
  (both-equal
     [1.0 0.81] (union [1 0.9] [1 0.9])
     [1.0 0.18000000000000002] (union [1 0.9] [1 0.2])
     [1.0 0.18000000000000002] (union [1 0.2] [1 0.9])
     [0.94 0.08000000000000002] (union [0.7 0.4] [0.8 0.2])))

(deftest test-intersection
  (both-equal
     [1.0 0.81] (intersection [1 0.9] [1 0.9])
     [1.0 0.36000000000000004] (intersection [1 0.9] [1 0.4])
     [1.0 0.36000000000000004] (intersection [1 0.4] [1 0.9])
     [0.522 0.18000000000000002] (intersection [0.87 0.9] [0.6 0.2])))

(deftest test-anonymous-analogy
  (both-equal
     [1.0 0.42631578947368426] (anonymous-analogy [1 0.9] [1 0.9])
     [1.0 0.14210526315789473] (anonymous-analogy [1 0.9] [1 0.3])
     [0.16000000000000003 0.04153846153846154] (anonymous-analogy [0.2 0.3] [0.8 0.9])))

(deftest test-decompose-pnn
  (both-equal
     [1.0 0.0] (decompose-pnn [1 0.9] [1 0.9])
     [0.9 0.018] (decompose-pnn [1 0.9] [0.9 0.2])
     [0.9 0.018] (decompose-pnn [1 0.2] [0.9 0.9])
     [0.97 0.0053999999999999986] (decompose-pnn [0.3 0.2] [0.9 0.9])))

(deftest test-decompose-npp
  (both-equal
     [0.0 0.0] (decompose-npp [1 0.9] [1 0.9])
     [0.63 0.1134] (decompose-npp [0.3 0.2] [0.9 0.9])
     [0.029999999999999992 0.0053999999999999986] (decompose-npp [0.9 0.9] [0.3 0.2])))

(deftest test-decompose-pnp
  (both-equal
     [0.0 0.0] (decompose-pnp [1 0.9] [1 0.9])
     [0.07999999999999999 0.0144] (decompose-pnp [0.4 0.9] [0.8 0.2])
     [0.48 0.0864] (decompose-pnp [0.8 0.2] [0.4 0.9])))

(deftest test-decompose-ppp
  (both-equal
     [1.0 0.81] (decompose-ppp [1 0.9] [1 0.9])
     [1.0 0.54] (decompose-ppp [1 0.9] [1 0.6])
     [1.0 0.54] (decompose-ppp [1 0.6] [1 0.9])
     [0.44 0.2376] (decompose-ppp [0.5 0.9] [0.88 0.6])))

(deftest test-decompose-nnn
  (both-equal
     [1.0 0.0] (decompose-nnn [1 0.9] [1 0.9])
     [0.99 0.008099999999999996] (decompose-nnn [0.9 0.9] [0.9 0.9])
     [0.52 0.3888] (decompose-nnn [0.2 0.9] [0.4 0.9])
     [0.52 0.3888] (decompose-nnn [0.4 0.9] [0.2 0.9])))

(deftest test-difference
  (both-equal
     [0.0 0.81] (difference [1 0.9] [1 0.9])
     [0.030000000000000006 0.552] (difference [0.1 0.92] [0.7 0.6])
     [0.63 0.552] (difference [0.7 0.6] [0.1 0.92])))

(deftest test-structual-intersection
  (both-equal
     [1.0 0.81] (structual-intersection [1 0.9] [1 0.9])
     [1.0 0.27] (structual-intersection [1 0.9] [1 0.3])
     [1.0 0.81] (structual-intersection [1 0.3] [1 0.9])))

(deftest test-structual-deduction
  (both-equal
     [1.0 0.81] (structual-deduction [1 0.9] [1 0.9])
     [0.4 0.03600000000000001] (structual-deduction [0.4 0.1] [1 0.9])
     [1.0 0.54] (structual-deduction [1 0.6] [0.4 0.1])))

(deftest test-structual-abduction
  (both-equal
     [1 0.44751381215469616] (structual-abduction [1 0.9] [1 0.9])
     [0.7 0.15254237288135597] (structual-abduction [0.7 0.2] [1 0.9])
     [1 0.44751381215469616] (structual-abduction [1 0.9] [0.7 0.2])))

(deftest test-reduce-conjunction
  (both-equal
     [1.0 0.0] (reduce-conjunction [1 0.9] [1 0.9])
     [0.7989999999999999 0.16281000000000004] (reduce-conjunction [0.7 0.9] [0.67 0.9])
     [0.769 0.18710999999999997] (reduce-conjunction [0.67 0.9] [0.7 0.9])))

(deftest test-t-identity
  (both-equal
     [1 0.6] (t-identity [1 0.6] [1 0.7])
     [0.3 0.6] (t-identity [0.3 0.6] [0.1 0.8])
     [0.3 0.6] (t-identity [0.3 0.6] nil)))

(deftest test-belief-identity
  (both-equal
     [1 0.6] (belief-identity [1 0.6] [1 0.7])
     [0.3 0.6] (belief-identity [0.3 0.6] [0.1 0.8]))
  (is (nil? (belief-identity [0.3 0.6] nil))))

(deftest test-belief-structural-deduction
  (both-equal
     [1.0 0.81] (belief-structural-deduction [1 0.9] [1 0.9])
     [0.3 0.24300000000000002] (belief-structural-deduction [1 0.6] [0.3 0.9])
     [0.6 0.12420000000000002] (belief-structural-deduction [0.9 0.67] [0.6 0.23])))

(deftest test-belief-structural-difference
  (is (nil? (belief-structural-difference [1 0.9] nil)))
  (both-equal
    [0.5 0.135] (belief-structural-difference [0.9 0.9] [0.5 0.3])
    [0.09999999999999998 0.7290000000000001] (belief-structural-difference [0.5 0.3] [0.9 0.9])))

(deftest test-belief-negation
  (both-equal
     [0.0 0.9] (belief-negation [1 0.9] [1 0.9])
     [0.8 0.1] (belief-negation [0.3 0] [0.2 0.1])
     [0.7 0.9] (belief-negation [0.2 0.1] [0.3 0.9])))
