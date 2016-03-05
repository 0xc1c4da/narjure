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
           :occurence ETERNAL))

;whether the task has a question variable
(defn has-question-var [ref] false)

;ranking function, confidence for y/n, expectation for wh-tasks
(defn rank-value [ref t]
  (if (has-question-var ref)
    (:confidence t)
    (expectation t)))

;project task to ref (note: this is only for event tasks!!)
(defn project [t ref curtime]
  (let [sourcetime (:occurence t)
        targettime (:occurence ref)
        dist (fn [a b] (Math/abs (- a b)))]
    (assoc t
     :confidence (* (:confidence t)
                    (/ (dist sourcetime targettime)
                       (+ (dist sourcetime curtime)
                          (dist targettime curtime))))
     :occurence targettime)))

(defn project-eternalize [t ref curtime]
  ;projecting/eternalizing a task to ref time
  (let [source-time (:occurence t)
        target-time (:occurence ref)
        get-eternal (fn [a] (if (= a ETERNAL) :eternal :temporal))]
    (case [(get-eternal source-time) (get-eternal target-time)]
      [:eternal  _        ] t
      [:temporal :eternal ] (eternalize t)
      [:temporal :temporal] (let [tEternal (eternalize t)
                                  tProject (project t target-time curtime)]
                              (if (> (:confidence tEternal)
                                     (:confidence tProject))
                                tEternal
                                tProject)))))

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
  ((insta/parser (clojure.java.io/resource "narsese.bnf") :auto-whitespace :standard) "<bird --> swimmer>. %0.10;0.60%"))
