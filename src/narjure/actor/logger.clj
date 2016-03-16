(ns narjure.actor.logger
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ]
    [clj-time.local :as l])
  (:refer-clojure :exclude [promise await])
  )

(declare log-level-string)
(declare print-log-msg)
(declare process-log-msg)
(declare logger)

(defsfn logger
        "Actor that provides a message log to stdio service.
         Ensures multiple thread println do not clash

         useage: (! :logger [msg-type args])

         where msg-type can be:

          :log-level - sets the logger logging level
          arg
            :log-debug | :log-error | :log-warning | :log-info

          :log-msg - prints a log message to stdio specifying the requesting actor
          args
            log-level   - :log-debug | :log-error | :log-warning | :log-info
            from        - key representing the actor that sent the :log-msg, can be :anon
            msg-string  - string message to output

          Examples :-
            (! :logger [:log-level :log-info])
            (! :logger [:log-msg :log-debug :task-dispatcher \"processing task\"])
            (! :logger [:log-msg :log-debug :anon \"processing task\"])
        "
        [in-log-level]

        ; register logger actor as :logger
        (register! :logger @self)
        ; set state to input arg
        (set-state! in-log-level)
        (loop []
          (receive [msg]
                   [:log-level level] (set-state! level)
                   [:log-msg log-level from  msg-string] (process-log-msg log-level from msg-string)
                   :else   (print-log-msg :log-info :logger (str "unhandled msg" msg)))
                   (recur)))

(def log-level-string {:log-debug "[DEBUG]" :log-error "[ERROR]" :log-warning "[WARNING]" :log-info "[INFO]" })

(defn print-log-msg
  "Print local time, log-level, requesting actor and log-msg to standard out
  "
  [log-level from msg-string]
  (if (= from :anon)
    (println (str (l/local-now)) (log-level-string log-level) msg-string)
    (println (str (l/local-now)) (log-level-string log-level) (str from) msg-string))
  )

(defn process-log-msg
  "If msg log-level is in log level set (state) then output log-msg.
   state is the curently set global log-level and can be:
   :log-debug :log-warning :log-error :log-info
  "
  [log-level from msg-string]
  (case @state
    :log-debug   (when (#{:log-debug :log-warning :log-error :log-info} log-level) (print-log-msg log-level from msg-string))
    :log-error   (when (#{:log-warning :log-error :log-info} log-level)            (print-log-msg log-level from msg-string))
    :log-warning (when (#{:log-warning :log-info} log-level)                       (print-log-msg log-level from msg-string))
    :log-info    (when (#{:log-info} log-level)                                    (print-log-msg log-level from msg-string))))
