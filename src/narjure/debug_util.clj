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

(defn output-task [type task]
  (let [type-print (fn [t] "")
        narsese-print (fn [st] st)                      ;todo make fancy ^^
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