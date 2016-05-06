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
  ([s1 s2]
   (and (truth-equal? first s1 s2)
        (truth-equal? last s1 s2)))
  ([f s1 s2]
   (< (Math/abs (- (f (:truth s1))
                   (f (:truth s2))))
      truth-tolerance)))

(defn derived                                               ;must derive single step (no tick parameter), no control dependency
  "Checks whether a certain expected conclusion is derived"
  ([p1 clist]
  (derived p1 "<unrelated --> UNRELATED>."))
  ([p1 p2 clist]
  (let [parsed-p1 (parse p1)]
    (every? (fn [c] (let [parsed-c (parse c)]
                     (some #(and (= (:statement %) (:statement parsed-c))
                                 (or (= (:action parsed-p1) :question)
                                     (= (:action parsed-p1) :quest)
                                     (truth-equal? % parsed-c)))
                           (conclusions p1 p2)))) clist))))


;NAL1 testcases:

(deftest nal1-deduction
  (is (derived "<shark --> fish>."
               "<fish --> animal>."
               ["<shark --> animal>. %1.00;0.81%"])))


(deftest nal1-abduction
  (is (derived "<sport --> competition>."
               "<chess --> competition>. %0.90;0.90%"
               ["<sport --> chess>. %1.00;0.42%"
                "<chess --> sport>. %0.90;0.45%"])))

(deftest nal1-induction
  (is (derived "<swan --> swimmer>. %0.90;0.90%"
               "<swan --> bird>."
               ["<bird --> swimmer>. %0.90;0.45%"
                "<swimmer --> bird>. %1.00;0.42%"])))

(deftest nal1-exemplification
  (is (derived "<robin --> bird>. %0.90;0.90%"
               "<bird --> animal>."
               ["<animal --> robin>. %1.00;0.42%"])))

(deftest nal1-conversion
  (is (derived "<swimmer --> bird>?"
               "<bird --> swimmer>."
               ["<swimmer --> bird>. %1.00;0.47%"])))

(deftest nal1-backward
  (is (derived "<?1 --> swimmer>?"
               "<bird --> swimmer>."
               ["<?1 --> bird>?",
                "<bird --> ?1>?"])))

;NAL2 testcases:

(deftest nal2-comparison
  (is (derived "<swan --> swimmer>. %0.9;0.9%"
               "<swan --> bird>."
               ["<bird <-> swimmer>. %0.9;0.45%"])))

(deftest nal2-comparison2
  (is (derived "<sport --> competition>."
               "<chess --> competition>. %0.9;0.9%"
               ["<chess <-> sport>. %0.9;0.45%"])))

(deftest nal2-analogy
  (is (derived "<swan --> swimmer>."
               "<gull <-> swan>."
               ["<gull --> swimmer>. %1.0;0.81%"])))

(deftest nal2-analogy2
  (is (derived "<gull --> swimmer>."
               "<gull <-> swan>."
               ["<swan --> swimmer>. %1.0;0.81%"])))

(deftest nal2-resemblance
  (is (derived "<robin <-> swan>."
               "<gull <-> swan>."
               ["<gull <-> robin>. %1.0;0.81%"])))

(deftest nal2-inheritance-to-similarity
  (is (derived "<swan --> bird>."
               "<bird --> swan>. %0.1;0.9%"
               ["<bird <-> swan>. %0.1;0.81%"])))

(deftest nal2-inheritance-to-similarity2
  (is (derived "<swan --> bird>."
               "<bird <-> swan>. %0.1;0.9%"
               ["<bird --> swan>. %0.1;0.73%"])))

(deftest nal2-inheritance-to-similarity3
  (is (derived "<swan --> bird>. %0.9;0.9%"
               "<bird <-> swan>."
               ["<bird --> swan>. %0.9;0.45%"])))

(deftest nal2-inheritance-to-similarity4
  (is (derived "<bird <-> swan>. %0.9;0.9%"
               "<swan --> bird>."
               ["<swan --> bird>. %0.9;0.81%"])))

(deftest setDefinition
  (is (derived "<{Tweety} --> {Birdie}>."
               "{Tweety}"
               ["<{Tweety} <-> {Birdie}>. %1.0;0.9%"
                ])))

(deftest setDefinition2
  (is (derived "<[smart] --> [bright]>."
               "[smart]."
               ["<[bright] <-> [smart]>. %1.0;0.9%"
                ])))

(deftest setDefinition3
  (is (derived "<{Birdie} <-> {Tweety}>."
               "{Birdie}."
               ["<Birdie <-> Tweety>. %1.0;0.9%"
                "<{Tweety} --> {Birdie}>. %1.0;0.9%"
                ])))

(deftest setDefinition4
  (is (derived "<[bright] <-> [smart]>."
               ["<bright <-> smart>. % 1.0; 0.9%"
                "<[bright] --> [smart]>. %1.0;0.9%"
                ])))
(deftest structureTransformation
  (is (derived "<Birdie <-> Tweety>", 0.9f, 0.9f);//Birdie is similar to Tweety.. % 0.9; 0.9%"
      ["<{Birdie} <-> {Tweety}>. % 0.9;0.9%"
       ]))

(deftest structureTransformation2
  (is (derived "<bright <-> smart>", 0.9f, 0.9f);//Bright is similar to smart.. % 0.9; 0.9%"
      ["<[bright] --> [smart]>. % 0.9;0.9%"
       ]))

(deftest structureTransformation3
  (is (derived "<bright <-> smart>", 0.9f, 0.9f);//Bright is similar to smart.. % 0.9; 0.9%"
      ["<{bright} --> {smart}>. % 0.9;0.9%"]))

(deftest backwardInference
  (is (derived "<{?x} --> swimmer>?"
        "<bird --> swimmer>."
               ["<{?1} --> bird>?"])))

(deftest missingEdgeCase1
  (is (derived "<p1 --> p2>."
               "<p2 <-> p3>."
               ["<p1 --> p3>. %1.00;0.81%"
                ])))

;NAL3 testcases:

;NAL4 testcases:

;NAL5 testcases:

;NAL6 testcases:

;NAL7 testcases:

;NAL8 testcases:
