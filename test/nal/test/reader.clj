(ns nal.test.reader
  (:require [nal.reader :refer :all]
            [clojure.test :refer :all]
            [nal.test.test-utils :refer [both-equal]]))

(deftest test-replacements
  (are [s1 s2] (= s1 (replacements s2))
    "(ext-set a b)" "{a b}"
    "(int-set a b)" "[a b]"
    "(retro-impl a b)" "(=\\> a b)"
    "(pred-impl a b)" "(=/> a b)"
    "(a int-dif b)" "(a ~ b)"
    "(seq-conj  a b c)" "(&/ a b c)"
    "(ext-inter a b)" "(& a b)"
    "(a ext-inter b)" "(a & b)"
    "(conj a b)" "(&& a b)"
    "(inst a b)" "({-- a b)"
    "(prop a b)" "(--] a b)"
    "(prop a (int-set b v))" "(--] a [b v])"
    "(inst-prop a b)" "({-] a b)"
    "(int-image a b)" "(\\ a b)"
    "(ext-image a b)" "(/ a b)"
    "(ind-var X)" "$X"
    "(dep-var X)" "#X"))

(deftest test-read-rule
  (are [l s] (= l (read-rule s))
    '[A --> B] "A --> B"
    '[(A --> (int-set D C)) (A int-dif B)] "(A --> [D C]) (A ~ B)"
    '[A --> B] "A --> B"

    '[((int-set A) <-> (int-set B)) (A <-> B) |- ((int-set A) <-> (int-set B))
      :pre (:question?)
      :post (:t/belief-identity :p/judgment)]
    "([A] <-> [B]) (A <-> B) |- ([A] <-> [B]) :pre (:question?) :post (:t/belief-identity :p/judgment)"

    '[(M retro-impl P) (M retro-impl S) |- ((M retro-impl (P &| S)) :post (:t/intersection)
                                             (M retro-impl (P || S)) :post (:t/union))
      :pre ((:!= S P))]
    "(M =\\> P) (M =\\> S) |- ((M =\\> (P &| S)) :post (:t/intersection) (M =\\> (P || S)) :post (:t/union)) :pre ((:!= S P))"))
