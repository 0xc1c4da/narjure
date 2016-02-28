(ns nal.deriver.list-expansion
  (:require [nal.deriver.utils :refer [walk]]
            [clojure.string :as s]))

;-----------------------------------------------------------------------
;lists :list/A etc...

;todo expand :list/B
(defn get-list
  [prefix statement]
  (cond
    (and (keyword? statement) (s/starts-with? (str statement) prefix))
    statement
    (coll? statement) (some identity (map #(get-list prefix %) statement))
    :default nil))

(defn gen-symbols [prefix n]
  (map #(symbol (str prefix (inc %))) (range n)))

(defn replace-list-elemets [statement list-name sym n]
  (walk statement
        (and (coll? el) (some #{list-name} el))
        (mapcat (fn [e] (if (= list-name e)
                          (concat '() (gen-symbols sym n))
                          (list e))) el)))

(defn generate-all-lists [list-name sym r]
  (mapcat #(let [st (replace-list-elemets r list-name sym %)]
            (if-let [from-name (get-list ":from" st)]
              (map (fn [idx]
                     (walk st (= from-name el) (symbol (str sym idx))))
                   (range 1 (inc %)))
              [st]))
          (range 1 6)))

(defn check-list [r]
  (if-let [list-name (get-list ":list" r)]
    (let [sym (apply str (drop 6 (str list-name)))]
      (generate-all-lists list-name sym r))
    [r]))
