

    ; Pei Wang's "Non-Axiomatic Logic" specified with a math. notation inspired DSL with given intiutive explainations:

    ; The rules of NAL can be interpreted by considering the intiution behind the following two relations:
    ; Statement:                 (A --> B):               A can stand for B
    ; Statement about Statement: (A ==> B): If A is true so is/will be B
    ; --> is a relation in meaning of terms while ==> is a relation of truth between statements.

    ; Revision 
    ; When a given belief is challenged by new experience a new belief2 with same content (and disjoint evidental base) 
    ; a new revised task which sums up the evidence of both belief and belief2 is derived:

    ; A A |- A  [:t/revision] (Commented out because it is already handled by belief management in java)

    ; Similarity to Inheritance

    (S --> P) (S <-> P) |- (S --> P) :pre [:question?] :post [:t/structural-intersection :p/belief]

    ; Inheritance to Similarity

    (S <-> P) (S --> P) |- (S <-> P) :pre [:question?] :post [:t/structural-abduction :p/belief]

    ; Set Definition Similarity to Inheritance

    (S <-> {P})   S |- (S --> {P}) :post [:t/identity :d/identity :allow-backward]
    (S <-> {P})  {P} |- (S --> {P}) :post [:t/identity :d/identity :allow-backward]
    ([S] <-> P) [S] |- ([S] --> P) :post [:t/identity :d/identity :allow-backward]
    ([S] <-> P)   P |- ([S] --> P) :post [:t/identity :d/identity :allow-backward]

    ({S} <-> {P})  {S} |- ({P} --> {S}) :post [:t/identity :d/identity :allow-backward]
    ({S} <-> {P})  {P} |- ({P} --> {S}) :post [:t/identity :d/identity :allow-backward]
    ([S] <-> [P]) [S] |- ([P] --> [S]) :post [:t/identity :d/identity :allow-backward]
    ([S] <-> [P]) [P] |- ([P] --> [S]) :post [:t/identity :d/identity :allow-backward]

    ; Set Definition Unwrap

    ({S} <-> {P})  {S} |- (S <-> P) :post [:t/identity :d/identity :allow-backward]
    ({S} <-> {P})  {P} |- (S <-> P) :post [:t/identity :d/identity :allow-backward]
    ([S] <-> [P]) [S] |- (S <-> P) :post [:t/identity :d/identity :allow-backward]
    ([S] <-> [P]) [P] |- (S <-> P) :post [:t/identity :d/identity :allow-backward]

    ; Nothing is more specific than a instance so it's similar

    (S --> {P})   S  |- (S <-> {P}) :post [:t/identity :d/identity :allow-backward]
    (S --> {P}) {P} |- (S <-> {P}) :post [:t/identity :d/identity :allow-backward]

    ; nothing is more general than a property so it's similar

    ([S] --> P) [S] |- ([S] <-> P) :post [:t/identity :d/identity :allow-backward]
    ([S] --> P)   P |- ([S] <-> P) :post [:t/identity :d/identity :allow-backward]

    ; Truth-value functions: see TruthFunctions.java

    ; Immediate Inference 
    ; If S can stand for P P can to a certain low degree also represent the class S
    ; If after S usually P happens then it might be a good guess that usually before P happens S happens.

    (P --> S) (S --> P) |- (P --> S) :pre [:question?] :post [:t/conversion :p/belief]
    (P --> S) (S --> P) |- (P --> S) :pre [:question?] :post [:t/conversion :p/belief]
    (P ==> S) (S ==> P) |- (P ==> S) :pre [:question?] :post [:t/conversion :p/belief]
    (P =|> S) (S =|> P) |- (P =|> S) :pre [:question?] :post [:t/conversion :p/belief]
    (P =\> S) (S =/> P) |- (P =\> S) :pre [:question?] :post [:t/conversion :p/belief]
    (P =/> S) (S =\> P) |- (P =/> S) :pre [:question?] :post [:t/conversion :p/belief]

    ; "If not smoking lets you be healthy being not healthy may be the result of smoking"

    ( --S ==> P)    P |- ( --P ==> S) :post [:t/contraposition :allow-backward]
    ( --S ==> P)  --S |- ( --P ==> S) :post [:t/contraposition :allow-backward]
    ( --S =|> P)    P |- ( --P =|> S) :post [:t/contraposition :allow-backward]
    ( --S =|> P)  --S |- ( --P =|> S) :post [:t/contraposition :allow-backward]
    ( --S =/> P)    P |- ( --P =\> S) :post [:t/contraposition :allow-backward]
    ( --S =/> P)  --S |- ( --P =\> S) :post [:t/contraposition :allow-backward]
    ( --S =\> P)    P |- ( --P =/> S) :post [:t/contraposition :allow-backward]
    ( --S =\> P)  --S |- ( --P =/> S) :post [:t/contraposition :allow-backward]

    ; A belief b <f c> is equal to --b <1-f c>  which is the negation rule:

    (A --> B) A |- --(A --> B) :post [:t/negation :d/negation :allow-backward]
    (A --> B) B |- --(A --> B) :post [:t/negation :d/negation :allow-backward]
  --(A --> B) A |-   (A --> B) :post [:t/negation :d/negation :allow-backward]
  --(A --> B) B |-   (A --> B) :post [:t/negation :d/negation :allow-backward]

    (A <-> B) A |- --(A <-> B) :post [:t/negation :d/negation :allow-backward]
    (A <-> B) B |- --(A <-> B) :post [:t/negation :d/negation :allow-backward]
  --(A <-> B) A |-   (A <-> B) :post [:t/negation :d/negation :allow-backward]
  --(A <-> B) B |-   (A <-> B) :post [:t/negation :d/negation :allow-backward]

    (A ==> B) A |- --(A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]
    (A ==> B) B |- --(A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]
  --(A ==> B) A |-   (A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]
  --(A ==> B) B |-   (A ==> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]

    (A <=> B) A |- --(A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]
    (A <=> B) B |- --(A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]
  --(A <=> B) A |-   (A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]
  --(A <=> B) B |-   (A <=> B) :post [:t/negation :d/negation :allow-backward :order-for-all-same]

    ; TODO: probably make simpler by just allowing it for all tasks in general

    ; inheritance-based syllogism
    ;       (A --> B) ------- (B --> C)
    ;            \               /
    ;             \             /
    ;              \           /
    ;               \         /
    ;                (A --> C)
    ; If A is a special case of B and B is a special case of C so is A a special case of C (strong) the other variations are hypotheses (weak)

    (A --> B) (B --> C) |- (A --> C) :pre [#(not= A C)] :post [:t/deduction :d/strong :allow-backward]
    (A --> B) (A --> C) |- (C --> B) :pre [#(not= B C)] :post [:t/abduction :d/weak :allow-backward]
    (A --> C) (B --> C) |- (B --> A) :pre [#(not= A B)] :post [:t/induction :d/weak :allow-backward]
    (A --> B) (B --> C) |- (C --> A) :pre [#(not= C A)] :post [:t/exemplification :d/weak :allow-backward]

    ; similarity from inheritance
    ; If S is a special case of P and P is a special case of S then S and P are similar

    (S --> P) (P --> S) |- (S <-> P) :post [:t/intersection :d/strong :allow-backward]

    ; inheritance from similarty <- TODO check why this one was missing

    (S <-> P) (P --> S) |- (S --> P) :post [:t/reduce-conjunction :d/strong :allow-backward]

    ; similarity-based syllogism
    ; If P and S are a special case of M then they might be similar (weak)
    ; also if P and S are a general case of M

    (P --> M) (S --> M) |- (S <-> P) :pre [#(not= S P)] :post [:t/comparison :d/weak :allow-backward]
    (M --> P) (M --> S) |- (S <-> P) :pre [#(not= S P)] :post [:t/comparison :d/weak :allow-backward]

    ; If M is a special case of P and S and M are similar then S is also a special case of P (strong)

    (M --> P) (S <-> M) |- (S --> P) :pre [#(not= S P)] :post [:t/analogy :d/strong :allow-backward]
    (P --> M) (S <-> M) |- (P --> S) :pre [#(not= S P)] :post [:t/analogy :d/strong :allow-backward]
    (M <-> P) (S <-> M) |- (S <-> P) :pre [#(not= S P)] :post [:t/resemblance :d/strong :allow-backward]

    ; inheritance-based composition
    ; If P and S are in the intension/extension of M then union/difference and intersection can be built:

    (P --> M) (S --> M) |- ((S | P) --> M) :pre [(not_set S) (not_set P) #(not= S P) (no_common_subterm S P)] :post [:t/intersection]
                                                                                            ((S & P) --> M) :post [:t/union]
                                                                                            ((P -i S) --> M) :post [:t/difference]

    (M --> P) (M --> S) |- (M --> (P & S)) :pre [(not_set S) (not_set P) #(not= S P) (no_common_subterm S P)] :post [:t/intersection]
                                                                                            (M --> (P | S)) :post [:t/union]
                                                                                            (M --> (P -e S)) :post [:t/difference]

    ; inheritance-based decomposition
    ; if (S --> M) is the case and ((| S :list/A) --> M) is not the case then ((| :list/A) --> M) is not the case hence :t/decompose-positive-negative-negative

    (S --> M) ((| S :list/A) --> M) |- ((| :list/A) --> M) :post [:t/decompose-positive-negative-negative]
    (S --> M) ((& S :list/A) --> M) |- ((& :list/A) --> M) :post [:t/decompose-negative-positive-positive]
    (S --> M) ((S -i P) --> M) |- (P --> M) :post [:t/decompose-positive-negative-positive]
    (S --> M) ((P -i S) --> M) |- (P --> M) :post [:t/decompose-negative-negative-negative]

    (M --> S) (M --> (& S :list/A)) |- (M --> (& :list/A)) :post [:t/decompose-positive-negative-negative]
    (M --> S) (M --> (| S :list/A)) |- (M --> (| :list/A)) :post [:t/decompose-negative-positive-positive]
    (M --> S) (M --> (S -e P)) |- (M --> P) :post [:t/decompose-positive-negative-positive]
    (M --> S) (M --> (P -e S)) |- (M --> P) :post [:t/decompose-negative-negative-negative]

    ; Set comprehension:

    (C --> A) (C --> B) |- (C --> R) :pre [(set-ext? A) (union A B R)] :post [:t/union]
    (C --> A) (C --> B) |- (C --> R) :pre [(set-int? A) (union A B R)] :post [:t/intersection]
    (A --> C) (B --> C) |- (R --> C) :pre [(set-ext? A) (union A B R)] :post [:t/intersection]
    (A --> C) (B --> C) |- (R --> C) :pre [(set-int? A) (union A B R)] :post [:t/union]

    (C --> A) (C --> B) |- (C --> R) :pre [(set-ext? A) (intersection A B R)] :post [:t/intersection]
    (C --> A) (C --> B) |- (C --> R) :pre [(set-int? A) (intersection A B R)] :post [:t/union]
    (A --> C) (B --> C) |- (R --> C) :pre [(set-ext? A) (intersection A B R)] :post [:t/union]
    (A --> C) (B --> C) |- (R --> C) :pre [(set-int? A) (intersection A B R)] :post [:t/intersection]

    (C --> A) (C --> B) |- (C --> R) :pre [(difference A B R)] :post [:t/difference]
    (A --> C) (B --> C) |- (R --> C) :pre [(difference A B R)] :post [:t/difference]

    ; Set element takeout:

    (C --> {:list/A}) C |- (C --> {:from/A}) :post [:t/structural-deduction]
    (C --> [:list/A]) C |- (C --> [:from/A]) :post [:t/structural-deduction]
    ({:list/A} --> C) C |- ({:from/A} --> C) :post [:t/structural-deduction]
    ([:list/A] --> C) C |- ([:from/A] --> C) :post [:t/structural-deduction]

    ; NAL3 single premise inference:

    ((| :list/A) --> M) M |- (:from/A --> M) :post [:t/structural-deduction]
    (M --> (& :list/A)) M |- (M --> :from/A) :post [:t/structural-deduction]

    ((B -i G) --> S) S |- (B --> S) :post [:t/structural-deduction]
    (R --> (B -e S)) R |- (R --> B) :post [:t/structural-deduction]

    ; NAL4 - Transformations between products and images:
    ; Relations and transforming them into different representations so that arguments and the relation it'self can become the subject or predicate

    ((:list/A) --> M) A_i |- (A_i --> (/ M A_1..A_i.(substitute _)..A_n )) :post [:t/identity :d/identity]

    (M --> (:list/A)) A_i |- ((\ M A_1..A_i.(substitute _)..A_n ) --> A_i) :post [:t/identity :d/identity]

    (A_i --> (/ M A_1..A_i.(substitute _)..A_n )) M |- ((:list/A) --> M) :post [:t/identity :d/identity]

    ((\ M A_1..A_i.(substitute _)..A_n ) --> A_i) M |- (M --> (:list/A)) :post [:t/identity :d/identity]

    ; implication-based syllogism
    ; (A ==> B) ------- (B ==> C)
    ; \               /
    ; \             /
    ; \           /
    ; \         /
    ; (A ==> C)
    ; If after S M happens and after M P happens so P happens after S

    (M ==> P) (S ==> M) |- (S ==> P) :pre [#(not= S P)] :post [:t/deduction :order-for-all-same :allow-backward]

    (P ==> M) (S ==> M) |- (S ==> P) :pre [#(not= S P)] :post [:t/induction :allow-backward]
    (P =|> M) (S =|> M) |- (S =|> P) :pre [#(not= S P)] :post [:t/induction :allow-backward]
    (P =/> M) (S =/> M) |- (S =|> P) :pre [#(not= S P)] :post [:t/induction :allow-backward]
    (P =\> M) (S =\> M) |- (S =|> P) :pre [#(not= S P)] :post [:t/induction :allow-backward]

    (M ==> P) (M ==> S) |- (S ==> P) :pre [#(not= S P)] :post [:t/abduction :allow-backward]
    (M =/> P) (M =/> S) |- (S =|> P) :pre [#(not= S P)] :post [:t/abduction :allow-backward]
    (M =|> P) (M =|> S) |- (S =|> P) :pre [#(not= S P)] :post [:t/abduction :allow-backward]
    (M =\> P) (M =\> S) |- (S =|> P) :pre [#(not= S P)] :post [:t/abduction :allow-backward]

    (P ==> M) (M ==> S) |- (S ==> P) :pre [#(not= S P)] :post [:t/exemplification :allow-backward]
    (P =/> M) (M =/> S) |- (S =\> P) :pre [#(not= S P)] :post [:t/exemplification :allow-backward]
    (P =\> M) (M =\> S) |- (S =/> P) :pre [#(not= S P)] :post [:t/exemplification :allow-backward]
    (P =|> M) (M =|> S) |- (S =|> P) :pre [#(not= S P)] :post [:t/exemplification :allow-backward]

    ; implication to equivalence
    ; If when S happens P happens and before P happens S has happened then they are truth-related equivalent

    (S ==> P) (P ==> S) |- (S <=> P) :pre [#(not= S P)] :post [:t/intersection :allow-backward]
    (S =|> P) (P =|> S) |- (S <|> P) :pre [#(not= S P)] :post [:t/intersection :allow-backward]
    (S =/> P) (P =\> S) |- (S </> P) :pre [#(not= S P)] :post [:t/intersection :allow-backward]
    (S =\> P) (P =/> S) |- (P </> S) :pre [#(not= S P)] :post [:t/intersection :allow-backward]

    ; equivalence-based syllogism
    ; Same as for inheritance again

    (P ==> M) (S ==> M) |- (S <=> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]
    (P =/> M) (S =/> M) |- (S <|> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]
                                            (S </> P) :post [:t/comparison :allow-backward]
                                            (P </> S) :post [:t/comparison :allow-backward]
    (P =|> M) (S =|> M) |- (S <|> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]
    (P =\> M) (S =\> M) |- (S <|> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]
                                            (S </> P) :post [:t/comparison :allow-backward]
                                            (P </> S) :post [:t/comparison :allow-backward]

    (M ==> P) (M ==> S) |- (S <=> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]
    (M =/> P) (M =/> S) |- (S <|> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]
                                            (S </> P) :post [:t/comparison :allow-backward]
                                            (P </> S) :post [:t/comparison :allow-backward]
    (M =|> P) (M =|> S) |- (S <|> P) :pre [#(not= S P)] :post [:t/comparison :allow-backward]

    ; Same as for inheritance again

    (M ==> P) (S <=> M) |- (S ==> P) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (M =/> P) (S </> M) |- (S =/> P) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (M =/> P) (S <|> M) |- (S =/> P) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (M =|> P) (S <|> M) |- (S =|> P) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (M =\> P) (M </> S) |- (S =\> P) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (M =\> P) (S <|> M) |- (S =\> P) :pre [#(not= S P)] :post [:t/analogy :allow-backward]

    (P ==> M) (S <=> M) |- (P ==> S) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (P =/> M) (S <|> M) |- (P =/> S) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (P =|> M) (S <|> M) |- (P =|> S) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (P =\> M) (S </> M) |- (P =\> S) :pre [#(not= S P)] :post [:t/analogy :allow-backward]
    (P =\> M) (S <|> M) |- (P =\> S) :pre [#(not= S P)] :post [:t/analogy :allow-backward]

    (M <=> P) (S <=> M) |- (S <=> P) :pre [#(not= S P)] :post [:t/resemblance :order-for-all-same :allow-backward]
    (M </> P) (S <|> M) |- (S </> P) :pre [#(not= S P)] :post [:t/resemblance :allow-backward]
    (M <|> P) (S </> M) |- (S </> P) :pre [#(not= S P)] :post [:t/resemblance :allow-backward]

    ; implication-based composition
    ; Same as for inheritance again

    (P ==> M) (S ==> M) |- ((P || S) ==> M) :pre [#(not= S P)] :post [:t/intersection]
                                            ((P && S) ==> M) :post [:t/union]
    (P =|> M) (S =|> M) |- ((P || S) =|> M) :pre [#(not= S P)] :post [:t/intersection]
                                            ((P &| S) =|> M) :post [:t/union]
    (P =/> M) (S =/> M) |- ((P || S) =/> M) :pre [#(not= S P)] :post [:t/intersection]
                                            ((P &| S) =/> M) :post [:t/union]
    (P =\> M) (S =\> M) |- ((P || S) =\> M) :pre [#(not= S P)] :post [:t/intersection]
                                            ((P &| S) =\> M) :post [:t/union]

    (M ==> P) (M ==> S) |- (M ==> (P && S)) :pre [#(not= S P)] :post [:t/intersection]
                                            (M ==> (P || S)) :post [:t/union]
    (M =/> P) (M =/> S) |- (M =/> (P &| S)) :pre [#(not= S P)] :post [:t/intersection]
                                            (M =/> (P || S)) :post [:t/union]
    (M =|> P) (M =|> S) |- (M =|> (P &| S)) :pre [#(not= S P)] :post [:t/intersection]
                                            (M =|> (P || S)) :post [:t/union]
    (M =\> P) (M =\> S) |- (M =\> (P &| S)) :pre [#(not= S P)] :post [:t/intersection]
                                            (M =\> (P || S)) :post [:t/union]

    (D =/> R) (D =\> K) |- (K =/> R) :pre [#(not= R K)] :post [:t/abduction]
                                            (R =\> K) :post [:t/induction]
                                            (K </> R) :post [:t/comparison]
    ; implication-based decomposition
    ; Same as for inheritance again

    (S ==> M) ((|| S :list/A) ==> M) |- ((|| :list/A) ==> M) :post [:t/decompose-positive-negative-negative :order-for-all-same]
    (S ==> M) ((&& S :list/A) ==> M) |- ((&& :list/A) ==> M) :post [:t/decompose-negative-positive-positive :order-for-all-same :seq-interval-from-premises]

    (M ==> S) (M ==> (&& S :list/A)) |- (M ==> (&& :list/A)) :post [:t/decompose-positive-negative-negative :order-for-all-same :seq-interval-from-premises]
    (M ==> S) (M ==> (|| S :list/A)) |- (M ==> (|| :list/A)) :post [:t/decompose-negative-positive-positive :order-for-all-same]

    ; conditional syllogism
    ; If after M P usually happens and M happens it means P is expected to happen

    M (M ==> P) |- P :post [:t/deduction :d/induction :order-for-all-same] :pre [(shift-occurrence-forward unused "==>")]
    M (P ==> M) |- P :post [:t/abduction :d/deduction :order-for-all-same] :pre [(shift-occurrence-backward unused "==>")]
    M (S <=> M) |- S :post [:t/analogy :d/strong :order-for-all-same] :pre [(shift-occurrence-backward unused "<=>")]
    M (M <=> S) |- S :post [:t/analogy :d/strong :order-for-all-same] :pre [(shift-occurrence-forward unused "==>")]

    ; conditional composition:
    ; They are let out for AGI purpose don't let the system generate conjunctions or useless <=> and ==> statements
    ; For this there needs to be a semantic dependence between both either by the predicate or by the subject
    ; or a temporal dependence which acts as special case of semantic dependence
    ; These cases are handled by "Variable Introduction" and "Temporal Induction"

    ; P S |- (S ==> P) :pre [(no_common_subterm S P)] :post [:t/induction]
    ; P S |- (S <=> P) :pre [(no_common_subterm S P)] :post [:t/comparison]
    ; P S |- (P && S) :pre [(no_common_subterm S P)] :post [:t/intersection]
    ; P S |- (P || S) :pre [(no_common_subterm S P)] :post [:t/union]

    ; conjunction decompose

    (&& :list/A) A_1 |- A_1 :post [:t/structural-deduction :d/structural-strong]
    (&/ :list/A) A_1 |- A_1 :post [:t/structural-deduction :d/structural-strong]
    (&| :list/A) A_1 |- A_1 :post [:t/structural-deduction :d/structural-strong]
    (&/ B :list/A) B |- (&/ :list/A) :pre [(task "!")] :post [:t/deduction :d/strong :seq-interval-from-premises]

    ; propositional decomposition
    ; If S is the case and (&& S :list/A) is not the case it can't be that (&& :list/A) is the case

    S (&/ S :list/A) |- (&/ :list/A) :post [:t/decompose-positive-negative-negative :seq-interval-from-premises]
    S (&| S :list/A) |- (&| :list/A) :post [:t/decompose-positive-negative-negative]
    S (&& S :list/A) |- (&& :list/A) :post [:t/decompose-positive-negative-negative]
    S (|| S :list/A) |- (|| :list/A) :post [:t/decompose-negative-positive-positive]

    ; Additional for negation: https://groups.google.com/forum/#!topic/open-nars/g-7r0jjq2Vc

    S (&/ (-- S) :list/A) |- (&/ :list/A) :post [:t/decompose-negative-negative-negative :seq-interval-from-premises]
    S (&| (-- S) :list/A) |- (&| :list/A) :post [:t/decompose-negative-negative-negative]
    S (&& (-- S) :list/A) |- (&& :list/A) :post [:t/decompose-negative-negative-negative]
    S (|| (-- S) :list/A) |- (|| :list/A) :post [:t/decompose-positive-positive-positive]

    ; multi-conditional syllogism
    ; Inference about the pre/postconditions

    Y ((&& X :list/A) ==> B) |- ((&& :list/A) ==> B) :pre [(substitute-if-unifies "$" X Y)] :post [:t/deduction :order-for-all-same :seq-interval-from-premises]
    ((&& M :list/A) ==> C) ((&& :list/A) ==> C) |- M :post [:t/abduction :order-for-all-same]

    ; Can be derived by NAL7 rules so this won't be necessary there (:order-for-all-same left out here)

    ; the first rule does not have :order-for-all-same because it would be invalid see: https://groups.google.com/forum/#!topic/open-nars/r5UJo64Qhrk
    ((&& :list/A) ==> C) M |- ((&& M :list/A) ==> C) :pre [(not-implication-or-equivalence M)] :post [:t/induction]
    ((&& :list/A) =|> C) M |- ((&& M :list/A) =|> C) :pre [(not-implication-or-equivalence M)] :post [:t/induction]
    ((&& :list/A) =/> C) M |- ((&& M :list/A) =/> C) :pre [(not-implication-or-equivalence M)] :post [:t/induction]
    ((&& :list/A) =\> C) M |- ((&& M :list/A) =\> C) :pre [(not-implication-or-equivalence M)] :post [:t/induction]
    (A ==> M) ((&& M :list/A) ==> C) |- ((&& A :list/A) ==> C) :post [:t/deduction :order-for-all-same :seq-interval-from-premises]
    ((&& M :list/A) ==> C) ((&& A :list/A) ==> C) |- (A ==> M) :post [:t/induction :order-for-all-same]
    (A ==> M) ((&& A :list/A) ==> C) |- ((&& M :list/A) ==> C) :post [:t/abduction :order-for-all-same :seq-interval-from-premises]

    ; variable introduction
    ; Introduce variables by common subject or predicate

    (S --> M) (P --> M) |- ((P --> $X) ==> (S --> $X)) :pre [#(not= S P)] :post [:t/abduction]
                                            ((S --> $X) ==> (P --> $X)) :post [:t/induction]
                                            ((P --> $X) <=> (S --> $X)) :post [:t/comparison]
                                            (&& (S --> #Y) (P --> #Y)) :post [:t/intersection]

    (S --> M) (P --> M) |- ((&/ (P --> $X) I) =/> (S --> $X)) :pre [#(not= S P) (measure-time I)] :post [:t/induction :linkage-temporal]
                                                             ((S --> $X) =\> (&/ (P --> $X) I)) :post [:t/abduction :linkage-temporal]
                                                             ((&/ (P --> $X) I) </> (S --> $X)) :post [:t/comparison :linkage-temporal]
                                                             (&/ (P --> #Y) I (S --> #Y)) :post [:t/intersection :linkage-temporal]

    (S --> M) (P --> M) |- ((P --> $X) =|> (S --> $X)) :pre [#(not= S P) (concurrent Task Belief)] :post [:t/abduction :linkage-temporal]
                                                                     ((S --> $X) =|> (P --> $X)) :post [:t/induction :linkage-temporal]
                                                                     ((P --> $X) <|> (S --> $X)) :post [:t/comparison :linkage-temporal]
                                                                     (&| (P --> #Y) (S --> #Y)) :post [:t/intersection :linkage-temporal]

    (M --> S) (M --> P) |- (($X --> S) ==> ($X --> P)) :pre [#(not= S P)] :post [:t/induction]
                                            (($X --> P) ==> ($X --> S)) :post [:t/abduction]
                                            (($X --> S) <=> ($X --> P)) :post [:t/comparison]
                                            (&& (#Y --> S) (#Y --> P)) :post [:t/intersection]

    (M --> S) (M --> P) |- ((&/ ($X --> P) I) =/> ($X --> S)) :pre [#(not= S P) (measure-time I)] :post [:t/induction :linkage-temporal]
                                                             (($X --> S) =\> (&/ ($X --> P) I)) :post [:t/abduction :linkage-temporal]
                                                             ((&/ ($X --> P) I) </> ($X --> S)) :post [:t/comparison :linkage-temporal]
                                                             (&/ (#Y --> P) I (#Y --> S)) :post [:t/intersection :linkage-temporal]

    (M --> S) (M --> P) |- (($X --> S) =|> ($X --> P)) :pre [#(not= S P) (concurrent (M --> P) (M --> S))] :post [:t/induction :linkage-temporal]
                                                                             (($X --> P) =|> ($X --> S)) :post [:t/abduction :linkage-temporal]
                                                                             (($X --> S) <|> ($X --> P)) :post [:t/comparison :linkage-temporal]
                                                                             (&| (#Y --> S) (#Y --> P)) :post [:t/intersection :linkage-temporal]

    ; 2nd variable introduction

    (A ==> (M --> P)) (M --> S) |- ((&& A ($X --> S)) ==> ($X --> P)) :pre [#(not= A (M --> S))] :post [:t/induction]
                                                             (&& (A ==> (#Y --> P)) (#Y --> S)) :post [:t/intersection]

    (&& (M --> P) :list/A) (M --> S) |- (($Y --> S) ==> (&& ($Y --> P) :list/A)) :pre [#(not= S P)] :post [:t/induction]
                                                         (&& (#Y --> S) (#Y --> P) :list/A) :post [:t/intersection]

    (A ==> (P --> M)) (S --> M) |- ((&& A (P --> $X)) ==> (S --> $X)) :pre [#(not= S P) #(not= A (S --> M))] :post [:t/abduction]
                                                                             (&& (A ==> (P --> #Y)) (S --> #Y)) :post [:t/intersection]

    (&& (P --> M) :list/A) (S --> M) |- ((S --> $Y) ==> (&& (P --> $Y) :list/A)) :pre [#(not= S P)] :post [:t/abduction]
                                                         (&&  (S --> #Y) (P --> #Y) :list/A) :post [:t/intersection]

    (A --> L) ((A --> S) ==> R) |- ((&& (#X --> L) (#X --> S)) ==> R) :post [:t/induction]
    (A --> L) ((&& (A --> S) :list/A) ==> R) |- ((&& (#X --> L) (#X --> S) :list/A) ==> R) :pre [(substitute A #X)] :post [:t/induction]

    ; dependent variable elimination
    ; Decomposition with elimination of a variable

    B (&& A :list/A) |- (&& :list/A) :pre [(task ".") (substitute-if-unifies "#" A B)] :post [:t/anonymous-analogy :d/strong :order-for-all-same :seq-interval-from-premises]

    ; conditional abduction by dependent variable

    ((A --> R) ==> Z) ((&& (#Y --> B) (#Y --> R) :list/A) ==> Z) |- (A --> B) :post [:t/abduction]
    ((A --> R) ==> Z) ((&& (#Y --> B) (#Y --> R)) ==> Z) |- (A --> B) :post [:t/abduction]

    ; conditional deduction "An inverse inference has been implemented as a form of deduction" https://code.google.com/p/open-nars/issues/detail?id=40&can=1

    (U --> L) ((&& (#X --> L) (#X --> R)) ==> Z) |- ((U --> R) ==> Z) :post [:t/deduction]
    (U --> L) ((&& (#X --> L) (#X --> R) :list/A) ==> Z) |- ((&& (U --> R) :list/A) ==> Z) :pre [(substitute #X U)] :post [:t/deduction]

    ; independent variable elimination

    B (A ==> C) |- C  [:t/deduction :order-for-all-same] :pre [(substitute-if-unifies "$" A B) (shift-occurrence-forward unused "==>")]
    B (C ==> A) |- C  [:t/abduction :order-for-all-same] :pre [(substitute-if-unifies "$" A B) (shift-occurrence-backward unused "==>")]

    B (A <=> C) |- C  [:t/deduction :order-for-all-same] :pre [(substitute-if-unifies "$" A B) (shift-occurrence-backward unused "<=>")]
    B (C <=> A) |- C  [:t/deduction :order-for-all-same] :pre [(substitute-if-unifies "$" A B) (shift-occurrence-forward unused "<=>")]

    ; second level variable handling rules
    ; second level variable elimination (termlink level2 growth needed in order for these rules to work)

    (A --> K) (&& (#X --> L) (($Y --> K) ==> (&& :list/A))) |- (&& (#X --> L) :list/A) :pre [(substitute $Y A)] :post [:t/deduction]
    (A --> K) (($X --> L) ==> (&& (#Y --> K) :list/A)) |- (($X --> L) ==> (&& :list/A)) :pre [(substitute #Y A)] :post [:t/anonymous-analogy]

    ; precondition combiner inference rule (variable_unification6):

    ((&& C :list/A) ==> Z) ((&& C :list/B) ==> Z) |- ((&& :list/A) ==> (&& :list/B)) :post [:t/induction]
    ((&& C :list/A) ==> Z) ((&& C :list/B) ==> Z) |- ((&& :list/B) ==> (&& :list/A)) :post [:t/induction]
    (Z ==> (&& C :list/A)) (Z ==> (&& C :list/B)) |- ((&& :list/A) ==> (&& :list/B)) :post [:t/abduction]
    (Z ==> (&& C :list/A)) (Z ==> (&& C :list/B)) |- ((&& :list/B) ==> (&& :list/A)) :post [:t/abduction]

    ; NAL7 specific inference
    ; Reasoning about temporal statements. those are using the ==> relation because relation in time is a relation of the truth between statements.

    X (XI ==> B) |- B  [:t/deduction :d/induction :order-for-all-same] :pre [(substitute-if-unifies "$" XI (&/ X /0)) (shift-occurrence-forward XI "==>")]
    X (BI ==> Y) |- BI  [:t/abduction :d/deduction :order-for-all-same] :pre [(substitute-if-unifies "$" Y X) (shift-occurrence-backward BI "==>")]

    ; Temporal induction:
    ; When P and then S happened according to an observation by induction (weak) it may be that alyways after P usually S happens.

    P S |- ((&/ S I) =/> P) :pre [(measure-time I) (not-implication-or-equivalence P) (not-implication-or-equivalence S)] :post [:t/induction :linkage-temporal]
                                                                                                   (P =\> (&/ S I)) :post [:t/abduction :linkage-temporal] 
                                                                                                   ((&/ S I) </> P) :post [:t/comparison :linkage-temporal] 
                                                                                                   (&/ S I P) :post [:t/intersection :linkage-temporal]


    P S |- (S =|> P) :pre [(concurrent Task Belief) (not-implication-or-equivalence P) (not-implication-or-equivalence S)] :post [:t/induction :linkage-temporal]
                                                                                                           (P =|> S) :post [:t/induction :linkage-temporal] 
                                                                                                           (S <|> P) :post [:t/comparison :linkage-temporal] 
                                                                                                           (&| S P) :post [:t/intersection :linkage-temporal]

    ; backward inference is mostly handled by the rule transformation:

    ; T B |- C  [post] =>
    ; C B |- T  [post] :pre [:question?]
    ; C T |- B  [post] :pre [:question?]

    ; here now are the backward inference rules which should really only work on backward inference:

    (A --> S) (B --> S) |- (A --> B) :pre [:question?] :post [:p/question]
                                       (B --> A) :post [:p/question]
                                       (A <-> B) :post [:p/question]

    ; and the backward inference driven forward inference:

    ; NAL2:

    ([A] <-> [B]) (A <-> B) |- ([A] <-> [B]) :pre [:question?] :post [:t/belief-identity :p/belief]
    ({A} <-> {B}) (A <-> B) |- ({A} <-> {B}) :pre [:question?] :post [:t/belief-identity :p/belief]

    ([A] --> [B]) (A <-> B) |- ([A] --> [B]) :pre [:question?] :post [:t/belief-identity :p/belief]
    ({A} --> {B}) (A <-> B) |- ({A} --> {B}) :pre [:question?] :post [:t/belief-identity :p/belief]

    ; NAL3:

    ; composition on both sides of a statement:

    ((& B :list/A) --> (& A :list/A)) (B --> A) |- ((& B :list/A) --> (& A :list/A)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((| B :list/A) --> (| A :list/A)) (B --> A) |- ((| B :list/A) --> (| A :list/A)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]

    ((- S A) --> (- S B)) (B --> A) |- ((- S A) --> (- S B)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((~ S A) --> (~ S B)) (B --> A) |- ((~ S A) --> (~ S B)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]

    ; composition on one side of a statement:

    (W --> (| B :list/A)) (W --> B) |- (W --> (| B :list/A)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((& B :list/A) --> W) (B --> W) |- ((& B :list/A) --> W) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]

    (W --> (- S B)) (W --> B) |- (W --> (- S B)) :pre [:question?] :post [:t/beliefStructuralDifference :p/belief]
    ((~ S B) --> W) (B --> W) |- ((~ S B) --> W) :pre [:question?] :post [:t/beliefStructuralDifference :p/belief]

    ; NAL4:

    ; composition on both sides of a statement:

    ((B P) --> Z) (B --> A) |- ((B P) --> (A P)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((P B) --> Z) (B --> A) |- ((P B) --> (P A)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((B P) <-> Z) (B <-> A) |- ((B P) <-> (A P)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((P B) <-> Z) (B <-> A) |- ((P B) <-> (P A)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((\ N A _) --> Z) (N --> R) |- ((\ N A _) --> (\ R A _)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
    ((/ N _ B) --> Z) (S --> B) |- ((/ N _ B) --> (/ N _ S)) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]

    ; NAL5:

  --A    A |- --A  :pre [:question?] :post [:t/belief-negation :p/belief]
    A  --A |-   A  :pre [:question?] :post [:t/belief-negation :p/belief]

    ; compound composition one premise

    (|| B :list/A) B |- (|| B :list/A) :pre [:question?] :post [:t/belief-structural-deduction :p/belief]
