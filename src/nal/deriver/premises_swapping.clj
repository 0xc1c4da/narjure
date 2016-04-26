(ns nal.deriver.premises-swapping
  (:require [nal.deriver.key-path :refer [rule-path]]
            [nal.deriver.normalization :refer [commutative-ops]]))

;the set of keys which prevent premises swapping for rule
(def anti-swapping-keys
  #{:question? :belief? :goal? :measure-time :t/belief-structural-deduction
    :t/structural-deduction :t/belief-structural-difference :t/identity
    :t/negation :union :intersection :t/intersection :t/union})

(defn allow-swapping?
  "Checks if rule allow swapping of premises."
  [{:keys [pre conclusions]}]
  (let [{:keys [post conclusion]} (first conclusions)]
    (and (not-any? anti-swapping-keys (flatten (concat pre post)))
         (not-any? commutative-ops (flatten conclusion)))))

(defn swap-premises
  [{:keys [p1 p2] :as rule}]
  (assoc rule :p1 p2
              :p2 p1
              :full-path (rule-path p2 p1)))

(defn swap [rule] [rule (swap-premises rule)])
