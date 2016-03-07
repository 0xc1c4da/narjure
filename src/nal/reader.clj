(ns nal.reader
  (:require [clojure.string :as s])
  (:import (clojure.lang LispReader)))

(defn dispatch-reader-macro [ch fun]
  (let [dm (.get
             (doto
               (.getDeclaredField LispReader "dispatchMacros")
               (.setAccessible true))
             nil)]
    (aset dm (int ch) fun)))

(defn fetch-rule
  ([rdr] (fetch-rule rdr "" 0))
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

(defn replacements [s]
  (-> s
      (s/replace #"\{([^\}]*)\}" "(ext-set $1)")
      (s/replace #"\[([^\]]*)]" "(int-set $1)")
      (s/replace #"=\\>" "retro-impl")
      (s/replace #"=/>" "pred-impl")
      (s/replace #"~" "int-dif")
      (s/replace #"&/" "seq-conj ")
      (s/replace #"\(&\s" "(ext-inter ")
      (s/replace #"\s&\s" " ext-inter ")
      (s/replace #"&&" "conj")
      (s/replace #"\{--" "inst")
      (s/replace #"--]" "prop")
      (s/replace #"\{-\]" "inst-prop")
      (s/replace #"\(\\" "(int-image")
      (s/replace #"\(/" "(ext-image")
      (s/replace #"\$([A-Z])" "(ind-var $1)")
      (s/replace #"#([A-Z])" "(dep-var $1)")
      ;todo what does /0 mean?
      (s/replace #"\/([0-9]+)" "(op $1)")))

(defn read-rule [s]
  (-> s replacements add-brackets read-string))

(defn rule [rdr letter-R opts & other]
  (let [c (.read rdr)]
    (if (= c (int \[))
      (read-rule (fetch-rule rdr))
      (throw (Exception. (str "Reader barfed on " (char c)))))))

(dispatch-reader-macro \R rule)
