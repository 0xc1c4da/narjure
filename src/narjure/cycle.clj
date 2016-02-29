(ns narjure.cycle
  (:require [narjure.bag :refer :all]
            [narjure.narsese :refer [parse]]
            [clojure.core.logic :as l]
            [nal.core :as c]
            [clojure.set :refer [intersection union]]))

;TODO think about modules

(declare task->buffer)

;TODO create record for memory abstraction, but only after
;api will become more/less stable
(defn memory [buffer concepts]
  {:concepts            concepts
   :cycles-cnt          0
   :tasks-cnt           0
   :buffer              buffer
   :local-inf-results   #{}
   :forward-inf-results #{}
   :answers             []})

(defn default-memory
  ([] (default-memory 100 100))
  ([buffer-capacity concepts-capacity]
   (memory (default-bag buffer-capacity)
           (default-bag concepts-capacity))))

(defn default-concept [term]
  {:key      term
   :priority 1
   :tasks    (default-bag 100)
   :beliefs  (default-bag 100)
   ;here will be the map with patterns for possible questions
   :answers  {}})

(defn get-concept
  "Check for concept in database, creates new in case in didn't find it."
  [concepts term]
  (if-let [concept (get-el concepts term)]
    concept
    (default-concept term)))

(defn overlapping-evidences?
  [belief task]
  (let [belief-ev-base (:evidental-base belief)
        task-ev-base (:evidental-base task)]
    (not-empty (intersection belief-ev-base task-ev-base))))

;TODO should be configurable
(def max-ev-base 100)

(defn total-ev-base
  ;TODO https://github.com/opennars/opennars/wiki/Stamp-In-NARS#evidential-base
  [b1 b2]
  (let [b1-ev-base (:evidental-base b1)
        b2-ev-base (:evidental-base b2)]
    (set (take max-ev-base (union b1-ev-base b2-ev-base)))))

;TODO bad name for function
(defn inf-statement
  [{:keys [statement truth]}]
  [statement truth])

(defn raw-choice [b t]
  (first (l/run* [q] (c/choice b t q))))

(defn choice [belief task]
  (let [b (inf-statement belief)
        t (inf-statement task)
        [statement truth] (raw-choice b t)]
    {:statement      statement
     :key            statement
     :truth          truth
     :evidental-base (total-ev-base belief task)}))

(defn revision [belief task]
  ;TODO selecting the one with lower complexity here
  ;<(&/,<tim --> cat>,<tom --> cat>) =/> <sam --> cat>>.
  ;<<tim --> cat> =/> <sam --> cat>>.
  ;<?how =/> <sam --> cat>>?
  ;
  ;<<tim --> cat> =/> <sam --> cat>>. :12791129: %1.00;0.90%
  ;
  ; because the other ranking params, truth expectation and originality are in
  ; both cases the same, so complexity is the determining factor
  ; in this case
  (let [b (inf-statement belief)
        t (inf-statement task)
        [statement truth] (first (l/run* [q] (c/revision b t q)))]
    {:statement      statement
     :key            statement
     :truth          truth
     :evidental-base (total-ev-base belief task)}))

(defn local-inference
  "Revision/choice"
  [belief task]
  (when belief
    (if (overlapping-evidences? belief task)
      (choice belief task)
      (revision belief task))))

(defn possible-questions
  "Vector of questions that can be answered by the term."
  [[copula term1 term2 :as term]]
  [term [copula term1 '_0] [copula '_0 term2]])

(defn choice-with-nil [b t]
  (if (nil? b)
    t
    (choice b t)))

(defn update-answers
  [concept questions belief]
  (reduce
    (fn [c q]
      (update-in c [:answers q] choice-with-nil belief))
    concept questions))

(defmulti task->concept (fn [& args] (:action (first args))))

(defmethod task->concept :question
  [{:keys [statement] :as task} {:keys [concepts] :as m} term]
  (let [concept (get-concept concepts term)
        answer (get-in concept [:answers statement])
        upd-concept (-> concept
                        (update :tasks put-el task))
        upd-m (update m :concepts put-el upd-concept)]
    (if answer
      (update upd-m :answers conj [task answer])
      (task->buffer upd-m task))))

(defmethod task->concept :default
  [{:keys [statement] :as task} {:keys [concepts] :as m} term]
  (let [{:keys [beliefs] :as concept} (get-concept concepts term)
        belief (get-el beliefs statement)
        result (local-inference belief task)
        task (if result (merge task result) (assoc task :key statement))
        questions (possible-questions statement)
        upd-concept (-> concept
                        (update :beliefs put-el task)
                        (update :tasks put-el task)
                        (update-answers questions task))
        upd-m (update m :concepts put-el upd-concept)]
    (if result
      (update upd-m :local-inf-results conj result)
      upd-m)))

