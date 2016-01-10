(ns nal.core
  (:refer-clojure :exclude [!= == >= <= > < =])
  (:require [clojure.core.logic :refer
             [project fresh conde conda defna conso membero nonlvaro == !=
              defne appendo all or* u# s# onceo lvar fne]
             :as l]
            [clojure.core.logic.arithmetic :refer :all]
            [nal.utils :refer :all]
            [nal.truth-value :refer :all]
            [nal.args-processing :refer :all]
            [clojure.core.logic.pldb :refer [db-rel db with-db]]))

(declare similarity inheritance ext-intersection int-intersection
         instance property ext-set int-set product negation
         inst-prop ext-difference revision inference equivalence
         inference2 inference3 equivalence-list reduceo replace-var
         replace-all implication)

;===============================================================================
;revision

(defna revision [R1 R2 R]
  ([[S T1] [S T2] [S T]] (f-rev T1 T2 T)))

;===============================================================================
;choice

(defna choice [A1 A2 A3]
  ([[S [F1 C1]] [S [_F2 C2]] [S [F1 C1]]] (>= C1 C2))
  ([[S [_F1 C1]] [S [F2 C2]] [S [F2 C2]]] (< C1 C2))
  ([[S1 T1] [S2 T2] [S1 T1]]
    (fresh [E1 E2]
      (!= S1 S2) (f-exp T1 E1) (f-exp T2 E2) (>= E1 E2)))
  ([[S1 T1] [S2 T2] [S2 T2]]
    (fresh [E1 E2]
      (!= S1 S2) (f-exp T1 E1) (f-exp T2 E2) (< E1 E2))))

;===============================================================================
;simplified version

(defne infer3 [T1 T2 T3]
  ([['inheritance W1 ['ext-image ['ext-image 'represent [nil ['inheritance ['product [X T2]] R]]] [nil W2 W3]]]
    ['inheritance W1 ['ext-image 'represent [nil X]]]
    [['inheritance ['ext-image 'represent [nil Y]] ['ext-image ['ext-image 'represent [nil ['inheritance ['product [Y T2]] R]]] [nil W2 W3]]] V]]
    (f-ind [10.9] [1 0.9] V))

  ([['inheritance W3 ['ext-image ['ext-image 'represent [nil ['inheritance ['product [T1 X]] R]]] [W1 W2 nil]]]
    ['inheritance W3 ['ext-image 'represent [nil X]]]
    [['inheritance ['ext-image 'represent [nil Y]] ['ext-image ['ext-image 'represent [nil ['inheritance ['product [T1 Y]] R]]] [W1 W2 nil]]] V]]
    (f-ind [10.9] [1 0.9] V))

  ([T1 T2 T] (inference [T1 [1 0.9]] [T2 [1 0.9]] T)))

(defn infer
  ([T1 T2] (inference [T1 [1 0.9]] T2))
  ([T1 T2 T3] (infer3 T1 T2 T3)))

;===============================================================================
;inference


(defn- call [vec]
  (all
    (nonlvaro vec)
    (project [vec]
      (let [[predicat & args] vec
            vr (ns-resolve 'nal.core predicat)]
        (if vr (apply vr args) u#)))))

(defne inference2 [A1 A2]
  ;immediate inference
  ([[['inheritance S P] T1] [['inheritance P S] T]] (f-cnv T1 T))
  ([[['implication S P] T1] [['implication P S] T]] (f-cnv T1 T))
  ([[['implication ['negation S] P] T1] [['implication ['negation P] S] T]]
    (f-cnt T1 T))
  ([[['negation S] T1] [S T]] (f-neg T1 T))
  ([[S [F1 C1]] [['negation S] T]] (< F1 0.5) (f-neg [F1 C1] T))
  ;structural inference
  ([[S1 T] [S T]]
    (conda [(all (nonlvaro S) (reduceo S1 S) (!= S1 S))]
           [(or* [(equivalence S1 S) (equivalence S S1)])]))
  ([P C] (fresh [S] (inference3 P [S [1 1]] C) (call S)))
  ([P C] (fresh [S] (inference3 [S [1 1]] P C) (call S))))

(defne inference3 [A1 A2 A3]
  ;inheritance-based syllogism
  ([[['inheritance M P] T1] [['inheritance S M] T2] [['inheritance S P] T]]
    (noto= S P) (f-ded T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['inheritance S P] T]]
    (noto= S P) (f-abd T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['inheritance S P] T]]
    (noto= S P) (f-ind T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance M S] T2] [['inheritance S P] T]]
    (noto= S P) (f-exe T1 T2 T))
  ; similarity from inheritance
  ([[['inheritance S P] T1] [['inheritance P S] T2] [['similarity S P] T]]
    (f-int T1 T2 T))
  ; similarity-based syllogism
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['similarity S P] T]]
    (noto= S P) (f-com T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['similarity S P] T]]
    (noto= S P) (f-com T1 T2 T))
  ([[['inheritance M P] T1] [['similarity S M] T2] [['inheritance S P] T]]
    (noto= S P) (f-ana T1 T2 T))
  ([[['inheritance P M] T1] [['similarity S M] T2] [['inheritance P S] T]]
    (noto= S P) (f-ana T1 T2 T))
  ([[['similarity M P] T1] [['similarity S M] T2] [['similarity S P] T]]
    (noto= S P) (f-res T1 T2 T))
  ; inheritance-based composition
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['inheritance N M] T]]
    (noto= S P) (reduceo ['int-intersection [P S]] N) (f-int T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['inheritance N M] T]]
    (noto= S P) (reduceo ['ext-intersection [P S]] N) (f-uni T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['inheritance N M] T]]
    (noto= S P) (reduceo ['int-difference P S] N) (f-dif T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['inheritance M N] T]]
    (noto= S P) (reduceo ['ext-intersection [P S]] N) (f-int T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['inheritance M N] T]]
    (noto= S P) (reduceo ['int-intersection [P S]] N) (f-uni T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['inheritance M N] T]]
    (noto= S P) (reduceo ['ext-difference P S] N) (f-dif T1 T2 T))
  ; inheirance-based decomposition
  ([[['inheritance S M] T1] [['inheritance ['int-intersection L] M] T2] [['inheritance P M] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['int-intersection N] P) (f-pnn T1 T2 T)))
  ([[['inheritance S M] T1] [['inheritance ['ext-intersection L] M] T2] [['inheritance P M] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['ext-intersection N] P) (f-npp T1 T2 T)))
  ([[['inheritance S M] T1] [['inheritance ['int-difference S P] M] T2] [['inheritance P M] T]]
    (atomo S) (atomo P) (f-pnp T1 T2 T))
  ([[['inheritance S M] T1] [['inheritance ['int-difference P S] M] T2] [['inheritance P M] T]]
    (atomo S) (atomo P) (f-nnn T1 T2 T))
  ([[['inheritance M S] T1] [['inheritance M ['ext-intersection L]] T2] [['inheritance M P] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['ext-intersection N] P) (f-pnn T1 T2 T)))
  ([[['inheritance M S] T1] [['inheritance M ['int-intersection L]] T2] [['inheritance M P] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['int-intersection N] P) (f-npp T1 T2 T)))

  ([[['inheritance M S] T1] [['inheritance M ['ext-difference S P]] T2] [['inheritance M P] T]]
    (atomo S) (atomo P) (f-pnp T1 T2 T))
  ([[['inheritance M S] T1] [['inheritance M ['ext-difference P S]] T2] [['inheritance M P] T]]
    (atomo S) (atomo P) (f-nnn T1 T2 T))
  ; implication-based syllogism
  ([[['implication M P] T1] [['implication S M] T2] [['implication S P] T]]
    (noto= S P) (f-ded T1 T2 T))
  ([[['implication P M] T1] [['implication S M] T2] [['implication S P] T]]
    (noto= S P) (f-abd T1 T2 T))
  ([[['implication M P] T1] [['implication M S] T2] [['implication S P] T]]
    (noto= S P) (f-ind T1 T2 T))
  ([[['implication P M] T1] [['implication M S] T2] [['implication S P] T]]
    (noto= S P) (f-exe T1 T2 T))
  ; implication to equivalence
  ([[['implication S P] T1] [['implication P S] T2] [['equivalence S P] T]]
    (f-int T1 T2 T))
  ; equivalence-based syllogism
  ([[['implication P M] T1] [['implication S M] T2] [['equivalence S P] T]]
    (noto= S P) (f-com T1 T2 T))
  ([[['implication M P] T1] [['implication M S] T2] [['equivalence S P] T]]
    (noto= S P) (f-com T1 T2 T))
  ([[['implication M P] T1] [['equivalence S M] T2] [['implication S P] T]]
    (noto= S P) (f-ana T1 T2 T))
  ([[['implication P M] T1] [['equivalence S M] T2] [['implication P S] T]]
    (noto= S P) (f-ana T1 T2 T))
  ([[['equivalence M P] T1] [['equivalence S M] T2] [['equivalence S P] T]]
    (noto= S P) (f-res T1 T2 T))
  ; implication-based composition
  ([[['implication P M] T1] [['implication S M] T2] [['implication N M] T]]
    (noto= S P) (reduceo ['disjunction [P S]] N) (f-int T1 T2 T))
  ([[['implication P M] T1] [['implication S M] T2] [['implication N M] T]]
    (noto= S P) (reduceo ['conjunction [P S]] N) (f-uni T1 T2 T))
  ([[['implication M P] T1] [['implication M S] T2] [['implication M N] T]]
    (noto= S P) (reduceo ['conjunction [P S]] N) (f-int T1 T2 T))
  ([[['implication M P] T1] [['implication M S] T2] [['implication M N] T]]
    (noto= S P) (reduceo ['disjunction [P S]] N) (f-uni T1 T2 T))
  ; implication-based decomposition
  ([[['implication S M] T1] [['implication ['disjunction L] M] T2] [['implication P M] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['disjunction N] P) (f-pnn T1 T2 T)))
  ([[['implication S M] T1] [['implication ['conjunction L] M] T2] [['implication P M] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['conjunction N] P) (f-npp T1 T2 T)))
  ([[['implication M S] T1] [['implication M ['conjunction L]] T2] [['implication M P] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['conjunction N] P) (f-pnn T1 T2 T)))
  ([[['implication M S] T1] [['implication M ['disjunction L]] T2] [['implication M P] T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['disjunction N] P) (f-npp T1 T2 T)))
  ; conditional syllogism
  ([[['implication M P] T1] [M T2] [P T]]
    ;(nonlvaro P1) (== P1 P)
    (groundo P) (f-ded T1 T2 T))
  ([[['implication P M] T1] [M T2] [P T]]
    (groundo P) (f-abd T1 T2 T))
  ([[M T1] [['equivalence S M] T2] [S T]]
    (groundo S) (f-ana T1 T2 T))
  ; conditional composition
  ([[P T1] [S T2] [C T]]
    (project [S P] (= C ['implication S P])) (f-ind T1 T2 T))
  ([[P T1] [S T2] [C T]]
    (project [S P] (= C ['equivalence S P])) (f-com T1 T2 T))
  ([[P T1] [S T2] [C T]]
    (fresh [N]
      (reduceo ['conjunction [P S]] N)
      (project [N] (= N C)) (f-int T1 T2 T)))
  ([[P T1] [S T2] [C T]]
    (fresh [N]
      (reduceo ['disjunction [P S]] N)
      (project [N] (= N C)) (f-uni T1 T2 T)))
  ; propositional decomposition
  ([[S T1] [['conjunction L] T2] [P T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['conjunction N] P) (f-pnn T1 T2 T)))
  ([[S T1] [['disjunction L] T2] [P T]]
    (nonlvaro S) (nonlvaro L) (membero S L)
    (fresh [N]
      (subtracto L [S] N) (reduceo ['disjunction N] P) (f-npp T1 T2 T)))
  ; multi-conditional syllogism
  ([[['implication ['conjunction L] C] T1] [M T2] [['implication P C] T]]
    (fresh [A]
      (nonlvaro L) (membero M L) (subtracto L [M] A)
      (!= A []) (reduceo ['conjunction A] P) (f-ded T1 T2 T)))
  ([[['implication ['conjunction L] C] T1] [['implication P C] T2] [M T]]
    (fresh [A]
      (nonlvaro L) (membero M L) (subtracto L [M] A) (!= A [])
      (reduceo ['conjunction A] P) (f-abd T1 T2 T)))
  ([[['implication ['conjunction L] C] T1] [M T2] [S T]]
    (fresh [x] (conso M L x)
      (project [x C] (= S ['implication ['conjunction x] C])) (f-ind T1 T2 T)))
  ([[['implication ['conjunction Lm] C] T1] [['implication A M] T2] [['implication P C] T]]
    (fresh [La]
      (nonlvaro Lm) (replaceo Lm M La A)
      (reduceo ['conjunction La] P) (f-ded T1 T2 T)))
  ([[['implication ['conjunction Lm] C] T1] [['implication ['conjunction La] C] T2] [['implication A M] T]]
    (nonlvaro Lm) (replaceo Lm M La A) (f-abd T1 T2 T))
  ([[['implication ['conjunction La] C] T1] [['implication A M] T2] [['implication P C] T]]
    (fresh [Lm]
      (nonlvaro La) (replaceo Lm M La A)
      (reduceo ['conjunction Lm] P) (f-ind T1 T2 T)))
  ; variable introduction
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['implication ['inheritance X S] ['inheritance X P]] T]]
    (noto= S P) (f-ind T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['implication ['inheritance P X] ['inheritance S X]] T]]
    (noto= S P) (f-abd T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['equivalence ['inheritance X S] ['inheritance X P]] T]]
    (noto= S P) (f-com T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['equivalence ['inheritance P X] ['inheritance S X]] T]]
    (noto= S P) (f-com T1 T2 T))
  ([[['inheritance M P] T1] [['inheritance M S] T2] [['conjunction [['inheritance ['var Y []] S] ['inheritance ['var Y []] P]]] T]]
    (noto= S P) (f-int T1 T2 T))
  ([[['inheritance P M] T1] [['inheritance S M] T2] [['conjunction [['inheritance S ['var Y []]] ['inheritance P ['var Y []]]]] T]]
    (noto= S P) (f-int T1 T2 T))
  ; 2nd variable introduction
  ([[['implication A ['inheritance M1 P]] T1] [['inheritance M2 S] T2] [['implication ['conjunction [A ['inheritance X S]]] ['inheritance X P]] T]]
    (noto= S P) (= M1 M2) (noto= A ['inheritance M2 S]) (f-ind T1 T2 T))
  ([[['implication A ['inheritance M1 P]] T1] [['inheritance M2 S] T2] [['conjunction [['implication A ['inheritance ['var Y []] P]] ['inheritance ['var Y []] S]]] T]]
    (noto= S P) (= M1 M2) (noto= A ['inheritance M2 S]) (f-int T1 T2 T))
  ([[['conjunction L1] T1] [['inheritance M S] T2] [['implication ['inheritance Y S] ['conjunction [['inheritance Y P2] . L3]]] T]]
    (fresh [P L2]
      (subtracto L1 [['inheritance M P]] L2) (noto= L1 L2)
      (noto= S P) (dependento P Y P2) (dependento L2 Y L3) (f-ind T1 T2 T)))
  ([[['conjunction L1] T1] [['inheritance M S] T2] [['conjunction [['inheritance ['var Y []] S] ['inheritance ['var Y []] P] . L2]] T]]
    (subtracto L1 [['inheritance M P]] L2) (noto= L1 L2) (noto= S P) (f-int T1 T2 T))
  ([[['implication A ['inheritance P M1]] T1] [['inheritance S M2] T2] [['implication ['conjunction [A ['inheritance P X]]] ['inheritance S X]] T]]
    (noto= S P) (= M1 M2) (noto= A ['inheritance S M2]) (f-abd T1 T2 T))
  ([[['implication A ['inheritance P M1]] T1] [['inheritance S M2] T2] [['conjunction [['implication A ['inheritance P ['var Y []]]] ['inheritance S ['var Y []]]]] T]]
    (noto= S P) (= M1 M2) (noto= A ['inheritance S M2]) (f-int T1 T2 T))
  ([[['conjunction L1] T1] [['inheritance S M] T2] [['implication ['inheritance S Y] ['conjunction [['inheritance P2 Y] . L3]]] T]]
    (fresh [P L2]
      (subtracto L1 [['inheritance P M]] L2) (noto= L1 L2) (noto= S P)
      (dependento P Y P2) (dependento L2 Y L3) (f-abd T1 T2 T)))
  ([[['conjunction L1] T1] [['inheritance S M] T2] [['conjunction [['inheritance S ['var Y []]] ['inheritance P ['var Y []]] . L2]] T]]
    (subtracto L1 [['inheritance P M]] L2) (noto= L1 L2) (noto= S P) (f-int T1 T2 T))
  ; dependent variable elimination
  ([[['conjunction L1] T1] [['inheritance M S] T2] [C T]]
    (fresh [N D L2 L3 T0]
      (subtracto L1 [['inheritance ['var N D] S]] L2) (project [L1 L2] (!= L1 L2))
      (replace-var L2 ['var N D] L3 M) (reduceo ['conjunction L3] C)
      (f-cnv T2 T0) (f-ana T1 T0 T)))
  ([[['conjunction L1] T1] [['inheritance S M] T2] [C T]]
    (fresh [N D L2 L3 T0]
      (subtracto L1 [['inheritance S ['var N D]]] L2) (project [L1 L2] (!= L1 L2))
      (replace-var L2 ['var N D] L3 M) (reduceo ['conjunction L3] C)
      (f-cnv T2 T0) (f-ana T1 T0 T))))

(defn inference
  ([A1 A2] (inference2 A1 A2))
  ([A1 A2 A3] (inference3 A1 A2 A3)))

(defna replace-var [A1 A2 A3 A4]
  ([[] _ [] _])
  ([[['inheritance S1 P] . T1] S1 [['inheritance S2 P] . T2] S2]
    (replace-var T1 S1 T2 S2))
  ([[['inheritance S P1] . T1] P1 [['inheritance S P2] . T2] P2]
    (replace-var T1 P1 T2 P2)))

(defne replace-all [A1 A2 A3 A4]
  ([[H . T1] H1 [H . T2] H2]
    (replace-var T1 H1 T2 H2)))

;===============================================================================
;inheritance

(defna inheritance [A1 A2]
  ([['ext-intersection Ls] P] (includeo [P] Ls))
  ([S ['int-intersection Lp]] (includeo [S] Lp))
  ([['ext-intersection S] ['ext-intersection P]] (includeo P S) (!= P [(lvar)]))
  ([['int-intersection S] ['int-intersection P]] (includeo S P) (!= S [(lvar)]))
  ([['ext-set S] ['ext-set P]] (includeo S P))
  ([['int-set S] ['int-set P]] (includeo P S))
  ([['ext-difference S P] S] (nonlvaro S) (nonlvaro P))
  ([S ['int-difference S P]] (nonlvaro S) (nonlvaro P))
  ([['product L1] R]
    (fresh [L2]
      (nonlvaro L1) (membero ['ext-image R L2] L1)
      (replaceo L1 ['ext-image R L2] L2)))
  ([R ['product L1]]
    (fresh [L2]
      (nonlvaro L1) (membero ['int-image R L2] L1)
      (replaceo L1 ['int-image R L2] L2))))

;===============================================================================
;similarity
; There is a green cut in prolog's version, the second version of similarity
; implements it.
(defna similarity [A1 A2]
  ([X Y] (all (nonlvaro X) (reduceo X Y) (!= X Y)))
  ([['ext-intersection L1] ['ext-intersection L2]] (same-seto L1 L2))
  ([['int-intersection L1] ['int-intersection L2]] (same-seto L1 L2))
  ([['ext-set L1] ['ext-set L2]] (same-seto L1 L2))
  ([['int-set L1] ['int-set L2]] (same-seto L1 L2)))

;===============================================================================
;implication

(defna implication [A1 A2]
  ([['similarity S P] ['inheritance S P]])
  ([['equivalence S P] ['implication S P]])
  ([['conjunction L] M] (nonlvaro L) (membero M L))
  ([M ['disjunction L]] (nonlvaro L) (membero M L))
  ([['conjunction L1] ['conjunction L2]]
    (nonlvaro L1) (nonlvaro L2) (subseto L2 L1))
  ([['disjunction L1] ['disjunction L2]]
    (nonlvaro L1) (nonlvaro L2) (subseto L1 L2))
  ([['inheritance S P]
    ['inheritance ['ext-intersection Ls] ['ext-intersection Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['inheritance S P]
    ['inheritance ['int-intersection Ls] ['int-intersection Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['similarity S P]
    ['similarity ['ext-intersection Ls] ['ext-intersection Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['similarity S P]
    ['similarity ['int-intersection Ls] ['int-intersection Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['inheritance S P]
    ['inheritance ['ext-difference S M] ['ext-difference P M]]] (nonlvaro M))
  ([['inheritance S P]
    ['inheritance ['int-difference S M] ['int-difference P M]]] (nonlvaro M))
  ([['similarity S P] ['similarity ['ext-difference S M] ['ext-difference P M]]]
    (nonlvaro M))
  ([['similarity S P] ['similarity ['int-difference S M] ['int-difference P M]]]
    (nonlvaro M))
  ([['inheritance S P] ['inheritance ['ext-difference M P] ['ext-difference M S]]]
    (nonlvaro M))
  ([['inheritance S P] ['inheritance ['int-difference M P] ['int-difference M S]]]
    (nonlvaro M))
  ([['similarity S P] ['similarity ['ext-difference M P] ['ext-difference M S]]]
    (nonlvaro M))
  ([['similarity S P] ['similarity ['int-difference M P] ['int-difference M S]]]
    (nonlvaro M))
  ([['inheritance S P] ['negation ['inheritance S ['ext-difference M P]]]]
    (nonlvaro M))
  ([['inheritance S ['ext-difference M P]] ['negation ['inheritance S P]]]
    (nonlvaro M))
  ([['inheritance S P] ['negation ['inheritance ['int-difference M S] P]]]
    (nonlvaro M))
  ([['inheritance ['int-difference M S] P] ['negation ['inheritance S P]]]
    (nonlvaro M))
  ([['inheritance S P] ['inheritance ['ext-image S M] ['ext-image P M]]]
    (nonlvaro M))
  ([['inheritance S P] ['inheritance ['int-image S M] ['int-image P M]]]
    (nonlvaro M))
  ([['inheritance S P] [inheritance ['ext-image M Lp] ['ext-image M Ls]]]
    (fresh [L1 L2 x1 x2] (nonlvaro Ls) (nonlvaro Lp)
      (conso S L2 x1) (appendo L1 x1 Ls)
      (conso P L2 x2) (appendo L1 x2 Lp)))
  ([['inheritance S P] [inheritance ['int-image M Lp] ['int-image M Ls]]]
    (fresh [L1 L2 x1 x2]
      (nonlvaro Ls) (nonlvaro Lp)
      (conso S L2 x1) (appendo L1 x1 Ls)
      (conso P L2 x2) (appendo L1 x2 Lp)))
  ([['negation M] ['negation ['conjunction L]]] (includeo [M] L))
  ([['negation ['disjunction L]] ['negation M]] (includeo [M] L))

  ([['implication S P] ['implication ['conjunction Ls] ['conjunction Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['implication S P] ['implication ['disjunction Ls] ['disjunction Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['equivalence S P] ['equivalence ['conjunction Ls] ['conjunction Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp)))
  ([['equivalence S P] ['equivalence ['disjunction Ls] ['disjunction Lp]]]
    (fresh [L] (nonlvaro Ls) (nonlvaro Lp) (replaceo Ls S L P) (sameo L Lp))))


;===============================================================================
;equialence
(defna equivalence [A1 A2]
  ([X Y] (all (nonlvaro X) (reduceo X Y) (!= X Y)))
  ([['similarity S P] ['similarity P S]])
  ([['inheritance S ['ext-set [P]]] ['similarity S ['ext-set [P]]]])
  ([['inheritance ['int-set [S]] P] ['similarity ['int-set [S]] P]])
  ([['inheritance S ['ext-intersection Lp]] ['conjunction L]]
    (fresh [P] (findallo ['inheritance S P] (membero P Lp) L)))
  ([['inheritance ['int-intersection Ls] P] ['conjunction L]]
    (fresh [S] (findallo ['inheritance S P] (membero S Ls) L)))
  ([['inheritance S ['ext-difference P1 P2]]
    ['conjunction [['inheritance S P1] ['negation ['inheritance S P2]]]]])
  ([['inheritance ['int-difference S1 S2] P]
    ['conjunction [['inheritance S1 P] ['negation ['inheritance S2 P]]]]])
  ([['inheritance ['product Ls] ['product Lp]] ['conjunction L]]
    (equ-producto Ls Lp L))
  ([['inheritance ['product [S . L]] ['product [P . L]]] ['inheritance S P]]
    (nonlvaro L))
  ([['inheritance S P] ['inheritance ['product [H . Ls]] ['product [H . Lp]]]]
    (nonlvaro H)
    (equivalence ['inheritance ['product Ls] ['product Lp]] ['inheritance S P]))
  ([['inheritance ['product L] R] ['inheritance T ['ext-image R L1]]]
    (replaceo L T L1))
  ([['inheritance R ['product L]] ['inheritance ['int-image R L1] T]]
    (replaceo L T L1))
  ([['equivalence S P] ['equivalence P S]])
  ([['equivalence ['negation S] P] ['equivalence ['negation P] S]])
  ([['conjunction L1] ['conjunction L2]] (same-seto L1 L2))
  ([['disjunction L1] ['disjunction L2]] (same-seto L1 L2))
  ([['implication S ['conjunction Lp]] ['conjunction L]]
    (fresh [P] (findallo ['implication S P] (membero P Lp) L)))
  ([['implication ['disjunction Ls] P] ['conjunction L]]
    (fresh [S] (findallo ['implication S P] (membero S Ls) L)))
  ([T1 T2]
    (noto (atomo T1)) (noto (atomo T2)) (nonlvaro T1) (nonlvaro T2)
    (fresh [L1 L2] (== T1 L1) (== T2 L2) (equivalence-list L1 L2))))

(defna equivalence-list [A1 A2]
  ([L L])
  ([[H . L1] [H . L2]] (equivalence-list L1 L2))
  ([[H1 . L1] [H2 . L2]] (similarity H1 H2) (equivalence-list L1 L2))
  ([[H1 . L1] [H2 . L2]] (equivalence H1 H2) (equivalence-list L1 L2)))

;===============================================================================
;compound term structure reduction

(defna reduceo [A1 A2]
  ([['similarity ['ext-set [S]] ['ext-set [P]]] ['similarity S P]])
  ([['similarity ['int-set [S]] ['int-set [P]]] ['similarity S P]])
  ([['instance S P] ['inheritance ['ext-set [S]] P]])
  ([['property S P] ['inheritance S ['int-set [P]]]])
  ([['inst-prop S P] ['inheritance ['ext-set [S]] ['int-set [P]]]])
  ([['ext-intersection [T]] T])
  ([['int-intersection [T]] T])
  ([['ext-intersection [['ext-intersection L1] ['ext-intersection L2]]] ['ext-intersection L]]
    (uniono L1 L2 L))
  ([['ext-intersection [['ext-intersection L1] L2]] ['ext-intersection L]]
    (uniono L1 [L2] L))
  ([['ext-intersection [L1 ['ext-intersection L2]]] ['ext-intersection L]]
    (uniono [L1] L2 L))
  ([['ext-intersection [['ext-set L1] ['ext-set L2]]] ['ext-set L]]
    (intersectiono L1 L2 L))
  ([['ext-intersection [['int-set L1] ['int-set L2]]] ['int-set L]]
    (uniono L1 L2 L))
  ([['int-intersection [['int-intersection L1] ['int-intersection L2]]] ['int-intersection L]]
    (uniono L1 L2 L))
  ([['int-intersection [['int-intersection L1] L2]] ['int-intersection L]]
    (uniono L1 [L2] L))
  ([['int-intersection [L1 ['int-intersection L2]]] ['int-intersection L]]
    (uniono [L1] L2 L))
  ([['int-intersection [['int-set L1] ['int-set L2]]] ['int-set L]]
    (intersectiono L1 L2 L))
  ([['int-intersection [['ext-set L1] ['ext-set L2]]] ['ext-set L]]
    (uniono L1 L2 L))
  ([['ext-difference ['ext-set L1] ['ext-set L2]] ['ext-set L]]
    (subtracto L1 L2 L))
  ([['int-difference ['int-set L1] ['int-set L2]] ['int-set L]]
    (subtracto L1 L2 L))
  ([['product ['product L] T] ['product L1]]
    (appendo L [T] L1))
  ([['ext-image ['product L1] L2] T1]
    (membero T1 L1) (replaceo L1 T1 L2))
  ([['int-image ['product L1] L2] T1]
    (membero T1 L1) (replaceo L1 T1 L2))
  ([['negation ['negation S]] S])
  ([['conjunction [T]] T])
  ([['disjunction [T]] T])
  ([['conjunction [['conjunction L1] ['conjunction L2]]] ['conjunction L]]
    (uniono L1 L2 L))
  ([['conjunction [['conjunction L1] L2]] ['conjunction L]]
    (uniono L1 [L2] L))
  ([['conjunction [L1 ['conjunction L2]]] ['conjunction L]]
    (uniono [L1] L2 L))
  ([['disjunction ['disjunction L1] ['disjunction L2]] ['disjunction L]]
    (uniono L1 L2 L))
  ([['disjunction ['disjunction L1] L2] ['disjunction L]]
    (uniono L1 [L2] L))
  ([['disjunction L1 ['disjunction L2]] ['disjunction L]]
    (uniono [L1] L2 L))
  ([X X]))
