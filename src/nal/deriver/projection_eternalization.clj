(ns nal.deriver.projection-eternalization
  (:require
    [nal.deriver.truth :refer [w2c frequency confidence]]))

;temporally project task to ref task (note: this is only for event tasks!!)"
(defn project-to [target-time t cur-time]
  (when (= :eternal (:occurence t))
    (println "ERROR: Project called on eternal task!!"))
  (let [source-time (:occurrence t)
        dist (fn [a b] (Math/abs (- a b)))]
    (assoc t
      :truth [(frequency t)
              (* (confidence t)
                 (- 1 (/ (dist source-time target-time)
                         (+ (dist source-time cur-time)
                            (dist target-time cur-time)))))]
      :occurrence target-time)))

;eternalize an event task to a task of eternal occurrence time
(defn eternalize [t]
  (assoc t :truth [(frequency t) (w2c (confidence t))]
             :occurrence :eternal))

;temporally projecting/eternalizing a task to ref time
(defn project-eternalize-to [target-time t cur-time]
  (let [source-time (:occurrence t)
        get-eternal (fn [x] (if (= x :eternal) :eternal :temporal))]
    (case [(get-eternal target-time) (get-eternal source-time)]
      [:eternal  :eternal ] t
      [:temporal :eternal ] t
      [:eternal  :temporal] (eternalize t)
      [:temporal :temporal] (let [t-eternal (eternalize t)
                                  t-project (project-to target-time t cur-time)]
                              (if (> (confidence t-eternal)
                                     (confidence t-project))
                                t-eternal
                                t-project)))))