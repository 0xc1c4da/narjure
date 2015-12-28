(ns narjure.nal)
  (:require [clojure.core.logic :refer
             [run* project == fresh conde conso run]]))

(defn f-rev [T1 T2 T]
  (fresh [M1 M2 F1 C1 F2 C2 F C]
         (== T1 [F1 C1])
         (== T2 [F2 C2])
         (== T [F C])
         (project [F1 C1 F2 C2]
                  (== true (> 1 C1))
                  (== true (> 1 C2))
                  (== M1 (/ C1 (- 1 C1)))
                  (== M2 (/ C2 (- 1 C2)))
                  (project [M1 M2]
                           (== F (/ (+ (* M1 F1) (* M2 F2))
                                    (+ M1 M2)))
                           (== C (/ (+ M1 M2) (+ M1 M2 1)))))))

(defn revision [R1 R2 R]
  (fresh [S T1 T2 T]
         (== R1 [S T1])
         (== R2 [S T2])
         (== R [S T])
         (f-rev T1 T2 T)))

(run 1 [R] (revision [nil [1, 0.8]] [nil [0, 0.5]] R))
=> ([nil [0.8 0.8333333333333334]])
;revision([inheritance(bird, swimmer), [1, 0.8]], [inheritance(bird, swimmer), [0, 0.5]], R).
;R = [inheritance(bird, swimmer), [0.8, 0.83]]