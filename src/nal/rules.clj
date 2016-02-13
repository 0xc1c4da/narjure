(ns nal.rules
  (:require [nal.deriver :refer [defrules] :as d]))

(declare S P <-> |- --> ==> M || && eset iset)

(def set-def-post [:t/identity :d/identity :allow-backward])
(defrules rules
  ;Similarity to Inheritance
  [(S --> P) (S <-> P) |- (S --> P) :pre [:question?] 
                                    :post [:t/struct-int :p/judgment]]
  ;Inheritance to Similarity
  [(S <-> P) (S --> P) |- (S <-> P) :pre [:question?]
                                    :post [:t/struct-abd :p/judgment]]
  ;Set Definition Similarity to Inheritance
  [(S <-> (eset P)) S |- (S --> (eset P)) :post set-def-post]
  [(S <-> (eset P)) (eset P) |- (S --> (eset P)) :post set-def-post]
  [((iset S) <-> P) (iset S) |- ((iset S) --> P) :post set-def-post]
  [((iset S) <-> P) P |- ((iset S) --> P) :post set-def-post]
  [((eset S) <-> (eset P)) (eset S) |- ((eset P) --> (eset S)) :post set-def-post]
  [((eset S) <-> (eset P)) (eset P) |- ((eset P) --> (eset S)) :post set-def-post]
  [((iset S) <-> (iset P)) (iset S) |- ((iset P) --> (iset S)) :post set-def-post]
  [((iset S) <-> (iset P)) (iset P) |- ((iset P) --> (iset S)) :post set-def-post]

  ;Set Definition Unwrap
  [((eset S) <-> (eset P)) (eset S) |- (S <-> P) :post set-def-post]
  [((eset S) <-> (eset P)) (eset P) |- (S <-> P) :post set-def-post]
  [((iset S) <-> (iset P)) (iset S) |- (S <-> P) :post set-def-post]
  [((iset S) <-> (iset P)) (iset P) |- (S <-> P) :post set-def-post]

  ;Same as for inheritance again
  [(P ==> M) (S ==> M) |- [((P || S) ==> M) :post [:t/int]
                           ((P && S) ==> M) :post [:t/union]]
                          :pre [#(not= S P)]])

(comment
  ;check frequency
  (into {} (map (fn [[k v]] [k (count (:rules v))]) rules))

  {[(--> :any :any) :and (<-> :any :any)]           1,
   [(<-> :any :any) :and (--> :any :any)]           1,
   [(<-> :any (eset :any)) :and :any]               4,
   [(<-> :any (eset :any)) :and (eset :any)]        3,
   [(<-> (iset :any) :any) :and (iset :any)]        3,
   [(<-> (iset :any) :any) :and :any]               4,
   [(<-> (eset :any) (eset :any)) :and (eset :any)] 2,
   [(<-> (iset :any) (iset :any)) :and (iset :any)] 2})
