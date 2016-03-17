(ns nal.rules
  (:require [nal.deriver.rules :refer [defrules]]
            nal.reader))

(declare --S S --P P <-> |- --> ==> M || && =|> -- A Ai B <=>)

;; <!-- <div style="z-index: 9999; position: fixed; left: 0; top: 0;"> <button <iframe name="bible" src="NAL-Specification.pdf" style="position:fixed" width=100% height=35%></iframe> </div> -->


(defrules nal1-nal2-nal3-equivalence-and-implication
          "<h1><a href=\"NAL-Specification.pdf#page=87\" style=\"text-decoration:none\">NAL1 NAL2 NAL3 Equivalence and Implication Rules</a></h1><br/>  <!-- target=\"bible\" -->
These rules are used to capture equivalence and implication theorems as described in the NAL reference.
Their correctness follows by the definitions of the NAL statement copulas.
Since the conclusion is equivalent, the truth value of the conclusion is using Identity as truth and desire function.
So these rules are for bringing NAL-statements into a different, implied and more appropriate form.
          "
  ;;Equivalence and Implication Rules
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
          )


(defrules nal1-nal5-conversion-contraposition-negation
          "<h1><a href=\"NAL-Specification.pdf#page=52\" style=\"text-decoration:none\">NAL1 NAL5 Conversion, Contraposition, Negation</a></h1><br/> <!-- target=\"bible\" -->
          In term logics, \"conversion\" is an inference from a single premise to a conclusion by interchanging the subject
          and predicate terms of the premise. How the truth value is calculated can be seen in
          <a href=\"NAL-Specification.pdf#page=25\">Conversion</a><br/><br/>
          In logic, contraposition is a law that says that a conditional statement is logically equivalent to its contrapositive.
          In NAL however it is not equivalent, click <a href=\"NAL-Specification.pdf#page=52\">Contraposition</a> to see more detail about this.
          The contrapositive of the statement has its antecedent and consequent inverted and flipped.<br/><br/>
          <a href=\"NAL-Specification.pdf#page=50\">Negation</a> just inverts the truth of a statement,
          which means, due to the semantics of the frequency, that the truth value can be directly obtained by 1-f where f was the frequency of the premise."
  ;; Conversion
  ; If S can stand for P P can to a certain low degree also represent the class S
  ; If after S usually P happens then it might be a good guess that usually before P happens S happens.
  #R[(P --> S) (S --> P) |- (P --> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P ==> S) (S ==> P) |- (P ==> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P =|> S) (S =|> P) |- (P =|> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P =\> S) (S =/> P) |- (P =\> S) :post (:t/conversion :p/judgment) :pre (:question?)]
  #R[(P =/> S) (S =\> P) |- (P =/> S) :post (:t/conversion :p/judgment) :pre (:question?)]

  ;; Contraposition
  ; "If not smoking lets you be healthy being not healthy may be the result of smoking"
  #R[(--S ==> P) P |- (--P ==> S) :post (:t/contraposition :allow-backward)]
  #R[(--S ==> P) --S |- (--P ==> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =|> P) P |- (--P =|> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =|> P) --S |- (--P =|> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =/> P) P |- (--P =\> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =/> P) --S |- (--P =\> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =\> P) P |- (--P =/> S) :post (:t/contraposition :allow-backward)]
  #R[(--S =\> P) --S |- (--P =/> S) :post (:t/contraposition :allow-backward)]

  ;;Negation
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
          )

(defules nal1-nal2-inheritance-related-syllogisms
         "<h1><a href=\"NAL-Specification.pdf#page=24\" style=\"text-decoration:none\">NAL1 NAL2 Inheritance-Related Syllogisms</a></h1><br/>  <!-- target=\"bible\" -->
         <a href=\"NAL-Specification.pdf#page=24\">Deduction</a>, <a href=\"NAL-Specification.pdf#page=24\">Induction</a> and
         <a href=\"NAL-Specification.pdf#page=24\">Abduction</a> can be naturally represented using the Inheritance-Relation.
         This relation A --> B describes that A is a special case of B, and thus makes it possible for the system to create a generalization-hierachy by using multiple statements.
         The following rules implement Deduction, Induction and Abduction just based on this relation, while additionally rules for the
         Similarity relation are added. The <a href=\"NAL-Specification.pdf#page=29\">Similarity</a> relation itself is defined as a bi-directional Inheritance-Relation.
         The main purpose of these rules is to establish Deduction, Induction, Abduction, for the Inheritance-Copula
         and Analogy and Resemblance as a special case of a deduction for the Similarity copula,
         as well as Comparison as a special case of Induction/Abduction also for the Similarity copula.
         This makes it possible for the system to draw strong conclusions as well as to construct weak but reasonable hypothesis just based on Inheritance-based statements.
         "
  ;;Inheritance-Related Syllogisms
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
          )

