(ns nal.test.nal5
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run*]]
            [nal.test.test-utils :refer [trun trun*]]))

(deftest test-inference-nal5
  ;revision
  (trun*
    '([(implication (inheritance robin flyer) (inheritance robin bird))
       [0.8 0.8333333333333334]])
    [R]
    (revision ['(implication (inheritance robin, flyer), (inheritance robin, bird)), [1, 0.8]],
              ['(implication (inheritance robin, flyer), (inheritance robin, bird)), [0, 0.5]], R))
  (trun*
    '([(equivalence (inheritance robin flyer) inheritance (robin bird))
       [0.8 0.8333333333333334]])
    [R]
    (revision ['(equivalence (inheritance robin, flyer), inheritance (robin, bird)), [1, 0.8]],
              ['(equivalence (inheritance robin, flyer), inheritance (robin, bird)), [0, 0.5]], R))
  ;choice
  (trun*
    '([(implication (inheritance robin flyer) (inheritance robin bird)) [1 0.8]])
    [R]
    (choice ['(implication (inheritance robin, flyer), (inheritance robin, bird)), [1, 0.8]],
            ['(implication (inheritance robin, flyer), inheritance (robin, bird)), [0, 0.5]], R))
  (trun*
    '([(implication (inheritance robin flyer) (inheritance robin bird)) [0.8 0.9]])
    [R]
    (choice ['(implication (inheritance robin, flyer), (inheritance robin, bird)), [0.8, 0.9]],
            ['(implication (inheritance robin, swimmer), (inheritance robin, bird)), [1, 0.5]], R))
  ; deduction
  (trun* '([[implication (inheritance robin flyer) (inheritance robin animal)]
            [0.9 0.36000000000000004]])
         [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin flyer) (inheritance robin bird)) [1 0.5]] R))
  (trun* '([[equivalence (inheritance robin flyer) (inheritance robin animal)]
            [0.9 0.39999999999999997]])
         [R]
         (inference ['(equivalence (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(equivalence (inheritance robin flyer) (inheritance robin bird)) [1 0.5]] R))
  ;induction
  (trun* '([[implication (inheritance robin flyer) (inheritance robin animal)]
            [0.9 0.28571428571428575]]
            [[equivalence (inheritance robin flyer) (inheritance robin animal)]
             [0.9000000000000001 0.2857142857142857]]
            [[implication
              (inheritance robin bird)
              [conjunction [(inheritance robin animal) (inheritance robin flyer)]]]
             [0.9 0.4]]
            [[implication
              (inheritance robin bird)
              [disjunction [(inheritance robin animal) (inheritance robin flyer)]]]
             [0.9999999999999999 0.4]])
         [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin flyer)) [1 0.5]] R))
  ;abduction
  (trun* '([[implication (inheritance robin flyer) (inheritance robin bird)]
            [1 0.2647058823529412]]
            [[equivalence (inheritance robin flyer) (inheritance robin bird)]
             [0.9000000000000001 0.2857142857142857]]
            [[implication
              [disjunction [(inheritance robin bird) (inheritance robin flyer)]]
              (inheritance robin animal)]
             [0.9 0.4]]
            [[implication
              [conjunction [(inheritance robin bird) (inheritance robin flyer)]]
              (inheritance robin animal)]
             [0.9999999999999999 0.4]])
         [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin flyer) (inheritance robin animal)) [1 0.5]] R))
  ;examplification
  (trun* '([[implication (inheritance robin animal) (inheritance robin flyer)]
            [1 0.2647058823529412]])
         [R]
         (inference ['(implication (inheritance robin flyer) (inheritance robin bird)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) [1 0.5]] R))
  ;convension NAL-5
  (trun* '([[implication (inheritance robin animal) (inheritance robin flyer)]
            [1 0.4186046511627907]])
         [R]
         (inference ['(implication (inheritance robin flyer) (inheritance robin animal))
                     [0.9 0.8]] R))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(equivalence (inheritance robin flyer) (inheritance robin bird)) [0.9 0.8]]
                    ['(implication (inheritance robin flyer) (inheritance robin bird)) V]))
  (trun* '([[equivalence (inheritance robin flyer) (inheritance robin bird)]
            [0.81 0.6400000000000001]]) [R]
         (inference ['(implication (inheritance robin flyer) (inheritance robin bird)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin flyer)) [0.9 0.8]] R))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(similarity swan bird) [0.9 0.8]] ['(inheritance swan bird) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(inheritance swan bird) [0.9 0.8]] ['(similarity swan bird) V]))
  (trun* '([[similarity swan bird] [0.1 0.6400000000000001]]) [R]
         (inference ['(inheritance swan bird) [1 0.8]] ['(inheritance bird swan) [0.1 0.8]] R))
  ;comparison
  (trun*
    '([(inheritance robin flyer) (inheritance robin animal) [0.8181818181818182 0.3878550440744369]])
    [A B V]
    (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
               ['(implication (inheritance robin bird) (inheritance robin flyer)) [0.9 0.8]]
               [['equivalence A B] V]))
  (trun*
    '([(inheritance robin flyer) (inheritance robin bird) [0.8181818181818182 0.3878550440744369]])
    [A B V]
    (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
               ['(implication (inheritance robin flyer) (inheritance robin animal)) [0.9 0.8]] [['equivalence A B] V]))
  ; analogy
  (trun* '([[implication (inheritance robin bird) (inheritance robin flyer)] [0.81 0.5760000000000001]]) [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]] ['(equivalence (inheritance robin flyer) (inheritance robin animal)) [0.9 0.8]] R))
  (trun* '([[implication (inheritance robin flyer) (inheritance robin animal)] [0.81 0.5760000000000001]]) [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]] ['(equivalence (inheritance robin flyer) (inheritance robin bird)) [0.9 0.8]] R))
  ; compound construction two premises
  (trun* '([[implication (inheritance robin flyer) (inheritance robin animal)] [0.9 0.36548223350253817]]
            [[equivalence (inheritance robin flyer) (inheritance robin animal)] [0.8181818181818182 0.3878550440744369]]
            [[implication (inheritance robin bird) [conjunction [(inheritance robin animal) (inheritance robin flyer)]]] [0.81 0.6400000000000001]]
            [[implication (inheritance robin bird) [disjunction [(inheritance robin animal) (inheritance robin flyer)]]] [0.99 0.6400000000000001]])
         [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]] ['(implication (inheritance robin bird) (inheritance robin flyer)) [0.9 0.8]] R))
  (trun* '([[implication (inheritance robin flyer) (inheritance robin bird)] [0.9 0.36548223350253817]]
            [[equivalence (inheritance robin flyer) (inheritance robin bird)] [0.8181818181818182 0.3878550440744369]]
            [[implication [disjunction [(inheritance robin bird) (inheritance robin flyer)]] (inheritance robin animal)] [0.81 0.6400000000000001]]
            [[implication [conjunction [(inheritance robin bird) (inheritance robin flyer)]] (inheritance robin animal)] [0.99 0.6400000000000001]])
         [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin flyer) (inheritance robin animal)) [0.9 0.8]] R))
  (trun* '([0.81 0.81]) [V]
         (inference ['(inheritance robin animal) [0.9 0.9]] ['(inheritance robin flyer) [0.9 0.9]]
                    ['(conjunction [(inheritance robin animal) (inheritance robin flyer)]) V]))
  (trun* '([0.99 0.6400000000000001]) [V]
         (inference ['(inheritance robin animal) [0.9 0.8]] ['(inheritance robin flyer) [0.9 0.8]]
                    ['(disjunction [(inheritance robin animal) (inheritance robin flyer)]) V]))
  ; compound construction single premise
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (conjunction [(inheritance robin animal) (inheritance robin flyer)])) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (disjunction [(inheritance robin animal) (inheritance robin flyer)])) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (disjunction [(inheritance robin bird) (inheritance robin flyer)]) (inheritance robin animal)) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (conjunction [(inheritance robin bird) (inheritance robin flyer)]) (inheritance robin animal)) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(inheritance robin animal) [0.9 0.8]] ['(conjunction [(inheritance robin animal) (inheritance robin flyer)]) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(inheritance robin animal) [0.9 0.8]] ['(disjunction [(inheritance robin animal) (inheritance robin flyer)]) V]))
  ; compound destruction two premises
  (trun* '([0 0.6400000000000001]) [T]
         (inference ['(implication (inheritance robin bird) (inheritance robin flyer)) [1 0.8]]
                    ['(implication (inheritance robin bird) (conjunction [(inheritance robin animal) (inheritance robin flyer)])) [0 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) T]))
  (trun* '([1 0.6400000000000001]) [T]
         (inference ['(implication (inheritance robin bird) (inheritance robin flyer)) [0 0.8]]
                    ['(implication (inheritance robin bird) (disjunction [(inheritance robin animal) (inheritance robin flyer)])) [1 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) T]))
  (trun* '([0 0.6400000000000001]) [T]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [1 0.8]]
                    ['(implication (disjunction [(inheritance robin bird) (inheritance robin flyer)]) (inheritance robin animal)) [0 0.8]]
                    ['(implication (inheritance robin flyer) (inheritance robin animal)) T]))
  (trun* '([1 0.6400000000000001]) [T]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0 0.8]]
                    ['(implication (conjunction [(inheritance robin bird) (inheritance robin flyer)]) (inheritance robin animal)) [1 0.8]]
                    ['(implication (inheritance robin flyer) (inheritance robin animal)) T]))
  (trun* '([(inheritance robin flyer) [0 0.6400000000000001]])
         [R]
         (inference ['(inheritance robin bird) [1 0.8]]
                    ['(conjunction [(inheritance robin bird) (inheritance robin flyer)]) [0 0.8]] R))
  (trun* '([(inheritance robin flyer) [1 0.6400000000000001]])
         [R]
         (inference ['(inheritance robin bird) [0 0.8]]
                    ['(disjunction [(inheritance robin bird) (inheritance robin flyer)]) [1 0.8]] R))

  ; compound destruction single premise
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(implication (inheritance robin bird) (conjunction [(inheritance robin animal) (inheritance robin flyer)])) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(implication (inheritance robin bird) (disjunction [(inheritance robin animal) (inheritance robin flyer)])) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) V]))

  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(implication (disjunction [(inheritance robin bird) (inheritance robin flyer)]) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(implication (conjunction [(inheritance robin bird) (inheritance robin flyer)]) (inheritance robin animal)) [0.9 0.8]]
                    ['(implication (inheritance robin bird) (inheritance robin animal)) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(conjunction [(inheritance robin bird) (inheritance robin flyer)]) [0.9 0.8]] ['(inheritance robin bird) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(disjunction [(inheritance robin bird) (inheritance robin flyer)]) [0.9 0.8]] ['(inheritance robin bird) V]))

  ; operation on both sides of a relation
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(implication p q) [0.9 0.8]] ['(implication (conjunction [p r]) (conjunction [q r])) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(implication (conjunction [p r]) (conjunction [q r])) [0.9 0.8]] ['(implication p q) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(implication p q) [0.9 0.8]] ['(implication (disjunction [p r]) (disjunction [q r])) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(implication (disjunction [p r]) (disjunction [q r])) [0.9 0.8]] ['(implication p q) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(equivalence p q) [0.9 0.8]] ['(equivalence (conjunction [p r]) (conjunction [q r])) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(equivalence (conjunction [p r]) (conjunction [q r])) [0.9 0.8]] ['(equivalence p q) V]))
  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(equivalence p q) [0.9 0.8]] ['(equivalence (disjunction [p r]) (disjunction [q r])) V]))
  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(equivalence (disjunction [p r]) (disjunction [q r])) [0.9 0.8]] ['(equivalence p q) V]))
  ; negation
  (trun* '([(inheritance robin bird) [0.09999999999999998 0.8]]) [R]
         (inference ['(negation (inheritance robin bird)) [0.9 0.8]] R))
  (trun* '([0.8 0.8]) [T]
         (inference ['(inheritance robin bird) [0.2 0.8]]
                    ['(negation (inheritance robin bird)) T]))
  (trun* '([0 0.4186046511627907]) [T]
         (inference ['(implication (negation (inheritance penguin flyer)) (inheritance penguin swimmer)) [0.1 0.8]]
                    ['(implication (negation (inheritance penguin swimmer)) (inheritance penguin flyer)) T]))
  ; conditional inference
  (trun* '([(inheritance robin animal) [0.9 0.36000000000000004]]) [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(inheritance robin bird) [1 0.5]] R))

  (trun* '([(inheritance robin bird) [1 0.2647058823529412]]) [R]
         (inference ['(implication (inheritance robin bird) (inheritance robin animal)) [0.9 0.8]]
                    ['(inheritance robin animal) [1 0.5]] R))

  (trun* '([0.9 0.28571428571428575]
            [0.9 0.28571428571428575]) [V]
         (inference ['(inheritance robin animal) [0.9 0.8]]
                    ['(inheritance robin flyer) [1 0.5]]
                    ['(implication (inheritance robin flyer) (inheritance robin animal)) V]))

  (trun* '([(inheritance robin flyer) [0.9 0.36000000000000004]]) [R]
         (inference ['(inheritance robin animal) [1 0.5]]
                    ['(equivalence (inheritance robin flyer) (inheritance robin animal)) [0.9 0.8]] R))

  (trun* '([0.9000000000000001 0.2857142857142857]
            [0.9000000000000001 0.2857142857142857]) [V]
         (inference ['(inheritance robin animal) [0.9 0.8]]
                    ['(inheritance robin flyer) [1 0.5]]
                    ['(equivalence (inheritance robin flyer) (inheritance robin animal)) V]))

  (trun* '([[implication [conjunction (a1 a3)] c] [0.81 0.6561000000000001]]) [R]
         (inference ['(implication (conjunction [a1 a2 a3]) c) [0.9 0.9]] ['a2 [0.9 0.9]] R))

  (trun* '([0.9 0.42163100057836905]) [V]
         (inference ['(implication (conjunction [a1 a2 a3]) c) [0.9 0.9]]
                    ['(implication (conjunction [a1 a3]) c) [0.9 0.9]] ['a2 V]))

  (trun* '([0.9 0.42163100057836905]) [V]
         (inference ['(implication (conjunction [a1 a3]) c) [0.9 0.9]] ['a2 [0.9 0.9]]
                    ['(implication (conjunction [a2 a1 a3]) c) V]))

  (trun* '([[implication [conjunction (a1 b2 a3)] c]
            [0.81 0.6561000000000001]]) [R]
         (inference ['(implication (conjunction [a1 a2 a3]) c) [0.9 0.9]]
                    ['(implication b2 a2) [0.9 0.9]] R))
  (trun* '([0.9 0.42163100057836905]) [V]
         (inference ['(implication (conjunction [a1 a2 a3]) c) [0.9 0.9]]
                    ['(implication (conjunction [a1 b2 a3]) c) [0.9 0.9]]
                    ['(implication b2 a2) V]))
  (trun* '([[implication [conjunction (a1 a2 a3)] c] [0.9 0.42163100057836905]]) [R]
         (inference ['(implication (conjunction [a1 b2 a3]) c) [0.9 0.9]]
                    ['(implication b2 a2) [0.9 0.9]] R)))



























