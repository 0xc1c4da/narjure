(ns narjure.repl
  (:require [clojure.string :as s]
            [narjure.narsese :refer [parse]]
            [instaparse.core :as i]
            [nal.core :as c]
            [clojure.core.logic :as l]
            [clojure.tools.nrepl.middleware :refer [set-descriptor!]]))

(defonce narsese-repl-mode (atom false))
(def stop-word "stop!")

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

(defn handle-narsese [code]
  (let [result (parse code)]
    (if (and (not (i/failure? result)))
      (do (swap! buffer conj result)
          (collect! result))
      result)))

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

(defn- wrap-run-code [code] (str "(narjure.repl/run " code ")"))
(defn- wrap-stop-repl [_] "(narjure.repl/stop-narsese-repl!)")
(defn- narsese? [code]
  (#{\< \$} (first code)))

(defn wrapper [code]
  (cond
    (= stop-word code) wrap-stop-repl
    (integer? (parse-int code)) wrap-run-code
    :default wrap-code))

(defn narsese-handler [handler]
  (fn [{:keys [code] :as args} & tail]
    (println code)
    (apply handler (if @narsese-repl-mode
                     [(update args :code (wrapper code))]
                     (cons args tail)))))

(set-descriptor! #'narsese-handler
  {:requires #{}
   :expects  #{"eval"}
   :handles  {"stdin"
              {:doc      "Parses Narsese"
               :requires {}
               :optional {}
               :returns  {"status" "Clojure data structure."}}}})