(defules nal3-intersection-union-difference
         "<h1><a href=\"NAL-Specification.pdf#page=40\" style=\"text-decoration:none\">NAL3 Intersection, Union, Difference</a></h1><br/>  <!-- target=\"bible\" -->
          These are the NAL3-related <a href=\"NAL-Specification.pdf#page=35\">union, intersection</a> and <a href=\"NAL-Specification.pdf#page=37\">difference</a> rules,
          allowing the system to create new extensional intersections / intensional intersections and differences based on existing terms.
          A union of instances (extensional union) are a special case of the properties they share, so are a special case of the intersection of the shared properties (intensional intersection)
          Also a union of two properties (intensional union), is a general case of all the instances which have both properties (extensional intersection)
          This symmetry holds when reasoning about the extension and intension, which is why the truth-calculations
          are swapped in the extension-related and intension-related versions of the specific rules.
          The rules here are not used for sets, since it would be redundant,
          for sets there are special rules which follow the same philosophy.
          Additionally no-common-subterm is used to hamper evidence to be counted twice.
          Additionally note that ((S & P) & Q) is automatically reduced to (S & P & Q), same for |, due to the associativity of this operator.
          These rules give the system the ability to form differences, intersections and unions of elements of the generalization hierachy.
          "
  ;; inheritance-based composition
  ; If P and S are in the intension/extension of M then union/difference and intersection can be built:
  #R[(P --> M) (S --> M) |- (((S | P) --> M) :post (:t/intersection)
                              ((S & P) --> M) :post (:t/union)
                              ((P ~ S) --> M) :post (:t/difference))
     :pre ((:not-set? S) (:not-set? P)(:!= S P) (:no-common-subterm S P))]

  #R[(M --> P) (M --> S) |- ((M --> (P & S)) :post (:t/intersection)
                              (M --> (P | S)) :post (:t/union)
                              (M --> (P - S)) :post (:t/difference))
     :pre ((:not-set? S) (:not-set? P)(:!= S P) (:no-common-subterm S P))]
         )

(defules nal3-inheritance-based-decomposition
         "<h1><a href=\"NAL-Specification.pdf#page=86\" style=\"text-decoration:none\">NAL3 Inheritance-based Decomposition</a></h1><br/>  <!-- target=\"bible\" -->
         This rules are the opposite of what the above rules represent.
         Instead of composing new intersections, this rules are responsible for decomposing them.
         This gives the system the ability to work with differences, intersections and unions of elements of the generalization hierachy."
         ;; inheritance-based decomposition
  ; if (S --> M) is the case and ((| S :list/A) --> M) is not the case then ((| :list/A) --> M) is not the case hence :t/decompose-pnn
  #R[(S --> M) ((| S :list/A) --> M) |- ((| :list/A) --> M) :post (:t/decompose-pnn)]
  #R[(S --> M) ((& S :list/A) --> M) |- ((& :list/A) --> M) :post (:t/decompose-npp)]
  #R[(S --> M) ((S - P) --> M) |- (P --> M) :post (:t/decompose-pnp)]
  #R[(S --> M) ((P - S) --> M) |- (P --> M) :post (:t/decompose-nnn)]

  #R[(M --> S) (M --> (& S :list/A)) |- (M --> (& :list/A)) :post (:t/decompose-pnn)]
  #R[(M --> S) (M --> (| S :list/A)) |- (M --> (| :list/A)) :post (:t/decompose-npp)]
  #R[(M --> S) (M --> (S ~ P)) |- (M --> P) :post (:t/decompose-pnp)]
  #R[(M --> S) (M --> (P ~ S)) |- (M --> P) :post (:t/decompose-nnn)]
         )

