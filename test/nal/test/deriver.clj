(ns nal.test.deriver
  (:require [clojure.test :refer :all]
            [nal.deriver :refer :all]
            [nal.rules :as r]))

(deftest test-generate-conclusions
  (is (= (set
           '({:statement [==> [--> chess [ind-var X]] [--> sport [ind-var X]]]
              :task-type :judgement
              :truth [1 0.44751381215469616]}
              {:statement [==> [--> chess [ind-var X]] [--> sport [ind-var X]]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [conj [--> chess [dep-var Y]] [--> sport [dep-var Y]]]
               :task-type :judgement
               :truth [1.0 0.81]}
              {:statement [==> [--> sport [ind-var X]] [--> chess [ind-var X]]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [--> sport chess]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [<-> sport chess]
               :task-type :judgement
               :truth [1.0 0.44751381215469616]}
              {:statement [<=> [--> chess [ind-var X]] [--> sport [ind-var X]]]
               :task-type :judgement
               :truth [1.0 0.44751381215469616]}
              {:statement [--> chess sport]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [==> [--> sport [ind-var X]] [--> chess [ind-var X]]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [pred-impl
                           [seq-conj [--> chess [ind-var X]] [:interval 1000]]
                           [--> sport [ind-var X]]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [</>
                           [seq-conj [--> chess [ind-var X]] [:interval 1000]]
                           [--> sport [ind-var X]]]
               :task-type :judgement
               :truth [1.0 0.44751381215469616]}
              {:statement [seq-conj
                           [--> chess [dep-var Y]]
                           [:interval 1000]
                           [--> sport [dep-var Y]]]
               :task-type :judgement
               :truth [1.0 0.81]}
              {:statement [retro-impl
                           [--> sport [ind-var X]]
                           [seq-conj [--> chess [ind-var X]] [:interval 1000]]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [--> [int-dif sport chess] competition]
               :task-type :judgement
               :truth [0.0 0.81]}
              {:statement [--> [ext-inter chess sport] competition]
               :task-type :judgement
               :truth [1.0 0.81]}
              {:statement [--> [| chess sport] competition]
               :task-type :judgement
               :truth [1.0 0.81]}
              {:statement [--> [int-dif chess sport] competition]
               :task-type :judgement
               :truth [0.0 0.81]}
              {:statement [</>
                           [seq-conj [--> chess competition] [:interval 1000]]
                           [--> sport competition]]
               :task-type :judgement
               :truth [1.0 0.44751381215469616]}
              {:statement [seq-conj
                           [--> chess competition]
                           [:interval 1000]
                           [--> sport competition]]
               :task-type :judgement
               :truth [1.0 0.81]}
              {:statement [pred-impl
                           [seq-conj [--> chess competition] [:interval 1000]]
                           [--> sport competition]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}
              {:statement [retro-impl
                           [--> sport competition]
                           [seq-conj [--> chess competition] [:interval 1000]]]
               :task-type :judgement
               :truth [1 0.44751381215469616]}))
         (set (generate-conclusions
                (r/rules :judgement)
                '{:statement [--> sport competition]
                  :truth     [1 0.9]
                  :task-type :judgement
                  :occurence 1000}

                '{:statement [--> chess competition]
                  :truth     [1 0.9]
                  :occurence 0})))))
