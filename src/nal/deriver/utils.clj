(ns nal.deriver.utils
  (:require [clojure.walk :as w]))

(defn not-operator?
  "Checks if element is not operator"
  [el] (re-matches #"[akxA-Z$]" (-> el str first str)))

(def operator? (complement not-operator?))

(defmacro walk
  "Macro that helps to replace elements during walk. The first argument
  is collection, rest of the arguments are cond-like
  expressions. Default result of cond is element itself.
  el is reserved name for current element of collection."
  [coll & conditions]
  (let [el (gensym)
        replace-el (fn [coll]
                     (w/postwalk #(if (or (= 'el %) (= :el %)) el %) coll))]
    `(w/postwalk
       (fn [~el] (cond ~@(replace-el conditions)
                       :else ~el))
       ~coll)))
