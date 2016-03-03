(ns nal.deriver.substitution
  (:require [nal.deriver.utils :refer [walk]]
            [clojure.core.unify :as u]
            [clojure.string :as s]))

(def vars-map {"$" 'ind-var "#" 'dep-var})

(defn unification-map [p1 p2 p3]
  (let [var-type (vars-map p1)
        p2 (walk p2 (and (coll? el) (= var-type (first el)))
                 (symbol (str "?" (second el))))]
    (u/unify p2 p3)))

(def munification-map (memoize unification-map))

(defn substitute [p1 p2 p3 conclusion]
  (let [u-map (munification-map p1 p2 p3)
        var-type (vars-map p1)
        u-map (into {} (map (fn [[k v]]
                              [[var-type (->> k str (drop 1) s/join symbol)] v])
                            u-map))]
    (walk conclusion (u-map el) (u-map el))))
