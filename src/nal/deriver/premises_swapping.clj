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
                   :t/negation :union}
                 (flatten (concat pre (:post (first conclusions)))))
       (not-any? commutative-ops (flatten (:conclusion (first conclusions))))))

(defn swap-premises
  [{:keys [p1 p2 pre] :as rule}]
  (assoc rule :p1 p2
              :p2 p1
              :full-path (rule-path p2 p1)
              #_:pre #_(map (fn [p]
                          (cond
                            (and (seq? p) (= :difference (first p)))
                            (let [[f p1 p2 p3] p] (list f p2 p1 p3))
                            :else p))
                        pre)))

(defn swap [rule] [rule (swap-premises rule)])
