(ns nal.args-processing
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer
             [run* project fresh conde conso run membero defne fne !=
              nonlvaro == emptyo succeed fail conda s# u# onceo all defna]
             :as l]
            [nal.utils :refer [subtracto]]
            [clojure.core.logic.pldb :refer [db-rel db with-db]]))

(declare equ-producto sameo include1o include not-member dependento)

(defna dependento [A1 A2 A3]
  ([['var V L] Y ['var V [Y . L]]])
  ([[H . T] Y [H1 . T1]] (dependento H Y H1) (dependento T Y T1))
  ([['inheritance S P] Y ['inheritance S1 P1]]
    (dependento S Y S1) (dependento P Y P1))
  ([['ext-image R A] Y ['ext-image R A1]] (dependento A Y A1))
  ([['int-image R A] Y ['int-image R A1]] (dependento A Y A1))
  ([X _ X]))

(defna equ-producto [A1 A2 A3]
  ([[] [] []])
  ([[T . Ls] [T . Lp] L]
    (equ-producto Ls Lp L))
  ([[S . Ls] [P . Lp] [['inheritance S P] . L]]
    (equ-producto Ls Lp L)))

(defna sameo [A1 A2]
  ([[] []])
  ([L [H . T]]
    (nonlvaro L)
    (fresh [L1] (membero H L) (subtracto L [H] L1) (sameo L1 T))))

(defn same-seto [X Y]
  (fresh [x]
    (!= X []) (!= X [x]) (sameo X Y) (project [X Y] (!= X Y))))

(defna include1o [A1 A2]
  ([[] _])
  ([[H . T1] [H . T2]] (include1o T1 T2))
  ([[H1 . T1] [H2 . T2]] (!= H2 H1) (include1o A1 T2)))

(defn includeo [L1 L2]
  (all (nonlvaro L2) (include1o L1 L2) (!= L1 []) (!= L1 L2)))

;never used in project
(defn not-member [E C]
  (fresh [X]
    (conde [(emptyo C)]
           [(conso E X C) fail]
           ;[(fresh [S T S1] (== E [S T]) (conso [S1 T] X C) (equivalence S S1) fail)]
           [(fresh [L] (conso X L C) (not-member L C))])))

(defn replaceo
  ([A1 A2 A3]
   ((fne [A1 A2 A3]
         ([[T . L] T [nil . L]])
         ([[H . L] T [H . L1]] (replaceo L T L1)))
     A1 A2 A3))
  ([A1 A2 A3 A4]
   ((fne [A1 A2 A3 A4]
         ([[H1 . T] H1 [H2 . T] H2])
         ([[H . T1] H1 [H . T2] H2] (replaceo T1 H1 T2 H2)))
     A1 A2 A3 A4)))

