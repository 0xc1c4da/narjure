(ns narjure.test.narsese
  (:require [clojure.test :refer :all]
            [narjure.narsese :refer :all]
            [instaparse.core :refer [failure?]]))

(defn narsese->clj [s] (:statement (parse s)))
(defn get-truth [s] (:truth (parse s)))

(deftest test-parser
  (is (= '[[conj [--> tim fish] [--> tom fish]]]
         (narsese->clj "(&&, <tim --> fish>, <tom --> fish>).")))
  (is (= '[[conj [--> tim fish] [--> tom fish]]]
         (narsese->clj "(<tim --> fish> && <tom --> fish>).")))
  (is (= '[[conj [--> tim fish] [--> tom fish]]]
         (narsese->clj "((tim --> fish) && (tom --> fish)).")))
  (is (= '[--> bird swimmer]
         (narsese->clj "<bird --> swimmer>.")))
  (is (= [1.0 0.9] (get-truth "<bird --> swimmer>. %1;0.9%")))
  (is (= '[[-- bird]] (narsese->clj "--bird.")))
  (is (= '[[-- bird]] (narsese->clj "(--,bird).")))
  (is (= '[[int-image bird animal]] (narsese->clj "(\\,bird,animal).")))
  (is (= '[[conj [--> [dep-var Y] [int-set red]] [--> [dep-var Y] apple]]]
         (narsese->clj "(&&,<#Y --> [red]>,<#Y --> apple>).")))
  (is (= '[retrospective-implication
           [--> [product [ind-var x] room_101] enter]
           [--> [product [ind-var x] door_101] open]]
         (narsese->clj "<<( $x, room_101) --> enter> =\\> <( $x, door_101) --> open>>. %0.9;0.1%"))))

(deftest test-numbers-validation
  (is (not (failure? (parse "<bird --> swimmer>. %1;0.9%"))))
  ;check confidence
  (is (failure? (parse "<bird --> swimmer>. %1;1.9%")))
  (is (failure? (parse "<bird --> swimmer>. %1;1%")))
  (is (failure? (parse "<bird --> swimmer>. %1;0%")))
  (is (failure? (parse "<bird --> swimmer>. %1;0.0%")))
  (is (failure? (parse "<bird --> swimmer>. %1;101%")))
  (is (failure? (parse "<bird --> swimmer>. %1;55.55%")))
  ;check frequence
  (is (not (failure? (parse "<bird --> swimmer>. %0.1;0.9%"))))
  (is (not (failure? (parse "<bird --> swimmer>. %0.0;0.9%"))))
  (is (not (failure? (parse "<bird --> swimmer>. %0;0.9%"))))
  (is (not (failure? (parse "<bird --> swimmer>. %1.00;0.9%"))))
  (is (not (failure? (parse "<bird --> swimmer>. %1.;0.9%"))))
  (is (failure? (parse "<bird --> swimmer>. %5;0.9%")))
  (is (failure? (parse "<bird --> swimmer>. %-1;0.9%"))))
