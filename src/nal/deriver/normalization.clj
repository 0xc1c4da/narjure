(ns nal.deriver.normalization
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clojure.core.match :as m]))

;operators that should be movet from infix to prefix position
(def operators
  #{'&| '--> '<-> '==> 'retro-impl 'pred-impl 'seq-conj 'inst 'prop 'inst-prop
    'int-image 'ext-image '=/> '=|> '| '<=> '</> '<|> '- 'int-dif '|| '&&
    'ext-inter 'conj})

;commutative:	<-> <=> <|> & | && ||
;not commutative: --> ==> =/> =\> </> &/ - ~

(def commutative-ops #{'<-> '<=> '<|> '| '|| 'conj 'ext-inter})

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

(defn sort-commutative [conclusions]
  (if (coll? conclusions)
    (let [f (first conclusions)]
      (if (commutative-ops f)
        (vec (conj (sort-by hash (drop 1 conclusions)) f))
        conclusions))
    conclusions))

;https://gist.github.com/TonyLo1/a3f8e05458c5e90c2e72
(defn union
  ([c1 c2] (sort-by hash (set (concat c1 c2))))
  ([op c1 c2] (vec (conj (union c1 c2) op))))

(defn diff
  ([c1 c2] (into '() (set/difference (set c1) (set c2))))
  ([op c1 c2] (vec (conj (diff c1 c2) op))))

(defn reduce-ext-inter
  [st]
  (m/match
    st
    [_ t] t
    [_ ['ext-inter & l1] ['ext-inter & l2]] (union 'ext-inter l1 l2)
    [_ ['ext-inter & l1] l2] (union 'ext-inter l1 [l2])
    [_ l1 ['ext-inter & l2]] (union 'ext-inter [l1] l2)
    [_ ['int-set & l1] ['int-set & l2]] (union 'int-set l1 l2)
    ;[_ ['ext-set & l1] ['ext-set & l2]] (union 'ext-set l1 l2)
    :else st))

(defn reduce-int-inter
  [st]
  (m/match st
    ['| t] t
    ['| ['| & l1] ['| & l2]] (union '| l1 l2)
    ['| ['| & l1] l2] (union '| l1 [l2])
    ['| l1 ['| & l2]] (union '| [l1] l2)
    ['| ['int-set & l1] ['int-set & l2]] (union 'int-set l1 l2)
    ['| ['ext-set & l1] ['ext-set & l2]] (union 'ext-set l1 l2)
    :else st))

(defn reduce-int-dif
  [st]
  (m/match st
    [_ ['int-set & l1] ['int-set & l2]] (diff 'int-set l1 l2)
    :else st))

(defn reduce-ext-dif
  [st]
  (m/match st
    [_ ['ext-set & l1] ['ext-set & l2]] (diff 'ext-set l1 l2)
    :else st))

(defn reduce-symilarity
  [st]
  (m/match st
    ['<-> ['ext-set s] ['ext-set p]] ['<-> s p]
    ['<-> ['int-set s] ['int-set p]] ['<-> s p]
    :else st))

(defn reduce-production
  [st]
  (m/match st
    ['* ['* & l1] & l2] (vec (conj (concat l1 l2) '*))
    :else st))

(defn reduce-image
  [st]
  (m/match st
    [_ ['* t1 t2] t3] (if (and (= t2 t3) (not= t1 t2)) t1 st)
    :else st))

(defn reduce-neg
  [st]
  (m/match st
    ['-- ['-- t]] t
    :else st))

(defn reduce-or
  [st]
  (m/match st
    ['|| t] t
    ['|| ['|| & l1] ['|| & l2]] (union '|| l1 l2)
    ['|| ['|| & l1] l2] (union '|| l1 [l2])
    ['|| l1 ['|| & l2]] (union '|| [l1] l2)
    ['|| t1 t2] (if (= t1 t2) t1 st)
    :else st))

(defn reduce-and
  [st]
  (m/match st
    ['conj t] t
    ['conj ['conj & l1] ['conj & l2]] (union 'conj l1 l2)
    ['conj ['conj & l1] l2] (union 'conj l1 [l2])
    ['conj l1 ['conj & l2]] (union 'conj [l1] l2)
    ['conj t1 t2] (if (= t1 t2) t1 st)
    :else st))

(defn reduce-seq-conj
  [st]
  (let [cnt (count st)]
    (cond
      (= 3 cnt) (second st)
      (odd? cnt) (vec (butlast st))
      :else st)))

(def reducible-ops
  {'ext-inter `reduce-ext-inter
   '|         `reduce-int-inter
   '-         `reduce-ext-dif
   'int-dif   `reduce-int-dif
   '<->       `reduce-symilarity
   '*         `reduce-production
   'int-image `reduce-image
   'ext-image `reduce-image
   '--        `reduce-neg
   'conj      `reduce-and
   '||        `reduce-or})

(defn reduce-ops
  [st]
  (let [f (first st)]
    (case f
      ext-inter (reduce-ext-inter st)
      | (reduce-int-inter st)
      - (reduce-ext-dif st)
      int-dif (reduce-int-dif st)
      <-> (reduce-symilarity st)
      * (reduce-production st)
      int-image (reduce-image st)
      ext-image (reduce-image st)
      -- (reduce-neg st)
      conj (reduce-and st)
      || (reduce-or st)
      st)))

