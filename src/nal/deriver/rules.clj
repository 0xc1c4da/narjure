(ns nal.deriver.rules
  (:require [clojure.string :as s]
            [nal.deriver.key-path :refer [rule-path all-paths]]
            [clojure.set :refer [map-invert]]
            [nal.deriver.utils :refer [walk]]
            [nal.deriver.list-expansion :refer [check-list]]
            [nal.deriver.premises-swapping :refer [check-swapping]]
            [nal.deriver.matching :refer [generate-matching]]
            [nal.deriver.backward-rules :refer [generate-backward-rules]]
            [nal.deriver.normalization :refer [infix->prefix replace-negation]]
            [nal.deriver.terms-permutation :refer [check-orders]]))

(defn options
  "Generates map from rest of the rule's args."
  [args]
  (when-not (empty? args)
    (into {} (map vec (partition 2 args)))))

(defn get-conclusions
  "Parses conclusions from the rule."
  [c opts]
  (if (and (seq? c) (some #{:post} c))
    (map (fn [[c _ post]] {:conclusion c :post post}) (partition 3 c))
    [{:conclusion c :post (:post opts)}]))

(defn rule [data]
  "Generates rule from #R statement."
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
              :pre         (:pre opts)})
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

(defn add-possible-paths
  "Selects all rules that will match the same path as current rule and adds
  these rules to the set of rules that matches path.
  For instance:
  current rule's path [[--> [- :any :any] :any] :and [--> [:any :any]]]

  so, if we find rule with path [[--> :any :any] :and [--> [:any :any]]],
  it matches to current's rule path too, hence it should be added to the set
  of rules that matches [[--> [- :any :any] :any] :and [--> [:any :any]]] path."
  [ac [k {:keys [all]}]]
  (let [rules (mapcat :rules (vals (select-keys ac all)))]
    (-> ac
        (update-in [k :rules] concat rules)
        (update-in [k :rules] set))))

(defn rule->map
  "Adds rule to map of rules, conjoin rule to set of rules that
  matches to pattern. Rules paths are keys in this map."
  [ac {:keys [p1 p2 full-path] :as rule}]
  (-> ac
      (update-in [full-path :rules] conj rule)
      (assoc-in [full-path :pattern] [p1 p2])
      (assoc-in [full-path :all] (all-paths p1 p2))))

(defn rules-map
  "Generates map from list of #R satetments, whetre key is path, and value is
  another map with keys pattern ans rules. Pattern is will be used to match
  values from the premises, rules will be used to generate deriver."
  [ruleset]
  (let [rules (reduce rule->map {} ruleset)]
    (-> (reduce add-possible-paths rules rules)
        generate-matching)))

;---------------------------------------------------------------------------

(defmacro defrules
  "Define rules. Rules must be #R statements."
  ;TODO exception on duplication of the rule
  [name & rules]
  `(let [rules# (->> (quote ~rules)
                     (mapcat check-list)
                     (mapcat rule)
                     (mapcat check-orders)
                     (mapcat check-swapping)
                     generate-backward-rules
                     check-duplication)
         judgement-rules# (remove question? rules#)
         question-rules# (filter question? rules#)]
     (println "Q rules:" (count question-rules#))
     (println "J rules:" (count judgement-rules#))
     (def ~name {:judgement (rules-map judgement-rules#)
                 :question  (rules-map question-rules#)})))
