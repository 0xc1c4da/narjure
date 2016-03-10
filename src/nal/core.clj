(ns nal.core
  (:require [nal.deriver.truth :as t]
            [nal.deriver :refer [generate-conclusions]]
            [nal.rules :as r]))

(defn choice [[f1 c1] [f2 c2]]
  (if (>= c1 c2) [f1 c1] [f2 c2]))

(defn inference
  [{:keys [task-type] :as task} belief]
  (generate-conclusions (r/rules task-type) task belief))

(def revision t/revision)

(comment
  ;missed things
  :p/judgment
  :d/identity
  :d/negation
  :d/strong
  :d/weak
  :no-common-subterm                                        ;pre
  :seq-interval-from-premises                               ;post
  :shift-occurrence-forward                                 ;pre
  :measure-time                                             ;pre
  :concurrent                                               ;pre
  :d/deduction
  :d/induction
  :goal?
  :linkage-temporal)
