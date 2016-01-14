(ns narjure.narsese
  (:require [instaparse.core :as i]
            [clojure.java.io :as io]))

(def bnf-file "narsese.bnf")

(def parser
  (i/parser (io/resource bnf-file) :auto-whitespace :standard))

(def copulas
  {"-->"  'inheritance
   "<->"  'similarity
   "{--"  'instance
   "--]"  'property
   "{-]"  'instance-property
   "==>"  'implication
   "=/>"  'predictive-implication
   "=|>"  'concurrent-implication
   "=\\>" 'retrospective-implication
   "<=>"  'equivalence
   "</>"  'predictive-equivalence
   "<|>"  'concurrent-equivalence})

(def compound-terms
  {"{"    'ext-set
   "["    'int-set
   "(&,"  'ext-intersection
   "(|,"  'int-intersection
   "(-,"  'ext-difference
   "(~,"  'int-difference
   "(*,"  'product
   "("  'product
   "(/,"  'ext-image
   "(\\," 'int-image
   "(--," 'negation
   "(||," 'disjunction
   "(&&," 'conjunction
   "(&/," 'sequential-events
   "(&|," 'parallel-events})

(def actions {"." 'judgement
              "?" 'question})

(def ^:dynamic *action* (atom nil))
(def ^:dynamic *lvars* (atom []))

(defn keep-cat [fun col]
  (into [] (comp (mapcat fun) (filter (complement nil?))) col))

(defn dispatcher [data]
  (if (string? data) :default (first data)))

(defmulti element dispatcher)

(defn get-copula [[_ [_ cop-symbol]]]
  (copulas cop-symbol))

(defmethod element :sentence [[_ & data]]
  (let [filtered (group-by string? data)]
    (reset! *action* (actions (first (filtered true))))
    (keep element (filtered false))))

(defmethod element :statement [[_ & data]]
  (let [copula (get-copula data)]
    `[~copula ~@(keep-cat element data)]))

(defmethod element :task [[_ & data]]
  `[~@(keep-cat element data)])

(defmethod element :compound-term [[_ term-symbol & data]]
  `[~(compound-terms term-symbol) ~@(keep-cat element data)])

(defmethod element :copula [_])

(defmethod element :variable [[_ _ [_ v]]]
  (let [v (symbol v)]
    (swap! *lvars* conj v)
    v))

(defmethod element :word [[_ word]] (symbol word))

(defmethod element :truth
  [[_ & data]]
  (mapv element data))

(defmethod element :budget
  [[_ & data]]
  `[[~@(mapv element data)]])

(defmethod element :frequency [[_ d]] (Double/parseDouble d))
(defmethod element :confidence [[_ d]] (Double/parseDouble d))
(defmethod element :priority [[_ d]] (Double/parseDouble d))
(defmethod element :durability [[_ d]] (Double/parseDouble d))

(defmethod element :default [[_ & data]]
  (when (seq? data)
    (keep element data)))

(defn parse [narsese-str]
  (let [data (parser narsese-str)]
    (if-not (i/failure? data)
      (binding [*action* (atom nil)
                *lvars* (atom [])]
        (let [parsed-code (element data)]
          {:action @*action*
           :lvars  @*lvars*
           :data   parsed-code}))
      data)))
