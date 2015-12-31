(ns nal.cut
  (:refer-clojure :exclude [== reduce replace])
  (:require [clojure.core.logic :refer
             [run* project fresh conde conso run membero defne
              nonlvaro == emptyo succeed fail conda s# u# onceo defna] :as l]
            [clojure.core.logic.pldb :refer [db-rel db with-db]]))

;https://en.wikibooks.org/wiki/Prolog/Cuts_and_Negation
;===============================================================================
;(db-rel b p)
;(db-rel c p)
;
;(defn a [X Y]
;  (fresh [] (onceo (b X)) (c Y)))
;
;(with-db
;  (db [b 'k]
;      [b 1]
;      [c 1]
;      [c 2]
;      [c 3])
;  (run 10 [X Y]
;       (a X Y)))
;
; is equal to
;
; a(X, Y) :- b(X), !, c(Y).
; b(1).
; b(2).
; b(3).
;
; c(1).
; c(2).
; c(3).
;
;===============================================================================
;(run* [x y]
;  (conda
;    [(== 2 x) u#]
;    [(== 1 y)]))
;
; is equal to
;
; k(X, _) :- X = 2, !, fail.
; k(_, Y) :- Y = 1.
;
;===============================================================================
;(run* [x y]
;  (conda
;    [(== 2 x)]
;    [(== 1 y)]))
;
; is equal to
;
; k(X, _) :- X = 2, !.
; k(_, Y) :- Y = 1.
