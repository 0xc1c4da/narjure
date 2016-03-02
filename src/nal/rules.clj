(ns nal.rules
  (:require [nal.deriver.rules :refer [defrules]]
            nal.reader))

(declare --S S --P P <-> |- --> ==> M || && =|> -- A Ai B <=>)

(defrules rules
  ;Similarity to Inheritance
  #R[(S --> P) (S <-> P) |- (S --> P) :post (:t/struct-int :p/judgment) :pre (:question?)]
  ;Inheritance to Similarity
  #R[(S <-> P) (S --> P) |- (S <-> P) :post (:t/struct-abd :p/judgment) :pre (:question?)]
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
  #R[(P --> S) (S --> P) |- (P --> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P ==> S) (S ==> P) |- (P ==> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P =|> S) (S =|> P) |- (P =|> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P =\> S) (S =/> P) |- (P =\> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P =/> S) (S =\> P) |- (P =/> S) :post (:t/conversion :p/judgment) :pre (:question?)]

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
  #R[(A --> B) A |- --(A --> B) :post (:t/negation :d/negation :allow-backward)]
  #R[(A --> B) B |- --(A --> B) :post (:t/negation :d/negation :allow-backward)]
  #R[--(A --> B) A |- (A --> B) :post (:t/negation :d/negation :allow-backward)]
  #R[--(A --> B) B |- (A --> B) :post (:t/negation :d/negation :allow-backward)]

  #R[(A <-> B) A |- --(A <-> B) :post (:t/negation :d/negation :allow-backward)]
  #R[(A <-> B) B |- --(A <-> B) :post (:t/negation :d/negation :allow-backward)]
  #R[--(A <-> B) A |- (A <-> B) :post (:t/negation :d/negation :allow-backward)]
  #R[--(A <-> B) B |- (A <-> B) :post (:t/negation :d/negation :allow-backward)]

  #R[(A ==> B) A |- --(A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[(A ==> B) B |- --(A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[--(A ==> B) A |- (A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[--(A ==> B) B |- (A ==> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]

  #R[(A <=> B) A |- --(A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[(A <=> B) B |- --(A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[--(A <=> B) A |- (A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]
  #R[--(A <=> B) B |- (A <=> B) :post (:t/negation :d/negation :allow-backward :order-for-all-same)]

  ; If A is a special case of B and B is a special case of C so is A a special case of C (strong) the other variations are hypotheses (weak)
  #R[(A --> B) (B --> C) |- (A --> C) :pre ((:!= A C)) :post (:t/deduction :d/strong :allow-backward)]
  #R[(A --> B) (A --> C) |- (C --> B) :pre ((:!= B C)) :post (:t/abduction :d/weak :allow-backward)]
  #R[(A --> C) (B --> C) |- (B --> A) :pre ((:!= A B)) :post (:t/induction :d/weak :allow-backward)]
  #R[(A --> B) (B --> C) |- (C --> A) :pre ((:!= C A)) :post (:t/exemplification :d/weak :allow-backward)]

  ; similarity from inheritance
  ; If S is a special case of P and P is a special case of S then S and P are similar
  #R[(S --> P) (P --> S) |- (S <-> P) :post (:t/intersection :d/strong :allow-backward)]

  ; inheritance from similarty <- TODO check why this one was missing
  #R[(S <-> P) (P --> S) |- (S --> P) :post (:t/reduce-conjunction :d/strong :allow-backward)]

  ; similarity-based syllogism
  ; If P and S are a special case of M then they might be similar (weak)
  ; also if P and S are a general case of M
  #R[(P --> M) (S --> M) |- (S <-> P) :post (:t/comparison :d/weak :allow-backward) :pre ((:!= S P))]
  #R[(M --> P) (M --> S) |- (S <-> P) :post (:t/comparison :d/weak :allow-backward) :pre ((:!= S P))]

  ; If M is a special case of P and S and M are similar then S is also a special case of P (strong)
  #R[(M --> P) (S <-> M) |- (S --> P) :pre ((:!= S P)) :post (:t/analogy :d/strong :allow-backward)]
  #R[(P --> M) (S <-> M) |- (P --> S) :pre ((:!= S P)) :post (:t/analogy :d/strong :allow-backward)]
  #R[(M <-> P) (S <-> M) |- (S <-> P) :pre ((:!= S P)) :post (:t/resemblance :d/strong :allow-backward)]

  ; inheritance-based composition
  ; If P and S are in the intension/extension of M then union/difference and intersection can be built:
  #R[(P --> M) (S --> M) |- (((S | P) --> M) :post (:t/intersection)
                              ((S & P) --> M) :post (:t/union)
                              ((P ~ S) --> M) :post (:t/difference))
     :pre ((:not-set? S) (:not-set? P)(:!= S P) (:no-common-subterm S P))]

  #R[(M --> P) (M --> S) |- ((M --> (P & S)) :post (:t/intersection)
                              (M --> (P | S)) :post (:t/union)
                              (M --> (P - S)) :post (:t/difference))
     :pre ((:not-set? S) (:not-set? P)(:!= S P) (:no-common-subterm S P))]

  ; inheritance-based decomposition
  ; if (S --> M) is the case and ((| S :list/A) --> M) is not the case then ((| :list/A) --> M) is not the case hence :t/decompose-pnn
  #R[(S --> M) ((| S :list/A) --> M) |- ((| :list/A) --> M) :post (:t/decompose-pnn)]
  #R[(S --> M) ((& S :list/A) --> M) |- ((& :list/A) --> M) :post (:t/decompose-npp)]
  #R[(S --> M) ((S - P) --> M) |- (P --> M) :post (:t/decompose-positive-negative-positive)]
  #R[(S --> M) ((P - S) --> M) |- (P --> M) :post (:t/decompose-nnn)]

  #R[(M --> S) (M --> (& S :list/A)) |- (M --> (& :list/A)) :post (:t/decompose-pnn)]
  #R[(M --> S) (M --> (| S :list/A)) |- (M --> (| :list/A)) :post (:t/decompose-npp)]
  #R[(M --> S) (M --> (S ~ P)) |- (M --> P) :post (:t/decompose-positive-negative-positive)]
  #R[(M --> S) (M --> (P ~ S)) |- (M --> P) :post (:t/decompose-nnn)]

  ; Set comprehension:
  #R[(C --> A) (C --> B) |- (C --> R) :post (:t/union) :pre ((:set-ext? A) (:union A B R))]
  #R[(C --> A) (C --> B) |- (C --> R) :post (:t/intersection) :pre ((:set-int? A) (:union A B R))]
  #R[(A --> C) (B --> C) |- (R --> C) :post (:t/intersection) :pre ((:set-ext? A) (:union A B R))]
  #R[(A --> C) (B --> C) |- (R --> C) :post (:t/union) :pre ((:set-int? A) (:union A B R))]

  #R[(C --> A) (C --> B) |- (C --> R) :post (:t/intersection) :pre ((:set-ext? A) (:intersection A B R))]
  #R[(C --> A) (C --> B) |- (C --> R) :post (:t/union) :pre ((:set-int? A) (:intersection A B R))]
  #R[(A --> C) (B --> C) |- (R --> C) :post (:t/union) :pre ((:set-ext? A) (:intersection A B R))]
  #R[(A --> C) (B --> C) |- (R --> C) :post (:t/intersection) :pre ((:set-int? A) (:intersection A B R))]

  #R[(C --> A) (C --> B) |- (C --> R) :post (:t/difference) :pre ((:difference A B R))]
  #R[(A --> C) (B --> C) |- (R --> C) :post (:t/difference) :pre ((:difference A B R))]

  ; Set element takeout:
  #R[(C --> {:list/A}) C |- (C --> {:from/A}) :post (:t/structural-deduction)]
  #R[(C --> [:list/A]) C |- (C --> [:from/A]) :post (:t/structural-deduction)]
  #R[({:list/A} --> C) C |- ({:from/A} --> C) :post (:t/structural-deduction)]
  #R[([:list/A] --> C) C |- ([:from/A] --> C) :post (:t/structural-deduction)]

  ; NAL3 single premise inference:
  #R[((| :list/A) --> M) M |- (:from/A --> M) :post (:t/structural-deduction)]
  #R[(M --> (& :list/A)) M |- (M --> :from/A) :post (:t/structural-deduction)]

  #R[((B - G) --> S) S |- (B --> S) :post (:t/structural-deduction)]
  #R[(R --> (B ~ S)) R |- (R --> B) :post (:t/structural-deduction)]

  ; NAL4 - Transformations between products and images:
  ; Relations and transforming them into different representations so that arguments and the relation it'self can become the subject or predicate
  #R[((* :list/A) --> M) Ai |- (Ai --> (/ M :list/A))
     :pre ((:substitute :listA Ai _))
     :post (:t/identity :d/identity)]
  #R[(M --> (* :list/A)) Ai |- ((\ M :list/A) --> Ai)
     :pre ((:substitute :listA Ai _))
     :post (:t/identity :d/identity)]
  #R[(Ai --> (/ M :list/A )) M |- ((* :list/A) --> M)
     :pre ((:substitute :listA _ Ai))
     :post (:t/identity :d/identity)]
  #R[((\ M :list/A) --> Ai) M |- (M --> (:list/A))
      :pre ((:substitute :listA _ Ai))
     :post (:t/identity :d/identity)]

  ; implication-based syllogism
  #R[(M ==> P) (S ==> M) |- (S ==> P) :post (:t/deduction :order-for-all-same :allow-backward) :pre ((:!= S P))]

  #R[(P ==> M) (S ==> M) |- (S ==> P) :post (:t/induction :allow-backward) :pre ((:!= S P))]
  #R[(P =|> M) (S =|> M) |- (S =|> P) :post (:t/induction :allow-backward) :pre ((:!= S P))]
  #R[(P =/> M) (S =/> M) |- (S =|> P) :post (:t/induction :allow-backward) :pre ((:!= S P))]
  #R[(P =\> M) (S =\> M) |- (S =|> P) :post (:t/induction :allow-backward) :pre ((:!= S P))]

  #R[(M ==> P) (M ==> S) |- (S ==> P) :post (:t/abduction :allow-backward) :pre ((:!= S P))]
  #R[(M =/> P) (M =/> S) |- (S =|> P) :post (:t/abduction :allow-backward) :pre ((:!= S P))]
  #R[(M =|> P) (M =|> S) |- (S =|> P) :post (:t/abduction :allow-backward) :pre ((:!= S P))]
  #R[(M =\> P) (M =\> S) |- (S =|> P) :post (:t/abduction :allow-backward) :pre ((:!= S P))]

  #R[(P ==> M) (M ==> S) |- (S ==> P) :post (:t/exemplification :allow-backward) :pre ((:!= S P))]
  #R[(P =/> M) (M =/> S) |- (S =\> P) :post (:t/exemplification :allow-backward) :pre ((:!= S P))]
  #R[(P =\> M) (M =\> S) |- (S =/> P) :post (:t/exemplification :allow-backward) :pre ((:!= S P))]
  #R[(P =|> M) (M =|> S) |- (S =|> P) :post (:t/exemplification :allow-backward) :pre ((:!= S P))]

  ; equivalence-based syllogism
  ; Same as for inheritance again
  #R[(P ==> M) (S ==> M) |- (S <=> P) :pre ((:!= S P)) :post (:t/comparison :allow-backward)]
  #R[(P =/> M) (S =/> M) |- ((S <|> P) :post (:t/comparison :allow-backward)
                              (S </> P) :post (:t/comparison :allow-backward)
                              (P </> S) :post (:t/comparison :allow-backward))
     :pre ((:!= S P))]
  #R[(P =|> M) (S =|> M) |- (S <|> P) :pre ((:!= S P)) :post (:t/comparison :allow-backward)]
  #R[(P =\> M) (S =\> M) |- ((S <|> P) :post (:t/comparison :allow-backward)
                              (S </> P) :post (:t/comparison :allow-backward)
                              (P </> S) :post (:t/comparison :allow-backward))
     :pre ((:!= S P))]

  #R[(M ==> P) (M ==> S) |- (S <=> P) :pre ((:!= S P)) :post (:t/comparison :allow-backward)]
  #R[(M =/> P) (M =/> S) |- ((S <|> P) :post (:t/comparison :allow-backward)
                              (S </> P) :post (:t/comparison :allow-backward)
                              (P </> S) :post (:t/comparison :allow-backward))
     :pre ((:!= S P))]
  #R[(M =|> P) (M =|> S) |- (S <|> P) :pre ((:!= S P)) :post (:t/comparison :allow-backward)]

  ; Same as for inheritance again
  #R[(M ==> P) (S <=> M) |- (S ==> P) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(M =/> P) (S </> M) |- (S =/> P) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(M =/> P) (S <|> M) |- (S =/> P) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(M =|> P) (S <|> M) |- (S =|> P) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(M =\> P) (M </> S) |- (S =\> P) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(M =\> P) (S <|> M) |- (S =\> P) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]

  #R[(P ==> M) (S <=> M) |- (P ==> S) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(P =/> M) (S <|> M) |- (P =/> S) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(P =|> M) (S <|> M) |- (P =|> S) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(P =\> M) (S </> M) |- (P =\> S) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]
  #R[(P =\> M) (S <|> M) |- (P =\> S) :pre ((:!= S P)) :post (:t/analogy :allow-backward)]

  #R[(M <=> P) (S <=> M) |- (S <=> P) :pre ((:!= S P)) :post (:t/resemblance :order-for-all-same :allow-backward)]
  #R[(M </> P) (S <|> M) |- (S </> P) :pre ((:!= S P)) :post (:t/resemblance :allow-backward)]
  #R[(M <|> P) (S </> M) |- (S </> P) :pre ((:!= S P)) :post (:t/resemblance :allow-backward)]

  ; implication-based composition
  ; Same as for inheritance again
  #R[(P ==> M) (S ==> M) |- (((P || S) ==> M) :post (:t/intersection)
                              ((P && S) ==> M) :post (:t/union))
     :pre ((:!= S P))]
  #R[(P =|> M) (S =|> M) |- (((P || S) =|> M) :post (:t/intersection)
                              ((P &| S) =|> M) :post (:t/union))
     :pre ((:!= S P))]
  #R[(P =/> M) (S =/> M) |- (((P || S) =/> M) :post (:t/intersection)
                              ((P &| S) =/> M) :post (:t/union))
     :pre ((:!= S P)) ]
  #R[(P =\> M) (S =\> M) |- (((P || S) =\> M) :post (:t/intersection)
                                ((P &| S) =\> M) :post (:t/union))
     :pre ((:!= S P))]

  #R[(M ==> P) (M ==> S) |- ((M ==> (P && S)) :post (:t/intersection)
                              (M ==> (P || S)) :post (:t/union))
     :pre ((:!= S P))]
  #R[(M =/> P) (M =/> S) |- ((M =/> (P &| S)) :post (:t/intersection)
                              (M =/> (P || S)) :post (:t/union))
     :pre ((:!= S P))]
  #R[(M =|> P) (M =|> S) |- ((M =|> (P &| S)) :post (:t/intersection)
                              (M =|> (P || S)) :post (:t/union))
     :pre ((:!= S P))]
  #R[(M =\> P) (M =\> S) |- ((M =\> (P &| S)) :post (:t/intersection)
                              (M =\> (P || S)) :post (:t/union))
     :pre ((:!= S P))]

  #R[(D =/> R) (D =\> K) |- ((K =/> R) :post (:t/abduction)
                              (R =\> K) :post (:t/induction)
                              (K </> R) :post (:t/comparison))
     :pre ((:!= R K))]

  ; implication-based decomposition
  ; Same as for inheritance again
  #R[(S ==> M) ((|| S :list/A) ==> M) |- ((|| :list/A) ==> M) :post (:t/decompose-pnn :order-for-all-same)]
  #R[(S ==> M) ((&& S :list/A) ==> M) |- ((&& :list/A) ==> M) :post (:t/decompose-npp :order-for-all-same :seq-interval-from-premises)]
  #R[(M ==> S) (M ==> (&& S :list/A)) |- (M ==> (&& :list/A)) :post (:t/decompose-pnn :order-for-all-same :seq-interval-from-premises)]
  #R[(M ==> S) (M ==> (|| S :list/A)) |- (M ==> (|| :list/A)) :post (:t/decompose-npp :order-for-all-same)]

  ; conditional syllogism
  ; If after M P usually happens and M happens it means P is expected to happen
  #R[M (M ==> P) |- P :post (:t/deduction :d/induction :order-for-all-same) :pre ((:shift-occurrence-forward unused ==>))]
  #R[M (P ==> M) |- P :post (:t/abduction :d/deduction :order-for-all-same) :pre ((:shift-occurrence-backward unused ==>))]
  #R[M (S <=> M) |- S :post (:t/analogy :d/strong :order-for-all-same) :pre ((:shift-occurrence-backward unused <=>))]
  #R[M (M <=> S) |- S :post (:t/analogy :d/strong :order-for-all-same) :pre ((:shift-occurrence-forward unused ==>))]

  ; conjunction decompose
  #R[(&& :list/A) A_1 |- A_1 :post (:t/structural-deduction :d/structural-strong)]
  #R[(&/ :list/A) A_1 |- A_1 :post (:t/structural-deduction :d/structural-strong)]
  #R[(&| :list/A) A_1 |- A_1 :post (:t/structural-deduction :d/structural-strong)]
  #R[(&/ B :list/A) B |- (&/ :list/A) :pre (:goal?) :post (:t/deduction :d/strong :seq-interval-from-premises)]

  ; propositional decomposition
  ; If S is the case and (&& S :list/A) is not the case it can't be that (&& :list/A) is the case
  #R[S (&/ S :list/A) |- (&/ :list/A) :post (:t/decompose-pnn :seq-interval-from-premises)]
  #R[S (&| S :list/A) |- (&| :list/A) :post (:t/decompose-pnn)]
  #R[S (&& S :list/A) |- (&& :list/A) :post (:t/decompose-pnn)]
  #R[S (|| S :list/A) |- (|| :list/A) :post (:t/decompose-npp)]

  ; Additional for negation: https://groups.google.com/forum/#!topic/open-nars/g-7r0jjq2Vc
  #R[S (&/ (-- S) :list/A) |- (&/ :list/A) :post (:t/decompose-nnn :seq-interval-from-premises)]
  #R[S (&| (-- S) :list/A) |- (&| :list/A) :post (:t/decompose-nnn)]
  #R[S (&& (-- S) :list/A) |- (&& :list/A) :post (:t/decompose-nnn)]
  #R[S (|| (-- S) :list/A) |- (|| :list/A) :post (:t/decompose-ppp)]

  ; multi-conditional syllogism
  ; Inference about the pre/postconditions
  #R[Y ((&& X :list/A) ==> B) |- ((&& :list/A) ==> B) :pre ((:substitute-if-unifies "$" X Y)) :post (:t/deduction :order-for-all-same :seq-interval-from-premises)]
  #R[((&& M :list/A) ==> C) ((&& :list/A) ==> C) |- M :post (:t/abduction :order-for-all-same)]

  ; Can be derived by NAL7 rules so this won't be necessary there (:order-for-all-same left out here)
  ; the first rule does not have :order-for-all-same because it would be invalid see: https://groups.google.com/forum/#!topic/open-nars/r5UJo64Qhrk
  #R[((&& :list/A) ==> C) M |- ((&& M :list/A) ==> C) :pre ((:not-implication-or-equivalence M)) :post (:t/induction)]
  #R[((&& :list/A) =|> C) M |- ((&& M :list/A) =|> C) :pre ((:not-implication-or-equivalence M)) :post (:t/induction)]
  #R[((&& :list/A) =/> C) M |- ((&& M :list/A) =/> C) :pre ((:not-implication-or-equivalence M)) :post (:t/induction)]
  #R[((&& :list/A) =\> C) M |- ((&& M :list/A) =\> C) :pre ((:not-implication-or-equivalence M)) :post (:t/induction)]
  #R[(A ==> M) ((&& M :list/A) ==> C) |- ((&& A :list/A) ==> C) :post (:t/deduction :order-for-all-same :seq-interval-from-premises)]
  #R[((&& M :list/A) ==> C) ((&& A :list/A) ==> C) |- (A ==> M) :post (:t/induction :order-for-all-same)]
  #R[(A ==> M) ((&& A :list/A) ==> C) |- ((&& M :list/A) ==> C) :post (:t/abduction :order-for-all-same :seq-interval-from-premises)]

  ; variable introduction
  ; Introduce variables by common subject or predicate
  #R[(S --> M) (P --> M) |- (((P --> $X) ==> (S --> $X)) :post (:t/abduction)
                              ;TODO is this conclusion necessary?
                              ((S --> $X) ==> (P --> $X)) :post (:t/induction)
                              ((P --> $X) <=> (S --> $X)) :post (:t/comparison)
                              (&& (S --> #Y) (P --> #Y)) :post (:t/intersection))
                                  :pre ((:!= S P))]

 #_#R[(S --> M) (P --> M) |- (((&/ (P --> $X) I) =/> (S --> $X)) :post (:t/induction :linkage-temporal)
                              ((S --> $X) =\> (&/ (P --> $X) I)) :post (:t/abduction :linkage-temporal)
                              ((&/ (P --> $X) I) </> (S --> $X)) :post (:t/comparison :linkage-temporal)
                              (&/ (P --> #Y) I (S --> #Y)) :post (:t/intersection :linkage-temporal))
            :pre ((:!= S P) (:measure-time I))]

  #_#R[(S --> M) (P --> M) |- (((P --> $X) =|> (S --> $X)) :post (:t/abduction :linkage-temporal)
                              ((S --> $X) =|> (P --> $X)) :post (:t/induction :linkage-temporal)
                              ((P --> $X) <|> (S --> $X)) :post (:t/comparison :linkage-temporal)
                              (&| (P --> #Y) (S --> #Y)) :post (:t/intersection :linkage-temporal))
            :pre ((:!= S P) (concurrent Task Belief))]

  #R[(M --> S) (M --> P) |- ((($X --> S) ==> ($X --> P)) :post (:t/induction)
                              (($X --> P) ==> ($X --> S)) :post (:t/abduction)
                              (($X --> S) <=> ($X --> P)) :post (:t/comparison)
                              (&& (#Y --> S) (#Y --> P)) :post (:t/intersection))
     :pre ((:!= S P)) ]

  #_#R[(M --> S) (M --> P) |- (((&/ ($X --> P) I) =/> ($X --> S))  :post (:t/induction :linkage-temporal)
                              (($X --> S) =\> (&/ ($X --> P) I)) :post (:t/abduction :linkage-temporal)
                              ((&/ ($X --> P) I) </> ($X --> S)) :post (:t/comparison :linkage-temporal)
                              (&/ (#Y --> P) I (#Y --> S)) :post (:t/intersection :linkage-temporal))
     :pre ((:!= S P) (:measure-time I))]

  #R[(M --> S) (M --> P) |- ((($X --> S) =|> ($X --> P)) :post (:t/induction :linkage-temporal)
                              (($X --> P) =|> ($X --> S)) :post (:t/abduction :linkage-temporal)
                              (($X --> S) <|> ($X --> P)) :post (:t/comparison :linkage-temporal)
                              (&| (#Y --> S) (#Y --> P)) :post (:t/intersection :linkage-temporal))
     :pre ((:!= S P) (:concurrent (M --> P) (M --> S)))]

  ; 2nd variable introduction
  #R[(A ==> (M --> P)) (M --> S) |- (((&& A ($X --> S)) ==> ($X --> P)) :post (:t/induction)
                                      (&& (A ==> (#Y --> P)) (#Y --> S)) :post (:t/intersection))
     :pre ((:!= A (M --> S)))]

  #R[(&& (M --> P) :list/A) (M --> S) |- ((($Y --> S) ==> (&& ($Y --> P) :list/A)) :post (:t/induction)
                                           (&& (#Y --> S) (#Y --> P) :list/A) :post (:t/intersection))
     :pre ((:!= S P))]

  #R[(A ==> (P --> M)) (S --> M) |- (((&& A (P --> $X)) ==> (S --> $X)) :post (:t/abduction)
                                       (&& (A ==> (P --> #Y)) (S --> #Y)) :post (:t/intersection)) ]

  #R[(&& (P --> M) :list/A) (S --> M) |- (((S --> $Y) ==> (&& (P --> $Y) :list/A))  :post (:t/abduction)
                                            (&&  (S --> #Y) (P --> #Y) :list/A) :post (:t/intersection))
                                                 :pre ((:!= S P))]

  #R[(A --> L) ((A --> S) ==> R) |- ((&& (#X --> L) (#X --> S)) ==> R) :post (:t/induction)]
  #R[(A --> L) ((&& (A --> S) :list/A) ==> R) |- ((&& (#X --> L) (#X --> S) :list/A) ==> R) :pre ((:substitute A #X)) :post (:t/induction)]

  ; dependent variable elimination
  ; Decomposition with elimination of a variable
  #_#R[B (&& A :list/A) |- (&& :list/A) :pre (:judgement? (:substitute-if-unifies "#" A B)) :post (:t/anonymous-analogy :d/strong :order-for-all-same :seq-interval-from-premises)]

  ; conditional abduction by dependent variable
  #R[((A --> R) ==> Z) ((&& (#Y --> B) (#Y --> R) :list/A) ==> Z) |- (A --> B) :post (:t/abduction)]
  #R[((A --> R) ==> Z) ((&& (#Y --> B) (#Y --> R)) ==> Z) |- (A --> B) :post (:t/abduction)]

  ; conditional deduction "An inverse inference has been implemented as a form of deduction" https://code.google.com/p/open-nars/issues/detail?id=40&can=1
  #R[(U --> L) ((&& (#X --> L) (#X --> R)) ==> Z) |- ((U --> R) ==> Z) :post (:t/deduction)]
  #R[(U --> L) ((&& (#X --> L) (#X --> R) :list/A) ==> Z) |- ((&& (U --> R) :list/A) ==> Z) :pre ((:substitute #X U)) :post (:t/deduction)]


  ; independent variable elimination
  #_#R[B (A ==> C) |- C (:t/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-forward unused ==>))]
  #_#R[B (C ==> A) |- C (:t/abduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-backward unused ==>))]

  #_#R[B (A <=> C) |- C (:t/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-backward unused <=>))]
  #_#R[B (C <=> A) |- C (:t/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-forward unused <=>))]

  ; second level variable handling rules
  ; second level variable elimination (termlink level2 growth needed in order for these rules to work)
  #R[(A --> K) (&& (#X --> L) (($Y --> K) ==> (&& :list/A))) |- (&& (#X --> L) :list/A) :pre ((:substitute $Y A)) :post (:t/deduction)]
  #R[(A --> K) (($X --> L) ==> (&& (#Y --> K) :list/A)) |- (($X --> L) ==> (&& :list/A)) :pre ((:substitute #Y A)) :post (:t/anonymous-analogy)]

  ; precondition combiner inference rule (variable_unification6):
  #_#R[((&& C :list/A) ==> Z) ((&& C :list/B) ==> Z) |- (((&& :list/A) ==> (&& :list/B)) :post (:t/induction)
                                                        ((&& :list/B) ==> (&& :list/A)) :post (:t/induction))]
  #_#R[(Z ==> (&& C :list/A)) (Z ==> (&& C :list/B)) |- (((&& :list/A) ==> (&& :list/B)) :post (:t/abduction)
                                                      ((&& :list/B) ==> (&& :list/A)) :post (:t/abduction))]

  ; NAL7 specific inference
  ; Reasoning about temporal statements. those are using the ==> relation because relation in time is a relation of the truth between statements.
  #_#R[X (XI ==> B) |- B  :post (:t/deduction :d/induction :order-for-all-same) :pre ((:substitute-if-unifies "$" XI (&/ X /0)) (:shift-occurrence-forward XI ==>))]
  #_#R[X (BI ==> Y) |- BI :post (:t/abduction :d/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" Y X) (:shift-occurrence-backward BI ==>))]

  ; Temporal induction:
  ; When P and then S happened according to an observation by induction (weak) it may be that alyways after P usually S happens.
  #_#R[P S |- (((&/ S I) =/> P) :post (:t/induction :linkage-temporal)
              (P =\> (&/ S I)) :post (:t/abduction :linkage-temporal)
              ((&/ S I) </> P) :post (:t/comparison :linkage-temporal)
              (&/ S I P) :post (:t/intersection :linkage-temporal))
     :pre ((:measure-time I))]
  #_#R[P S |- ((S =|> P) :post (:t/induction :linkage-temporal)
              (P =|> S) :post (:t/induction :linkage-temporal)
              (S <|> P) :post (:t/comparison :linkage-temporal)
              (&| S P) :post (:t/intersection :linkage-temporal))
     :pre [(:concurrent Task Belief) (:not-implication-or-equivalence P) (:not-implication-or-equivalence S)]]

  ; here now are the backward inference rules which should really only work on backward inference:
  #R[(A --> S) (B --> S) |- ((A --> B) :post (:p/question)
                              (B --> A) :post (:p/question)
                              (A <-> B) :post (:p/question))
     :pre (:question?)]

  ; and the backward inference driven forward inference:
  ; NAL2:
  #R[([A] <-> [B]) (A <-> B) |- ([A] <-> [B]) :pre (:question?) :post (:t/belief-identity :p/judgment)]
  #R[({A} <-> {B}) (A <-> B) |- ({A} <-> {B}) :pre (:question?) :post (:t/belief-identity :p/judgment)]

  #R[([A] --> [B]) (A <-> B) |- ([A] --> [B]) :pre (:question?) :post (:t/belief-identity :p/judgment)]
  #R[({A} --> {B}) (A <-> B) |- ({A} --> {B}) :pre (:question?) :post (:t/belief-identity :p/judgment)]

  ; NAL3:
  ; composition on both sides of a statement:
  #R[((& B :list/A) --> (& A :list/A)) (B --> A) |- ((& B :list/A) --> (& A :list/A)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((| B :list/A) --> (| A :list/A)) (B --> A) |- ((| B :list/A) --> (| A :list/A)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((- S A) --> (- S B)) (B --> A) |- ((- S A) --> (- S B)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((~ S A) --> (~ S B)) (B --> A) |- ((~ S A) --> (~ S B)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]

  ; composition on one side of a statement:
  #R[(W --> (| B :list/A)) (W --> B) |- (W --> (| B :list/A)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((& B :list/A) --> W) (B --> W) |- ((& B :list/A) --> W) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[(W --> (- S B)) (W --> B) |- (W --> (- S B)) :pre (:question?) :post (:t/belief-structural-difference :p/judgment)]
  #R[((~ S B) --> W) (B --> W) |- ((~ S B) --> W) :pre (:question?) :post (:t/belief-structural-difference :p/judgment)]

  ; NAL4:
  ; composition on both sides of a statement:
  #R[((* B P) --> Z) (B --> A) |- ((* B P) --> (* A P)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((* P B) --> Z) (B --> A) |- ((* P B) --> (* P A)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((* B P) <-> Z) (B <-> A) |- ((* B P) <-> (* A P)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((* P B) <-> Z) (B <-> A) |- ((* P B) <-> (* P A)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((\ N A _) --> Z) (N --> R) |- ((\ N A _) --> (\ R A _)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
  #R[((/ N _ B) --> Z) (S --> B) |- ((/ N _ B) --> (/ N _ S)) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]

  ; NAL5:
  #R[--A    A |- --A  :pre (:question?) :post (:t/belief-negation :p/judgment)]
  #R[A  --A |-   A  :pre (:question?) :post (:t/belief-negation :p/judgment)]

  ; compound composition one premise
  #R[(|| B :list/A) B |- (|| B :list/A) :pre (:question?) :post (:t/belief-structural-deduction :p/judgment)]
)

(defn freq [task-type]
  "Check frequency"
  (into {} (map (fn [[k v]] [(str k) (count (:rules v))]) (task-type rules))))

(defn stats [task-type]
  (let [fr (freq task-type)]
    (println "Total" (reduce + (vals fr)))
    (println "Total keys" (count (task-type rules)))
    (println "Freq" (sort (frequencies (vals fr))))
    (println "Min" (reduce min (vals fr)))
    (println "Max" (reduce (fn [[_ v1 :as p] [_ v :as n]]
                             (if (> v1 v) p n)) fr))))
