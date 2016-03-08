(ns patham
  (:require [instaparse.core :as insta]
            [clojure.core.contracts :as c])
  (:gen-class))

(def ETERNAL -100000)

;type definitions

;a truth value is a tuple (confidence,frequency) in (0,1)x[0,1]
(defn truth-inbound? [{:keys [confidence frequency]}]
  (and (<  0 confidence 1)
       (<= 0 frequency  1)))

(defn truth? [M] (and (map?  M)
                      (contains? M :confidence)
                      (contains? M :frequency)
                      (truth-inbound? M)))

;a task is a term with a punctuation and occurrence time
(defn task? [t]
  (and (map? t)
       (contains? t :term)
       (contains? t :occurrence)
       (contains? t :punctuation)))

;a task is of type eternal if its occurrence time is eternal
(defn eternal? [t]
  (= (:occurrence t) ETERNAL))

;a task is of type event if its occurrence time is not eternal
(defn event? [t]
  (not= (:occurrence t) ETERNAL))

;truth expectation value in [0,1] calculated based on a truth value t
(defn expectation [t]
  {pre [(truth? t)]}
  {:post [#(<= 0 % 1)]}
    (+ (* (:confidence t) (- (:frequency t) 0.5)) 0.5))

;weight of evidence in [0,1] calculated from a number
(defn w2c [w]
  {:pre  [(number? t)]}
  {:post [#(<= 0 % 1)]}
  (/ w (+ w 1)))

;eternalize a event task to a task of eternal occurrence time
(defn eternalize [t]
  {:pre [(event? t) (task? t)]}
  {:post [eternal?,  task?   ]}
  (assoc t :confidence (w2c (:confidence t))
           :occurrence ETERNAL))

;whether the task has question variables
(defn has-question-var [ref] false)

;the ranking value of the task t in regards to ref
;if it has question variables, the truth expectation matters,
;while when it has
(defn rank-value [ref t]
  {:pre [(task? t)]}
  {:post [number?]}
  (if (has-question-var ref)
    (:confidence t)
    (expectation t)))

;temporally project task to ref task (note: this is only for event tasks!!)"
(defn project [t ref curtime]
  {:pre [(task? t) (task? ref) (number? curtime)]}
  {:post [#(and (task? %) (= (:occurrence %) (:occurrence ref)))]}
  (let [sourcetime (:occurrence t)
        targettime (:occurrence ref)
        dist (fn [a b] (Math/abs (- a b)))]
    (assoc t
     :confidence (* (:confidence t)
                    (- 1 (/ (dist sourcetime targettime)
                            (+ (dist sourcetime curtime)
                               (dist targettime curtime)))))
     :occurrence targettime)))

;temporally projecting/eternalizing a task to ref time
(defn project-eternalize [t ref curtime]
  {:pre [(task? t) (task? ref) (number? curtime)]}
  {:post [task?, #(= (:occurrence %) (:occurrence ref))]}
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
  {:pre [(task? ref) (task? t) (?number curtime)]}
  {:task t
   :value (rank-value ref (project-eternalize t ref curtime))})

;get the best ranked table entry when ranked according to ref
(defn best-ranked [table ref curtime]
  {:pre [(task? ref) (number? curtime)]
   :post [task?]}
  (apply max-key :value
         (map (partial rank-task ref curtime) table)))


;whether the two evidental bases do not overlap
(defn non-overlapping-base? [t1 t2]
  {:pre [(vector? t1) (vector? t2)]}
  true)

;TODO revision
(defn revision [t1 t2]
  {:pre [(task? t1) (task? t2)]}
  t1)

;TODO add to table
(defn add-to-table [concept table x curtime]
  ;1 get best ranked one and revise with
  (if (= (count (concept table)) 0)
    (assoc concept table [x])
    (let [best (best-ranked table {:occurrence curtime} curtime)
          revised (revision x best)]
      nil                                                   ;<- TODO
      ))
  )

;add to belief table
(defn add-to-beliefs [concept x curtime]
  (add-to-table concept :beliefs x curtime))

;add to desires table
(defn add-to-desires [concept x curtime]
  (add-to-table concept :desires x curtime))

;Concept data structure
(defn buildConcept [name]
  {:term name :beliefs [] :desires [] :tasklinks [] :termlinks []})

; (let [concept (buildConcept "test")]
;  (add-to-beliefs concept {:term "tim --> cat" :frequency 1 :confidence 0.75 :occurrence 10} 0))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;((insta/parser (clojure.java.io/resource "narsese.bnf") :auto-whitespace :standard) "<bird --> swimmer>. %0.10;0.60%")
  (do
    (project-eternalize {:term "tim --> cat" :frequency 1 :confidence 0.75 :occurrence 10} {:term "tim --> cat" :frequency 1 :confidence 0.75 :occurrence 10} 100))
    (print "lol")
  )
