(ns nal.test.deriver
  (:require [clojure.test :refer :all]
            [nal.deriver :refer :all]
            [nal.rules :as r]))

(deftest test-generate-conclusions
  (is (= (set
           '({:statement [--> [int-dif chess sport] competition]
              :truth [0.0 0.81]
              :task-type :judgement}
              {:statement [conj [--> chess [dep-var Y]] [--> sport [dep-var Y]]]
               :truth [1.0 0.81]
               :task-type :judgement}
              {:statement [--> [int-dif sport chess] competition]
               :truth [0.0 0.81]
               :task-type :judgement}
              {:statement [--> [| chess sport] competition]
               :truth [1.0 0.81]
               :task-type :judgement}
              {:statement [--> chess sport]
               :truth [1 0.44751381215469616]
               :task-type :judgement}
              {:statement [--> sport chess]
               :truth [1 0.44751381215469616]
               :task-type :judgement}
              {:statement [--> [ext-inter chess sport] competition]
               :truth [1.0 0.81]
               :task-type :judgement}
              {:statement [<-> sport chess]
               :truth [1.0 0.44751381215469616]
               :task-type :judgement}
              {:statement [==> [--> chess [ind-var X]] [--> sport [ind-var X]]]
               :truth [1 0.44751381215469616]
               :task-type :judgement}
              {:statement [==> [--> sport [ind-var X]] [--> chess [ind-var X]]]
               :truth [1 0.44751381215469616]
               :task-type :judgement}
              {:statement [==> [--> sport [ind-var X]] [--> chess [ind-var X]]]
               :truth [1 0.44751381215469616]
               :task-type :judgement}
              {:statement [<=> [--> chess [ind-var X]] [--> sport [ind-var X]]]
               :truth [1.0 0.44751381215469616]
               :task-type :judgement}
              {:statement [==> [--> chess [ind-var X]] [--> sport [ind-var X]]]
               :truth [1 0.44751381215469616]
               :task-type :judgement}))
         (set (generate-conclusions
                (r/rules :judgement)
                '{:statement [--> sport competition]
                  :truth     [1 0.9]
                  :task-type :judgement}

                '{:statement [--> chess competition]
                  :truth     [1 0.9]})))))
