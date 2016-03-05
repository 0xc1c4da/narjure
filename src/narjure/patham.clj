(ns patham
  (:require [instaparse.core :as insta])
  (:gen-class))

(def ETERNAL -1)

;truth expectation, w2c, eternalization
(defn expectation [t]
    (+ (* (:confidence t) (- (:frequency t) 0.5)) 0.5))

(defn w2c [w] (/ w (+ w 1)))

;eternalie a task (note: this is only for event tasks!!)
(defn eternalize [t]
  (assoc t :confidence (w2c (:confidence t))
           :occurrence ETERNAL))

;whether the task has a question variable
(defn has-question-var [ref] false)

;ranking function, confidence for y/n, expectation for wh-tasks
(defn rank-value [ref t]
  (if (has-question-var ref)
    (:confidence t)
    (expectation t)))

;project task to ref (note: this is only for event tasks!!)
(defn project [t ref curtime]
  (let [sourcetime (:occurrence t)
        targettime (:occurrence ref)
        dist (fn [a b] (Math/abs (- a b)))]
    (assoc t
     :confidence (* (:confidence t)
                    (- 1 (/ (dist sourcetime targettime)
                            (+ (dist sourcetime curtime)
                               (dist targettime curtime)))))
     :occurrence targettime)))

(defn project-eternalize [t ref curtime]
  ;projecting/eternalizing a task to ref time
  (let [source-time (:occurrence t)
        target-time (:occurrence ref)
        get-eternal (fn [x] (if (= x ETERNAL) :eternal :temporal))]
    (case [(get-eternal target-time) (get-eternal source-time)]
      [:eternal  :eternal ] t
      [:temporal :eternal ] t
      [:eternal  :temporal] (eternalize t)
      [:temporal :temporal] (let [t-eternal (eternalize t)
                                  t-project (project t ref curtime)]
                              (if (> (:confidence t-eternal)
                                     (:confidence t-project))
                                t-eternal
                                t-project)))))

;rank a task according to a reference
(defn rank-task [ref curtime t]
  {:task t
   :value (rank-value ref (project-eternalize t ref curtime))})

;get the best ranked table entry when ranked according to ref
(defn best-ranked [table ref curtime]
  (apply max-key :value
         (map (partial rank-task ref curtime) table)))

;add belief to a table
(defn add-to-table [concept table x]
  (assoc concept table
                 (conj (concept table) x)))

;add to belief table
(defn add-to-beliefs [concept x]
  (add-to-table concept :beliefs x))

;add to desires table
(defn add-to-desires [concept x]
  (add-to-table concept :desires x))

;Concept data structure
(defn buildConcept [name]
  {:term name :beliefs [] :desires [] :tasklinks [] :termlinks []})

; (let [concept (buildConcept "test")]
;  (add-to-beliefs concept {:term "tim --> cat" :frequency 1 :confidence 0.75 :occurrence 10}))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;((insta/parser (clojure.java.io/resource "narsese.bnf") :auto-whitespace :standard) "<bird --> swimmer>. %0.10;0.60%")
  (do
    (project-eternalize {:term "tim --> cat" :frequency 1 :confidence 0.75 :occurrence 10} {:term "tim --> cat" :frequency 1 :confidence 0.75 :occurrence 10} 100))
    (print "lol")
  )
