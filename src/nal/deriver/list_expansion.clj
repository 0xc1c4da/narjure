(ns nal.deriver.list-expansion
  (:require [nal.deriver.utils :refer [walk]]
            [clojure.string :as s]))

(defn get-list
  "Finds the first :list element in the rule."
  [prefix statement]
  (cond
    (and (keyword? statement) (s/starts-with? (str statement) prefix))
    statement
    (coll? statement) (some identity (map #(get-list prefix %) statement))
    :default nil))

(defn gen-symbols
  "Generates n symbols with prefix."
  [prefix n]
  (map #(symbol (str prefix (inc %))) (range n)))

(defn replace-list-elemets
  "Replaces :list/ with symbols."
  [statement l list-name n]
  (walk statement
    (and (coll? el) (some #{l} el))
    (mapcat (fn [e] (if (= l e)
                   (concat '() (gen-symbols list-name n))
                   (list e))) el)))

(defn list-name
  "Fetches name of the list."
  [l]
  (->> l str (drop 6) s/join))

(defn expand-:from-element
  "Expands :from/ element in rule, so as a result will be created n rules, in
  each of them :form/A will be replaced with A1 or A2 or A3, etc."
  [statement from-name list-name n]
  (map (fn [idx]
         (walk statement
           (= from-name el) (symbol (str list-name idx))))
       (range 1 (inc n))))

(defn generate-all-lists
  "Expands rules with :list/ elements, as a result will be created 5 rules,
  where :list/ will be replaced with A1, A1 A2, ..., A1..A5."
  [r]
  (let [list (get-list ":list" r)
        l-name (list-name list)]
    (mapcat #(let [st (replace-list-elemets r list l-name %)]
              (if-let [from-name (get-list ":from" st)]
                (expand-:from-element st from-name l-name %)
                [st]))
            (range 1 6))))

(defn contains-list?
  "Checks if rule contains any :list element."
  [r]
  (get-list ":list" r))
