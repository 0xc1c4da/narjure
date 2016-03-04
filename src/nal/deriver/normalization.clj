(ns nal.deriver.normalization
  (:require [clojure.string :as s]))

;operators that should be movet from infix to prefix position
(def operators
  #{'&| '--> '<-> '==> 'retro-impl 'pred-impl 'seq-conj 'inst 'prop 'inst-prop
    'int-image 'ext-image '=/> '=|> '| '<=> '</> '<|> '- 'int-dif '|| '&&
    'ext-inter 'conj})

(defn infix->prefix
  "Makes transformaitions like [A --> B] to [--> A B]."
  [premise]
  (if (coll? premise)
    (let [[f s & tail] premise]
      (map infix->prefix
           (if (operators s)
             (concat [s f] tail)
             premise)))
    premise))

(defn- neg-symbol?
  "Checks if symbol starts with --, true for --A, --X"
  [el]
  (and (not= el '-->) (symbol? el) (s/starts-with? (str el) "--")))

(defn- trim-negation
  "Removes -- from symbols with negation:
  (trim-negation '--A) => A"
  [el]
  (symbol (s/join (drop 2 (str el)))))

(defn neg [el] (list '-- el))

(defn replace-negation
  "Replaces negations's \"new notation\"."
  [statement]
  (cond
    (neg-symbol? statement) (neg (trim-negation statement))
    (or (vector? statement) (and (seq? statement) (not= '-- (first statement))))
    (:st
      (reduce
        (fn [{:keys [prev st] :as ac} el]
          (if (= '-- el)
            (assoc ac :prev true)
            (->> [(cond prev (neg el)
                        (coll? el) (replace-negation el)
                        (neg-symbol? el) (neg (trim-negation el))
                        :else el)]
                 (concat st)
                 (assoc ac :prev false :st))))
        {:prev false :st '()}
        statement))
    :else statement))
