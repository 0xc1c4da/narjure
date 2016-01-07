(ns nal.test.nal3
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run*]]
            [nal.test.test-utils :refer [trun trun*]]))

(deftest test-inference-nal3
  ;compound construction, two premises
  (trun* '([[inheritance bird swimmer] [0.9 0.3932038834951457]]
            [[similarity bird swimmer] [0.7346938775510204 0.4425242501951166]]
            [[inheritance swan [ext-intersection [swimmer bird]]]
             [0.7200000000000001 0.81]]
            [[inheritance swan [int-intersection [swimmer bird]]]
             [0.9800000000000001 0.81]]
            [[inheritance swan [ext-difference swimmer bird]]
             [0.17999999999999997 0.81]]
            [[implication [inheritance _0 bird] [inheritance _0 swimmer]]
             [0.9 0.3932038834951457]]
            [[conjunction
              [[inheritance [var _0 []] bird] [inheritance [var _0 []] swimmer]]]
             [0.7200000000000001 0.81]]
            [[equivalence [inheritance _0 bird] [inheritance _0 swimmer]]
             [0.7346938775510204 0.4425242501951166]])
         [R]
         (inference ['(inheritance swan swimmer) [0.9 0.9]]
                    ['(inheritance swan bird) [0.8 0.9]] R))
  (trun* '([[inheritance chess sport] [0.8 0.42163100057836905]]
            [[similarity chess sport] [0.7346938775510204 0.4425242501951166]]
            [[inheritance [int-intersection [sport chess]] competition]
             [0.7200000000000001 0.81]]
            [[inheritance [ext-intersection [sport chess]] competition]
             [0.9800000000000001 0.81]]
            [[inheritance [int-difference sport chess] competition]
             [0.17999999999999997 0.81]]
            [[implication [inheritance sport _0] [inheritance chess _0]]
             [0.8 0.42163100057836905]]
            [[conjunction
              [[inheritance chess [var _0 []]] [inheritance sport [var _0 []]]]]
             [0.7200000000000001 0.81]]
            [[equivalence [inheritance sport _0] [inheritance chess _0]]
             [0.7346938775510204 0.4425242501951166]])
         [R]
         (inference ['(inheritance sport competition) [0.9 0.9]]
                    ['(inheritance chess competition) [0.8 0.9]] R))
  ;compound construction, single premise
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance swan swimmer) [0.9 0.8]]
                    ['(inheritance swan [ext-intersection [swimmer bird]]) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance swan swimmer) [0.9 0.8]]
                    ['(inheritance swan (int-intersection [swimmer bird])) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance swan swimmer) [0.9 0.8]]
                    ['(inheritance swan (ext-difference swimmer bird)) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance swan swimmer) [0.9 0.8]]
                    ['(negation (inheritance swan (ext-difference bird swimmer))) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance sport competition) [0.9 0.8]]
                    ['(inheritance (int-intersection [sport chess]) competition) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance sport competition) [0.9 0.8]]
                    ['(inheritance (ext-intersection [sport chess]) competition) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance sport competition) [0.9 0.8]]
                    ['(inheritance (int-difference sport chess) competition) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance sport competition) [0.9 0.8]]
                    ['(negation (inheritance (int-difference chess sport) competition)) V]))
  ;compound destruction, single premise
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance swan (ext-intersection [swimmer bird])) [0.9 0.8]]
                    ['(inheritance swan swimmer) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance swan (int-intersection [swimmer bird])) [0.9 0.8]]
                    ['(inheritance swan swimmer) V]))

  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance swan (ext-difference swimmer bird)) [0.9 0.8]]
                    ['(inheritance swan swimmer) V]))

  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance swan (ext-difference swimmer bird)) [0.9 0.8]]
                    ['(negation (inheritance swan bird)) V]))

  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance (int-intersection [sport chess]) competition) [0.9 0.8]]
                    ['(inheritance sport competition) V]))

  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (ext-intersection [sport chess]) competition) [0.9 0.8]]
                    ['(inheritance sport competition) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance (int-difference sport chess) competition) [0.9 0.8]]
                    ['(inheritance sport competition) V]))

  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance (int-difference sport chess) competition) [0.9 0.8]]
                    ['(negation (inheritance chess competition)) V]))
  ;operation on both sides of a relation
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (ext-intersection [swimmer bird])
                                   (ext-intersection [swimmer animal])) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (ext-intersection [swimmer bird])
                                   (ext-intersection [swimmer animal])) [0.9 0.8]]
                    ['(inheritance bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (int-intersection [swimmer bird])
                                   (int-intersection [swimmer animal])) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (int-intersection [swimmer bird])
                                   (int-intersection [swimmer animal])) [0.9 0.8]]
                    ['(inheritance bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(similarity bird animal) [0.9 0.8]]
                    ['(similarity (ext-intersection [swimmer bird])
                                  (ext-intersection [swimmer animal])) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(similarity (ext-intersection [swimmer bird])
                                  (ext-intersection [swimmer animal])) [0.9 0.8]]
                    ['(similarity bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(similarity bird animal) [0.9 0.8]]
                    ['(similarity (int-intersection [swimmer bird])
                                  (int-intersection [swimmer animal])) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(similarity (int-intersection [swimmer bird])
                                  (int-intersection [swimmer animal])) [0.9 0.8]]
                    ['(similarity bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (ext-difference bird swimmer)
                                   (ext-difference animal swimmer)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (ext-difference bird swimmer)
                                   (ext-difference animal swimmer)) [0.9 0.8]]
                    ['(inheritance bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (int-difference bird swimmer)
                                   (int-difference animal swimmer)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (int-difference bird swimmer)
                                   (int-difference animal swimmer)) [0.9 0.8]]
                    ['(inheritance bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(similarity bird animal) [0.9 0.8]]
                    ['(similarity (ext-difference bird swimmer)
                                  (ext-difference animal swimmer)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(similarity (ext-difference bird swimmer)
                                  (ext-difference animal swimmer)) [0.9 0.8]]
                    ['(similarity bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(similarity bird animal) [0.9 0.8]]
                    ['(similarity (int-difference bird swimmer)
                                  (int-difference animal swimmer)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(similarity (int-difference bird swimmer)
                                  (int-difference animal swimmer)) [0.9 0.8]]
                    ['(similarity bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (ext-difference swimmer animal)
                                   (ext-difference swimmer bird)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (ext-difference swimmer animal)
                                   (ext-difference swimmer bird)) [0.9 0.8]]
                    ['(inheritance bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(inheritance bird animal) [0.9 0.8]]
                    ['(inheritance (int-difference swimmer animal)
                                   (int-difference swimmer bird)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(inheritance (int-difference swimmer animal)
                                   (int-difference swimmer bird)) [0.9 0.8]]
                    ['(inheritance bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(similarity bird animal) [0.9 0.8]]
                    ['(similarity (ext-difference swimmer animal)
                                  (ext-difference swimmer bird)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(similarity (ext-difference swimmer animal)
                                  (ext-difference swimmer bird)) [0.9 0.8]]
                    ['(similarity bird animal) V]))
  (trun* [[0.9 0.7200000000000001]] [V]
         (inference ['(similarity bird animal) [0.9 0.8]]
                    ['(similarity (int-difference swimmer animal)
                                  (int-difference swimmer bird)) V]))
  (trun* [[0.9 0.4444444444444445]] [V]
         (inference ['(similarity (int-difference swimmer animal)
                                  (int-difference swimmer bird)) [0.9 0.8]]
                    ['(similarity bird animal) V]))
  ;set operations
  (trun*
    '([[inheritance (ext-set [pluto saturn]) (ext-set [venus mars pluto])] [0.9 0.3093922651933702]]
       [[similarity (ext-set [pluto saturn]) (ext-set [venus mars pluto])] [0.6494845360824741 0.38302073050345514]]
       [[inheritance (ext-set [earth]) [ext-set (pluto)]] [0.63 0.6400000000000001]]
       [[inheritance (ext-set [earth]) [ext-set (venus mars pluto saturn)]] [0.9700000000000001 0.6400000000000001]]
       [[inheritance (ext-set [earth]) [ext-set (venus mars)]] [0.2700000000000001 0.6400000000000001]]
       [[implication [inheritance _0 (ext-set [pluto saturn])] [inheritance _0 (ext-set [venus mars pluto])]] [0.9 0.3093922651933702]]
       [[conjunction [[inheritance [var _0 []] (ext-set [pluto saturn])] [inheritance [var _0 []] (ext-set [venus mars pluto])]]] [0.63 0.6400000000000001]]
       [[equivalence [inheritance _0 (ext-set [pluto saturn])] [inheritance _0 (ext-set [venus mars pluto])]] [0.6494845360824741 0.38302073050345514]])
    [R]
    (inference ['(inheritance (ext-set [earth])
                              (ext-set [venus mars pluto])) [0.9 0.8]]
               ['(inheritance (ext-set [earth])
                              (ext-set [pluto saturn])) [0.7 0.8]] R))
  (trun*
    '([[inheritance (int-set [purple green]) (int-set [red green blue])] [0.7 0.36548223350253817]]
       [[similarity (int-set [purple green]) (int-set [red green blue])] [0.6494845360824741 0.38302073050345514]]
       [[inheritance [int-set (green)] (int-set [colorful])] [0.63 0.6400000000000001]]
       [[inheritance [int-set (red blue purple green)] (int-set [colorful])] [0.9700000000000001 0.6400000000000001]]
       [[inheritance [int-set (red blue)] (int-set [colorful])] [0.2700000000000001 0.6400000000000001]]
       [[implication [inheritance (int-set [red green blue]) _0] [inheritance (int-set [purple green]) _0]] [0.7 0.36548223350253817]]
       [[conjunction [[inheritance (int-set [purple green]) [var _0 []]] [inheritance (int-set [red green blue]) [var _0 []]]]] [0.63 0.6400000000000001]]
       [[equivalence [inheritance (int-set [red green blue]) _0] [inheritance (int-set [purple green]) _0]] [0.6494845360824741 0.38302073050345514]])
    [R]
    (inference ['(inheritance (int-set [red green blue]) (int-set [colorful]))
                [0.9 0.8]]
               ['(inheritance (int-set [purple green]) (int-set [colorful]))
                [0.7 0.8]] R)))
