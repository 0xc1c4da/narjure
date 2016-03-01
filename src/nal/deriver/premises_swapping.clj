(ns nal.deriver.premises-swapping
  (:require [nal.deriver.key-path :refer [rule-path]]))

;-------------------------------------------------------------------------
;premises swapping

;commutative:	<-> <=> <|> & | && ||
;not commutative: --> ==> =/> =\> </> &/ - ~

(def commutative-ops #{'<-> '<=> '<|> '| '|| 'conj 'ext-inter})

(defn allow-swapping? [{:keys [pre conclusions]}]
  (and (not-any? #{:question? :judgement? :goal? :measure-time :t/belief-structural-deduction
                   :t/structural-deduction :t/belief-structural-difference :t/identity
                   :t/negation}
                 (flatten (concat pre (:post (first conclusions)))))
       (not-any? commutative-ops (flatten (:conclusion (first conclusions))))))

(defn swap-premises
  [{:keys [p1 p2] :as rule}]
  (assoc rule :p1 p2
              :p2 p1
              :full-path (rule-path p2 p1)))

(defn swap [rule] [rule (swap-premises rule)])
