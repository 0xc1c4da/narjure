(ns nal.core
  (:require [nal.deriver.truth :as t]
            [nal.deriver :refer [generate-conclusions]]
            [nal.rules :as r]))

(defn choice [[f1 c1] [f2 c2]]
  (if (>= c1 c2) [f1 c1] [f2 c2]))

(defn inference [task belief]
  (generate-conclusions (r/rules :judgement) task belief))

(def revision t/revision)
