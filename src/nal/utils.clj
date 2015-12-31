(ns nal.utils
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.core.logic
             :refer [project fresh == defne lvaro conda] :as l]))

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

(defn- vec-remove
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn- vec-diff [c1 c2]
  (clojure.core/reduce
    (fn [coll element]
      (let [idx (.indexOf coll element)]
        (if-not (neg? idx)
          (vec-remove coll idx)
          coll))) c1 c2))

(defn subtract [S D R]
  "Subtract like in prolog."
  (conda [(lvaro S) l/u#]
         [(lvaro D) l/u#]
         [(project [S D] (== R (vec-diff (vec S) (vec D))))]))
