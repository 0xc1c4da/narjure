(ns gui.gui
  (:require [seesaw.core :refer :all]
            [gui.globals :refer :all]))

(def gui-width 75)
(def gui-height 50)
(def nodes [{:name :button1 :px 0 :py -300 :onclick (fn [state] (println "clicked")) :backcolor [144 155 255]}
            {:name :input :px 300 :py -300 :onclick (fn [state]
                                                      (println (str "input narsese " @input-string))
                                                      (swap! input-string (fn [st] ""))) :backcolor [144 155 255]}])

(def graph-gui [nodes [{:from :button1 :to :button1}] gui-width gui-height])