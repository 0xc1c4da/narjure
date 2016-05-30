(ns narjure.debug-util)

(def debug-messages 21)

(defn limit-string [st cnt]
  (subs st 0 (min (count st) cnt)))

(defn debuglogger [display message]
  (if (> debug-messages 0)
    (swap! display (fn [L] (if (< (count @display) debug-messages)
                            (conj @display [(limit-string (str message) 750) "§"])
                            (conj (drop-last @display) [(limit-string (str message) 750) "§"]))))))
