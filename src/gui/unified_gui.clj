(ns gui.unified_gui
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def width 800)
(def height 800)

;HNAV API:
(defn mouse-to-world-coord-x [x zoom difx]
  (* (/ 1.0 zoom) (- (- x difx) (/ width 2.0))))

(defn mouse-to-world-coord-y [y zoom dify]
  (* (/ 1.0 zoom) (- (- y dify) (/ height 2.0))))

(defn hnav-transform [{:keys [difx dify zoom]}]
  (q/translate (+ difx (* 0.5 width))
               (+ dify (* 0.5 height)))
  (q/scale zoom zoom))
;END HNAV

(defn setup []
  ; initial state
  {:x      0.0 :y 0.0 :r 10
   :difx   (- (/ width 2))
   :dify   (- (/ height 2))
   :savepx 0.0
   :savepy 0.0
   :md     false
   :zoom   1.0})

(defn update [state]
  state)

(defn draw-actor [state {:keys [name px py]}]
  (apply q/fill [0 0 0 0])
  (q/rect px py 200 100)
  (apply q/fill [53 108 237])
  (q/text name (+ px 50) (+ py 50)))

(def actors [{:name "Concept Manager" :px 0 :py 0}
             {:name "Task Dispatcher" :px 350 :py 50}])

(defn draw [state]
  (q/reset-matrix)
  (hnav-transform state)
  (q/background 255)
  (doseq [a actors]
    (draw-actor state a))
  (q/ellipse (mouse-to-world-coord-x (:x state) (:zoom state) (:difx state))
             (mouse-to-world-coord-y (:y state) (:zoom state) (:dify state)) (:r state) (:r state)))

;HNAV implementation
(defn mouse-pressed [state event]
  (assoc state :savepx (:x event) :savepy (:y event) :md true))

(defn mouse-dragged [state event]
  (-> state
        (assoc :difx (+ (:difx state) (- (:x event) (:savepx state)))
               :dify (+ (:dify state) (- (:y event) (:savepy state)))
               :savepx (:x event)
               :savepy (:y event))))

(def scrollcamspeed 1.1)
(defn mouse-wheel [state mouse-scroll]
  (let [zoom-before (:zoom state)
        state2 (if (> mouse-scroll 0)
                 (assoc state :zoom (/ (:zoom state) scrollcamspeed))
                 (assoc state :zoom (* (:zoom state) scrollcamspeed)))]
    (-> state2
        (assoc :difx (* (:difx state2) (/ (:zoom state2) zoom-before)))
        (assoc :dify (* (:dify state2) (/ (:zoom state2) zoom-before))))))
;END HNAV

(q/defsketch example
             :size [width height]
             :setup setup
             :draw draw
             :update update
             :mouse-pressed mouse-pressed
             :mouse-dragged mouse-dragged
             :mouse-wheel mouse-wheel
             :middleware [m/fun-mode])