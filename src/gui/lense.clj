(ns gui.lense
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [gui.actors :refer [graph-actors]]
            [gui.gui :refer [graph-gui]]
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
            [narjure.memory-management.concept :as concepts]
            [narjure.memory-management.concept-manager :refer [c-bag]]
            [narjure.memory-management.event-buffer :refer [e-bag]]))

(defn bag-format [st]
  (clojure.string/replace st "}" "}\n"))

(def debugmessage {:concept-selector   (fn [] (deref concept-selector/display))
                   :event-selector     (fn [] (deref event-selector/display))
                   :general-inferencer (fn [] (deref general-inferencer/display))
                   :concept-manager    (fn [] (deref concept-manager/display))
                   :event-buffer       (fn [] (deref event-buffer/display))
                   :task-dispatcher    (fn [] (deref task-dispatcher/display))
                   :operator-executor  (fn [] (deref operator-executor/display))
                   :sentence-parser    (fn [] (deref sentence-parser/display))
                   :task-creator       (fn [] (deref task-creator/display))
                   :concepts           (fn [] (deref concepts/display))
                   :concept-bag        (fn [] (bag-format (str (:priority-index (deref c-bag)))))
                   :event-bag          (fn [] (bag-format (str (:priority-index (deref e-bag)))))
                   :input              (fn [] (deref input-string))})

(def graphs [[graph-actors] [graph-gui]])

(defn setup []
  (q/frame-rate 30)
  ;(nar/run)
  (merge hnav/states {}))

(defn update [state] state)

(defn nameof [a]
  (if (string? a) a (name a)))

(defn draw-actor [{:keys [name px py backcolor frontcolor displaysize]} node-width node-height]
  (apply q/fill (if (= backcolor nil) [255 255 255] backcolor))
  (q/rect px py node-width node-height)
  (apply q/fill (if (= frontcolor nil) [0 0 0] frontcolor))
  (q/text-size 10.0)
  (q/text (nameof name) (+ px 5) (+ py 10))
  (q/text-size (if (= displaysize nil) 1.0 displaysize))
  (when (contains? debugmessage name)
    (q/text (clojure.string/replace (str ((debugmessage name))) #"§" "\n")
            (+ px 5) (+ py 20))))

(defn draw-graph [[nodes vertices node-width node-height]]
  (doseq [c vertices]
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
  (doseq [[g] graphs]
    (draw-graph g)))

(defn key-pressed [state event]
  (let [name (name (:key event))
        code (:key-code event)]
    (swap! input-string (fn [inputstr] (str (if (not (= code 8))
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
             :mouse-pressed (partial hnav/mouse-pressed graphs)
             :mouse-dragged hnav/mouse-dragged
             :mouse-wheel hnav/mouse-wheel
             :key-pressed key-pressed
             :middleware [m/fun-mode]
             :features [ :resizable ]
             :title "OpenNARS 2.0.0: Lense")