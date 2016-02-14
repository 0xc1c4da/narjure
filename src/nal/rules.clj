(ns nal.rules
  (:require [nal.deriver :refer [defrules rule] :as d]))

(declare --S S --P P <-> |- --> ==> M || && =|> =-> =+> -- A Ai B <=>)

(def default [:t/identity :d/identity :allow-backward])
(defrules rules
  ;Similarity to Inheritance
  [(S --> P) (S <-> P) |- (S --> P) :pre [:question?]
   :post [:t/struct-int :p/judgment]]
  ;Inheritance to Similarity
  [(S <-> P) (S --> P) |- (S <-> P) :pre [:question?]
   :post [:t/struct-abd :p/judgment]]
  ;Set Definition Similarity to Inheritance
  [(S <-> #{P}) S |- (S --> #{P}) :post default]
  [(S <-> #{P}) #{P} |- (S --> #{P}) :post default]
  [([S] <-> P) [S] |- ([S] --> P) :post default]
  [([S] <-> P) P |- ([S] --> P) :post default]
  [(#{S} <-> #{P}) #{S} |- (#{P} --> #{S}) :post default]
  [(#{S} <-> #{P}) #{P} |- (#{P} --> #{S}) :post default]
  [([S] <-> [P]) [S] |- ([P] --> [S]) :post default]
  [([S] <-> [P]) [P] |- ([P] --> [S]) :post default]

  ;Set Definition Unwrap
  [(#{S} <-> #{P}) #{S} |- (S <-> P) :post default]
  [(#{S} <-> #{P}) #{P} |- (S <-> P) :post default]
  [([S] <-> [P]) [S] |- (S <-> P) :post default]
  [([S] <-> [P]) [P] |- (S <-> P) :post default]

  ; Nothing is more specific than a instance so it's similar
  [(S --> #{P}) S |- (S <-> #{P}) :post default]
  [(S --> #{P}) #{P} |- (S <-> #{P}) :post default]

  ; nothing is more general than a property so it's similar
  [([S] --> P) [S] |- ([S] <-> P) :post default]
  [([S] --> P) P |- ([S] <-> P) :post default]

  ; Immediate Inference
  ; If S can stand for P P can to a certain low degree also represent the class S
  ; If after S usually P happens then it might be a good guess that usually before P happens S happens.
  [(P --> S) (S --> P) |- (P --> S) :pre [:question?] :post [:t/conversion :p/judgment]]
  [(P ==> S) (S ==> P) |- (P ==> S) :pre [:question?] :post [:t/conversion :p/judgment]]
  [(P =|> S) (S =|> P) |- (P =|> S) :pre [:question?] :post [:t/conversion :p/judgment]]
  [(P =-> S) (S =+> P) |- (P =-> S) :pre [:question?] :post [:t/conversion :p/judgment]]
  [(P =+> S) (S =-> P) |- (P =+> S) :pre [:question?] :post [:t/conversion :p/judgment]]

  ; "If not smoking lets you be healthy being not healthy may be the result of smoking"
  [(--S ==> P) P |- (--P ==> S) :post [:t/contraposition :allow-backward]]
  [(--S ==> P) --S |- (--P ==> S) :post [:t/contraposition :allow-backward]]
  [(--S =|> P) P |- (--P =|> S) :post [:t/contraposition :allow-backward]]
  [(--S =|> P) --S |- (--P =|> S) :post [:t/contraposition :allow-backward]]
  [(--S =+> P) P |- (--P =-> S) :post [:t/contraposition :allow-backward]]
  [(--S =+> P) --S |- (--P =-> S) :post [:t/contraposition :allow-backward]]
  [(--S =-> P) P |- (--P =+> S) :post [:t/contraposition :allow-backward]]
  [(--S =-> P) --S |- (--P =+> S) :post [:t/contraposition :allow-backward]]

  ; "If not smoking lets you be healthy being not healthy may be the result of smoking"
  [(--S ==> P) P |- (--P ==> S) :post [:t/contraposition :allow-backward]]
  [(--S ==> P) --S |- (--P ==> S) :post [:t/contraposition :allow-backward]]
  [(--S =|> P) P |- (--P =|> S) :post [:t/contraposition :allow-backward]]
  [(--S =|> P) --S |- (--P =|> S) :post [:t/contraposition :allow-backward]]
  [(--S =+> P) P |- (--P =-> S) :post [:t/contraposition :allow-backward]]
  [(--S =+> P) --S |- (--P =-> S) :post [:t/contraposition :allow-backward]]
  [(--S =-> P) P |- (--P =+> S) :post [:t/contraposition :allow-backward]]
  [(--S =-> P) --S |- (--P =+> S) :post [:t/contraposition :allow-backward]]

  ; A belief b <f c> is equal to --b <1-f c>  which is the negation rule:
  [(A --> B) A |- -- (A --> B) :post [:t/negation :d/negation :allow-backward]]
  [(A --> B) B |- -- (A --> B) :post [:t/negation :d/negation :allow-backward]]
  [-- (A --> B) A |- (A --> B) :post [:t/negation :d/negation :allow-backward]]
  [-- (A --> B) B |- (A --> B) :post [:t/negation :d/negation :allow-backward]]

  [(A <-> B) A |- -- (A <-> B) :post [:t/negation :d/negation :allow-backward]]
  [(A <-> B) B |- -- (A <-> B) :post [:t/negation :d/negation :allow-backward]]
  [-- (A <-> B) A |- (A <-> B) :post [:t/negation :d/negation :allow-backward]]
  [-- (A <-> B) B |- (A <-> B) :post [:t/negation :d/negation :allow-backward]]

  [(A ==> B) A |- -- (A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]
  [(A ==> B) B |- -- (A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]
  [-- (A ==> B) A |- (A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]
  [-- (A ==> B) B |- (A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]

  [(A <=> B) A |- -- (A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]
  [(A <=> B) B |- -- (A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]
  [-- (A <=> B) A |- (A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]
  [-- (A <=> B) B |- (A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]]

  ; If A is a special case of B and B is a special case of C so is A a special case of C (strong) the other variations are hypotheses (weak)
  [(A --> B) (B --> C) |- (A --> C) :pre [#(not= A C)] :post [:t/deduction :d/strong :allow-backward]]
  [(A --> B) (A --> C) |- (C --> B) :pre [#(not= B C)] :post [:t/abduction :d/weak :allow-backward]]
  [(A --> C) (B --> C) |- (B --> A) :pre [#(not= A B)] :post [:t/induction :d/weak :allow-backward]]
  [(A --> B) (B --> C) |- (C --> A) :pre [#(not= C A)] :post [:t/exemplification :d/weak :allow-backward]]

  ; similarity from inheritance
  ; If S is a special case of P and P is a special case of S then S and P are similar
  [(S --> P) (P --> S) |- (S <-> P) :post [:t/intersection :d/strong :allow-backward]]

  ; inheritance from similarty <- TODO check why this one was missing
  [(S <-> P) (P --> S) |- (S --> P) :post [:t/reduce-conjunction :d/strong :allow-backward]]

  ; similarity-based syllogism
  ; If P and S are a special case of M then they might be similar (weak)
  ; also if P and S are a general case of M
  [(P --> M) (S --> M) |- (S <-> P) :pre [#(not= S P)] :post [:t/comparison :d/weak :allow-backward]]
  [(M --> P) (M --> S) |- (S <-> P) :pre [#(not= S P)] :post [:t/comparison :d/weak :allow-backward]]

  ; If M is a special case of P and S and M are similar then S is also a special case of P (strong)
  [(M --> P) (S <-> M) |- (S --> P) :pre [#(not= S P)] :post [:t/analogy :d/strong :allow-backward]]
  [(P --> M) (S <-> M) |- (P --> S) :pre [#(not= S P)] :post [:t/analogy :d/strong :allow-backward]]
  [(M <-> P) (S <-> M) |- (S <-> P) :pre [#(not= S P)] :post [:t/resemblance :d/strong :allow-backward]]

  ; inheritance-based composition
  ; If P and S are in the intension/extension of M then union/difference and intersection can be built:

  [(P --> M) (S --> M) |- [((S | P) --> M) :post [:t/intersection]
                           ((S & P) --> M) :post [:t/union]
                           ((P -i S) --> M) :post [:t/difference]]
   :pre [(not-set? S) (not-set? P) #(not= S P) (no-common-subterm S P)]]

  [(M --> P) (M --> S) |- [(M --> (P & S)) :post [:t/intersection]
                           (M --> (P | S)) :post [:t/union]
                           (M --> (P -e S)) :post [:t/difference]]
   :pre [(not-set? S) (not-set? P) #(not= S P) (no-common-subterm S P)]]

  ; inheritance-based decomposition
  ; if (S --> M) is the case and ((| S :list/A) --> M) is not the case then ((| :list/A) --> M) is not the case hence :t/decompose-positive-negative-negative
  [(S --> M) ((| S :list/A) --> M) |- ((| :list/A) --> M) :post [:t/decompose-positive-negative-negative]]
  [(S --> M) ((& S :list/A) --> M) |- ((& :list/A) --> M) :post [:t/decompose-negative-positive-positive]]
  [(S --> M) ((S -i P) --> M) |- (P --> M) :post [:t/decompose-positive-negative-positive]]
  [(S --> M) ((P -i S) --> M) |- (P --> M) :post [:t/decompose-negative-negative-negative]]

  [(M --> S) (M --> (& S :list/A)) |- (M --> (& :list/A)) :post [:t/decompose-positive-negative-negative]]
  [(M --> S) (M --> (| S :list/A)) |- (M --> (| :list/A)) :post [:t/decompose-negative-positive-positive]]
  [(M --> S) (M --> (S -e P)) |- (M --> P) :post [:t/decompose-positive-negative-positive]]
  [(M --> S) (M --> (P -e S)) |- (M --> P) :post [:t/decompose-negative-negative-negative]]

  ; Set comprehension:

  [(C --> A) (C --> B) |- (C --> R) :pre [(set-ext? A) (union A B R)] :post [:t/union]]
  [(C --> A) (C --> B) |- (C --> R) :pre [(set-int? A) (union A B R)] :post [:t/intersection]]
  [(A --> C) (B --> C) |- (R --> C) :pre [(set-ext? A) (union A B R)] :post [:t/intersection]]
  [(A --> C) (B --> C) |- (R --> C) :pre [(set-int? A) (union A B R)] :post [:t/union]]

  [(C --> A) (C --> B) |- (C --> R) :pre [(set-ext? A) (intersection A B R)] :post [:t/intersection]]
  [(C --> A) (C --> B) |- (C --> R) :pre [(set-int? A) (intersection A B R)] :post [:t/union]]
  [(A --> C) (B --> C) |- (R --> C) :pre [(set-ext? A) (intersection A B R)] :post [:t/union]]
  [(A --> C) (B --> C) |- (R --> C) :pre [(set-int? A) (intersection A B R)] :post [:t/intersection]]

  [(C --> A) (C --> B) |- (C --> R) :pre [(difference A B R)] :post [:t/difference]]
  [(A --> C) (B --> C) |- (R --> C) :pre [(difference A B R)] :post [:t/difference]]

  ; Set element takeout:

  [(C --> #{:list/A}) C |- (C --> #{:from/A}) :post [:t/structural-deduction]]
  [(C --> [:list/A]) C |- (C --> [:from/A]) :post [:t/structural-deduction]]
  [(#{:list/A} --> C) C |- (#{:from/A} --> C) :post [:t/structural-deduction]]
  [([:list/A] --> C) C |- ([:from/A] --> C) :post [:t/structural-deduction]]

  ; NAL3 single premise inference:

  [((| :list/A) --> M) M |- (:from/A --> M) :post [:t/structural-deduction]]
  [(M --> (& :list/A)) M |- (M --> :from/A) :post [:t/structural-deduction]]

  [((B -i G) --> S) S |- (B --> S) :post [:t/structural-deduction]]
  [(R --> (B -e S)) R |- (R --> B) :post [:t/structural-deduction]]

  ;Same as for inheritance again
  [(P ==> M) (S ==> M) |- [((P || S) ==> M) :post [:t/int]
                           ((P && S) ==> M) :post [:t/union]]
   :pre [#(not= S P)]])

(defn freq []
  "Check frequency"
  (into {} (map (fn [[k v]] [k (count (:rules v))]) rules)))

(defn stats []
  (let [fr (freq)]
    (println "Freq:")
    (clojure.pprint/pprint fr)
    (println "Total" (reduce + (vals fr)))
    (println "Min" (reduce min (vals fr)))
    (println "Max" (reduce max (vals fr)))))
