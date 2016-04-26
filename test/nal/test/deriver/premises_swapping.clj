(ns nal.test.deriver.premises-swapping
  (:require [clojure.test :refer :all]
            [nal.deriver.premises-swapping :refer :all]))

(def question-rule
  '{:p1          (--> P S)
    :p2          (--> S P)
    :conclusions [{:conclusion (--> P S)
                   :post       (:t/conversion :p/belief)}]
    :full-path   [(--> :any :any) :and (--> :any :any)]
    :pre         (:question?)})

(def rule-wth-commutative-term
  '{:p1          (--> P M),
    :p2          (--> S M),
    :conclusions [{:conclusion (<-> S P),
                   :post       (:t/comparison :d/weak :allow-backward)}],
    :full-path   [(--> :any :any) :and (--> :any :any)],
    :pre         ((:!= S P))})

(def rule
  '{:p1          (--> M P),
    :p2          (<-> S M),
    :conclusions [{:conclusion (--> S P),
                   :post       (:t/analogy :d/strong :allow-backward)}],
    :full-path   [(--> :any :any) :and (<-> :any :any)],
    :pre         ((:!= S P))})

(deftest test-allow-swapping?
  (are [r] (false? (allow-swapping? r))
    question-rule
    rule-wth-commutative-term)
  (is (true? (allow-swapping? rule))))

(deftest test-swap-premises
  (let [{:keys [p1 p2]} rule
        swapped (swap-premises rule)]
    (is (and (= p1 (swapped :p2))
             (= p2 (swapped :p1))))))

