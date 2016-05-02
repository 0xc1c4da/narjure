(ns narjure.test.nal
  (:require [clojure.test :refer :all]
            [narjure.narsese :refer :all]
            [instaparse.core :refer [failure?]]
            [nal.deriver :refer :all]
            [nal.rules :as r]))

(defn conclusions
  "Create all conclusions based on two Narsese premise strings"
  ([p1 p2]                                                  ;todo probably parser should probably recognize and set occurrence 0 by default in case of :|:
   (conclusions p1 p2 0))                                   ;after this task creator assigns current time when it becomes a real task
  ([p1 p2 occurrence]
   (let [parsed-p1 (parse p1)
         parsed-p2 (parse p2)
         punctuation (:action parsed-p1)]
     (set (generate-conclusions
            (r/rules punctuation)
            (assoc parsed-p1 :occurrence occurrence)
            (assoc parsed-p2 :occurrence occurrence))))))

(def truth-tolerance 0.005)

(defn truth-equal?
  [f s1 s2]
  (< (Math/abs (- (f (:truth s1))
                  (f (:truth s2))))
     truth-tolerance))

(defn derived                                               ;must derive single step (no tick parameter), no control dependency
  "Checks whether a certain expected conclusion is derived"
  [p1 p2 clist]
  (let [parsed-p1 (parse p1)]
    (every? (fn [c] (let [parsed-c (parse c)]
                     (some #(and (= (:statement %) (:statement parsed-c))
                                 (or (= (:action parsed-p1) :question)
                                     (= (:action parsed-p1) :quest)
                                     (and (truth-equal? first % parsed-c)
                                          (truth-equal? last % parsed-c))))
                           (conclusions p1 p2)))) clist)))

;NAL1 testcases:

(deftest nal1-inheritance-related-syllogisms-deduction
  (is (derived "<shark --> fish>."
               "<fish --> animal>."
               ["<shark --> animal>. %1.00;0.81%"])))


(deftest nal1-inheritance-related-syllogisms-abduction
  (is (derived "<sport --> competition>."
               "<chess --> competition>. %0.90;0.90%"
               ["<sport --> chess>. %1.00;0.42%"
                "<chess --> sport>. %0.90;0.45%"])))

(deftest nal1-inheritance-related-syllogisms-induction
  (is (derived "<swan --> swimmer>. %0.90;0.90%"
               "<swan --> bird>."
               ["<bird --> swimmer>. %0.90;0.45%"
                "<swimmer --> bird>. %1.00;0.42%"])))

(deftest nal1-inheritance-related-syllogisms-exemplification
  (is (derived "<robin --> bird>. %0.90;0.90%"
               "<bird --> animal>."
               ["<animal --> robin>. %1.00;0.42%"])))

(deftest nal1-inheritance-related-syllogisms-conversion
  (is (derived "<swimmer --> bird>?"
               "<bird --> swimmer>."
               ["<swimmer --> bird>. %1.00;0.47%"])))

(deftest nal1-backward
  (is (derived "<?1 --> swimmer>?"
               "<bird --> swimmer>."
               ["<?1 --> bird>?",
                "<bird --> ?1>?"])))

;NAL2 testcases:

;NAL3 testcases:

;NAL4 testcases:

;NAL5 testcases:

;NAL6 testcases:

;NAL7 testcases:

;NAL8 testcases:
