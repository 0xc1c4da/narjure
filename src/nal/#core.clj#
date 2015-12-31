(ns nal.core
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.core.logic :refer
             [run* project fresh conde conda defna conso run membero
              nonlvaro == emptyo succeed fail defne appendo] :as l]
            [nal.utils :refer :all]
            [nal.truth-value :refer :all]
            [nal.args-processing :refer :all]
            [clojure.core.logic.pldb :refer [db-rel db with-db]]))

(declare similarity inheritance ext-intersection int-intersection
         instance property union ext-set int-set product negation
         inst-prop ext-difference)

(db-rel ext-intersection p)
(db-rel int-intersection p)
(db-rel ext-set p)
(db-rel int-set p)
(db-rel product p)
(db-rel negation p)
(db-rel inst-prop p)
(db-rel ext-difference p1 p2)
(db-rel property p1 p2)
(db-rel instance p1 p2)
(db-rel inst-prop p1 p2)

;===============================================================================
;revision

(defn revision [R1 R2 R]
  (fresh [S T1 T2 T]
    (== R1 [S T1])
    (== R2 [S T2])
    (== R [S T])
    (f-rev T1 T2 T)))

;===============================================================================
;choice

(defn choice [A1 A2 A3]
  (fresh [S1 S2 T1 T2 F1 F2 C1 C2]
    (== [A1 A2] [[S1 T1] [S2 T2]])
    (== [T1 T2] [[F1 C1] [F2 C2]])
    (conde
      [(== [S1 A3] [S2 [S1 T1]])
       (project [C1 C2] (== true (>= C1 C2)))]
      [(== [S1 A3] [S2 [S1 T2]])
       (project [C1 C2] (== true (< C1 C2)))]
      [(l/!= S1 S2)
       (fresh [E1 E2]
         (f-exp T1 E1) (f-exp T2 E2)
         (conde [(project [E1 E2] (== true (>= E1 E2)))
                 (== A3 [S1 T1])]
                [(project [E1 E2] (== true (< E1 E2)))
                 (== A3 [S2 T2])]))])))
;===============================================================================
;simplified version

;===============================================================================
;inference
;(with-db
;  (db [ext-intersection 'bird]
;      [ext-intersection 'animal])
;  (run 1 [q1]
;       (inference [['animal 'bird] [1 0.9]] q1)))
;=> ([[bird animal] [1 0.4736842105263158]])
(defn inference
  ([A1 A2]
   (fresh [S P T1 T]
     ;[['animal 'bird] T1]
     (== A1 [[S P] T1])
     ;[['bird 'animal T]]
     (== A2 [[P S] T])
     (conde [(inheritance S P)
             (inheritance P S)
             (f-cnv T1 T)])))
  ([A1 A2 A3]
   (fresh [S P M T1 T2 T]
     (== A3 [[S P] T])
     (l/!= S P)
     (conde [(== A1 [[M P] T1]) (== A2 [[S M] T2]) (f-ded T1 T2 T)]))))




;===============================================================================
;inheritance

(defn inheritance [A1 A2]
  (fresh [X]
    (conde [(ext-intersection A1) (include [A2] A1)]
           [(ext-intersection A2) (include [A1] A2)]
           [(ext-intersection A1) (ext-intersection A2)

            (conde [(include A2 A1) (l/!= A2 [X])]
                   ;warning! I'm not sure about this check (l/!= A2 [X])
                   [(include A1 A2) (l/!= A1 [X])])]
           [(int-intersection A1) (int-intersection A2)
            (include A1 A2) (l/!= A1 [X])]
           [(ext-set A1) (ext-set A2) (include A1 A2)]
           [(int-set A1) (int-set A2) (include A2 A1)]
           [(== A1 [A2 X]) (ext-difference A2 X) (nonlvaro A2) (nonlvaro X)]
           [(== A2 [A1 X]) (ext-difference A1 X) (nonlvaro A1) (nonlvaro X)]
           [(product A1) (nonlvaro A1)])))

;===============================================================================
;reduce
(defn reduce [A1 A2]
  (conde
    [(fresh [S P]
       (== A1 [[S] [P]])
       (== A2 [S P])
       (conde
         [(ext-set [S]) (ext-set [P]) (similarity S P)]
         [(int-set [S]) (int-set [P]) (similarity S P)]))]
    #_[(fresh [S P]
         (== A1 [S P])
         (conda
           [(instance S P) (== A2 [[S] P]) (ext-set [S]) (inheritance [S] P)]
           [(property S P) (== A2 [S [P]]) (int-set [P]) (inheritance S [P])]
           [(inst-prop S P) (== A2 [S [P]]) (int-set [P]) (inheritance S [P])]))]))

;TODO I'm not sure about this, because is defined as relation there
; https://github.com/opennars/opennars/blob/radical2/nal/original/prolog/NAL%20User's%20Guide.html
; there is instance/2 in swi prolog
;(defn instance [S P] l/s#)

(defne union [S1 S2 S3]
  ([[] S2 S3] (== S2 S3))
  ([S1 [] S3] (== S1 S3))
  ([S1 S2 []] (== S1 []) (== S1 S3))
  ([S1 S2 S3]
    (fresh [x]
      (conda
        [(nonlvaro S1) (nonlvaro S2)
         (subtract S1 S2 x) (appendo x S2 S3)]
        [(nonlvaro S1) (== S2 S3) (appendo S1 x S3)]
        [(nonlvaro S2) (== S1 S3) (appendo S2 x S3)]))))

(defne intersection [S1 S2 S3]
  #_([[] _ []])
  ([S1 S2 S3]
    (== S3 (run* [q1]
             (fresh [a]
               (membero q1 S2)
               (membero a S1)
               (== a q1))))))

;TODO test if with defna
(defne reduce2 [A1 A2]
  ([[S] [P]]
    (conde [(ext-set [S]) (ext-set [P])]
           [(int-set [S]) (int-set [P])])
    (similarity [S] [P])
    (similarity S P))
  ([[S P] [[S] P]]
    (instance S P) (inheritance [S] P) (ext-set [S]))
  ([[S P] [S [P]]]
    (property S P) (inheritance S [P]) (int-set [P]))
  ([[S P] [[S] [P]]]
    (inst-prop S P) (inheritance [S] [P]) (ext-set [S]) (int-set [P]))
  ([[T] T]
    (conda [(ext-intersection [T])]
           [(int-intersection [T])]))
  ([[L1 L2] L]
    (conde
      [(ext-intersection L1) (ext-intersection L2) (ext-intersection L)
       (ext-intersection [L1 L2]) (union L1 L2 L)]
      [(ext-intersection L1) (ext-intersection L)
       (ext-intersection [L1 L2]) (union L1 [L2] L)]
      [(ext-intersection L2) (ext-intersection L)
       (ext-intersection [L1 L2]) (union L1 [L2] L)])))

(defn similarity [X Y]
  (conde [(nonlvaro X) (reduce X Y) (project [X] (l/!= X Y))]
         [(ext-intersection X) (ext-intersection Y) (same-set X Y)]
         [(int-intersection X) (int-intersection Y) (same-set X Y)]
         [(ext-set X) (ext-set Y) (same-set X Y)]
         [(int-set X) (int-set Y) (same-set X Y)]))
