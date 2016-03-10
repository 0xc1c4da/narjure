(ns nal.test.core
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]))

(deftest test-inference
  (are [a1 a2] (= (set a1) (set (apply inference a2)))

    '({:statement [==>
                   [&| [--> [ext-set tim] [int-set driving]]]
                   [--> [ext-set tim] [int-set dead]]]
       :truth     [1.0 0.81]
       :task-type :judgement})

    '[{:statement [--> [ext-set tim] [int-set drunk]]
       :truth     [1 0.9]
       :task-type :judgement}

      {:statement [==> [&| [--> [ind-var X] [int-set drunk]] [--> [ind-var X] [int-set driving]]]
                   [--> [ind-var X] [int-set dead]]]
       :truth     [1 0.9]}]


    '({:statement [--> a1 [ext-image m _ a2 a3]]
       :truth     [1 0.9]
       :task-type :judgement})
    '[{:statement [--> [* a1 a2 a3] m]
       :truth     [1 0.9]
       :task-type :judgement}

      {:statement a1
       :truth     [1 0.9]}]

    []
    '[{:statement a1
       :truth     [1 0.9]
       :task-type :judgement}

      {:statement [--> [* a1 a2 a3] m]
       :truth     [1 0.9]}]

    '({:statement a1 :truth [1 0.44751381215469616] :task-type :judgement})
    '[{:statement [conj a1 a2 a3]
       :truth     [1 0.9]
       :task-type :judgement}

      {:statement a1
       :truth     [1 0.9]}]

    []
    '[{:statement [[--> M S] [--> M P]]
       :truth     [1 0.9]
       :task-type :judgement}

      {:statement [--> M S]
       :truth     [1 0.9]}]

    '({:statement [conj
                   [--> [dep-var Y] [int-set B]]
                   [==> [--> [ext-set A] [int-set Y]] [--> [dep-var Y] P]]]
       :truth     [1.0 0.81]
       :task-type :judgement}
       {:statement [==>
                    [conj [--> [ext-set A] [int-set Y]] [--> [ind-var X] [int-set B]]]
                    [--> [ind-var X] P]]
        :truth     [1 0.44751381215469616]
        :task-type :judgement})
    '[{:statement [==> [--> [ext-set A] [int-set Y]] [--> [ext-set A] P]]
       :truth     [1 0.9]
       :task-type :judgement}

      {:statement [--> [ext-set A] [int-set B]]
       :truth     [1 0.9]}]))
