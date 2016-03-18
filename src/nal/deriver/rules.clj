(ns nal.deriver.rules
  (:require [clojure.string :as s]
            [clojure.set :refer [map-invert]]
            [nal.deriver
             [key-path :refer [rule-path all-paths path-invariants path]]
             [utils :refer [walk]]
             [list-expansion :refer [contains-list? generate-all-lists]]
             [premises-swapping :refer [allow-swapping? swap]]
             [matching :refer [generate-matching]]
             [backward-rules :refer [allow-backward? expand-backward-rules]]
             [normalization :refer [infix->prefix replace-negation]]
             [terms-permutation :refer [order-for-all-same? generate-all-orders]]]))

(defn options
  "Generates map from rest of the rule's args."
  [args]
  (when (seq args)
    (into {} (map vec (partition 2 args)))))

(defn get-conclusions
  "Parses conclusions from the rule."
  [c opts]
  (if (and (seq? c) (some #{:post} c))
    (map (fn [[c _ post]] {:conclusion c :post post}) (partition 3 c))
    [{:conclusion c :post (:post opts)}]))

(defn rule
  "Generates rule from #R statement."
  [data]
  (let [[p1 p2 _ c & other] (replace-negation data)]
    (let [p1 (infix->prefix p1)
          p2 (infix->prefix p2)
          c (infix->prefix c)
          opts (options other)
          conclusions (get-conclusions c opts)]
      (map (fn [c]
             {:p1          p1
              :p2          p2
              :conclusions [c]
              :full-path   (rule-path p1 p2)
              :pre         (infix->prefix (:pre opts))})
           conclusions))))

(defn check-duplication
  "Checks if there are rules with same premises and preconditions but with
  different conclusions, merges them if they exist."
  [rules]
  (vals (reduce (fn [ac {:keys [p1 p2 pre conclusions] :as r}]
                  (let [k [p1 p2 pre]]
                    (if (ac k)
                      (update-in ac [k :conclusions] concat conclusions)
                      (assoc ac k r))))
                {} rules)))

(defn question?
  "Return true if rule allows only question as task."
  [{:keys [pre]}]
  (some #{:question?} pre))

(defn goal?
  "Return true if rule allows only goal as task."
  [{pre :pre [{post :post} :as concls] :conclusions}]
  (or (some #{:goal?} pre)
      (some (fn [el] (and (keyword? el)
                          (s/starts-with? (str el) ":d/")))
            post)))

(defn judgement?
  "Return true if rule allows only judgement as task."
  [{:keys [pre] :as rule}]
  (not (or (question? rule) (some #{:goal} pre))))

(defn add-possible-paths
  "Selects all rules that will match the same path as current rule and adds
  these rules to the set of rules that matches path.
  For instance:
  current rule's path [[--> [- :any :any] :any] :and [--> [:any :any]]]

  so, if we find rule with path [[--> :any :any] :and [--> [:any :any]]],
  it matches to current's rule path too, hence it should be added to the set
  of rules that matches [[--> [- :any :any] :any] :and [--> [:any :any]]] path."
  [ac [k {:keys [all starts-with]}]]
  (let [rules (mapcat :rules (vals (select-keys ac all)))]
    ac #_(-> ac
        (update-in [k :rules] concat rules)
        (update-in [k :rules] set))))

(defn rule->map
  "Adds rule to map of rules, conjoin rule to set of rules that
  matches to pattern. Rules paths are keys in this map."
  [ac {:keys [p1 p2 full-path] :as rule}]
  (-> ac
      (update-in [full-path :rules] conj rule)
      (assoc-in [full-path :pattern] [p1 p2])
      (assoc-in [full-path :all] (all-paths (path p1) (path p2)))
      (assoc-in [full-path :starts-with] (set (path-invariants p1)))
      (assoc-in [full-path :end-with] (set (path-invariants p2)))))

(defn rules-map
  "Generates map from list of #R satetments, whetre key is path, and value is
  another map with keys pattern ans rules. Pattern is will be used to match
  values from the premises, rules will be used to generate deriver."
  [ruleset task-type]
  (let [rules (reduce rule->map {} ruleset)]
    (generate-matching (reduce add-possible-paths rules rules) task-type)))

;---------------------------------------------------------------------------

(defmacro rules->> [raw-rules & transformations]
  (let [pairs (partition 2 transformations)]
    (reduce (fn [code [pred fun]]
              `(mapcat (fn [rule#]
                         (if (~pred rule#)
                           (~fun rule#)
                           [rule#]))
                       ~code))
            `~raw-rules
            pairs)))

(defmacro defrules [name & rules]
  `(def ~name (quote ~rules)))

(defn compile-rules
  "Define rules. Rules must be #R statements."
  ;TODO exception on duplication of the rule
  [& rules]
  (time
    (let [rules (rules->> (apply concat rules)
                          contains-list? generate-all-lists
                          contains-list? generate-all-lists
                          identity rule
                          order-for-all-same? generate-all-orders
                          allow-swapping? swap
                          allow-backward? expand-backward-rules)
          judgement-rules# (check-duplication (filter judgement? rules))
          question-rules# (check-duplication (filter question? rules))
          goal-rules# (check-duplication (filter goal? rules))]
      (println "Q rules:" (count question-rules#))
      (println "J rules:" (count judgement-rules#))
      (println "G rules:" (count goal-rules#))
      {:judgement (rules-map judgement-rules# :judgement)
       :question  (rules-map question-rules# :question)
       :goal      (rules-map goal-rules# :goal)})))
