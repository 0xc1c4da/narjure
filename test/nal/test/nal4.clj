(ns nal.test.nal4
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run*]]
            [nal.test.test-utils :refer [trun trun*]]))

(deftest test-inference-nal4
  ;extensional image
  (trun*
    '([[inheritance reaction (product [acid base])] [1 0.4736842105263158]]
       [[inheritance acid [ext-image reaction (nil base)]] [1 0.9]]
       [[inheritance base [ext-image reaction (acid nil)]] [1 0.9]])
    [C]
    (inference ['(inheritance (product [acid base]) reaction)
                [1 0.9]] C))
  (trun*
    '([[inheritance (ext-image reaction [nil base]) acid] [1 0.4736842105263158]]
       [[inheritance [product (acid base)] reaction] [1 0.9]])
    [C]
    (inference ['(inheritance acid (ext-image reaction [nil base]))
                [1 0.9]] C))
  (trun*
    '([[inheritance (ext-image reaction [acid nil]) acid] [1 0.4736842105263158]]
       [[inheritance [product (acid acid)] reaction] [1 0.9]])
    [C]
    (inference ['(inheritance acid (ext-image reaction [acid nil]))
                [1 0.9]] C))
  ;intensional image
  (trun*
    '([[inheritance (product [acid base]) neutralization] [1 0.4736842105263158]]
       [[inheritance [int-image neutralization (nil base)] acid] [1 0.9]]
       [[inheritance [int-image neutralization (acid nil)] base] [1 0.9]])
    [C]
    (inference ['(inheritance neutralization, (product [acid, base])),
                [1, 0.9]], C))
  (trun*
    '([[inheritance acid (int-image neutralization [nil base])]
       [1 0.4736842105263158]]
       [[inheritance neutralization [product (acid base)]] [1 0.9]])
    [C]
    (inference ['(inheritance (int-image neutralization, [nil, base]), acid),
                [1, 0.9]], C))
  (trun*
    '([[inheritance base (int-image neutralization [acid nil])]
       [1 0.4736842105263158]]
       [[inheritance neutralization [product (acid base)]] [1 0.9]])
    [C]
    (inference ['(inheritance (int-image neutralization, [acid, nil]), base),
                [1, 0.9]], C))
  ;operation on both sides of a relation
  (trun* '([0.9 0.8] [0.9 0.8]) [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (product [bird plant])
                                   (product [animal plant])) V]))

  (trun* '([0.9 0.8] [0.9 0.8]) [V]
         (inference ['(inheritance (product [plant bird]) (product [plant animal]))
                     [0.9 0.8]]
                    ['(inheritance bird animal) V]))

  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(inheritance neutralization reaction) [0.9 0.8]]
                    ['(inheritance (ext-image neutralization [acid nil])
                                   (ext-image reaction [acid nil])) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(inheritance (ext-image neutralization [acid nil])
                                   (ext-image reaction [acid nil])) [0.9 0.8]]
                    ['(inheritance neutralization reaction) V]))

  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(inheritance neutralization reaction) [0.9 0.8]]
                    ['(inheritance (int-image neutralization [acid nil])
                                   (int-image reaction [acid nil])) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(inheritance (int-image neutralization [acid nil])
                                   (int-image reaction [acid nil])) [0.9 0.8]]
                    ['(inheritance neutralization reaction) V]))

  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(inheritance soda base) [0.9 0.8]]
                    ['(inheritance (ext-image reaction [nil base])
                                   (ext-image reaction [nil soda])) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(inheritance (ext-image reaction [nil base])
                                   (ext-image reaction [nil soda])) [0.9 0.8]]
                    ['(inheritance soda base) V]))

  (trun* '([0.9 0.7200000000000001]) [V]
         (inference ['(inheritance soda base) [0.9 0.8]]
                    ['(inheritance (int-image neutralization [nil base])
                                   (int-image neutralization [nil soda])) V]))

  (trun* '([0.9 0.4444444444444445]) [V]
         (inference ['(inheritance (int-image neutralization [nil base])
                                   (int-image neutralization [nil soda])) [0.9 0.8]]
                    ['(inheritance soda base) V])))
