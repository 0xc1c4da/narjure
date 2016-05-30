(ns narjure.debug-util)

(def debug-messages 6)
(defn debuglogger [display message]
  (if (> debug-messages 0)
    (swap! display (fn [L] (if (< (count @display) debug-messages)
                            (conj @display [message "§"])
                            (conj (drop-last @display) [message "§"]))))))