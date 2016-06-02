(ns narjure.debug-util)

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
