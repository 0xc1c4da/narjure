(ns nal.truth-value
  (:refer-clojure :exclude [== < >])
  (:require [clojure.core.logic :refer [project fresh == defne]]
            [clojure.core.logic.arithmetic :refer [< >]]
            [nal.utils :refer :all]))

(declare f-exp f-neg f-cnv f-cnt f-ded f-ana f-res f-abd f-exe f-com f-int
         f-uni f-dif f-pnn f-npp f-pnp f-nnn)

(defne f-rev [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (< C1 1) (< C2 1)
    (project [C1 C2]
      (fresh [M1 M2]
        (== M1 (/ C1 (- 1 C1)))
        (== M2 (/ C2 (- 1 C2)))
        (project [M1 M2 F1 F2]
          (== F (/ (+ (* M1 F1) (* M2 F2)) (+ M1 M2)))
          (== C (/ (+ M1 M2) (+ M1 M2 1))))))))

(defne f-exp [A1 A2]
  ([[F C] E] (project [F C] (== E (+ (* C (- F 0.5)) 0.5)))))

(defne f-neg [A1 A2] ([[F1 C1] [F C1]] (u-not F1 F)))

(defne f-cnv [A1 A2]
  ([[F1 C1] [1 C]] (fresh [W] (u-and [F1 C1] W) (u-w2c W C))))

(defne f-cnt [A1 A2]
  ([[F1 C1] [0 C]] (fresh [F0 W] (u-not F1 F0) (u-and [F0 C1] W) (u-w2c W C))))

(defne f-ded [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]] (u-and [F1 F2] F) (u-and [C1 C2 F] C)))

(defne f-ana [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]] (u-and [F1 F2] F) (u-and [C1 C2 F2] C)))

(defne f-res [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F0] (u-and [F1 F2] F) (u-or [F1 F2] F0) (u-and [C1 C2 F0] C))))

(defne f-abd [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F2 C]]
    (fresh [W] (u-and [F1 C1 C2] W) (u-w2c W C))))

(defn f-ind [T1 T2 T] (f-abd T2 T1 T))

(defne f-exe [A1 A2 A3]
  ([[F1 C1] [F2 C2] [1 C]]
    (fresh [W] (u-and [F1 C1 F2 C2] W) (u-w2c W C))))

(defne f-com [A1 A2 A3]
  ([[0 C1] [0 C2] [0 0]])
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F0 W]
      (u-or [F1 F2] F0)
      (project [F0] (> F0 0))
      (project [F1 F2 F0] (== F (/ (* F1 F2) F0)))
      (u-and [F0 C1 C2] W)
      (u-w2c W C))))

(defne f-int [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (u-and [F1 F2] F)
    (u-and [C1 C2] C)))

(defne f-uni [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (u-or [F1 F2] F)
    (u-and [C1 C2] C)))

(defne f-dif [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F0]
      (u-not F2 F0)
      (u-and [F1 F0] F)
      (u-and [C1 C2] C))))

(defne f-pnn [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F2n Fn]
      (u-not F2 F2n)
      (u-and [F1 F2n] Fn)
      (u-not Fn F)
      (u-and [Fn C1 C2] C))))

(defne f-npp [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F1n]
      (u-not F1 F1n)
      (u-and [F1n F2] F)
      (u-and [F C1 C2] C))))

(defne f-pnp [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F2n]
      (u-not F2 F2n)
      (u-and [F1 F2n] F)
      (u-and [F C1 C2] C))))

(defne f-nnn [A1 A2 A3]
  ([[F1 C1] [F2 C2] [F C]]
    (fresh [F1n F2n Fn]
      (u-not F1 F1n)
      (u-not F2 F2n)
      (u-and [F1n F2n] Fn)
      (u-not Fn F)
      (u-and [Fn C1 C2] C))))
