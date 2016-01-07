(ns narjure.core
    (:require [instaparse.core :as insta])
    (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ((insta/parser (clojure.java.io/resource "narsese.bnf") :auto-whitespace :standard) "<bird --> swimmer>. %0.10;0.60%"))
