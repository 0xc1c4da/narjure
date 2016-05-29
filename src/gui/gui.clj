(ns gui.gui)

(def gui-width 75)
(def gui-height 50)
(def nodes [{:name :button1 :px 0 :py -300 :onclick (fn [] (println "clicked")) :color [128 128 128]}])

(def graph-gui [nodes [{:from :button1 :to :button1}] gui-width gui-height])