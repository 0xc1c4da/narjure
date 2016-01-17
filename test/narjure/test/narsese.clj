(ns narjure.test.narsese
  (:require [clojure.test :refer :all]
            [narjure.narsese :refer :all]
            [instaparse.core :refer [failure?]]))

(defn narsese->clj [s] (:data (parse s)))
(defn get-truth [s] (:truth (parse s)))

(deftest test-parser
  (is (= '([[conjunction [inheritance tim fish] [inheritance tom fish]]])
         (narsese->clj "(&&, <tim --> fish>, <tom --> fish>).")))
  (is (= '([[conjunction [inheritance tim fish] [inheritance tom fish]]])
         (narsese->clj "(<tim --> fish> && <tom --> fish>).")))
  (is (= '([[conjunction [inheritance tim fish] [inheritance tom fish]]])
         (narsese->clj "((tim --> fish) && (tom --> fish)).")))
  (is (= '([inheritance bird swimmer])
         (narsese->clj "<bird --> swimmer>.")))
  (is (= [1.0 0.9] (get-truth "<bird --> swimmer>. %1;0.9%")))
  (is (= '[[[negation bird]]] (:data (parse "--bird."))))
  (is (= '[[[negation bird]]] (:data (parse "(--,bird)."))))
  (is (= '[[[int-image bird animal]]] (:data (parse "(\\,bird,animal).")))))

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
