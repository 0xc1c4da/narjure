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

(def log-level-string {:log-debug "DEBUG" :log-error "ERROR" :log-warning "WARNING" :log-info "INFO" })

(defn print-log-msg
  "Print local time, log-level and log-msg to standard out
  "
  [log-level msg-string]
  (println (str (l/local-now)) (log-level-string log-level) msg-string)
  )

(def my-state (atom :log-debug))


(defn process-log-msg
  "If msg log-level is in log level set (state) then output log-msg.
   state is the curently set global log-level and can be:
   :log-debug :log-warning :log-error :log-info
  "
  [log-level msg-string]
  (case @my-state
    :log-debug (if (#{:log-debug :log-warning :log-error :log-info} log-level) (print-log-msg log-level msg-string))
    :log-error (if (#{:log-warning :log-error :log-info} log-level) (print-log-msg log-level msg-string))
    :log-warning (if (#{:log-warning :log-info} log-level) (print-log-msg log-level msg-string))
    :log-info (if (#{:log-info} log-level) (print-log-msg log-level msg-string))))

(defn process-unhandled-msg [msg]
  (! @self [:log-msg :log-debug (str "In logger :else " msg)]))

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
                   [:log-msg log-level msg-string] (process-log-msg log-level msg-string)
                   :else (process-unhandled-msg msg))
          (recur)))