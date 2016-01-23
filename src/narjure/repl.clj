(ns narjure.repl
  (:require [clojure.string :as s]
            [narjure.narsese :refer [parse]]
            [instaparse.core :as i]
            [nal.core :as c]
            [clojure.core.logic :as l]
            [clojure.string :refer [trim]]
            [clojure.pprint :as p]
            [clojure.tools.nrepl.middleware :refer [set-descriptor!]]))

(defonce narsese-repl-mode (atom false))

(defn start-narsese-repl! []
  (reset! narsese-repl-mode true)
  (println "Narsese repl was started."))

(defn stop-narsese-repl! []
  (reset! narsese-repl-mode false)
  (println "Narsese repl was stopped."))

(defonce db (atom {}))
(defonce buffer (atom []))

(defn clear-db! [] (reset! db {}))
(defn clear-buffer! [] (reset! buffer {}))

(defmulti collect! :action)

(defn revision! [statement known-truth truth]
  ;I think that core.logic usage is not necesssary for revision calculation
  (let [[[_ new-truth]]
        (l/run* [q]
          (c/revision [statement known-truth] [statement truth] q))]
    (swap! db assoc statement new-truth)
    [statement new-truth]))

(defn inference [n st1 st2]
  (l/run n [q] (c/inference st1 st2 q)))

(defmethod collect! :judgement [{:keys [truth data]}]
  (let [truth (if (empty? truth) [1 0.9] truth)
        statement (first data)
        known-truth (@db statement)]
    (cond (nil? known-truth) (do (swap! db assoc statement truth) [statement truth])
          (not= known-truth truth) (revision! statement known-truth truth)
          :default [statement known-truth])))

(defmethod collect! :default [_])

(defn- parse-int [s]
  (try (Integer/parseInt s) (catch Exception _)))

(defn- wrap-code [code]
  (str "(narjure.repl/handle-narsese \"" code "\")"))

(defn- sentence [{:keys [data]}]
  (let [statement (first data)]
    [statement (@db statement)]))

(defn run [n]
  (let [last-two (map sentence (take-last 2 @buffer))
        forward (apply inference n last-two)
        backward (if (> n (count forward))
                   (apply inference n (reverse last-two))
                   [])]
    (into forward backward)))

(defn- get-result [code]
  (let [result (parse code)]
    (if (and (not (i/failure? result)))
      (do (swap! buffer conj result)
          (collect! result))
      result)))

(defn handle-narsese [code]
  (let [n (parse-int (trim code))]
    (cond
      (integer? n) (p/pprint (run n))
      (= "stop!" (trim code)) (stop-narsese-repl!)
      (= \* (first code)) (reset! buffer [])
      (= \/ (first code)) ""
      :default (get-result code))))

(defn narsese-handler [handler]
  (fn [args & tail]
    (apply handler (if @narsese-repl-mode
                     [(update args :code wrap-code)]
                     (cons args tail)))))

(set-descriptor! #'narsese-handler
  {:expects #{"eval"}
   :handles {"stdin"
             {:doc      "Parses Narsese"
              :requires #{"code" "Code."}}}})
