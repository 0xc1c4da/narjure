(ns nal.test.deriver.list-expansion
  (:require
    [clojure.test :refer :all]
    [nal.deriver.list-expansion :refer :all]))

(deftest test-get-list
  (are [a1 a2] (= a1 (apply get-list a2))
    :list/A
    [":list"
     '[(W --> (| B :list/A))
       (W --> B)
       |-
       (W --> (| B :list/A))
       :pre
       (:question?)
       :post
       (:t/belief-structural-deduction :p/judgment)]]

    :from/A
    [":from"
     '[(C --> (ext-set :list/A))
       C
       |-
       (C --> (ext-set :from/A))
       :post
       (:t/structural-deduction)]]))


(deftest test-gen-symbols
  (are [a1 a2] (= a1 (apply gen-symbols a2))
    '[A1 A2 A3] ['A 3]
    '[B1 B2 B3 B4 B5] ['B 5]))

(deftest test-replace-list-elemets
  (are [a1 a2] (= a1 (apply replace-list-elemets a2))
    '[(W --> (| B A1 A2 A3 A4))
      (W --> B)
      |-
      (W --> (| B A1 A2 A3 A4))
      :pre
      (:question?)
      :post
      (:t/belief-structural-deduction :p/judgment)]

    ['[(W --> (| B :list/A))
       (W --> B)
       |-
       (W --> (| B :list/A))
       :pre
       (:question?)
       :post
       (:t/belief-structural-deduction :p/judgment)]
     :list/A
     "A"
     4]

    '[--> B1 B2 B3 B4 B5]
    ['[--> :list/B] :list/B "B" 5]))

(deftest test-list-name
  (are [a1 a2] (= a1 (list-name a2))
    "A" :list/A
    "B" :list/B))

(deftest test-expand-:from-element
  (are [a1 a2] (= a1 (apply expand-:from-element a2))
    '([--> k A1] [--> k A2] [--> k A3])
    ['[--> k :from/A] :from/A "A" 3]

    '([--> k [conj d A1]])
    ['[--> k [conj d :from/A]] :from/A "A" 1]))

(deftest test-generate-all-lists
  (are [a1 a2] (= a1 (generate-all-lists a2))

    '([(W --> (| B A1))
       (W --> B)
       |-
       (W --> (| B A1))
       :pre
       (:question?)
       :post
       (:t/belief-structural-deduction :p/judgment)]
       [(W --> (| B A1 A2))
        (W --> B)
        |-
        (W --> (| B A1 A2))
        :pre
        (:question?)
        :post
        (:t/belief-structural-deduction :p/judgment)]
       [(W --> (| B A1 A2 A3))
        (W --> B)
        |-
        (W --> (| B A1 A2 A3))
        :pre
        (:question?)
        :post
        (:t/belief-structural-deduction :p/judgment)]
       [(W --> (| B A1 A2 A3 A4))
        (W --> B)
        |-
        (W --> (| B A1 A2 A3 A4))
        :pre
        (:question?)
        :post
        (:t/belief-structural-deduction :p/judgment)]
       [(W --> (| B A1 A2 A3 A4 A5))
        (W --> B)
        |-
        (W --> (| B A1 A2 A3 A4 A5))
        :pre
        (:question?)
        :post
        (:t/belief-structural-deduction :p/judgment)])
    '[(W --> (| B :list/A))
      (W --> B)
      |-
      (W --> (| B :list/A))
      :pre
      (:question?)
      :post
      (:t/belief-structural-deduction :p/judgment)]))
