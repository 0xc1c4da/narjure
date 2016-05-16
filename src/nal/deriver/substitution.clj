(ns nal.deriver.substitution
  (:require [nal.deriver.utils :refer [walk]]
            [clojure.core.unify :as u]
            [clojure.string :as s]))

(defn replace-vars
  "Defn replaces var-elements from statemts by placeholders for unification."
  [var-type statement]
  (walk statement
    (and (coll? :el) (= var-type (first :el)))
    (->> :el second (str "?") symbol)))

(defn unification-map
  "Returns map of inified elements from both collections."
  [var-symbol p2 p3]
  (let [check-var-type (if (= var-symbol "$")                ;in this cases also dependent var
                         #(or (= % 'ind-var) (= % 'dep-var)) ;elimination is fine!
                         #(= % 'dep-var))]
    ((u/make-occurs-unify-fn #(and (coll? %) (check-var-type (first %)))) p2 p3)))

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
  (let [u-map (munification-map var-symbol p2 p3)]
    (walk conclusion (u-map el) (u-map el))))
