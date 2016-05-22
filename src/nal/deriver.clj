(ns nal.deriver
  (:require
    [nal.deriver.utils :refer [walk]]
    [nal.deriver.key-path :refer [mall-paths all-paths mpath-invariants
                                  path-with-max-level]]
    [nal.deriver.rules :refer [rule]]
    [nal.deriver.normalization :refer [commutative-ops]]
    [clojure.set :as set]
    [nal.term_utils :refer :all]
    [nal.rules :as r]))

(defn get-matcher [rules p1 p2]
  (let [matchers (->> (mall-paths p1 p2)
                      (filter rules)
                      (map rules))]
    (case (count matchers)
      0 (constantly [])
      1 (first matchers)
      (fn [t1 t2] (mapcat #(% t1 t2) matchers)))))

(def mget-matcher (memoize get-matcher))
(def mpath (memoize path-with-max-level))

(defn generate-conclusions-no-commutativity
  "generate conclusions not taking commutative subterms of premises into account"
  [rules {p1 :statement :as t1} {p2 :statement :as t2}]
  (let [matcher (mget-matcher rules (mpath p1) (mpath p2))]
    (matcher t1 t2)))

;USE COUNTER (global seed, for making testcased deterministic
(def use-counter (ref 0))

(defn use-counter-reset []
  (do
    (dosync (ref-set use-counter 0))
    @use-counter))

;Adjusted shuffle given a seed, "shuffle-with-seed", from sloth:
;http://stackoverflow.com/questions/24553212/how-to-take-n-random-items-from-a-collection-in-clojure
;(since we don't want non-deterministic testcases)
(defn shuffle-random
  "Return a random permutation of coll with a seed"
  [coll]
  (dosync (commute use-counter inc)
          (let [seed (deref use-counter)
                al (java.util.ArrayList. coll)
                rnd (java.util.Random. (* seed 50000))]
            (java.util.Collections/shuffle al rnd)
            (clojure.lang.RT/vector (.toArray al)))))

(defn shuffle-term-one-layer [t]                            ;TODO put into term utils once merged with master
  (concat (list (first t)) (shuffle-random (rest t))))

(defn shuffle-term [A]                                      ;TODO put into term utils once merged with master
  "Shuffle a term recursively (all commutative subterms like (A <-> B)"
  (let [shuffle? (fn [A] (and (coll? A)
                              (some #(= (first A) %) commutative-ops)))
        shuffled (if (shuffle? A)
                   (shuffle-term-one-layer A)
                   A)]
    (if (shuffle? A)
      (for [x shuffled]
        (shuffle-term x))
      A)))

(defn generate-conclusions
  [rules {p1 :statement :as t1} {p2 :statement :as t2}]
  ;assign statement
  (apply set/union (for [x (range 50)]
     (generate-conclusions-no-commutativity rules
                                            (assoc t1 :statement (shuffle-term p1))
                                            (assoc t2 :statement (shuffle-term p2))))))

;this is the inference function we should use
(defn inference
  "Inference between two premises"
  [parsed-p1 parsed-p2]
  (set (map #(assoc % :statement
                      (apply-interval-precision (normalize-variables (:statement %))))
            (filter (fn [st] (or (= (:task-type st) :question)
                                 (= (:task-type st) :quest)
                                 (and (contains? st :truth)
                                      (coll? (:truth st))
                                      (> (second (:truth st)) 0))))
                    (map no-truth-for-questions-and-quests
                         (map interval-reduction
                              (generate-conclusions
                                (r/rules (:task-type parsed-p1))
                                parsed-p1
                                parsed-p2)))))))