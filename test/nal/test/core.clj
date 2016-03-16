(ns nal.test.core
  (:require [clojure.test :refer :all]
            [nal.core :refer :all]))

(deftest test-inference
  (are [a1 a2] (= (set a1) (set (apply inference a2)))

    '({:statement [==>
                   [&| [--> [ext-set tim] [int-set driving]]]
                   [--> [ext-set tim] [int-set dead]]]
       :truth     [1.0 0.81]
       :task-type :judgement
       :occurrence 1})

    '[{:statement [--> [ext-set tim] [int-set drunk]]
       :truth     [1 0.9]
       :task-type :judgement
       :occurrence 1}

      {:statement [==> [&| [--> [ind-var X] [int-set drunk]] [--> [ind-var X] [int-set driving]]]
                   [--> [ind-var X] [int-set dead]]]
       :truth     [1 0.9]
       :occurrence 0}]

    '({:occurrence 1
       :statement  [&| a1 [--> [* a1 a2 a3] m]]
       :task-type  :judgement
       :truth      [1.0
                    0.81]}
       {:occurrence 1
        :statement  [--> a1 [ext-image m _ a2 a3]]
        :task-type  :judgement
        :truth      [1
                     0.9]}
       {:occurrence 1
        :statement  [<|> a1 [--> [* a1 a2 a3] m]]
        :task-type  :judgement
        :truth      [1.0
                     0.44751381215469616]}
       {:occurrence 1
        :statement  [=|> [--> [* a1 a2 a3] m] a1]
        :task-type  :judgement
        :truth      [1
                     0.44751381215469616]}
       {:occurrence 1
        :statement  [=|> a1 [--> [* a1 a2 a3] m]]
        :task-type  :judgement
        :truth      [1
                     0.44751381215469616]})
    '[{:statement [--> [* a1 a2 a3] m]
       :truth     [1 0.9]
       :task-type :judgement
       :occurrence 1}

      {:statement a1
       :truth     [1 0.9]
       :occurrence 0}]

    '[{:statement [=|> [--> [* a1 a2 a3] m] a1],
       :task-type :judgement,
       :occurrence 1,
       :truth [1 0.44751381215469616]}
      {:statement [<|> a1 [--> [* a1 a2 a3] m]],
       :task-type :judgement,
       :occurrence 1,
       :truth [1.0 0.44751381215469616]}
      {:statement [&| [--> [* a1 a2 a3] m] a1],
       :task-type :judgement,
       :occurrence 1,
       :truth [1.0 0.81]}
      {:statement [=|> a1 [--> [* a1 a2 a3] m]],
       :task-type :judgement,
       :occurrence 1,
       :truth [1 0.44751381215469616]}]
    '[{:statement a1
       :truth     [1 0.9]
       :task-type :judgement
       :occurrence 1}

      {:statement [--> [* a1 a2 a3] m]
       :truth     [1 0.9]
       :occurrence 0}]

    '({:occurrence 1
       :statement  [&| a1 [conj a1 a2 a3]]
       :task-type  :judgement
       :truth      [1.0 0.81]}
       {:occurrence 1
        :statement  [<|> a1 [conj a1 a2 a3]]
        :task-type  :judgement
        :truth      [1.0 0.44751381215469616]}
       {:occurrence 1
        :statement  [=|> [conj a1 a2 a3] a1]
        :task-type  :judgement
        :truth      [1
                     0.44751381215469616]}
       {:occurrence 1
        :statement  [=|> a1 [conj a1 a2 a3]]
        :task-type  :judgement
        :truth      [1
                     0.44751381215469616]}
       {:occurrence 1
        :statement  a1
        :task-type  :judgement
        :truth      [1 0.44751381215469616]})
    '[{:statement [conj a1 a2 a3]
       :truth     [1 0.9]
       :task-type :judgement
       :occurrence 1}

      {:statement a1
       :truth     [1 0.9]
       :occurrence 0}]

    '[{:statement [=|> [--> M S] [[--> M S] [--> M P]]],
       :task-type :judgement,
       :occurrence 1,
       :truth [1 0.44751381215469616]}
      {:statement [<|> [--> M S] [[--> M S] [--> M P]]],
       :task-type :judgement,
       :occurrence 1,
       :truth [1.0 0.44751381215469616]}
      {:statement [&| [--> M S] [[--> M S] [--> M P]]],
       :task-type :judgement,
       :occurrence 1,
       :truth [1.0 0.81]}
      {:statement [=|> [[--> M S] [--> M P]] [--> M S]],
       :task-type :judgement,
       :occurrence 1,
       :truth [1 0.44751381215469616]}]
    '[{:statement [[--> M S] [--> M P]]
       :truth     [1 0.9]
       :task-type :judgement
       :occurrence 1}

      {:statement [--> M S]
       :truth     [1 0.9]
       :occurrence 0}]

    '({:statement [conj [--> [dep-var Y] [int-set B]]
                   [==> [--> [ext-set A] [int-set Y]] [--> [dep-var Y] P]]]
       :truth     [1.0 0.81]
       :task-type :judgement
       :occurrence 1}
       {:statement [==>
                    [conj [--> [ext-set A] [int-set Y]] [--> [ind-var X] [int-set B]]]
                    [--> [ind-var X] P]]
        :truth     [1 0.44751381215469616]
        :task-type :judgement
        :occurrence 1})
    '[{:statement [==> [--> [ext-set A] [int-set Y]] [--> [ext-set A] P]]
       :truth     [1 0.9]
       :task-type :judgement
       :occurrence 1}

      {:statement [--> [ext-set A] [int-set B]]
       :truth     [1 0.9]
       :occurrence 1}]))