(defules nal3-set-related-rules
         "<h1><a href=\"NAL-Specification.pdf#page=37\" style=\"text-decoration:none\">NAL3 Set-related rules</a></h1><br/>  <!-- target=\"bible\" -->
         These are the set-versions of the rules above. Sets form the boundaries of the taxonomic hierachy
         spanned by the inheritance-statements. A property is a thing for which there can be no further generalization,
         as [furry], while an instance is a thing for which there can be no further specialization, for example {tim}.
         Most statements, are somewhere between properties and instances in the generalization hierachy,
         as most things can indeed be meaningfully specialized and generalized.
         So these set statements are mostly only used by sensors and user input. As example one may consider a sensor which gives information on whether
         the first pixel in the retina is bright: <{pixel1} --> [bright]>. %degree%
         So the purpose of these rules is mainly to form compositions of sensor-related properties and instances.
          "
  ;; Set comprehension:
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
         )

(defules nal3-structural-inference
         "<h1><a href=\"NAL-Specification.pdf#page=40\" style=\"text-decoration:none\">NAL3 structural inference</a></h1><br/>  <!-- target=\"bible\" -->
          These are some additional meaningful structural deduction rule for the NAL3-statements.
          For example if it is known that a cat is a furry animal, it can be derived that a cat is an animal.
          So these rules are for valid deductions based on the premises containing intersections and differences."
  ; NAL3 single premise inference:
  #R[((| :list/A) --> M) M |- (:from/A --> M) :post (:t/structural-deduction)]
  #R[(M --> (& :list/A)) M |- (M --> :from/A) :post (:t/structural-deduction)]

  #R[((B - G) --> S) S |- (B --> S) :post (:t/structural-deduction)]
  #R[(R --> (B ~ S)) R |- (R --> B) :post (:t/structural-deduction)]

   ; Set element takeout:
   #R[(C --> {:list/A}) C |- (C --> {:from/A}) :post (:t/structural-deduction)]
   #R[(C --> [:list/A]) C |- (C --> [:from/A]) :post (:t/structural-deduction)]
   #R[({:list/A} --> C) C |- ({:from/A} --> C) :post (:t/structural-deduction)]
   #R[([:list/A] --> C) C |- ([:from/A] --> C) :post (:t/structural-deduction)]
         )

(defules nal4-structural-inference
          "<h1><a href=\"NAL-Specification.pdf#page=41\" style=\"text-decoration:none\">NAL4 structural inference</a></h1><br/>  <!-- target=\"bible\" -->
           The purpose of this rules is structural inference on relations.
           This allows the system to see a specific relation from different perspective,
           as demanded to make arguments of relations themselves possibly be the subject or predicate of an Inheritance-statement.
           For example if the cat eats the mouse, the cat is a special case of something which eats a mouse,
           additionally the mouse is a special case of something which is eaten by the cat,
           additionally the the cat and the mouse are in an anonymous relation which itself is a special case of eating.
           Since this are all different representations of the same relation, the rules use Identity as truth value.
           The key purpose of tese inference rules is to allow the system to do arbitrary relational reasoning
           be exposing all aspects about relations to the generalization hierachy.
           "
  ; NAL4 - Transformations between products and images:
  ; Relations and transforming them into different representations so that arguments and the relation it'self can become the subject or predicate
  #R[((* :list/A) --> M) Ai |- (Ai --> (/ M :list/A))
     :pre ((:substitute-from-list Ai _) (:contains? (:list/A) Ai))
     :post (:t/identity :d/identity)]
  #R[(M --> (* :list/A)) Ai |- ((\ M :list/A) --> Ai)
     :pre ((:substitute-from-list Ai _) (:contains? (:list/A) Ai))
     :post (:t/identity :d/identity)]
  #R[(Ai --> (/ M :list/A )) M |- ((* :list/A) --> M)
     :pre ((:substitute-from-list _ Ai) (:contains? (:list/A) Ai))
     :post (:t/identity :d/identity)]
  #R[((\ M :list/A) --> Ai) M |- (M --> (:list/A))
      :pre ((:substitute-from-list _ Ai) (:contains? (:list/A) Ai))
     :post (:t/identity :d/identity)]
         )

(defules nal5-implication-based-syllogisms
         "<h1><a href=\"NAL-Specification.pdf#page=49\" style=\"text-decoration:none\">NAL5 implication based syllogisms</a></h1><br/>  <!-- target=\"bible\" -->
           While Inheritance represents a relation in meaning, Implication represents a relation in truth.
           The meaning of ==> is very natural and roughly corresponds to the implication in classical logic.
           Syllogistic inference for ==> is analogous to how it is in case of -->.
           Also here a two-sided version of the ==> relation, namely <=> is considered, this one is called Equivalence.
           Note that there are temporal variants of these copulas, temporally
           consequent =/> </>, as well as the temporal concurrent copulas =|> <|>.
           For ==> there is an additional =&#92;> relation for \"before\", allowing the system
           to for example draw deductive conclusion also into the past if the evidence supports them.
           Note that all inference rules with :order-for-all-same also work for this temporal variants,
           even if the inference rule itself only speaks about ==>.
           The key purpose of these rules is to establish deduction, induction, abduction and exemplification for statements using ==> copulas."
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
         )

