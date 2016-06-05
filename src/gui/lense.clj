(ns gui.lense
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [gui.actors :refer [graph-actors]]
            [gui.gui :refer [graph-gui inputstr]]
            [gui.hnav :as hnav]
            [seesaw.core :refer :all]
            [gui.globals :refer :all]
            [narjure.core :as nar]
            [narjure.general-inference.concept-selector :as concept-selector]
            [narjure.general-inference.event-selector :as event-selector]
            [narjure.general-inference.general-inferencer :as general-inferencer]
            [narjure.memory-management.concept-manager :as concept-manager]
            [narjure.memory-management.event-buffer :as event-buffer]
            [narjure.memory-management.task-dispatcher :as task-dispatcher]
            [narjure.perception-action.operator-executor :as operator-executor]
            [narjure.perception-action.sentence-parser :as sentence-parser]
            [narjure.perception-action.task-creator :as task-creator]
            [narjure.perception-action.input-load-reducer :as input-load-reducer]
            [narjure.perception-action.derived-load-reducer :as derived-load-reducer]
            [narjure.memory-management.concept :as concepts]
            [narjure.global-atoms :refer :all]
            [narjure.debug-util :refer :all]))

(defn bag-format [st]
  (clojure.string/replace st "}" "}\n"))

;preparing printing concept bag completely meaning printing the individual concepts also completely with all tasks!!!
"(defn concept-bagprint [cbag]
  (let [items (:priority-index (deref bagresolve))]
    (for [z items]
      (assoc z :fullstring ))))"

(def concept-filter (atom ""))
(def event-filter (atom ""))
(defn bagfilter [fil bag]
  (apply vector (filter (fn [x] (.contains (str x) (deref fil))) bag)))

(def debugmessage {:event-selector       [(fn [] (deref event-selector/display)) event-selector/search]
                   :concept-selector     [(fn [] (deref concept-selector/display)) concept-selector/search]
                   :general-inferencer   [(fn [] (deref general-inferencer/display)) general-inferencer/search]
                   :concept-manager      [(fn [] (deref concept-manager/display)) concept-manager/search]
                   :event-buffer         [(fn [] (deref event-buffer/display)) event-buffer/search]
                   :task-dispatcher      [(fn [] (deref task-dispatcher/display)) task-dispatcher/search]
                   :operator-executor    [(fn [] (deref operator-executor/display)) operator-executor/search]
                   :sentence-parser      [(fn [] (deref sentence-parser/display)) sentence-parser/search]
                   :task-creator         [(fn [] (deref task-creator/display)) task-creator/search]
                   :concepts             [(fn [] (deref concepts/display)) concepts/search]
                   :concept-bag          [(fn [] (bag-format (limit-string (str (bagfilter concept-filter (:priority-index (deref c-bag)))) 20000))) concept-filter]
                   :event-bag            [(fn [] (bag-format (limit-string (str (bagfilter event-filter (:priority-index (deref e-bag)))) 20000))) event-filter]
                   :derived-load-reducer [(fn [] (deref derived-load-reducer/display)) derived-load-reducer/search]
                   :input-load-reducer   [(fn [] (deref input-load-reducer/display)) input-load-reducer/search]
                   :input                [(fn [] "") inputstr]
                   :output               [(fn [] (deref output-display)) output-search]})

(def graphs [graph-actors graph-gui])

(defn setup []
  (q/frame-rate 30)
  ;(nar/run)
  (merge hnav/states {}))

(defn update [state] state)

(defn nameof [a]
  (if (string? a) a (name a)))

(defn draw-actor [{:keys [name px py backcolor frontcolor displaysize titlesize]} node-width node-height]
  (apply q/fill (if (= backcolor nil) [255 255 255] backcolor))
  (q/rect px py node-width node-height)
  (apply q/fill (if (= frontcolor nil) [0 0 0] frontcolor))
  (q/text-size (if (= nil titlesize) 10.0 titlesize))
  (q/text (nameof name) (+ px 5) (+ py (if (= nil titlesize) 10.0 titlesize)))
  (q/text-size (if (= displaysize nil) 2.0 displaysize))
  (when (contains? debugmessage name)
    (q/text (clojure.string/replace (str (if (> (count (debugmessage name)) 1)
                                           (if (not= "" (deref (second (debugmessage name))))
                                             (str (deref (second (debugmessage name))) "\n"))
                                           "")
                                         ((first (debugmessage name)))) #"§" "\n")
            (+ px 5) (+ py 20))))

(defn draw-graph [[nodes edges node-width node-height]]
  (doseq [c edges]
    (let [left (first (filter #(= (:from c) (:name %)) nodes))
          right (first (filter #(= (:to c) (:name %)) nodes))
          pxtransform (fn [x] (+ (:px x) (/ node-width 2.0)))
          pytransform (fn [y] (+ (:py y) (/ node-height 2.0)))]
      (q/line (pxtransform left) (pytransform left)
              (pxtransform right) (pytransform right))))
  (doseq [a nodes]
    (draw-actor a node-width node-height)))

(defn draw [state]
  (q/background 255)
  (q/reset-matrix)
  (hnav/transform state)
  (doseq [g graphs]
    (draw-graph g))
  ;concept graph
  (try (let [elems (apply vector (:priority-index (deref c-bag)))
         nodes (for [i (range (count elems))]
                 (let [elem (elems i)
                       ratio (* 18.0 (+ 0.15 (/ i (count elems))))
                       a 15.0]
                   (when (.contains (str (:id elem)) (deref concept-filter))
                     {:name        (str "\n" (narsese-print (:id elem)))
                      :px          (+ 1000 (* a ratio (Math/cos ratio)))
                      :py          (+ -100 (* a ratio (Math/sin ratio)))
                      :displaysize 1.0
                      :titlesize   2.0})))]
     (draw-graph [(filter #(not= % nil) nodes) [] 20 20]))
       (catch Exception e ""))
  )

(defn key-pressed [state event]
  (let [name (name (:key event))
        code (:key-code event)]
    (swap! (deref input-string) (fn [inputstr] (str (if (not (= code 8))
                                                      inputstr "")
                                                    (if (not (= name "shift"))
                                                      (if (not (= code 8))
                                                        name "") ""))))
    state))

(q/defsketch example
             :size [(hnav/width) (hnav/height)]
             :setup setup
             :draw draw
             :update update
             :mouse-pressed (partial hnav/mouse-pressed graphs debugmessage)
             :mouse-dragged hnav/mouse-dragged
             :mouse-wheel hnav/mouse-wheel
             :key-pressed key-pressed
             :middleware [m/fun-mode]
             :features [ :resizable ]
             :title "OpenNARS 2.0.0: Lense")