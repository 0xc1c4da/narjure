(ns nal.test.deriver
  (:require [clojure.test :refer :all]
            [nal.deriver :refer :all]
            [nal.rules :as r]))

(deftest test-generate-conclusions
  (is (= '([[<-> chess sport] [1.0 0.44751381215469616]]
            [[==> [--> sport [ind-var X]] [--> chess [ind-var X]]] [1 0.44751381215469616]]
            [[==> [--> chess [ind-var X]] [--> sport [ind-var X]]] [1 0.44751381215469616]]
            [[--> [int-dif chess sport] competition] [0.0 0.81]]
            [[--> chess sport] [1 0.44751381215469616]]
            [[==> [--> chess [ind-var X]] [--> sport [ind-var X]]] [1 0.44751381215469616]]
            [[==> [--> sport [ind-var X]] [--> chess [ind-var X]]] [1 0.44751381215469616]]
            [[<=> [--> chess [ind-var X]] [--> sport [ind-var X]]] [1.0 0.44751381215469616]]
            [[conj [--> sport [dep-var Y]] [--> chess [dep-var Y]]] [1.0 0.81]]
            [[--> sport chess] [1 0.44751381215469616]]
            [[--> [| chess sport] competition] [1.0 0.81]]
            [[--> [ext-inter chess sport] competition] [1.0 0.81]]
            [[--> [int-dif sport chess] competition] [0.0 0.81]])
         (generate-conclusions
           (r/rules :judgement)
           '[[--> sport competition] [1 0.9]]
           '[[--> chess competition] [1 0.9]]))))
