(ns nal.test.deriver.backward-rules
  (:require [clojure.test :refer :all]
            [nal.deriver.backward-rules :refer :all]))

(deftest test-allow-backward?
  (are [res] (= res :allow-backward)
    (allow-backward? {:p1          :ok :p2 :boss
                      :conclusions [{:conclusion '[--> A B]
                                     :post       [:allow-backward]}]})
    (allow-backward? {:p1          :ok :p2 :boss
                      :conclusions [{:conclusion '[--> A B]
                                     :post       [:t/ok :another-condition
                                                  :allow-backward]}]}))
  (are [res] (nil? res)
    (allow-backward? {:p1          :ok :p2 :boss
                      :conclusions [{:conclusion '[--> A B]
                                     :post       []}]})
    (allow-backward? {:p1          :ok :p2 :boss
                      :conclusions [{:conclusion '[--> A B]
                                     :post       [:something :and-more
                                                  :t/abduction]}]})))

(def bw-rule
  '{:p1          (<-> S (ext-set P))
    :p2          S
    :conclusions [{:conclusion (--> S (ext-set P))
                   :post       (:t/identity :d/identity :allow-backward)}]
    :full-path   [(<-> :any (ext-set :any)) :and :any]
    :pre         nil})

(deftest test-generate-backward-rule
  (let [[_ r1 r2 r3 :as rules] (expand-backward-rules bw-rule)]
    (is (= 4 (count rules)))
    (are [np1 np2 rule]
      (let [{:keys [p1 p2]} rule]
        (and (= p1 np1) (= p2 np2)))

      '(<-> S (ext-set P)) 'S r1
      '(--> S (ext-set P)) 'S r2

      '(<-> S (ext-set P)) '(--> S (ext-set P)) r3)))

