(ns nal.test.deriver.terms-premutation
  (:require [clojure.test :refer :all]
            [nal.deriver.terms-permutation :refer :all]
            [nal.test.test-utils :refer [both-equal]]))

(deftest test-contains-op?
  (are [res] ((complement nil?) res)
    (contains-op? '[--> [A B] [<-> C [ext-set D K]]] #{'ext-set})
    (contains-op? '--> #{'-->})
    (contains-op? '[--> A B] #{'-->}))
  (are [res] (or (false? res) (nil? res))
    (contains-op? '[--> [A B] [<-> C [ext-set D K]]] #{'<=>})
    (contains-op? '--> '<->)))

(deftest test-replace-op
  (both-equal
    '[==> A B] (replace-op '[=|> A B] '=|> '==>)
    '[==> A B] (replace-op '[=|> A B] '=|> '==>)
    '[--> A [retro-impl A B]] (replace-op '[=|> A [retro-impl A B]] '=|> '-->)))

(deftest test-premure-op
  (both-equal
    '([=|> A B] [retro-impl A B] [==> A B] [pred-impl A B])
    (permute-op '[==> A B] implications)

    '([=|> A B] [retro-impl A B] [==> A B] [pred-impl A B])
    (permute-op '[=|> A B] implications)

    '(conj &| seq-conj) (permute-op '&| conjunctions)))

(def rule1
  '{:p1          (==> M P)
    :p2          (==> S M)
    :conclusions [{:conclusion (==> S P)
                   :post       (:t/deduction :order-for-all-same :allow-backward)}]
    :full-path   [(==> :any :any) :and (==> :any :any)]})

(def rule2
  '{:p1          (==> M P)
    :p2          (==> S M)
    :conclusions [{:conclusion (==> S P)
                   :post       (:allow-backward)}]
    :full-path   [(==> :any :any) :and (==> :any :any)]})

(def rule3
  '{:p1          (==> S M)
    :p2          (==> (conj S :list/A) M)
    :conclusions [{:conclusion (==> (conj :list/A) M)
                   :post       (:t/decompose-npp
                                 :order-for-all-same
                                 :seq-interval-from-premises)}]
    :full-path   [(==> :any :any) :and (==> (conj :any :any) :any)]})

(deftest test-order-for-all-same?
  (is ((complement nil?) (order-for-all-same? rule1)))
  (is (nil? (order-for-all-same? rule2))))

(def res1
  '({:p1          (=|> M P),
     :p2          (=|> S M),
     :conclusions [{:conclusion (=|> S P),
                    :post       (:t/deduction :order-for-all-same :allow-backward)}],
     :full-path   [(=|> :any :any) :and (=|> :any :any)],
     :pre         nil}
     {:p1          (==> M P),
      :p2          (==> S M),
      :conclusions [{:conclusion (==> S P),
                     :post       (:t/deduction :order-for-all-same :allow-backward)}],
      :full-path   [(==> :any :any) :and (==> :any :any)],
      :pre         nil}
     {:p1          (retro-impl M P),
      :p2          (retro-impl S M),
      :conclusions [{:conclusion (retro-impl S P),
                     :post       (:t/deduction :order-for-all-same :allow-backward)}],
      :full-path   [(retro-impl :any :any) :and (retro-impl :any :any)],
      :pre         nil}
     {:p1          (pred-impl M P),
      :p2          (pred-impl S M),
      :conclusions [{:conclusion (pred-impl S P),
                     :post       (:t/deduction :order-for-all-same :allow-backward)}],
      :full-path   [(pred-impl :any :any) :and (pred-impl :any :any)],
      :pre         nil}))

(def res2
  '({:p1          (pred-impl S M),
     :p2          (pred-impl (seq-conj S :list/A) M),
     :conclusions [{:conclusion (pred-impl (seq-conj :list/A) M),
                    :post       (:t/decompose-npp
                                  :order-for-all-same
                                  :seq-interval-from-premises)}],
     :full-path   [(pred-impl :any :any) :and (pred-impl (seq-conj :any :any) :any)],
     :pre         nil}
     {:p1          (retro-impl S M),
      :p2          (retro-impl (seq-conj S :list/A) M),
      :conclusions [{:conclusion (retro-impl (seq-conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(retro-impl :any :any)
                    :and
                    (retro-impl (seq-conj :any :any) :any)],
      :pre         nil}
     {:p1          (=|> S M),
      :p2          (=|> (seq-conj S :list/A) M),
      :conclusions [{:conclusion (=|> (seq-conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(=|> :any :any) :and (=|> (seq-conj :any :any) :any)],
      :pre         nil}
     {:p1          (pred-impl S M),
      :p2          (pred-impl (conj S :list/A) M),
      :conclusions [{:conclusion (pred-impl (conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(pred-impl :any :any) :and (pred-impl (conj :any :any) :any)],
      :pre         nil}
     {:p1          (==> S M),
      :p2          (==> (conj S :list/A) M),
      :conclusions [{:conclusion (==> (conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(==> :any :any) :and (==> (conj :any :any) :any)],
      :pre         nil}
     {:p1          (=|> S M),
      :p2          (=|> (&| S :list/A) M),
      :conclusions [{:conclusion (=|> (&| :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(=|> :any :any) :and (=|> (&| :any :any) :any)],
      :pre         nil}
     {:p1          (retro-impl S M),
      :p2          (retro-impl (conj S :list/A) M),
      :conclusions [{:conclusion (retro-impl (conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(retro-impl :any :any) :and (retro-impl (conj :any :any) :any)],
      :pre         nil}
     {:p1          (==> S M),
      :p2          (==> (&| S :list/A) M),
      :conclusions [{:conclusion (==> (&| :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(==> :any :any) :and (==> (&| :any :any) :any)],
      :pre         nil}
     {:p1          (=|> S M),
      :p2          (=|> (conj S :list/A) M),
      :conclusions [{:conclusion (=|> (conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(=|> :any :any) :and (=|> (conj :any :any) :any)],
      :pre         nil}
     {:p1          (pred-impl S M),
      :p2          (pred-impl (&| S :list/A) M),
      :conclusions [{:conclusion (pred-impl (&| :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(pred-impl :any :any) :and (pred-impl (&| :any :any) :any)],
      :pre         nil}
     {:p1          (==> S M),
      :p2          (==> (seq-conj S :list/A) M),
      :conclusions [{:conclusion (==> (seq-conj :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(==> :any :any) :and (==> (seq-conj :any :any) :any)],
      :pre         nil}
     {:p1          (retro-impl S M),
      :p2          (retro-impl (&| S :list/A) M),
      :conclusions [{:conclusion (retro-impl (&| :list/A) M),
                     :post       (:t/decompose-npp
                                   :order-for-all-same
                                   :seq-interval-from-premises)}],
      :full-path   [(retro-impl :any :any) :and (retro-impl (&| :any :any) :any)],
      :pre         nil}))

(deftest test-generate-all-orders
  (both-equal
    res1 (generate-all-orders rule1)
    res2 (generate-all-orders rule3)))
