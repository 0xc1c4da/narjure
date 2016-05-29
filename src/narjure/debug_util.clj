(ns narjure.debug-util)

(def debug 1000)
(defn debuglogger [display message]
  (if (> debug 0)
    (swap! display (fn [L] (if (< (count @display) debug)
                            (conj @display message)
                            (conj (drop-last @display) message))))))