(defules nal5-implication-based-composition
     "<h1><a href=\"NAL-Specification.pdf#page=50\" style=\"text-decoration:none\">NAL5 implication based composition</a></h1><br/>  <!-- target=\"bible\" -->
     Similar as in classical logics, more complicated statements, involving conjunctions and disjunctions can be composed,
     this rules are responsible for this. Note that for the conjunction, &&, there is again a temporal
     variant &| for concurrent conjunction, and &/ for sequential conjunction.
     Note that all conjunction variants are associative, but only &| and && are commutative exactly as their semantics suggest.
     The key purpose of these rules is to make it possible for the system to identify conditions or preconditions for certain statements."
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
         )

(defules nal5-nal8-implication-based-decomposition
        "<h1><a href=\"NAL-Specification.pdf#page=49\" style=\"text-decoration:none\">NAL5 implication based decomposition and procedural inference</a></h1><br/>  <!-- target=\"bible\" -->
       Analogical to the previous decomposition rules, decomposition rules for the ==> makes it possible for the system to decompose what was composed by the previous rules,
       they key purpose of these rules is to make it possible for the system to derive the consequences of a conditional statement when the condition is fullfilled,
       as well as finding a possible explaination for the truth of the postcondition."

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
  #R[(&& :list/A) Ai |- Ai :pre (:contains? (:list/A) Ai) :post (:t/structural-deduction :d/structural-strong)]
  #R[(&/ :list/A) Ai |- Ai :pre (:contains? (:list/A) Ai) :post (:t/structural-deduction :d/structural-strong)]
  #R[(&| :list/A) Ai |- Ai :pre (:contains? (:list/A) Ai) :post (:t/structural-deduction :d/structural-strong)]
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
         )

(defules nal5-multi-conditional-syllogism
         "<h1><a href=\"NAL-Specification.pdf#page=49\" style=\"text-decoration:none\">NAL5 implication based decomposition</a></h1><br/>  <!-- target=\"bible\" -->
         Additionally, there are some rules which allow syllogism-style inference directly happen on compounds with conjunctions,
         whether this rules are really necessary, I am not convinced, but they are at least valid.
         "
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

   ; precondition combiner inference rule (variable_unification6):
   #R[((&& C :list/A) ==> Z) ((&& C :list/B) ==> Z) |- (((&& :list/A) ==> (&& :list/B)) :post (:t/induction)
                                                         ((&& :list/B) ==> (&& :list/A)) :post (:t/induction))]
   #R[(Z ==> (&& C :list/A)) (Z ==> (&& C :list/B)) |- (((&& :list/A) ==> (&& :list/B)) :post (:t/abduction)
                                                         ((&& :list/B) ==> (&& :list/A)) :post (:t/abduction))]
         )

