(ns nal.deriver.truth
  (:require [narjure.defaults :refer [horizon] :as d]))

;https://github.com/opennars/opennars/blob/6611ee7f0b1428676b01ae4a382241a77ae3a346/nars_logic/src/main/java/nars/nal/meta/BeliefFunction.java
;https://github.com/opennars/opennars/blob/7b27dacec4cdbe77ca03d89296323d49875ac213/nars_logic/src/main/java/nars/truth/TruthFunctions.java

(defn t-and
  (^double [^double a ^double b] (* a b))
  (^double [^double a ^double b ^double c] (* a b c))
  (^double [^double a ^double b ^double c ^double d] (* a b c d)))

(defn t-or
  (^double [^double a ^double b] (- 1 (* (- 1 a) (- 1 b)))))

(defn w2c ^double [^double w]
  (let [^double h horizon] (/ w (+ w h))))

(defn c2w ^double [^double c]
  (let [^double h horizon] (/ (* h c) (- 1 c))))

;--------------------------------------------
(defn conversion [_ p1]
  (when-let [[f c] p1] [1 (w2c (and f c))]))

(defn negation [[^double f ^double c] _] [(- 1 f) c])

(defn contraposition [[^double f ^double c]  [^double f2 ^double c2]]
  [0 (w2c (and (- 1 f) c))])

(defn revision [[^double f1 ^double c1] [^double f2 ^double c2]]
  (let [w1 (c2w c1)
        w2 (c2w c2)
        w (+ w1 w2)]
    [(/ (+ (* w1 f1) (* w2 f2)) w) (w2c w)]))

(defn deduction [[^double f1 ^double c1] [^double f2 ^double c2]]
  (let [f (t-and f1 f2)]
    [f (t-and f c1 c2)]))

(defn a-deduction [[^double f1 ^double c1] c2] [f1 (t-and f1 c1 c2)])

(defn analogy [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 f2) (t-and c1 c2 f2)])

(defn resemblance [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 f2) (t-and c1 c2 (t-or f1 f2))])

(defn abduction [[^double f1 ^double c1] [^double f2 ^double c2]]
  [f1 (w2c (t-and f2 c1 c2))])

(defn induction [p1 p2] (abduction p2 p1))

(defn exemplification [[^double f1 ^double c1] [^double f2 ^double c2]]
  [1 (w2c (t-and f1 f2 c1 c2))])

(defn comparison [[^double f1 ^double c1] [^double f2 ^double c2]]
  (let [f0 (t-or f1 f2)
        f (if (zero? f0) 0 (/ (t-and f1 f2) f0))
        c (w2c (t-and f0 c1 c2))]
    [f c]))

(defn union [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-or f1 f2) (t-and c1 c2)])

(defn intersection [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 f2) (t-and c1 c2)])

(defn anonymous-analogy [[^double f1 ^double c1] p2] (analogy p2 [f1 (w2c c1)]))

(defn decompose-pnn [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          fn (t-and f1 (- 1 f2))]
      [(- 1 fn) (t-and fn c1 c2)])))

(defn decompose-npp [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          f (t-and (- 1 f1) f2)]
      [f (t-and f c1 c2)])))

(defn decompose-pnp [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          f (t-and f1 (- 1 f2))]
      [f (t-and f c1 c2)])))

(defn decompose-ppp [p1 p2] (decompose-npp (negation p1 p2) p2))

(defn decompose-nnn [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          fn (t-and (- 1 f1) (- 1 f2))]
      [(- 1 fn) (t-and fn c1 c2)])))

(defn difference [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 (- 1 f2)) (t-and c1 c2)])

(defn structual-intersection [_ p2] (deduction p2 [1 d/belief-confidence]))

(defn structual-deduction [p1 _] (deduction p1 [1 d/belief-confidence]))

(defn structual-abduction [p1 _] (abduction p1 [1 d/belief-confidence]))

(defn reduce-conjunction [p1 p2]
  (-> (negation p1 p2)
      (intersection p2)
      (a-deduction 1)
      (negation p2)))

(defn t-identity [p1 _] p1)

(defn d-identity [p1 _] p1)

(defn belief-identity [p1 p2] (when p2 p1))

(defn belief-structural-deduction [_ p2]
  (when p2 (deduction p2 [1 d/belief-confidence])))

(defn belief-structural-difference [_ p2]
  (when p2
    (let [[^double f ^double c] (deduction p2 [1 d/belief-confidence])]
      [(- 1 f) c])))

(defn belief-negation [_ p2] (when p2 (negation p2 nil)))

(defn desire-weak [[f1 c1] [f2 c2]]
  [(t-and f1 f2) (t-and c1 c2 f2 (w2c 1.0))])

(defn desire-induction
  [[f1 c1] [f2 c2]]
  [f1 (w2c (t-and f2 c1 c2))])

(defn desire-structural-strong
  [t _]
  (analogy t [1.0 d/belief-confidence]))

(def tvtypes
  {:t/structural-deduction         structual-deduction
   :t/struct-int                   structual-intersection
   :t/struct-abd                   structual-abduction
   :t/identity                     t-identity
   :t/conversion                   conversion
   :t/contraposition               contraposition
   :t/negation                     negation
   :t/comparison                   comparison
   :t/intersection                 intersection
   :t/union                        union
   :t/difference                   difference
   :t/decompose-ppp                decompose-ppp
   :t/decompose-pnn                decompose-pnn
   :t/decompose-nnn                decompose-nnn
   :t/decompose-npp                decompose-npp
   :t/decompose-pnp                decompose-pnp
   :t/induction                    induction
   :t/abduction                    abduction
   :t/deduction                    deduction
   :t/exemplification              exemplification
   :t/analogy                      analogy
   :t/resemblance                  resemblance
   :t/anonymous-analogy            anonymous-analogy
   :t/belief-identity              belief-identity
   :t/belief-structural-deduction  belief-structural-deduction
   :t/belief-structural-difference belief-structural-difference
   :t/belief-negation              belief-negation
   :t/reduce-conjunction           reduce-conjunction})

(def dvtypes
  {:d/strong            analogy
   :d/deduction         intersection
   :d/weak              desire-weak
   :d/induction         desire-induction
   :d/identity          d-identity
   :d/negation          negation
   :d/structural-strong desire-structural-strong})
