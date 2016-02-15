(ns nal.rules
  (:require [nal.deriver :refer [defrules rule]]
            [nal.reader :as r]))

(declare --S S --P P <-> |- --> ==> M || && =|> -- A Ai B <=>)

(defrules rules
  ;Similarity to Inheritance
  #R[(S --> P) (S <-> P) |- (S --> P) :pre (:question?)
     :post (:t/struct-int :p/judgment)]
  ;Inheritance to Similarity
  #R[(S <-> P) (S --> P) |- (S <-> P) :pre (:question?)
     :post (:t/struct-abd :p/judgment)]
  ;Set Definition Similarity to Inheritance
  #R[(S <-> {P}) S |- (S --> {P}) :post (:t/identity :d/identity :allow-backward)]
  #R[(S <-> {P}) {P} |- (S --> {P}) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] <-> P) [S] |- ([S] --> P) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] <-> P) P |- ([S] --> P) :post (:t/identity :d/identity :allow-backward)]
  #R[({S} <-> {P}) {S} |- ({P} --> {S}) :post (:t/identity :d/identity :allow-backward)]
  #R[({S} <-> {P}) {P} |- ({P} --> {S}) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] <-> [P]) [S] |- ([P] --> [S]) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] <-> [P]) [P] |- ([P] --> [S]) :post (:t/identity :d/identity :allow-backward)]

  ;Set Definition Unwrap
  #R[({S} <-> {P}) {S} |- (S <-> P) :post (:t/identity :d/identity :allow-backward)]
  #R[({S} <-> {P}) {P} |- (S <-> P) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] <-> [P]) [S] |- (S <-> P) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] <-> [P]) [P] |- (S <-> P) :post (:t/identity :d/identity :allow-backward)]

  ; Nothing is more specific than a instance so it's similar
  #R[(S --> {P}) S |- (S <-> {P}) :post (:t/identity :d/identity :allow-backward)]
  #R[(S --> {P}) {P} |- (S <-> {P}) :post (:t/identity :d/identity :allow-backward)]

  ; nothing is more general than a property so it's similar
  #R[([S] --> P) [S] |- ([S] <-> P) :post (:t/identity :d/identity :allow-backward)]
  #R[([S] --> P) P |- ([S] <-> P) :post (:t/identity :d/identity :allow-backward)]

  ; Immediate Inference
  ; If S can stand for P P can to a certain low degree also represent the class S
  ; If after S usually P happens then it might be a good guess that usually before P happens S happens.
  #R[(P --> S) (S --> P) |- (P --> S) :pre (:question?) :post (:t/conversion :p/judgment)]
  #R[(P ==> S) (S ==> P) |- (P ==> S) :pre (:question?) :post (:t/conversion :p/judgment)]
  #R[(P =|> S) (S =|> P) |- (P =|> S) :pre (:question?) :post (:t/conversion :p/judgment)]
  #R[(P =\> S) (S =/> P) |- (P =\> S) :pre (:question?) :post (:t/conversion :p/judgment)]
  #R[(P =/> S) (S =\> P) |- (P =/> S) :pre (:question?) :post (:t/conversion :p/judgment)]

  ; "If not smoking lets you be healthy being not healthy may be the result of smoking"
  #R[(--S ==> P) P |- (--P ==> S) :post (:t/contraposition :allow-backward)]
  #R[(--S ==> P) --S |- (--P ==> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =|> P) P |- (--P =|> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =|> P) --S |- (--P =|> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =/> P) P |- (--P =\> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =/> P) --S |- (--P =\> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =\> P) P |- (--P =/> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =\> P) --S |- (--P =/> S) :post (:t/contraposition :allow-backward)]

  ; "If not smoking lets you be healthy being not healthy may be the result of smoking"
  #R[(--S ==> P) P |- (--P ==> S) :post (:t/contraposition :allow-backward)]
  #R[(--S ==> P) --S |- (--P ==> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =|> P) P |- (--P =|> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =|> P) --S |- (--P =|> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =/> P) P |- (--P =\> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =/> P) --S |- (--P =\> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =\> P) P |- (--P =/> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =\> P) --S |- (--P =/> S) :post (:t/contraposition :allow-backward)]

  ; A belief b <f c> is equal to --b <1-f c>  which is the negation rule:
  #R[(A --> B) A |- -- (A --> B) :post (:t/negation :d/negation :allow-backward)]
  #R[(A --> B) B |- -- (A --> B) :post (:t/negation :d/negation :allow-backward)]
  #R[-- (A --> B) A |- (A --> B) :post (:t/negation :d/negation :allow-backward)]
  #R[-- (A --> B) B |- (A --> B) :post (:t/negation :d/negation :allow-backward)]

  #R[(A <-> B) A |- -- (A <-> B) :post (:t/negation :d/negation :allow-backward)]
  #R[(A <-> B) B |- -- (A <-> B) :post (:t/negation :d/negation :allow-backward)]
  #R[-- (A <-> B) A |- (A <-> B) :post (:t/negation :d/negation :allow-backward)]
  #R[-- (A <-> B) B |- (A <-> B) :post (:t/negation :d/negation :allow-backward)]

  #R[(A ==> B) A |- -- (A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[(A ==> B) B |- -- (A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[-- (A ==> B) A |- (A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[-- (A ==> B) B |- (A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]

  #R[(A <=> B) A |- -- (A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[(A <=> B) B |- -- (A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[-- (A <=> B) A |- (A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[-- (A <=> B) B |- (A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]

  ; If A is a special case of B and B is a special case of C so is A a special case of C (strong) the other variations are hypotheses (weak)
  #R[(A --> B) (B --> C) |- (A --> C) :pre (#(not= A C)) :post (:t/deduction :d/strong :allow-backward)]
  #R[(A --> B) (A --> C) |- (C --> B) :pre (#(not= B C)) :post (:t/abduction :d/weak :allow-backward)]
  #R[(A --> C) (B --> C) |- (B --> A) :pre (#(not= A B)) :post (:t/induction :d/weak :allow-backward)]
  #R[(A --> B) (B --> C) |- (C --> A) :pre (#(not= C A)) :post (:t/exemplification :d/weak :allow-backward)]

  ; similarity from inheritance
  ; If S is a special case of P and P is a special case of S then S and P are similar
  #R[(S --> P) (P --> S) |- (S <-> P) :post (:t/intersection :d/strong :allow-backward)]

  ; inheritance from similarty <- TODO check why this one was missing
  #R[(S <-> P) (P --> S) |- (S --> P) :post (:t/reduce-conjunction :d/strong :allow-backward)]

  ; similarity-based syllogism
  ; If P and S are a special case of M then they might be similar (weak)
  ; also if P and S are a general case of M
  #R[(P --> M) (S --> M) |- (S <-> P) :pre (#(not= S P)) :post (:t/comparison :d/weak :allow-backward)]
  #R[(M --> P) (M --> S) |- (S <-> P) :pre (#(not= S P)) :post (:t/comparison :d/weak :allow-backward)]

  ; If M is a special case of P and S and M are similar then S is also a special case of P (strong)
  #R[(M --> P) (S <-> M) |- (S --> P) :pre (#(not= S P)) :post (:t/analogy :d/strong :allow-backward)]
  #R[(P --> M) (S <-> M) |- (P --> S) :pre (#(not= S P)) :post (:t/analogy :d/strong :allow-backward)]
  #R[(M <-> P) (S <-> M) |- (S <-> P) :pre (#(not= S P)) :post (:t/resemblance :d/strong :allow-backward)]

  ; inheritance-based composition
  ; If P and S are in the intension/extension of M then union/difference and intersection can be built:

  #R[(P --> M) (S --> M) |- (((S | P) --> M) :post (:t/intersection)
                              ((S & P) --> M) :post (:t/union)
                              ((P - S) --> M) :post (:t/difference))
     :pre ((not-set? S) (not-set? P) #(not= S P) (no-common-subterm S P))]

  #R[(M --> P) (M --> S) |- ((M --> (P & S)) :post (:t/intersection)
                              (M --> (P | S)) :post (:t/union)
                              (M --> (P ~S)) :post (:t/difference))
     :pre ((not-set? S) (not-set? P) #(not= S P) (no-common-subterm S P))]

  ; inheritance-based decomposition
  ; if (S --> M) is the case and ((| S :list/A) --> M) is not the case then ((| :list/A) --> M) is not the case hence :t/decompose-positive-negative-negative
  #R[(S --> M) ((| S :list/A) --> M) |- ((| :list/A) --> M) :post (:t/decompose-positive-negative-negative)]
  #R[(S --> M) ((& S :list/A) --> M) |- ((& :list/A) --> M) :post (:t/decompose-negative-positive-positive)]
  #R[(S --> M) ((S - P) --> M) |- (P --> M) :post (:t/decompose-positive-negative-positive)]
  #R[(S --> M) ((P - S) --> M) |- (P --> M) :post (:t/decompose-negative-negative-negative)]

  #R[(M --> S) (M --> (& S :list/A)) |- (M --> (& :list/A)) :post (:t/decompose-positive-negative-negative)]
  #R[(M --> S) (M --> (| S :list/A)) |- (M --> (| :list/A)) :post (:t/decompose-negative-positive-positive)]
  #R[(M --> S) (M --> (S ~ P)) |- (M --> P) :post (:t/decompose-positive-negative-positive)]
  #R[(M --> S) (M --> (P ~ S)) |- (M --> P) :post (:t/decompose-negative-negative-negative)]

  ; Set comprehension:

  #R[(C --> A) (C --> B) |- (C --> R) :pre [(set-ext? A) (union A B R)] :post (:t/union)]
  #R[(C --> A) (C --> B) |- (C --> R) :pre [(set-int? A) (union A B R)] :post (:t/intersection)]
  #R[(A --> C) (B --> C) |- (R --> C) :pre [(set-ext? A) (union A B R)] :post (:t/intersection)]
  #R[(A --> C) (B --> C) |- (R --> C) :pre [(set-int? A) (union A B R)] :post (:t/union)]

  #R[(C --> A) (C --> B) |- (C --> R) :pre [(set-ext? A) (intersection A B R)] :post (:t/intersection)]
  #R[(C --> A) (C --> B) |- (C --> R) :pre [(set-int? A) (intersection A B R)] :post (:t/union)]
  #R[(A --> C) (B --> C) |- (R --> C) :pre [(set-ext? A) (intersection A B R)] :post (:t/union)]
  #R[(A --> C) (B --> C) |- (R --> C) :pre [(set-int? A) (intersection A B R)] :post (:t/intersection)]

  #R[(C --> A) (C --> B) |- (C --> R) :pre [(difference A B R)] :post (:t/difference)]
  #R[(A --> C) (B --> C) |- (R --> C) :pre [(difference A B R)] :post (:t/difference)]

  ; Set element takeout:

  #R[(C --> #{:list/A}) C |- (C --> #{:from/A}) :post (:t/structural-deduction)]
  #R[(C --> [:list/A]) C |- (C --> [:from/A]) :post (:t/structural-deduction)]
  #R[(#{:list/A} --> C) C |- (#{:from/A} --> C) :post (:t/structural-deduction)]
  #R[([:list/A] --> C) C |- ([:from/A] --> C) :post (:t/structural-deduction)]

  ; NAL3 single premise inference:

  #R[((| :list/A) --> M) M |- (:from/A --> M) :post (:t/structural-deduction)]
  #R[(M --> (& :list/A)) M |- (M --> :from/A) :post (:t/structural-deduction)]

  #R[((B - G) --> S) S |- (B --> S) :post (:t/structural-deduction)]
  #R[(R --> (B ~ S)) R |- (R --> B) :post (:t/structural-deduction)]

  ; NAL4 - Transformations between products and images:
  ; Relations and transforming them into different representations so that arguments and the relation it'self can become the subject or predicate

  #R[((* :list/A) --> M) Ai |- (Ai --> (/ M :list/A))
     :pre ((substitute :listA Ai _))
     :post (:t/identity :d/identity)]
  #R[(M --> (* :list/A)) Ai |- ((\ M :list/A) --> Ai)
     :pre ((substitute :listA Ai _))
     :post (:t/identity :d/identity)]
  #R[(Ai --> (/ M :list/A )) M |- ((* :list/A) --> M)
     :pre ((substitute :listA _ Ai))
     :post (:t/identity :d/identity)]
  #R[((\ M :list/A) --> Ai) M |- (M --> (:list/A))
      :pre ((substitute :listA _ Ai))
     :post (:t/identity :d/identity)]

  ; implication-based syllogism
  #R[(M ==> P) (S ==> M) |- (S ==> P) :pre (#(not= S P)) :post (:t/deduction :order-for-all-same :allow-backward)]

  #R[(P ==> M) (S ==> M) |- (S ==> P) :pre (#(not= S P)) :post (:t/induction :allow-backward)]
  #R[(P =|> M) (S =|> M) |- (S =|> P) :pre (#(not= S P)) :post (:t/induction :allow-backward)]
  #R[(P =/> M) (S =/> M) |- (S =|> P) :pre (#(not= S P)) :post (:t/induction :allow-backward)]
  #R[(P =\> M) (S =\> M) |- (S =|> P) :pre (#(not= S P)) :post (:t/induction :allow-backward)]

  #R[(M ==> P) (M ==> S) |- (S ==> P) :pre (#(not= S P)) :post (:t/abduction :allow-backward)]
  #R[(M =/> P) (M =/> S) |- (S =|> P) :pre (#(not= S P)) :post (:t/abduction :allow-backward)]
  #R[(M =|> P) (M =|> S) |- (S =|> P) :pre (#(not= S P)) :post (:t/abduction :allow-backward)]
  #R[(M =\> P) (M =\> S) |- (S =|> P) :pre (#(not= S P)) :post (:t/abduction :allow-backward)]

  #R[(P ==> M) (M ==> S) |- (S ==> P) :pre (#(not= S P)) :post (:t/exemplification :allow-backward)]
  #R[(P =/> M) (M =/> S) |- (S =\> P) :pre (#(not= S P)) :post (:t/exemplification :allow-backward)]
  #R[(P =\> M) (M =\> S) |- (S =/> P) :pre (#(not= S P)) :post (:t/exemplification :allow-backward)]
  #R[(P =|> M) (M =|> S) |- (S =|> P) :pre (#(not= S P)) :post (:t/exemplification :allow-backward)]

  ; equivalence-based syllogism
  ; Same as for inheritance again

  #R[(P ==> M) (S ==> M) |- (S <=> P) :pre (#(not= S P)) :post [:t/comparison :allow-backward]]
  #R[(P =/> M) (S =/> M) |- ((S <|> P) :post [:t/comparison :allow-backward]
                              (S </> P) :post [:t/comparison :allow-backward]
                              (P </> S) :post [:t/comparison :allow-backward])
     :pre (#(not= S P))]
  #R[(P =|> M) (S =|> M) |- (S <|> P) :pre (#(not= S P)) :post [:t/comparison :allow-backward]]
  #R[(P =\> M) (S =\> M) |- ((S <|> P) :post [:t/comparison :allow-backward]
                              (S </> P) :post [:t/comparison :allow-backward]
                              (P </> S) :post [:t/comparison :allow-backward])
     :pre ((#(not= S P)))]

  #R[(M ==> P) (M ==> S) |- (S <=> P) :pre (#(not= S P)) :post [:t/comparison :allow-backward]]
  #R[(M =/> P) (M =/> S) |- ((S <|> P) :post [:t/comparison :allow-backward]
                              (S </> P) :post [:t/comparison :allow-backward]
                              (P </> S) :post [:t/comparison :allow-backward])
     :pre (#(not= S P))]
  #R[(M =|> P) (M =|> S) |- (S <|> P) :pre (#(not= S P)) :post [:t/comparison :allow-backward]]
;Same as for inheritance again
  #R[(P ==> M) (S ==> M) |- [((P || S) ==> M) :post (:t/int)
                             ((P && S) ==> M) :post (:t/union)]
     :pre (#(not= S P))])

(defn freq []
  "Check frequency"
  (into {} (map (fn [[k v]] [(str k) (count (:rules v))]) rules)))

(defn stats []
  (let [fr (freq)]
    (println "All:")
    (clojure.pprint/pprint fr)
    (println "Total" (reduce + (vals fr)))
    (println "Total keys" (count rules))
    (println "Freq" (sort (frequencies (vals fr))))
    (println "Min" (reduce min (vals fr)))
    (println "Max" (reduce max (vals fr)))))
