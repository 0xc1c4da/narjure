(ns narjure.narsese
  (:require [instaparse.core :as i]
            [clojure.java.io :as io]
            [narjure.defaults :refer :all]))

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
  {"{"  'ext-set
   "["  'int-set
   "&"  'ext-intersection
   "|"  'int-intersection
   "-"  'ext-difference
   "~"  'int-difference
   "*"  'product
   "("  'product
   "/"  'ext-image
   "\\" 'int-image
   "--" 'negation
   "||" 'disjunction
   "&&" 'conjunction
   "&/" 'sequential-events
   "&|" 'parallel-events})

(def tenses
  {":|:"  :present
   ":/:"  :future
   ":\\:" :past})

(defn get-compound-term [[_ operator-srt]]
  (compound-terms operator-srt))

(def task-types {"." :judgement
                 "?" :question})

(def ^:dynamic *task-type* (atom nil))
(def ^:dynamic *lvars* (atom []))
(def ^:dynamic *truth* (atom []))
(def ^:dynamic *budget* (atom []))
(def ^:dynamic *tense* (atom :present))
(def ^:dynamic *syntactic-complexity* nil)

(defn keep-cat [fun col]
  (into [] (comp (mapcat fun) (filter (complement nil?))) col))

(defn dispatcher [data]
  (if (string? data) :default (first data)))

(defmulti element dispatcher)

(defn get-copula [[_ [_ cop-symbol]]]
  (copulas cop-symbol))

(defmethod element :sentence [[_ & data]]
  (let [filtered (group-by string? data)]
    (reset! *task-type* (task-types (first (filtered true))))
    (let [cols (filtered false)
          last-el (last cols)]
      (when (= :truth (first last-el))
        (element last-el))
      (when-let [tense (some #(when (= :tense (first %)) %) data)]
        (element tense))
      (element (first cols)))))

(defmethod element :statement [[_ & data]]
  (if-let [copula (get-copula data)]
    (do
      (swap! *syntactic-complexity* inc)
      `[~copula ~@(keep-cat element data)])
    (keep-cat element data)))

(defmethod element :task [[_ & data]]
  `[~@(keep-cat element data)])

;looks strange but it is because of special syntax for negation --bird.
(defn get-comp-operator [second-el data]
  (let [first-el-type (get-in (vec data) [0 0])]
    (if (some #{(first second-el)} [:op-negation :op-int-set
                                    :op-ext-set :op-product])
      second-el
      ((if (= :term first-el-type) second first) data))))

(defmethod element :compound-term [[_ second-el & data]]
  (let [comp-operator (get-comp-operator second-el data)]
    `[~(get-compound-term comp-operator)
      ~@(keep-cat element (remove string? data))]))

(def var-prefixes {"#" "d_" "?" "?"})
(defmethod element :variable [[_ type [_ v]]]
  (let [v (symbol (str (var-prefixes type) v))]
    (swap! *lvars* conj v)
    v))

(defmethod element :word [[_ word]] (symbol word))

(defmethod element :truth
  [[_ & data]]
  (mapv element data))

(defmethod element :budget
  [[_ & data]]
  `[[~@(mapv element data)]])

(defmacro double-element [n a]
  `(defmethod element ~n [[t# d#]]
     (let [d# (Double/parseDouble d#)]
       (swap! ~a conj d#) d#)))

(double-element :frequency *truth*)
(double-element :confidence *truth*)
(double-element :priority *budget*)
(double-element :durability *budget*)
(double-element :quality *budget*)

(defmethod element :task [[_ & data]]
  (when (= :budget (first (first data)))
    (element (first data)))
  (element (last data)))

(defmethod element :term [[_ & data]]
  (swap! *syntactic-complexity* inc)
  (when (seq? data)
    (keep element data)))

(defmethod element :tense [[_ key]]
  (reset! *tense* (tenses key)))

(defmethod element :default [_])

;TODO check for variables in statemnts, ignore subterm if it contains variable
(defn terms
  "Fetch terms from task."
  [statement]
  (into #{statement} (rest statement)))

(defn check-truth-value [v]
  (concat v (nthrest truth-value (count v))))

(defn check-budget [v act]
  (let [budget (budgets act)]
    (concat v (nthrest budget (count v)))))

(defn parse
  "Parses a Narsese string into task ready for inference"
  [narsese-str]
  (let [data (parser narsese-str)]
    (if-not (i/failure? data)
      (binding [*task-type* (atom nil)
                *lvars* (atom [])
                *truth* (atom [])
                *budget* (atom [])
                *tense* (atom :present)
                *syntactic-complexity* (atom 0)]
        (let [statement (element data)
              act @*task-type*]
          {:task-type            act
           :lvars                @*lvars*
           :truth                (check-truth-value @*truth*)
           :budget               (check-budget @*budget* act)
           :statement            statement
           :tense                @*tense*
           :syntactic-complexity @*syntactic-complexity*
           :terms                (terms statement)}))
      data)))
