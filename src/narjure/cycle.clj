(ns narjure.cycle
  (:require [narjure.bag :refer :all]
            [narjure.narsese :refer [parse]]))

;TODO create record for memory abstraction, but only after
;api will become more/less stable
(defn memory [buffer concepts]
  {:concepts   concepts
   :cycles-cnt 0
   :tasks-cnt  0
   :buffer     buffer})

(defn default-memory
  ([] (default-memory 100 100))
  ([buffer-capacity concepts-capacity]
   (memory (default-bag buffer-capacity)
           (default-bag concepts-capacity))))

(defn default-concept [term]
  {:key      term
   :priority 1
   :tasks    (default-bag 100)
   :beliefs  (default-bag 100)})

(defn next-cycle! []
  (swap! memory update :cycles-cnt inc))

(defn next-task! []
  (swap! memory update :tasks-cnt inc))

(defn get-concept [concepts term]
  (if-let [[_ concept] (get-el concepts term)]
    concept
    (default-concept term)))

(defn task->concept [task {:keys [concepts] :as m} term]
  (->> task
       (update (get-concept concepts term) :tasks put-el)
       (update m :concepts put-el)))

(defn task->concepts [m {:keys [terms] :as task}]
  (reduce (partial task->concept task) m terms))

(def tasks-to-fetch 1)

(defn buffer->tasks [{:keys [buffer] :as m}]
  (let [[buffer tasks]
        (reduce (fn [[buf tasks] _]
                  (let [[task buf] (take-el buf)]
                    [buf (conj tasks task)])) [buffer []]
                (range tasks-to-fetch))]
    (assoc m :buffer buffer
             :tasks tasks)))

(defn filling-tasks
  "1. Select tasks in the buffer to insert into the corresponding concepts,
  which may include the creation of new concepts and beliefs,
  as well as direct processing on the tasks."
  [{:keys [tasks] :as m}]
  (dissoc (reduce task->concepts m tasks) :tasks))

(defn do-cycle
  "The cycle of NARS."
  [memory]
  (-> memory
      (update :cycles-cnt inc)
      buffer->tasks
      filling-tasks
      #_(do-inference
          save-results)))

;TODO
(defn task-priority [_] 0.8)

(defn pack-task [task cycle n]
  (merge task
         {:key            (hash task)                       ;TODO to be replaced
          :priority       (task-priority task)
          :cycle          cycle
          :evidental-base [n]}))

(defn task->buffer [{:keys [cycles-cnt tasks-cnt] :as m} t]
  (let [n-task (inc tasks-cnt)]
    (assoc (->> (pack-task t cycles-cnt n-task)
                (update m :buffer put-el))
      :tasks-cnt n-task)))

(comment
  (let [task (parse "<a --> b>.")]
    (def t-m (task->buffer (default-memory) task))))


