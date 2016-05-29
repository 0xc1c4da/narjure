(ns gui.gui
  (:require [seesaw.core :refer :all]
            [gui.globals :refer :all]))

(def gui-width 50)
(def gui-height 25)
(def nodes [{:name :send :px 500 :py -325 :onclick (fn [state]
                                                      (println (str "input narsese " @input-string))
                                                      (swap! input-string (fn [st] ""))) :backcolor [200 200 255]}
            {:name :clear :px 400 :py -325 :onclick (fn [state]
                                                     (swap! input-string (fn [st] ""))) :backcolor [200 200 255]}
            {:name :input :px 450 :py -325 :onclick (fn [state]
                                                      (swap! input-string (fn [st]
                                                                            (str (input "Add Narsese" :to-string :name) "\n"))))
             :backcolor [200 200 255]}])

(def graph-gui [nodes [] gui-width gui-height])