(ns gui.unified_gui
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def min-r 10)
(def width 800)
(def height 800)

;;Hnav:
(defn MouseToWorldCoordX [x zoom difx]
  (* (/ 1.0 zoom) (- (- x difx) (/ width 2.0))))
(defn MouseToWorldCoordY [y zoom dify]
  (* (/ 1.0 zoom) (- (- y dify) (/ height 2.0))))





(defn setup []
  ; initial state
  {:x 0 :y 0 :r min-r})

(defn update [state]
  ; increase radius of the circle by 1 on each frame
  (update-in state [:r] inc))

(def scale 0.5)

(defn draw-actor [text px py]
  (q/reset-matrix)
  (q/scale scale)
  (apply q/fill [0 0 0 0])
  (q/rect px py 200 100)
  (apply q/fill [53 108 237])
  (q/text text (+ px 50) (+ py 50)))

(defn draw [state]
  (q/background 255)
  (draw-actor "Concept Manager" 50 50)
  (draw-actor "Task Dispatcher" 250 50)
  (q/ellipse (:x state) (:y state) (:r state) (:r state)))

; decrease radius by 1 but keeping it not less than min-r
(defn shrink [r]
  (max min-r (dec r)))

(defn mouse-moved [state event]
  (-> state
      ; set circle position to mouse position
      (assoc :x (/ (:x event) scale) :y (/ (:y event) scale))
      ; decrease radius
      (update-in [:r] shrink)))

(q/defsketch example
             :size [width height]
             :setup setup
             :draw draw
             :update update
             :mouse-moved mouse-moved
             :middleware [m/fun-mode])