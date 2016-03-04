(ns nal.test.deriver.substitution
  (:require [clojure.test :refer :all]
            [nal.deriver.substitution :refer :all]))

(deftest test-replace-vars
  (are [a1 a2 a3] (= a1 (replace-vars a2 a3))
    '[--> ?X a]
    'ind-var
    '[--> [ind-var X] a]

    '[--> [ind-var X] a]
    'dep-var
    '[--> [ind-var X] a]

    '[==> [--> ?X k] ?Y]
    'dep-var
    '[==> [--> [dep-var X] k] [dep-var Y]]

    '[==> [--> ?X k] ?X]
    'dep-var
    '[==> [--> [dep-var X] k] [dep-var X]]

    '[==> [--> ?X k] [ind-var Y]]
    'dep-var
    '[==> [--> [dep-var X] k] [ind-var Y]]))

(deftest test-unification-map
  (are [a1 a2] (= a1 (apply unification-map a2))
    ;successfully unifies
    '{?X tim} ["$" '[--> [ind-var X] alcoholic] '[--> tim alcoholic]]

    ;unifies even if there is no vars
    {} ["$" '[--> tim alcoholic] '[--> tim alcoholic]]

    ;doesn't unify, var can be binded only once
    nil ["$" '[==> [--> [ind-var X] a] [--> [ind-var X] b]]
         '[==> [--> ok a] [--> boss b]]]

    ;unifies, different vars can contain the same values
    '{?X ok ?Y ok}
    ["$" '[==> [--> [ind-var X] a] [--> [ind-var Y] b]]
     '[==> [--> ok a] [--> ok b]]]))

(deftest test-placeholder->symbol
  (are [a1 a2] (= a1 (placeholder->symbol a2))
    'X '?X
    (symbol "1") '?1))

(deftest test-replace-placeholders
  (are [a1 a2] (= a1 (apply replace-placeholders a2))
    '{[dep-var X] 1 [dep-var Y] 2}
    '[dep-var {?X 1 ?Y 2}]

    '{[ind-var X] 1}
    '[ind-var {?X 1}]))
