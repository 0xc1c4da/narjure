(ns gui.unified_gui
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def width 800)
(def height 800)

;HNAV API:
(defn mouse-to-world-coord-x [x zoom difx]
  (* (/ 1.0 zoom) (+ x (- difx) (- (/ width 2.0)))))

(defn mouse-to-world-coord-y [y zoom dify]
  (* (/ 1.0 zoom) (+ y (- dify) (- (/ height 2.0)))))

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

(def node-width 175)
(def node-height 100)

(defn draw-actor [{:keys [name px py]}]
  (apply q/fill [255 255 255])
  (q/rect px py node-width node-height)
  (apply q/fill [53 108 237])
  (q/text name (+ px 50) (+ py 50)))


(def actors [{:name "Concept Manager" :px 0 :py 0}
             {:name "Concepts" :px 0 :py 300}
             {:name "Task Dispatcher" :px 200 :py 0}
             {:name "Sentence Parser" :px 200 :py -300}
             {:name "Task Creator" :px 400 :py -150}
             {:name "Operator Executor" :px -350 :py -150}
             {:name "Event Buffer" :px 200 :py 150}
             {:name "General Inferencer" :px 400 :py 300}
             {:name "Event Selector" :px 600 :py 150}
             {:name "Concept Selector" :px 400 :py 450}])

(def connected [{:from "Concept Manager" :to "Task Dispatcher"}
                {:from "Concepts" :to "Concept Manager"}
                {:from "Task Dispatcher" :to "Event Buffer"}
                {:from "Concepts" :to "General Inferencer"}
                {:from "Concepts" :to "Operator Executor"}
                {:from "Operator Executor" :to "Task Creator"}
                {:from "Event Selector" :to "General Inferencer"}
                {:from "Concept Selector" :to "General Inferencer"}
                {:from "Sentence Parser" :to "Task Creator"}
                {:from "General Inferencer" :to "Task Creator"}])

(defn draw [state]
  (q/reset-matrix)
  (hnav-transform state)
  (q/background 255)
  (doseq [c connected]
    (let [left (first (filter #(= (:from c) (:name %)) actors))
          right (first (filter #(= (:to c) (:name %)) actors))
          pxtransform (fn [x] (+ x (/ node-width 2.0)))
          pytransform (fn [y] (+ y (/ node-height 2.0)))]
      (q/line (pxtransform (:px left)) (pytransform (:py left))
              (pxtransform (:px right)) (pytransform (:py right)))))
  (doseq [a actors]
    (draw-actor a))
  ; (q/ellipse (mouse-to-world-coord-x (:x state) (:zoom state) (:difx state))
  ;           (mouse-to-world-coord-y (:y state) (:zoom state) (:dify state)) (:r state) (:r state))
  )

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