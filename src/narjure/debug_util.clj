(ns narjure.debug-util
  (:require [narjure.global-atoms :refer :all]))

(def debug-messages 21)

(defn limit-string [st cnt]
  (subs st 0 (min (count st) cnt)))

(defn debuglogger
  ([display message]
    (debuglogger (atom "") display message))
  ([filter display message]
   (if (> debug-messages 0)
     (swap! display (fn [d] (let [msg (str message)]
                              (if (.contains msg (deref filter))
                                (if (< (count d) debug-messages)
                                 (conj d [(limit-string msg 750) "§"])
                                 (conj (drop-last d) [(limit-string msg 750) "§"]))
                                d)))))))

(defn narsese-print [st]
  (let [beautify (fn [co]
                   (case co
                     pred-impl "=/>"
                     retro-impl "=\\>"
                     ext-inter "&"
                     int-dif "~"
                     ext-image "/"
                     int-image "\\"
                     conj "&&"
                     seq-conj "&/"
                     co))]
    (if (coll? st)
     (let [cop (first st)
           [left right] (case cop
                          ext-set ["{" "}"]
                          int-set ["[" "]"]
                          --> ["<" ">"]
                          <-> ["<" ">"]
                          ==> ["<" ">"]
                          pred-impl ["<" ">"]
                          =|> ["<" ">"]
                          retro-impl ["<" ">"]
                          <=> ["<" ">"]
                          <|> ["<" ">"]
                          </> ["<" ">"]
                          ["(" ")"])
           syll-cop ['--> '<-> '==> '=|>
                     'pred-impl 'retro-impl
                     '<=> '<|> '</>]
           seperator (if (some #{cop} syll-cop)
                       " "
                       ",")
           infixprint (if (some #{cop} syll-cop)
                        [(second st) (first st) (nth st 2)]
                        st)
           var-and-ival (fn [st]
                          (if (= (first st) :interval)
                            [(str "i" (second st))]
                            (if (= (first st) 'dep-var)
                              [(str "#" (second st))]
                              (if (= (first st) 'ind-var)
                                [(str "$" (second st))]
                                (if (= (first st) 'qu-var)
                                  [(str "?" (second st))]
                                  st)))))
           ivar-val (var-and-ival infixprint)
           [leftres rightres] (if (= ivar-val infixprint)
                                [left right]
                                ["" ""])
           res (if (or (= (first ivar-val) 'ext-set)
                       (= (first ivar-val) 'int-set))
                 (rest ivar-val)
                 ivar-val)]
       (str leftres
            (apply str (for [x res]
                         (if (= x (first res))
                           (narsese-print x)
                           (str seperator (narsese-print x)))))
            rightres))
     (str (beautify st)))))

(defn output-task [type task]
  (let [type-print (fn [t] "")
        punctuation-print (fn [task-type]
                            (case task-type
                              :goal "!"
                              :quest "@"
                              :question "?"
                              :belief "."))
        time-print (fn [occurrence]
                      (if (= occurrence :eternal)
                        ""
                        (str ":|" occurrence "|:")))
        truth-print (fn [truth]
                      (if (= truth nil)
                        ""
                        (str "%" (first truth) ";" (second truth) "%")))]
    (debuglogger output-search output-display (str (type-print type)
                                                   (narsese-print (:statement task))
                                                   (punctuation-print (:task-type task))
                                                   " "
                                                   (time-print (:occurrence task))
                                                   " "
                                                   (truth-print (:truth task))))))