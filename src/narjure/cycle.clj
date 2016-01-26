(ns narjure.cycle
  (:require [narjure.bag :refer :all]
            [narjure.narsese :refer [parse]]))

(declare task->buffer)
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

(defn get-concept [concepts term]
  (if-let [concept (get-el concepts term)]
    concept
    (default-concept term)))

(defn local-revision
  [concept {:keys [statement] :as task}]
  #_(if-let [old-beleife ()]
    (if (overlaps?))))

;TODO local inefernce should be somewhere here
(defn task->concept
  [task {:keys [concepts] :as m} term]
  (->> task
       (update (get-concept concepts term) :tasks put-el)
       (update m :concepts put-el)))

(defn task->concepts
  [m {:keys [terms] :as task}]
  (reduce (partial task->concept task) m terms))

(def tasks-to-fetch 1)

(defn buffer->tasks
  [{:keys [buffer] :as m}]
  (let [[buffer tasks]
        (reduce (fn [[buf tasks] _]
                  (let [[task buf] (take-el buf)]
                    [buf (conj tasks task)])) [buffer []]
                (range tasks-to-fetch))]
    (assoc m :buffer buffer
             :tasks tasks)))

(defn filling-tasks
  "1. Select tasks in the buffer to insert into the corresponding concepts,
  which may include the creation of new concepts (I'm not sure about the rest)
  and beliefs, as well as direct processing on the tasks."
  ;TODO Should revision/choice be implemented here?
  [{:keys [tasks] :as m}]
  (dissoc (reduce task->concepts m tasks) :tasks))

(defn do-inference [task beleife]
  [])

(defn inference
  "2. Select a concept from the memory, then select a task and a belief
  from the concept.
  3. Feed the task and the belief to the inference engine
  to produce derived tasks."
  [{:keys [concepts] :as m}]
  (let [[{:keys [tasks beliefs] :as concept} concepts] (take-el concepts)
        [task tasks] (take-el tasks)
        [beleife beleifs] (take-el beliefs)]
    (if (and task beleife)
      (let [new-tasks (do-inference task beleife)
            upd-beleifs (put-el beleifs beleife)
            uod-concept (assoc concept :beliefs upd-beleifs)
            upd-concepts (put-el concepts uod-concept)
            upd-m (assoc m :tasks tasks)]
        (-> (reduce task->buffer upd-m new-tasks)
            (assoc :concepts upd-concepts)))
      m)))

(defn do-cycle
  "The cycle of NARS."
  [memory]
  (-> memory
      (update :cycles-cnt inc)
      buffer->tasks
      filling-tasks
      inference
      ;save-results
      ))

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
    (def t-m (task->buffer (default-memory) task)))
  (do-cycle t-m))


