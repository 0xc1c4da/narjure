(ns narjure.term_utils
  (:require [clojure.set :as set]
            [narjure.defaults :refer :all]))

(defn interval?
  "Is the term an interval?"
  [content]
  (and (sequential? content) (= (first content) :interval)))

(defn compound?
  "Is the term a compound term?"
  [content]
  (and (sequential? content) (not= (first content) :interval)))

(defn syntactic-complexity
  "Calculates the syntactic complexity of a content term,
  for example (| (& a b) c) has complexity 5"
  [content]
  (if (compound? content)
    (reduce + (map syntactic-complexity content))
    1))

;todo -  temp function below should be in nal.clj - Patham9 to resolve
(def logic-ops
  #{'--> '<-> 'instance 'property 'instance-property '==> 'pred-impl '=|> 'retro-impl '<=> '</> '<|> 'ext-set 'int-set 'ext-inter '| '- 'int-dif '* 'ext-image 'int-image '-- '|| 'conj 'seq-conj '&|})

(defn termlink-subterms
  "Extract the termlink relevant subterms of the term up to 3 levels as demanded by the NAL rules"
  ([level content]
   (if (and (< level 3) (compound? content))
     (reduce set/union #{content} (map (partial termlink-subterms (inc level)) content))
     #{content}))
  ([content]
   (remove #(or (logic-ops %) (interval? %))
           (termlink-subterms 0 content))))
