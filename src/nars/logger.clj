(ns nars.logger)
(ns nars.logger
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:require [clj-time.local :as l])
  (:refer-clojure :exclude [promise await])
  (:gen-class))

(declare logger)

(defn process-unhandled-msg [msg]
  (! @self [:log-msg :log-debug (str "In sentence-parser :else" msg)]))

(defsfn logger
        "Actor to provide log to console service.
         Ensures multiple thread println do not clash
        "
        [log-level]
        (register! :logger @self)
        (set-state! log-level)
        (loop []
          (receive [msg]
                   [:log-level level] (set-state! level)
                   [:log-msg log-level msg-string] (case @state
                                                     :log-debug (if (#{:log-debug :log-warning :log-error :log-info} log-level) (println (str (l/local-now)) "DEBUG " msg-string))
                                                     :log-error (if (#{:log-warning :log-error :log-info} log-level) (println (str (l/local-now)) "ERROR " msg-string))
                                                     :log-warning (if (#{:log-warning :log-info} log-level) (println (str (l/local-now)) "WARNING " msg-string))
                                                     :log-info (if (#{:log-info} log-level) (println (str (l/local-now)) "INFO " msg-string)))

                     :else (process-unhandled-msg msg))
                   (recur)))
