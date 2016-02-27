(ns nal.deriver.rules
  (:require [clojure.string :as s]
            [nal.deriver.key-path :refer [rule-path]]))

(def operators
  #{'&| '--> '<-> '==> 'retro-impl 'pred-impl 'seq-conj 'inst 'prop 'inst-prop
    'int-image 'ext-image '=/> '=|> '| '<=> '</> '<|> '- 'int-dif '|| '&&
    'ext-inter 'conj})

(defn infix->prefix
  [premise]
  (if (coll? premise)
    (let [[f s & tail] premise]
      (map infix->prefix
           (if (operators s)
             (concat [s f] tail)
             premise)))
    premise))

(defn options
  "Generates map from rest of the rule's args."
  [args]
  (when-not (empty? args)
    (into {} (map vec (partition 2 args)))))

(defn- neg-symbol?
  [el]
  (and (not= el '-->) (symbol? el) (s/starts-with? (str el) "--")))

(defn- trim-negation
  [el]
  (symbol (apply str (drop 2 (str el)))))

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

(defn get-conclusions
  "Parses conclusions from the rule."
  [c opts]
  (if (and (seq? c) (some #{:post} c))
    (map (fn [[c _ post]] {:conclusion c :post post}) (partition 3 c))
    [{:conclusion c :post (:post opts)}]))

(defn rule [data]
  "Generates rule from #R statement."
  (let [[p1 p2 _ c & other] (replace-negation data)]
    (let [p1 (infix->prefix p1)
          p2 (infix->prefix p2)
          c (infix->prefix c)
          opts (options other)
          conclusions (get-conclusions c opts)]
      {:p1          p1
       :p2          p2
       :conclusions conclusions
       :full-path   (rule-path p1 p2)
       :pre         (:pre opts)})))
