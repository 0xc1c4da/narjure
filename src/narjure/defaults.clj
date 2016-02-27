(ns narjure.defaults)

(def judgement-frequency 1.0)
(def judgement-confidence 0.9)

(def truth-value
  [judgement-frequency judgement-confidence])

(def judgement-priority 0.5)
(def judgement-durability 0.8)
;todo clarify this
(def judgement-quality 0.5)

(def judgement-budget
  [judgement-priority judgement-durability judgement-quality])

(def question-priority 0.5)
(def question-durability 0.9)
;todo clarify this
(def question-quality 0.5)

(def question-budget
  [judgement-priority judgement-durability judgement-quality])

(def goal-confidence 0.9)
(def goal-priority 0.5)
(def goal-durability 0.8)

(def budgets
  {:judgement judgement-budget
   :question question-budget})

(def ^{:type double} horizon 1)
