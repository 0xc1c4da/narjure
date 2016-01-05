(ns nal.args-processing
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.core.logic :refer
             [run* project fresh conde conso run membero defne fne
              nonlvaro == emptyo succeed fail conda s# u# onceo all defna]
             :as l]
            [nal.utils :refer [subtract]]
            [clojure.core.logic.pldb :refer [db-rel db with-db]]))

(declare equ-product same include1 include not-member dependent)

(defna dependent [A1 A2 A3]
  ([['var V L] Y ['var V [Y . L]]])
  ([[H . T] Y [H1 . T1]] (dependent H Y H1) (dependent T Y T1))
  ([['inheritance S P] Y ['inheritance S1 P1]]
    (dependent S Y S1) (dependent P Y P1))
  ([['ext-image R A] Y ['ext-image R A1]] (dependent A Y A1))
  ([['int-image R A] Y ['int-image R A1]] (dependent A Y A1))
  ([X _ X]))

(defne equ-product [A1 A2 A3]
  ([[] [] []])
  ([[T . Ls] [T . Lp] L]
    (equ-product Ls Lp L))
  ([[S . Ls] [P . Lp] [['inheritance S P] . L]]
    (equ-product Ls Lp L)))

(defne same [A1 A2]
  ([[] []])
  ([L [H . T]]
    (nonlvaro L)
    (fresh [L1] (membero H L) (subtract L [H] L1) (same L1 T))))

(defn same-set [X Y]
  (fresh [x]
    (nonlvaro X) (l/!= X []) (l/!= X [x]) (same X Y)
    (project [X Y] (l/!= X Y))))

(defne include1 [A1 A2]
  ([[] _])
  ([[H . T1] [H . T2]]
    (include1 T1 T2))
  ([[H1 . T1] [H2 . T2]]
    (project [H1 H2] (== true (not= H2 H1)))
    (include1 A1 T2)))

(defn include [L1 L2]
  (all (nonlvaro L2) (include1 L1 L2)
       (project [L1 L2] (l/!= L1 []) (l/!= L1 L2))))

;TODO is not covered with tests
(defn not-member [E C]
  (fresh [X]
    (conde [(emptyo C)]
           [(conso E X C) fail]
           ;[(fresh [S T S1] (== E [S T]) (conso [S1 T] X C) (equivalence S S1) fail)]
           [(fresh [L] (conso X L C) (not-member L C))])))

;(defne not-membero [x l]
;  ([_ []])
;  ([_ [?y . ?r]]
;    (!= x ?y)
;    (not-membero x ?r)))

(defn replace
  ([A1 A2 A3]
   ((fne [A1 A2 A3]
         ([[T . L] T [nil . L]])
         ([[H . L] T [H . L1]] (replace L T L1)))
     A1 A2 A3))
  ([A1 A2 A3 A4]
   ((fne [A1 A2 A3 A4]
         ([[H1 . T] H1 [H2 . T] H2])
         ([[H . T1] H1 [H . T2] H2] (replace T1 H1 T2 H2)))
     A1 A2 A3 A4)))

