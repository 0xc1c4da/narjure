(ns nal.test.deriver
  (:require [clojure.test :refer :all]
            [nal.deriver :refer :all]
            nal.reader))

(deftest test-infix->prefix
  (is (= '(--> A B) (infix->prefix '(A --> B))))
  (is (= '(--> (- B G) S) (infix->prefix '((B - G) --> S))))
  (is (= '(==> (--> $X S) (--> $X P))
         (infix->prefix '(($X --> S) ==> ($X --> P))))))

(deftest test-path
  (is (= '(--> :any :any) (path '(--> A B))))
  (is (= '(--> (- :any :any) :any) (path '(--> (- B G) S))))
  (is (= '(==> (--> :any :any) (--> :any :any))
         (path '(==> (--> $X S) (--> $X P))))))

(deftest test-rule-path
  (is (= [:any :and :any] (rule-path 'A 'B)))
  (is (= '((--> :any :any) :and (--> :any :any))
         (rule-path '(--> A B) '(--> B C)))))

(deftest test-cart
  (is (= '((1 2 3) (1 2 4) (2 2 3) (2 2 4)) (cart [[1 2] [2] [3 4]]))))

(deftest test-path-invariants
  (is (= '((==> (--> :any :any) (--> :any :any))
            (==> (--> :any :any) :any)
            (==> :any (--> :any :any))
            (==> :any :any)
            :any)
         (path-invariants '(==> (--> :any :any) (--> :any :any))))))

(deftest test-all-paths
  (is
    (= '([(==> (--> :any :any) (--> :any :any)) :and (--> (- :any :any) :any)]
          [(==> (--> :any :any) (--> :any :any)) :and (--> :any :any)]
          [(==> (--> :any :any) (--> :any :any)) :and :any]
          [(==> (--> :any :any) :any) :and (--> (- :any :any) :any)]
          [(==> (--> :any :any) :any) :and (--> :any :any)]
          [(==> (--> :any :any) :any) :and :any]
          [(==> :any (--> :any :any)) :and (--> (- :any :any) :any)]
          [(==> :any (--> :any :any)) :and (--> :any :any)]
          [(==> :any (--> :any :any)) :and :any]
          [(==> :any :any) :and (--> (- :any :any) :any)]
          [(==> :any :any) :and (--> :any :any)]
          [(==> :any :any) :and :any]
          [:any :and (--> (- :any :any) :any)]
          [:any :and (--> :any :any)]
          [:any :and :any])
       (all-paths
         '(==> (--> :any :any) (--> :any :any))
         '(--> (- :any :any) :any)))))

(deftest test-options
  (is (= {:pre [] :post []} (options [:pre [] :post []])))
  (is (= {:pre []} (options [:pre [] []]))))

(deftest test-neg-symbol?
  (is (true? (#'nal.deriver/neg-symbol? '--)))
  (is (true? (#'nal.deriver/neg-symbol? '--A)))
  (is (false? (#'nal.deriver/neg-symbol? '-->)))
  (is (false? (#'nal.deriver/neg-symbol? 'B))))

(deftest test-trim-negation
  (is (= 'A (#'nal.deriver/trim-negation '--A))))

(deftest test-neg-el
  (is (= '(-- A) (neg 'A))))

(deftest test-replace-negation
  (is (= '(-- A) (replace-negation '--A)))
  (is '(A (-- (--> A B))) (= (replace-negation '(A -- (--> A B)))))
  (is (= '(--> A B) (replace-negation '(--> A B))))
  (is (= '((-- A)) (replace-negation '[-- A])))
  (is (= '(-- A) (replace-negation '(-- A)))))

(deftest test-get-conclusions
  (is (= [{:conclusion '(--> A B), :post nil}] (get-conclusions '(--> A B) {})))
  (is (= [{:conclusion '(--> A B), :post [:something]}]
         (get-conclusions '(--> A B) {:post [:something]})))
  (is (= '({:conclusion (=|> S P), :post (:t/induction :linkage-temporal)}
            {:conclusion (=|> P S), :post (:t/induction :linkage-temporal)}
            {:conclusion (<|> S P), :post (:t/comparison :linkage-temporal)}
            {:conclusion (&| S P), :post (:t/intersection :linkage-temporal)})
         (get-conclusions
           '((=|> S P) :post (:t/induction :linkage-temporal)
              (=|> P S) :post (:t/induction :linkage-temporal)
              (<|> S P) :post (:t/comparison :linkage-temporal)
              (&| S P) :post (:t/intersection :linkage-temporal)) {}))))

(def trule (rule '#R[(D =/> R) (D --> K) |- ((K =/> R) :post (:t/abduction)
                                              (R --> K) :post (:t/induction)
                                              (K </> R) :post (:t/comparison))
                     :pre ((:!= R K))]))
(deftest test-rule
  (is (= '{:p1          (--> S P),
           :p2          (<-> S P),
           :conclusions [{:conclusion (--> S P), :post (:t/struct-int :p/judgment)}],
           :full-path   [(--> :any :any) :and (<-> :any :any)],
           :pre         (:question?)}
         (rule '#R[(S --> P) (S <-> P) |- (S --> P) :post (:t/struct-int :p/judgment) :pre (:question?)])))
  (is (= '{:p1          (=/> D R),
           :p2          (--> D K),
           :conclusions ({:conclusion (=/> K R), :post (:t/abduction)}
                          {:conclusion (--> R K), :post (:t/induction)}
                          {:conclusion (</> K R), :post (:t/comparison)}),
           :full-path   [(=/> :any :any) :and (--> :any :any)],
           :pre         ((:!= R K))}
         trule)))
