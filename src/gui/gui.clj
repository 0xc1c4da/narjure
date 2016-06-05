(ns gui.gui
  (:require [seesaw.core :refer :all]
            [gui.globals :refer :all]
            [co.paralleluniverse.pulsar.actors :refer [whereis cast!]]))

(def gui-width 50)
(def gui-height 25)
(def inputstr (atom ""))                                    ;py -525 with input load reducer
(def nodes [{:name :send :px 500 :py -325 :onclick (fn [state]
                                                     (println (str "input narsese " (deref inputstr)))
                                                     (cast! (whereis :sentence-parser) [:narsese-string-msg (deref inputstr)])
                                                     (swap! inputstr (fn [st] ""))) :backcolor [255 255 255]}
            {:name :clear :px 400 :py -325 :onclick (fn [state]
                                                     (swap! inputstr (fn [st] ""))) :backcolor [255 255 255]}
            {:name :putin :px 450 :py -325 :onclick (fn [state]
                                                      (swap! inputstr (fn [st]
                                                                            (str (input "Add Narsese" :to-string :name) "\n"))))
             :backcolor [255 255 255]}])

(def graph-gui [nodes [] gui-width gui-height])