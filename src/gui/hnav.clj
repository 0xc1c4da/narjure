(ns gui.hnav
  (:require [quil.core :as q]))

(def init-size 800)
(defn width []
  (try (if (or (= nil q/width) (= nil (q/width)))
         init-size
         (q/width))
       (catch Exception e init-size)))

(defn height []
  (try (if (or (= nil q/height) (= nil (q/height)))
         init-size
         (q/height))
       (catch Exception e init-size)))

(defn mouse-to-world-coord-x [x zoom difx]
  (* (/ 1.0 zoom) (+ x (- difx) (- (/ (width) 2.0)))))

(defn mouse-to-world-coord-y [y zoom dify]
  (* (/ 1.0 zoom) (+ y (- dify) (- (/ (height) 2.0)))))

(defn transform [{:keys [difx dify zoom]}]
  (q/translate (+ difx (* 0.5 (width)))
               (+ dify (* 0.5 (height))))
  (q/scale zoom zoom))

;HNAV implementation
(defn mouse-pressed [graphs state event]                           ;also HGUI click check here
  (doseq [g graphs]
    (doseq [[V E w h] g]
      (doseq [v V]
        (let [px (:px v)
              py (:py v)
              mousex (mouse-to-world-coord-x (:x event) (:zoom state) (:difx state))
              mousey (mouse-to-world-coord-y (:y event) (:zoom state) (:dify state))]
          (if (and (not (= (:onclick v) nil)) (> mousex px) (> mousey py)
                   (< mousex (+ px w)) (< mousey (+ py h)))
            ((:onclick v)))))))
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

(def states {:difx   (- (/ (width) 2))
             :dify   (- (/ (height) 2))
             :savepx 0.0
             :savepy 0.0
             :zoom   1.0})
