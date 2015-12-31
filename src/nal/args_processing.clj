(ns nal.args-processing
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.core.logic :refer
             [run* project fresh conde conso run membero defne fne
              nonlvaro == emptyo succeed fail conda s# u# onceo] :as l]
            [nal.utils :refer [subtract]]
            [clojure.core.logic.pldb :refer [db-rel db with-db]]))

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
  (fresh [] (nonlvaro L2) (include1 L1 L2)
    (project [L1 L2] (l/!= L1 []) (l/!= L1 L2))))

;TODO is not covered with tests
(defn not-member [E C]
  (fresh [X]
    (conde [(emptyo C)]
           [(conso E X C) fail]
           ;[(fresh [S T S1] (== E [S T]) (conso [S1 T] X C) (equivalence S S1) fail)]
           [(fresh [L] (conso X L C) (not-member L C))])))

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

