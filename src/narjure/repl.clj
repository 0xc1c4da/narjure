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

(defn clear-db! [] (reset! db {}))

(defn collect!
  [{:keys [statement truth] :as task}]
  #_(swap! db cycle/task->buffer task)
  [statement truth])

(defn- parse-int [s]
  (try (Integer/parseInt s) (catch Exception _)))

(defn- wrap-code [code]
  (str "(narjure.repl/handle-narsese \"" code "\")"))

(defn run [n]
  #_(swap! db cycle/do-cycles n)
  #_(cycle/print-results! @db)
  (swap! db dissoc :forward-inf-results :local-inf-results :answers)
  nil)

(defn- get-result [code]
  (let [result (parse code)]
    (if (and (not (i/failure? result)))
      (collect! result)
      result)))

(defn handle-narsese [code]
  (let [n (parse-int (trim code))]
    (cond
      (integer? n) (do (run n) nil)
      (= "stop!" (trim code)) (stop-narsese-repl!)
      (= \* (first code)) (do (clear-db!) nil)
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
