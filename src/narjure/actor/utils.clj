(ns narjure.actor.utils
  (:require
    [co.paralleluniverse.pulsar
     [actors :refer [set-state! state receive !]]]
    [taoensso.timbre :refer [debug]]))

(defmacro defhandler [name]
  `(do
     (defmulti ~name (fn [[t#] c#] t#))
     (defmethod ~name :default [a# b#] :unhandled)))

(defmacro actor-loop [name f]
  `(loop []
     (let [msg# (receive)
           result# (~f msg# @state)]
       (if (= :unhandled result#)
         (debug ~name (str "unhandled msg:" msg#))
         (set-state! result#))
       (recur))))
