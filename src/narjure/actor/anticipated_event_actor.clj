(ns narjure.actor.anticipated-event-actor
  (:require
    [co.paralleluniverse.pulsar
     [core :refer :all]
     [actors :refer :all]
     ])
  (:refer-clojure :exclude [promise await])
  )

(declare process-system-time)
(declare process-input-task)
(declare process-anticipated-event)
(declare anticipated-event-actor)

(defsfn anticipated-event-actor
        "state is system-time and collection of anticipated events"
        []
        (register! :anticipated-event @self)
        (set-state! {:time 0 :anticipated-events {}})
        (loop []
          (receive [msg]
                   [:system-time-msg time] (set-state! (process-system-time time @state))
                   [:input-task-msg input-task] (set-state! (process-input-task input-task @state))
                   [:anticipated-event-msg anticipated-event] (set-state! (process-anticipated-event anticipated-event @state))
                   :else (! :logger [:log-msg :log-debug :anticipated-event (str "unhandled msg:" msg)]))
          (recur)))

(defn process-system-time [time state]
                             (! :logger [:log-msg :log-debug :anticipated-event "process-system-time"])
                             {:time time :anticipated-events (state :percepts)})

(defn process-anticipated-event [_ _]
  ;(! :logger [:log-msg :log-debug :anticipated-event "process-anticipated-event"])
  )

(defn process-input-task [input-task _]
  ;(! :logger [:log-msg :log-debug :anticipated-event "process-input-task"])
  (! :task-dispatcher [:task-msg input-task]))
