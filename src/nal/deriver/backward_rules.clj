(ns nal.deriver.backward-rules
  (:require [nal.deriver.key-path :refer [rule-path]]))

;http://pastebin.com/3zLX7rPx
(defn allow-backward?
  "Return true if rule allows backward inference."
  [{:keys [conclusions]}]
  (some #{:allow-backward} (:post (first conclusions))))

(defn generate-backward-rule
  [{:keys [p1 p2 conclusions] :as rule}]
  (mapcat (fn [{:keys [conclusion post]}]
            (conj (map
                    (fn [r] (update r :pre conj :question?))
                    [(assoc rule :p1 conclusion
                                 :conclusions [{:conclusion p1
                                                :post       post}]
                                 :full-path (rule-path conclusion p2))
                     (assoc rule :p2 conclusion
                                 :conclusions [{:conclusion p2
                                                :post       post}]
                                 :full-path (rule-path p1 conclusion))])
                  rule))
          conclusions))

(defn generate-backward-rules
  [rules]
  (mapcat (fn [rule]
            (if (allow-backward? rule)
              (generate-backward-rule rule)
              [rule]))
          rules))