(defules nal6-variable-introduction
         "<h1><a href=\"NAL-Specification.pdf#page=57\" style=\"text-decoration:none\">NAL6 Variable Introduction</a></h1><br/>  <!-- target=\"bible\" -->
         The system has the ability to introduce variabes,
         similar as in FOPL where there is a all-quantor and an exists-quantor,
         where here independent-variables correspond to the all-quantor roughly and are written like $X,
         while dependent-variables correspond to the exists-quantor and are written like #X
         Note that there are also temporal variants here,
         allowing the system to introduce variables while not loosing the temporal information between the statements.
         Whether the temporal variants are really needed, is questionable.
         The purpose of variable introduction itself is mainly to make it possible for the system to talk
         abstractly but still precisely about things like the statement \"for every lock there exists a key which opens it\" demands.
         "
  ; variable introduction
  ; Introduce variables by common subject or predicate
  #R[(S --> M) (P --> M) |- (((P --> $X) ==> (S --> $X)) :post (:t/abduction)
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

  #R[(M --> S) (M --> P) |- (((&/ ($X --> P) I) =/> ($X --> S))  :post (:t/induction :linkage-temporal)
                              (($X --> S) =\> (&/ ($X --> P) I)) :post (:t/abduction :linkage-temporal)
                              ((&/ ($X --> P) I) </> ($X --> S)) :post (:t/comparison :linkage-temporal)
                              (&/ (#Y --> P) I (#Y --> S)) :post (:t/intersection :linkage-temporal))
     :pre ((:!= S P) (:measure-time I))]

  #_#R[(M --> S) (M --> P) |- ((($X --> S) =|> ($X --> P)) :post (:t/induction :linkage-temporal)
                              (($X --> P) =|> ($X --> S)) :post (:t/abduction :linkage-temporal)
                              (($X --> S) <|> ($X --> P)) :post (:t/comparison :linkage-temporal)
                              (&| (#Y --> S) (#Y --> P)) :post (:t/intersection :linkage-temporal))
     :pre ((:!= S P) (:concurrent (M --> P) (M --> S)))]

         )


(defules nal6-variable-syllogisms
         "<h1><a href=\"NAL-Specification.pdf#page=57\" style=\"text-decoration:none\">NAL6 Variable Syllogisms</a></h1><br/>  <!-- target=\"bible\" -->
         Additionally, these rules are valid due to the semantics of the dependent variables.
         Whether these rules are really needed is however questionable.
         "
; conditional abduction by dependent variable
#R[((A --> R) ==> Z) ((&& (#Y --> B) (#Y --> R) :list/A) ==> Z) |- (A --> B) :post (:t/abduction)]
#R[((A --> R) ==> Z) ((&& (#Y --> B) (#Y --> R)) ==> Z) |- (A --> B) :post (:t/abduction)]

; conditional deduction "An inverse inference has been implemented as a form of deduction" https://code.google.com/p/open-nars/issues/detail?id=40&can=1
#R[(U --> L) ((&& (#X --> L) (#X --> R)) ==> Z) |- ((U --> R) ==> Z) :post (:t/deduction)]
#R[(U --> L) ((&& (#X --> L) (#X --> R) :list/A) ==> Z) |- ((&& (U --> R) :list/A) ==> Z) :pre ((:substitute #X U)) :post (:t/deduction)]
         )

(defules nal6-multiple-variable-introduction
         "<h1><a href=\"NAL-Specification.pdf#page=57\" style=\"text-decoration:none\">NAL6 Multiple Variable Introduction</a></h1><br/>  <!-- target=\"bible\" -->
         In order to introduce additional variables and after one was already introduced,
         and in order to handle multiples these, these rules exist,
         allowing the system to create more complicated abstractions and to work with them.
         The purpose of these rules is the same as for variable introduction, while extending the principle to an arbitrary amount of variables.
         "

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
         )


(defules nal6-variable-elimination
         "<h1><a href=\"NAL-Specification.pdf#page=57\" style=\"text-decoration:none\">NAL6 Variable Elimination</a></h1><br/>  <!-- target=\"bible\" -->
         Additionally, the system has to be able to eliminate variables by filling in the pattern of the second premise,
         so to specialize a statement when a premise fits in its scheme, this holds for independent as well as dependent variables.
         The purpose of this is to specialize general statements containing variables to specific cases."
   ; dependent variable elimination
   ; Decomposition with elimination of a variable
   #R[B (&& A :list/A) |- (&& :list/A) :pre (:judgement? (:substitute-if-unifies "#" A B)) :post (:t/anonymous-analogy :d/strong :order-for-all-same :seq-interval-from-premises)]


   ; independent variable elimination
  #_#R[B (A ==> C) |- C (:t/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-forward unused ==>))]
  #_#R[B (C ==> A) |- C (:t/abduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-backward unused ==>))]

  #_#R[B (A <=> C) |- C (:t/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-backward unused <=>))]
  #_#R[B (C <=> A) |- C (:t/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" A B) (:shift-occurrence-forward unused <=>))]

         )

(defules nal6-second-layer-variable-handling
         "<h1><a href=\"NAL-Specification.pdf#page=57\" style=\"text-decoration:none\">NAL6 Second Level Variable Handling</a></h1><br/>  <!-- target=\"bible\" -->
         There were some meaningful cases where variables had to be handled by specific rules at a deeper level,
         this is what these rules are about. I am still not convinced whether these rules are really needed though."
  ; second level variable handling rules
  ; second level variable elimination (termlink level2 growth needed in order for these rules to work)
  #R[(A --> K) (&& (#X --> L) (($Y --> K) ==> (&& :list/A))) |- (&& (#X --> L) :list/A) :pre ((:substitute $Y A)) :post (:t/deduction)]
  #R[(A --> K) (($X --> L) ==> (&& (#Y --> K) :list/A)) |- (($X --> L) ==> (&& :list/A)) :pre ((:substitute #Y A)) :post (:t/anonymous-analogy)]
         )

(defules nal7-temporal-inference
         "<h1><a href=\"NAL-Specification.pdf#page=61\" style=\"text-decoration:none\">NAL7 Temporal Inference</a></h1><br/>  <!-- target=\"bible\" -->
         Altough all above rules also work for temporal statements, there are rules which are only for reasoning about time,
         these are them. The most important one of these is temporal induction:
         Temporal induction, a NAL7 principle, allows the system to temporally relate events.
         the <b> ==&gt; &lt;=&gt; </b> truth-related copulas are extended to capture whether two events happen after
         each other, <b> a =/> b  </b>, or concurrently <b> a =|&gt; </b> These operators are all transitive, also
         \\\\( \\forall a,b,c \\\\) events with truth values \\\\( T1, T2 \\in \\[0, 1\\] \\times \\[0, 1\\]: \\\\)
         <b> a =/> b </b> \\\\( \\wedge \\\\) <b> b =|> c </b> \\\\( \\implies \\\\) <b> a =|> c </b> with truth-value
         <b> induction( T1 , T2) </b> holds, consistent with the semantics of the copulas. Additionally intervals are
         used to measure the temporal occurrence time difference between the events. In order to support this,
         predicate <b> measure_time(I)</b> is introduced which is true if and only if the the time difference between
         both event premises is <b> I </b>. In the language, the time difference is encoded in the sequence, for
         example <b> ((&/,a,/10) =/> b) </b> encodes that <b> b </b> happens <b> 10  </b>  steps after <b> a </b>."
  ;; NAL7 specific inference
  ; Reasoning about temporal statements. those are using the ==> relation because relation in time is a relation of the truth between statements.
#R[X ((&/ K (:interval I)) ==> B) |- B  :post (:t/deduction :d/induction :order-for-all-same) :pre ((:substitute-if-unifies "$" K X) (:shift-occurrence-forward I ==>))]
#_#R[X (XI ==> B) |- B  :post (:t/deduction :d/induction :order-for-all-same) :pre ((:substitute-if-unifies "$" XI (&/ X :interval)) (:shift-occurrence-forward XI ==>))]
#_#R[X (BI ==> Y) |- BI :post (:t/abduction :d/deduction :order-for-all-same) :pre ((:substitute-if-unifies "$" Y X) (:shift-occurrence-backward BI ==>))]

  ; Temporal induction:
  ; When P and then S happened according to an observation by induction (weak) it may be that alyways after P usually S happens.
  #R[P S |- (((&/ S I) =/> P) :post (:t/induction :linkage-temporal)
              (P =\> (&/ S I)) :post (:t/abduction :linkage-temporal)
              ((&/ S I) </> P) :post (:t/comparison :linkage-temporal)
              (&/ S I P) :post (:t/intersection :linkage-temporal))
     :pre ((:measure-time I))]
  #R[P S |- ((S =|> P) :post (:t/induction :linkage-temporal)
              (S <|> P) :post (:t/comparison :linkage-temporal)
              (&| S P) :post (:t/intersection :linkage-temporal))
     :pre [(:concurrent Task Belief) (:not-implication-or-equivalence P) (:not-implication-or-equivalence S)]]

  ; here now are the backward inference rules which should really only work on backward inference:
  #R[(A --> S) (B --> S) |- ((A --> B) :post (:p/question)
                              (B --> A) :post (:p/question)
                              (A <-> B) :post (:p/question))
     :pre (:question?)]
         )

(defules backward-driven-forward-inference
         "<h1>Backward driven forward inference</h1><br/>  <!-- target=\"bible\" -->
         For some rules it is better to only let them succeed if there is a question which explicitly asks for their result.
         However whether this is really needed is questionable for me, but it has benefits in the preliminary form
         our control mechanism currently is in.
         "
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

;; <script>
;; function replace_html(a,b){ while(document.body.innerHTML.contains(a)) {
;;  document.body.innerHTML = document.body.innerHTML.replace(a, b); } }
;;  replace_html('#R[','    '); replace_html(')]',') '); replace_html('list/A','A_1..n'); replace_html('list/B','B_1..m');
;; </script>