(defn task->concepts
  [m {:keys [terms] :as task}]
  (reduce (partial task->concept task) m terms))

(def tasks-to-fetch 100)

(defn buffer->tasks
  "Fetch portion of tasks from the buffer for processing"
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
  [{:keys [tasks] :as m}]
  (dissoc (reduce task->concepts m tasks) :tasks))

(defn forward-inference [task belief]
  (let [t (inf-statement task)
        b (inf-statement belief)
        conclusions (l/run* [q] (c/inference t b q))
        total-ev-base (total-ev-base belief task)]
    (map (fn [[statement truth]]
           {:statement      statement
            :key            statement
            :truth          truth
            :evidental-base total-ev-base})
         conclusions)))

(defn inference
  "2. Select a concept from the memory, then select a task and a belief
  from the concept.
  3. Feed the task and the belief to the inference engine
  to produce derived tasks."
  [{:keys [concepts] :as m}]
  (let [;select concept
        [{:keys [tasks beliefs] :as concept} concepts] (take-el concepts)
        ;select task
        [{:keys [statement] :as task} tasks] (take-el tasks)
        same-belief (get-el beliefs statement)
        ;select belief
        [belief beliefs] (take-el (remove-el beliefs statement))]
    (if (and task belief)
      ;if both task and belief were found start inference
      ;just return memory otherwise
      (let [new-tasks (forward-inference task belief)
            ;update memory, putting tasks/beliefs/concepts back
            upd-beliefs (-> beliefs
                            (put-el belief)
                            (put-el same-belief))
            upd-concept (assoc concept :beliefs upd-beliefs
                                       :tasks tasks)
            upd-concepts (put-el concepts upd-concept)]
        (->
          ;filling buffer via new tasks and update memory
          (reduce task->buffer m new-tasks)
          (assoc :concepts upd-concepts)
          (update :forward-inf-results union (set new-tasks))))
      (update m :concepts put-el (update concept :priority - 0.4)))))

(defn print-results! [{:keys [local-inf-results
                              forward-inf-results
                              answers] :as m}]
  (when (not-empty local-inf-results)
    (println "Local inference:")
    (doall (map (fn [r] (println (inf-statement r))) local-inf-results)))
  (when (not-empty forward-inf-results)
    (println "Forward inference:")
    (doall (map (fn [r] (println (inf-statement r))) forward-inf-results)))
  (when (not-empty answers)
    (println "Answers:")
    (doall (map (fn [[q a]]
                  (println (:statement q) "? " a))
                answers))))

(defn choose-answers
  [{:keys [answers] :as m}]
  (let [by-question (group-by first answers)]
    (assoc m :answers
             (map (fn [[q ans]]
                    [q (reduce raw-choice (map inf-statement
                                               (map second ans)))])
                  by-question))))

(defn do-cycle
  "The cycle of NARS."
  [memory]
  (-> memory
      (update :cycles-cnt inc)
      buffer->tasks
      filling-tasks
      inference
      choose-answers))

;TODO what is default priority for the tasks that arrived from the inference?
(defn task-priority [_] 0.8)

(defn pack-task
  "Adds some properties to the task to make usable in Bag"
  ;TODO should be moved somewhere
  [task cycle n]
  (merge task
         {;TODO hash to be replaced
          :key            (hash task)
          :priority       (task-priority task)
          :cycle          cycle
          ;TODO data structure for evidental base should be discussed
          :evidental-base #{n}}))

(defn task->buffer
  "Put task into the buffer."
  [{:keys [cycles-cnt tasks-cnt] :as m} t]
  (let [n-task (inc tasks-cnt)]
    (assoc (->> (pack-task t cycles-cnt n-task)
                (update m :buffer put-el))
      :tasks-cnt n-task)))

(defn fill-memory [& expression]
  (reduce #(task->buffer %1 (parse %2)) (default-memory) expression))

(defn do-cycles [m n]
  (reduce (fn [m _] (do-cycle m)) m (range n)))

(defn do-cycles-no-results [n m]
  (do (do-cycles n m) nil))

(comment
  (def m (fill-memory "<sport --> competition>."
                      "<chess --> competition>. %0.90%"))
  (def mq (fill-memory "<bird --> swimmer>."
                       "<bird --> swimmer>?"))
  (def mqq
    (-> (default-memory)
        (task->buffer (parse "<bird --> swimmer>."))
        do-cycle
        (task->buffer (parse "<bird --> swimmer>?"))
        do-cycle)))
