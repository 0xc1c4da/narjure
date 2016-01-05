(ns nal.utils
  (:refer-clojure :exclude [!= == reduce replace >= <= > < =])
  (:require [clojure.core.logic
             :refer [project fresh == defne lvaro conda nonlvaro run* membero
                     conso run defna onceo u# s# appendo]
             :as l]))

(declare u-and u-or subtract intersection union subseto deleteo)

(defn u-not [n0 n]
  (project [n0] (== n (- 1 n0))))

(defne u-and [A1 A2]
  ([[N] N])
  ([[N0 . Nt] N]
    (fresh [N1]
      (u-and Nt N1)
      (project [N0 N1] (== N (* N0 N1))))))

(defne u-or [A1 A2]
  ([[N] N])
  ([[N0 . Nt] N]
    (fresh [N1]
      (u-or Nt N1)
      (project [N0 N1] (== N (- (+ N0 N1) (* N0 N1)))))))

(defn u-w2c [w c]
  (project [w c] (== c (/ w (inc w)))))

;ported from prlog
;http://eclipseclp.org/doc/bips/lib/lists/subtract-3.html
(defna subtract [L1 L2 L3]
  ([[] _ []])
  ([[Head . Tail] L2 L3]  
    (onceo (membero Head L2))
    (subtract Tail L2 L3))
  ([[Head . Tail1] L2 [Head . Tail3]]  
    (subtract Tail1 L2 Tail3)))

;ported from prolog
;http://eclipseclp.org/doc/bips/lib/lists/intersection-3.html
(defna intersection [S1 S2 S3]
  ([[] _ []])
  ([[Head . L1tail] L2 L3]
    (onceo (membero Head L2))
    (fresh [L3tail]
      (conso Head L3tail L3)
      (intersection L1tail L2 L3tail)))
  ([[_ . L1tail] L2 L3]
    (intersection L1tail L2 L3)))

;ported from prolog
;http://eclipseclp.org/doc/bips/lib/lists/union-3.html
(defna union [L1 L2 L3]
  ([[] L L])
  ([[Head . L1tail] L2 L3]
    (onceo (membero Head L2))
    (union L1tail L2 L3))
  ([[Head . L1tail] L2 [Head . L3tail]]
    (union L1tail L2 L3tail)))

;http://eclipseclp.org/doc/bips/kernel/typetest/atom-1.html
(defn atomo [a]
  (conda [(lvaro a)]
         [(== true (number? a))]
         [(== true (coll? a))]))

(defn noto [a] (conda [a u#] [s#]))

(defmacro findallo [T G L]
  `(== ~L (run* [q#] (== q# ~T) ~G)))

(defna subseto [A1 A2]
  ([[] A2])
  ([[X . L] [X . S]] (subseto L S))
  ([L [_ . S]] (subseto L S)))
