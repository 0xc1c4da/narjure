(ns narjure.narsese
  (:require [instaparse.core :as i]
            [clojure.java.io :as io]))

(def bnf-file "narsese.bnf")

(def parser
  "Loads narsese.bnf into instaparse"
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
   "&"    'ext-intersection
   "(|,"  'int-intersection
   "|"    'int-intersection
   "(-,"  'ext-difference
   "-"    'ext-difference
   "(~,"  'int-difference
   "~"    'int-difference
   "(*,"  'product
   "*"    'product
   "("    'product
   "/"    'ext-image
   "\\"   'int-image
   "(--," 'negation
   "--"   'negation
   "(||," 'disjunction
   "||"   'disjunction
   "(&&," 'conjunction
   "&&"   'conjunction
   "(&/," 'sequential-events
   "&/"   'sequential-events
   "(&|," 'parallel-events
   "&|"   'parallel-events})

(defn get-compound-term [[_ operator-srt]]
  (compound-terms operator-srt))

(def actions {"." :judgement
              "?" :question})

(def ^:dynamic *action* (atom nil))
(def ^:dynamic *lvars* (atom []))
(def ^:dynamic *truth* (atom []))

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
  (if-let [copula (get-copula data)]
    `[~copula ~@(keep-cat element data)]
    (keep-cat element data)))

(defmethod element :task [[_ & data]]
  `[~@(keep-cat element data)])

;looks strange but it is because of special syntax for negation --bird.
(defn get-comp-operator [second-el data]
  (let [first-el-type (get-in (vec data) [0 0])]
    (if (= :op-negation (first second-el))
      second-el
      ((if (= :term first-el-type) second first) data))))

(defmethod element :compound-term [[_ second-el & data]]
  (let [comp-operator (get-comp-operator second-el data)]
    `[~(get-compound-term comp-operator)
      ~@(keep-cat element (remove string? data))]))

(defmethod element :copula [_])
(defmethod element :op-multi [_])
(defmethod element :op-single [_])
(defmethod element :op-negation [_])
(defmethod element :op-ext-image [_])
(defmethod element :op-int-image [_])

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

(defmethod element :frequency [[_ d]]
  (let [d (Double/parseDouble d)]
    (swap! *truth* conj d) d))
(defmethod element :confidence [[_ d]]
  (let [d (Double/parseDouble d)]
    (swap! *truth* conj d) d))
(defmethod element :priority [[_ d]] (Double/parseDouble d))
(defmethod element :durability [[_ d]] (Double/parseDouble d))

(defmethod element :default [[_ & data]]
  (when (seq? data)
    (keep element data)))

(defn parse
  "Parses a Narsese string into task ready for inference"
  [narsese-str]
  (let [data (parser narsese-str)]
    (if-not (i/failure? data)
      (binding [*action* (atom nil)
                *lvars* (atom [])
                *truth* (atom [])]
        (let [parsed-code (element data)]
          {:action @*action*
           :lvars  @*lvars*
           :truth  @*truth*
           :data   parsed-code}))
      data)))
