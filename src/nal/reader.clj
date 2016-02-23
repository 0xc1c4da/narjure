(ns nal.reader
  (:require [clojure.string :as s]
            [nal.deriver :as d])
  (:import (clojure.lang LispReader$StringReader)))

(defn dispatch-reader-macro [ch fun]
  (let [dm (.get (doto (.getDeclaredField clojure.lang.LispReader "dispatchMacros")
                   (.setAccessible true))
                 nil)]
    (aset dm (int ch) fun)))

(defn read-rule
  ([rdr] (read-rule rdr "" 0))
  ([rdr prev cnt]
   (let [c (.read rdr)
         cnt (case (char c)
               \] (dec cnt)
               \[ (inc cnt)
               cnt)]
     (if (neg? cnt)
       prev
       (recur rdr (str prev (char c)) cnt)))))

(defn add-brackets [s]
  (str "[" s "]"))

(defn rule [rdr letter-R opts & other]
  (let [c (.read rdr)]
    (if (= c (int \[))
      (-> (read-rule rdr)
          (s/replace #"\{([^\}]*)\}" "(ext-set $1)")
          (s/replace #"\[([^\]]*)]" "(int-set $1)")
          (s/replace #"=\\>" "retro-impl")
          (s/replace #"=/>" "pred-impl")
          (s/replace #"~" "int-dif ")
          (s/replace #"&/" "seq-conj ")
          (s/replace #"\{--" "inst")
          (s/replace #"--]" "prop")
          (s/replace #"\{-\]" "inst-prop")
          (s/replace #"\(\\" "(int-image")
          (s/replace #"\(/" "(ext-image")
          (s/replace #"\$([A-Z])" "(ind-var $1)")
          (s/replace #"#([A-Z])" "(dep-var $1)")
          (s/replace #"\/([0-9]+)" "(/ $1)")
          add-brackets
          read-string)
      (throw (Exception. (str "Reader barfed on " (char c)))))))

(dispatch-reader-macro \R rule)
