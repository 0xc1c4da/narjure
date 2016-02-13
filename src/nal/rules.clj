(ns nal.rules
  (:require [nal.deriver :refer [defrules] :as d]))

(declare S P <-> |- --> ==> M || &&)

(def default [:t/identity :d/identity :allow-backward])
(defrules rules
  ;Similarity to Inheritance
  [(S --> P) (S <-> P) |- (S --> P) :pre [:question?] 
                                    :post [:t/struct-int :p/judgment]]
  ;Inheritance to Similarity
  [(S <-> P) (S --> P) |- (S <-> P) :pre [:question?]
                                    :post [:t/struct-abd :p/judgment]]
  ;Set Definition Similarity to Inheritance
  [(S <-> #{P}) S |- (S --> #{P}) :post default]
  [(S <-> #{P}) #{P} |- (S --> #{P}) :post default]
  [([S] <-> P) [S] |- ([S] --> P) :post default]
  [([S] <-> P) P |- ([S] --> P) :post default]
  [(#{S} <-> #{P}) #{S} |- (#{P} --> #{S}) :post default]
  [(#{S} <-> #{P}) #{P} |- (#{P} --> #{S}) :post default]
  [([S] <-> [P]) [S] |- ([P] --> [S]) :post default]
  [([S] <-> [P]) [P] |- ([P] --> [S]) :post default]

  ;Set Definition Unwrap
  [(#{S} <-> #{P}) #{S} |- (S <-> P) :post default]
  [(#{S} <-> #{P}) #{P} |- (S <-> P) :post default]
  [([S] <-> [P]) [S] |- (S <-> P) :post default]
  [([S] <-> [P]) [P] |- (S <-> P) :post default]

  ;Same as for inheritance again
  [(P ==> M) (S ==> M) |- [((P || S) ==> M) :post [:t/int]
                           ((P && S) ==> M) :post [:t/union]]
                          :pre [#(not= S P)]])

(defn freq []
  "Check frequency"
  (into {} (map (fn [[k v]] [k (count (:rules v))]) rules)))

