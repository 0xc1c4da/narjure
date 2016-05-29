(ns gui.lense
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [gui.actors :refer [graph-actors]]
            [gui.gui :refer [graph-gui]]
            [gui.hnav :as hnav]))

(def graphs [[graph-actors] [graph-gui]])

(defn setup []
  (merge hnav/states {:input-string ""}))

(defn update [state] state)

(defn nameof [a]
  (if (string? a) a (name a)))

(defn draw-actor [{:keys [name px py backcolor frontcolor]} node-width node-height]
  (apply q/fill (if (= backcolor nil) [255 255 255] backcolor))
  (q/rect px py node-width node-height)
  (apply q/fill (if (= frontcolor nil) [0 0 0] frontcolor))
  (q/text (nameof name) (+ px 5) (+ py 10)))

(defn draw-graph [[nodes vertices node-width node-height]]
  (doseq [c vertices]
    (let [left (first (filter #(= (:from c) (:name %)) nodes))
          right (first (filter #(= (:to c) (:name %)) nodes))
          pxtransform (fn [x] (+ (:px x) (/ node-width 2.0)))
          pytransform (fn [y] (+ (:py y) (/ node-height 2.0)))]
      (q/line (pxtransform left) (pytransform left)
              (pxtransform right) (pytransform right))
      (doseq [a nodes]
        (draw-actor a node-width node-height)))))

(defn draw [state]
  (q/background 255)
  (q/reset-matrix)
  (hnav/transform state)
  (doseq [[g] graphs]
    (draw-graph g))
  (q/text (:input-string state) 400 -280))

(defn key-pressed [state event]
  (let [name (name (:key event))
        code (:key-code event)]
    (assoc state :input-string (str (if (not (= code 8))
                                      (:input-string state) "")
                                    (if (not (= name "shift"))
                                      (if (not (= code 8))
                                        name "")
                                      "")))))

(q/defsketch example
             :size [hnav/width hnav/height]
             :setup setup
             :draw draw
             :update update
             :mouse-pressed (partial hnav/mouse-pressed graphs)
             :mouse-dragged hnav/mouse-dragged
             :mouse-wheel hnav/mouse-wheel
             :key-pressed key-pressed
             :middleware [m/fun-mode]
             :title "OpenNARS 2.0.0: Lense")