(ns nal.deriver.substitution
  (:require [nal.deriver.utils :refer [walk]]
            [clojure.core.unify :as u]
            [clojure.string :as s]))

(def vars-map {"$" 'ind-var "#" 'dep-var})

(defn replace-vars
  "Defn replaces var-elements from statemts by placeholders for unification."
  [var-type statement]
  (walk statement
    (and (coll? el) (= var-type (first el)))
    (->> el second (str "?") symbol)))

(defn unification-map
  "Returns map of inified elements from both collections."
  [var-symbol p2 p3]
  (let [var-type (vars-map var-symbol)]
    (u/unify (replace-vars var-type p2) p3)))

(def munification-map (memoize unification-map))

(defn placeholder->symbol [pl]
  (->> pl str (drop 1) s/join symbol))

(defn replace-placeholders
  "Updates keys in unification map from placeholders like ?X to vectors like
  ['dep-var X]"
  [var-type u-map]
  (->> u-map
       (map (fn [[k v]] [[var-type (placeholder->symbol k)] v]))
       (into {})))

(defn substitute
  "Unifies p2 and p3, then replaces elements from the unification map
  inside the conclusion."
  [var-symbol p2 p3 conclusion]
  (let [var-type (vars-map var-symbol)
        u-map (munification-map var-symbol p2 p3)
        u-map (replace-placeholders var-type u-map)]
    (walk conclusion (u-map el) (u-map el))))
