(ns narjure.actor.utils
  (:require
    [co.paralleluniverse.pulsar
     [core :refer [defsfn]]
     [actors :refer [set-state! state receive ! self register!]]]
    [taoensso.timbre :refer [debug]]))


(defmacro actor-loop [name f]
  `(loop []
     (let [msg# (receive)
           result# (~f msg# @state)]
       (if (= :unhandled result#)
         (debug ~name (str "unhandled msg:" msg#))
         (set-state! result#))
       (recur))))

(defn create-actor
  ([name handlers]
   (create-actor name "" {} handlers))
  ([name doc handlers]
   (create-actor name doc {} handlers))
  ([name doc default-state handlers]
   (let [aname (keyword name)
         has-args (and (vector? default-state)
                       (symbol? (first default-state)))]
     `(defsfn ~name ~doc ~(if has-args default-state [])
        ~@(when (not= 'concept name) [`(register! ~aname @self)])
        (set-state! ~(if has-args
                       (first default-state)
                       default-state))
        (loop []
          (let [msg# (receive)
                handler# (get ~handlers (first msg#) :unhandled)]
            (if (= :unhandled handler#)
              (debug ~name (str "unhandled msg:" msg#))
              (set-state! (handler# msg# @state)))
            (recur)))))))

(defmacro defactor [& args]
  (apply create-actor args))
