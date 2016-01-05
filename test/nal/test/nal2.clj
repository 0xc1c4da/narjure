(ns nal.test.nal2
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]
            [clojure.core.logic :refer [run run*]]
            [nal.test.test-utils :refer [trun]]))

(deftest test-inference-nal2
  ;inheritance to similarity
  (trun [[0.81 0.6400000000000001]]
        [q]
        (inference '((inheritance swan robin) [0.9 0.8])
                   '((inheritance robin swan) [0.9 0.8])
                   ['(similarity swan robin) q]))
  ;comparison
  (trun [[1 0.44751381215469616]]
        [q]
        (inference ['(inheritance swan swimmer) [1 0.9]]
                   ['(inheritance swan bird) [1 0.9]]
                   ['(similarity bird swimmer) q]))
  (trun [[1 0.44751381215469616]]
        [q]
        (inference ['(inheritance sport competition) [1 0.9]]
                   ['(inheritance chess competition) [1 0.9]]
                   ['(similarity chess sport) q]))
  ;analogy
  (trun [[0.9 0.7290000000000001]]
        [q]
        (inference ['(inheritance swan swimmer) [1 0.9]]
                   ['(similarity gull swan) [0.9 0.9]]
                   ['(inheritance gull swimmer) q]))
  (trun [[0.9 0.7290000000000001]]
        [q]
        (inference ['(inheritance chess competition) [1 0.9]]
                   ['(similarity sport competition) [0.9 0.9]]
                   ['(inheritance chess sport) q]))
  ;resemblance
  (trun [[0.7200000000000001 0.7056000000000001]]
        [q]
        (inference ['(similarity swan robin) [0.8 0.9]]
                   ['(similarity gull swan) [0.9 0.8]]
                   ['(similarity gull robin) q]))
  ;instance and property
  (trun '([[ext-set [tweety]] bird [1 0.9]])
        [S P T] (inference ['(instance tweety bird) [1 0.9]]
                           [['inheritance S P] T]))
  (trun '([raven [int-set [black]] [1 0.9]])
        [S P T] (inference ['(property raven black) [1 0.9]] 
                           [['inheritance S P] T]))
  (trun '([[ext-set [tweety]] [int-set [yellow]] [1 0.9]])
        [S P T] (inference ['(inst-prop tweety yellow) [1 0.9]]
                           [['inheritance S P] T]))
  ;set definition
  (trun '([[ext-set [tweety]] [ext-set [birdie]] [1 0.8]])
        [S P T] (inference ['(inheritance (ext-set [tweety]) (ext-set [birdie]))
                            [1 0.8]]
                           [['similarity S P] T]))
  (trun '([[int-set [smart]] [int-set [bright]] [1 0.8]])
        [S P T] (inference ['(inheritance (int-set [smart]) (int-set [bright]))
                            [1 0.8]]
                           [['similarity S P] T]))
  ;structure transformation
  (trun [[1 0.9]]
        [T]
        (inference ['(similarity (ext_set [tweety]) (ext_set [birdie])) [1 0.9]]
                   ['(similarity tweety birdie) T]))
  (trun [[0.8 0.9]]
        [T]
        (inference ['(similarity (ext-set [smart]) (ext-set [bright])) [0.8 0.9]]
                   ['(similarity smart bright) T])))
