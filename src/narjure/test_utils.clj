(ns narjure.test_utils
  (:require [clojure.set :as set]
            [narjure.defaults :refer :all]
            [narjure.term_utils :refer :all]
            [narjure.narsese :refer :all]
            [nal.deriver :refer :all]))

(def truth-tolerance 0.005)
(defn truth-equal?
  ([s1 s2]
   (and (truth-equal? first s1 s2)
        (truth-equal? last s1 s2)))
  ([f s1 s2]
   (< (Math/abs (- (f (:truth s1))
                   (f (:truth s2))))
      truth-tolerance)))

(defn conclusions
  "Create all conclusions based on two Narsese premise strings"
  ([p1 p2]
   (let [parsed-p1 (parse2 p1)
         parsed-p2 (parse2 p2)]
     (inference parsed-p1 parsed-p2))))

(defn derived                                               ;must derive single step (no tick parameter), no control dependency
  "Checks whether a certain expected conclusion is derived"
  ([p1 p2 clist]
   (dosync
     (use-counter-reset)                                    ;making sure each testcases starts with zero seed
     (let [parsed-p1 (parse2 p1)]
       (every? (fn [c] (let [parsed-c (parse2 c)]
                         (some #(and (= (:statement %) (:statement parsed-c))
                                     (= (:occurrence %) (:occurrence parsed-c))
                                     (or (= (:task-type parsed-p1) :question)
                                         (= (:task-type parsed-p1) :quest)
                                         (truth-equal? % parsed-c)))
                               (conclusions p1 p2)))) clist)))))
