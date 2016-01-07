(ns nal.test.nal6
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run* fresh]]
            [nal.test.test-utils :refer [trun trun*]]))

(deftest test-inference-nal6
  ; variable unification
  (trun* '([[implication [inheritance _0 bird] [inheritance _0 flyer]] [0.9200000000000002 0.8333333333333334]])
         [R]
         (fresh [X Y]
           (revision [['implication ['inheritance X 'bird] ['inheritance X 'flyer]] [0.9 0.8]] [['implication ['inheritance Y 'bird] ['inheritance Y 'flyer]] [1 0.5]] R)))
  (trun* '([[implication [inheritance _0 robin] [inheritance _0 animal]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'bird] ['inheritance X 'animal]] [1 0.9]] [['implication ['inheritance Y 'robin] ['inheritance Y 'bird]] [1 0.9]] R)))
  (trun* '([[implication [inheritance _0 robin] [inheritance _0 bird]] [1 0.44751381215469616]]
            [[equivalence [inheritance _0 robin] [inheritance _0 bird]] [1 0.44751381215469616]]
            [[implication [disjunction [[inheritance _0 bird] [inheritance _0 robin]]] [inheritance _0 animal]] [1 0.81]]
            [[implication [conjunction [[inheritance _0 bird] [inheritance _0 robin]]] [inheritance _0 animal]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'bird] ['inheritance X 'animal]] [1 0.9]] [['implication ['inheritance Y 'robin] ['inheritance Y 'animal]] [1 0.9]] R)))
  (trun* '([[implication [inheritance _0 bird] [inheritance _0 animal]] [1 0.44751381215469616]]
            [[equivalence [inheritance _0 bird] [inheritance _0 animal]] [1 0.44751381215469616]]
            [[implication [inheritance _0 robin] [conjunction [[inheritance _0 animal] [inheritance _0 bird]]]] [1 0.81]]
            [[implication [inheritance _0 robin] [disjunction [[inheritance _0 animal] [inheritance _0 bird]]]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'robin] ['inheritance X 'animal]] [1 0.9]] [['implication ['inheritance Y 'robin] ['inheritance Y 'bird]] [1 0.9]] R)))
  (trun* '([[implication [inheritance _0 feathered] [inheritance _0 flyer]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'feathered] ['inheritance X 'bird]] [1 0.9]] [['equivalence ['inheritance Y 'flyer] ['inheritance Y 'bird]] [1 0.9]] R)))
  (trun* '([[implication [inheritance _0 bird] [inheritance _0 flyer]] [1 0.44751381215469616]]
            [[equivalence [inheritance _0 bird] [inheritance _0 flyer]] [1 0.44751381215469616]]
            [[implication [inheritance _0 feathered] [conjunction [[inheritance _0 flyer] [inheritance _0 bird]]]] [1 0.81]]
            [[implication [inheritance _0 feathered] [disjunction [[inheritance _0 flyer] [inheritance _0 bird]]]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'feathered] ['inheritance X 'flyer]] [1 0.9]] [['implication ['inheritance Y 'feathered] ['inheritance Y 'bird]] [1 0.9]] R)))
  (trun* '([[implication [conjunction ([inheritance _0 swimmer] [inheritance _0 flyer])] [inheritance _0 bird]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['conjunction [['inheritance X 'feathered] ['inheritance X 'flyer]]] ['inheritance X 'bird]] [1 0.9]] [['implication ['inheritance Y 'swimmer] ['inheritance Y 'feathered]] [1 0.9]] R)))
  (trun* '([[implication [conjunction [[inheritance _0 swimmer] [inheritance _0 flyer]]] [conjunction [[inheritance _0 feathered] [inheritance _0 flyer]]]] [1 0.44751381215469616]]
            [[equivalence [conjunction [[inheritance _0 swimmer] [inheritance _0 flyer]]] [conjunction [[inheritance _0 feathered] [inheritance _0 flyer]]]] [1 0.44751381215469616]]
            [[implication [disjunction [[conjunction [[inheritance _0 feathered] [inheritance _0 flyer]]] [conjunction [[inheritance _0 swimmer] [inheritance _0 flyer]]]]] [inheritance _0 bird]] [1 0.81]]
            [[implication [conjunction ([inheritance _0 feathered] [inheritance _0 swimmer] [inheritance _0 flyer])] [inheritance _0 bird]] [1 0.81]]
            [[implication [inheritance _0 swimmer] [inheritance _0 feathered]] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['implication ['conjunction [['inheritance X 'feathered] ['inheritance X 'flyer]]] ['inheritance X 'bird]] [1 0.9]] [['implication ['conjunction [['inheritance X 'swimmer] ['inheritance X 'flyer]]] ['inheritance X 'bird]] [1 0.9]] R)))
  (trun* '([[implication [conjunction ([inheritance _0 feathered] [inheritance _0 flyer])] [inheritance _0 bird]] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['implication ['conjunction [['inheritance X 'swimmer] ['inheritance X 'flyer]]] ['inheritance X 'bird]] [1 0.9]] [['implication ['inheritance Y 'swimmer] ['inheritance Y 'feathered]] [1 0.9]] R)))
  (trun* '([[implication [conjunction ([inheritance _0 swimmer] [inheritance _0 flyer])] [inheritance _0 bird]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['conjunction [['inheritance X 'feathered] ['inheritance X 'flyer]]] ['inheritance X 'bird]] [1 0.9]] [['implication ['inheritance Y 'swimmer] ['inheritance Y 'feathered]] [1 0.9]] R)))
  ; variable elimination
  (trun* '([[inheritance robin animal] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'bird] ['inheritance X 'animal]] [1 0.9]] [['inheritance 'robin 'bird] [1 0.9]] R)))
  (trun* '([[inheritance robin bird] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'bird] ['inheritance X 'animal]] [1 0.9]] [['inheritance 'robin 'animal] [1 0.9]] R)))
  (trun* '([[inheritance robin bird] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['inheritance 'robin 'animal] [1 0.9]] [['equivalence ['inheritance X 'bird] ['inheritance X 'animal]] [1 0.9]] R)))
  (trun* '([[implication [inheritance swan flyer] [inheritance swan bird]] [1 0.81]]) [R]
         (fresh [X Y]
           (inference [['implication ['conjunction [['inheritance X 'feathered] ['inheritance X 'flyer]]] ['inheritance X 'bird]] [1 0.9]]
                      [['inheritance 'swan 'feathered] [1 0.9]] R)))
  (trun* '([1 0.42631578947368426]) [V]
         (fresh [X Y]
           (inference [['conjunction [['inheritance ['var X []] 'bird] ['inheritance ['var X []] 'swimmer]]] [1 0.9]] [['inheritance 'swan 'bird] [1 0.9]] [['inheritance 'swan 'swimmer] V])))

  (trun* '([[conjunction ([inheritance swan [var _0 []]] [inheritance [var _1 []] [var _0 []]] [inheritance [var _1 []] flyer] [inheritance [var _1 []] swimmer])] [1 0.81]]
            [[implication [inheritance swan _0] [conjunction ([inheritance [var _1 (_0)] _0] (inheritance [var _1 (_0)] flyer) (inheritance [var _1 (_0)] swimmer))]] [1 0.44751381215469616]]
            [[conjunction ([inheritance swan flyer] [inheritance swan swimmer])] [1 0.42631578947368426]])
         [R]
         (fresh [X Y]
           (inference [['conjunction [['inheritance ['var X []] 'flyer] ['inheritance ['var X []] 'bird] ['inheritance ['var X []] 'swimmer]]] [1 0.9]] [['inheritance 'swan 'bird] [1 0.9]] R)))
  ; variable introduction
  (trun* '([[inheritance bird animal] [1 0.44751381215469616]]
            [[similarity bird animal] [1 0.44751381215469616]]
            [[inheritance robin [ext-intersection [animal bird]]] [1 0.81]]
            [[inheritance robin [int-intersection [animal bird]]] [1 0.81]]
            [[inheritance robin [ext-difference animal bird]] [0 0.81]]
            [[implication [inheritance _0 bird] [inheritance _0 animal]] [1 0.44751381215469616]]
            [[conjunction [[inheritance [var _0 []] bird] [inheritance [var _0 []] animal]]] [1 0.81]]
            [[equivalence [inheritance _0 bird] [inheritance _0 animal]] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['inheritance 'robin 'animal] [1 0.9]] [['inheritance 'robin 'bird] [1 0.9]] R)))
  (trun* '([[inheritance chess sport] [1 0.44751381215469616]]
            [[similarity chess sport] [1 0.44751381215469616]]
            [[inheritance [int-intersection [sport chess]] competition] [1 0.81]]
            [[inheritance [ext-intersection [sport chess]] competition] [1 0.81]]
            [[inheritance [int-difference sport chess] competition] [0 0.81]]
            [[implication [inheritance sport _0] [inheritance chess _0]] [1 0.44751381215469616]]
            [[conjunction [[inheritance chess [var _0 []]] [inheritance sport [var _0 []]]]] [1 0.81]]
            [[equivalence [inheritance sport _0] [inheritance chess _0]] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['inheritance 'sport 'competition] [1 0.9]] [['inheritance 'chess 'competition] [1 0.9]] R)))
  ; multiple variables
  (trun* '([[inheritance key [ext-image open [nil lock1]]] [1 0.44751381215469616]]
            [[similarity key [ext-image open [nil lock1]]] [1 0.44751381215469616]]
            [[inheritance key1 [ext-intersection [[ext-image open [nil lock1]] key]]] [1 0.81]]
            [[inheritance key1 [int-intersection [[ext-image open [nil lock1]] key]]] [1 0.81]]
            [[inheritance key1 [ext-difference [ext-image open [nil lock1]] key]] [0 0.81]]
            [[implication [inheritance _0 key] [inheritance _0 [ext-image open [nil lock1]]]] [1 0.44751381215469616]]
            [[conjunction [[inheritance [var _0 []] key] [inheritance [var _0 []] [ext-image open [nil lock1]]]]] [1 0.81]]
            [[equivalence [inheritance _0 key] [inheritance _0 [ext-image open [nil lock1]]]] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['inheritance 'key1 ['ext-image 'open [nil 'lock1]]] [1 0.9]] [['inheritance 'key1 'key] [1 0.9]] R)))
  (trun* '([[implication [conjunction [[inheritance _0 key] [inheritance _1 lock]]] [inheritance _1 [ext-image open [_0 nil]]]] [1 0.44751381215469616]]
            [[conjunction [[implication [inheritance _0 key] [inheritance [var _1 []] [ext-image open [_0 nil]]]] [inheritance [var _1 []] lock]]] [1 0.81]])
         [R]
         (fresh [X Y]
           (inference [['implication ['inheritance X 'key] ['inheritance 'lock1 ['ext-image 'open [X nil]]]] [1 0.9]] [['inheritance 'lock1 'lock] [1 0.9]] R)))
  (trun* '([[conjunction ([inheritance [var _0 []] lock] [inheritance [var _0 []] [ext-image open [[var _1 []] nil]]] [inheritance [var _1 []] key])] [1 0.81]]
            [[implication [inheritance _0 lock] [conjunction ([inheritance _0 (ext-image open ([var _1 (_0)] nil))] (inheritance [var _1 (_0)] key))]] [1 0.44751381215469616]])
         [R]
         (fresh [X Y]
           (inference [['conjunction [['inheritance ['var X []] 'key] ['inheritance 'lock1 ['ext-image 'open [['var X []] nil]]]]] [1 0.9]] [['inheritance 'lock1 'lock] [1 0.9]] R))))
