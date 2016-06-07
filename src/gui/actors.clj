(ns gui.actors
  (:require [seesaw.core :refer :all]))

(def actor-level-width 175)
(def actor-level-height 100)

(def nodes [{:name :concept-manager :px 0 :py 0}
            {:name :concepts :px 0 :py 300}
            {:name :task-dispatcher :px 200 :py 0}
            {:name :input :px 400 :py -400 :displaysize 10.0} ;-600
            {:name :sentence-parser :px 400 :py -300}       ;-500
            {:name :task-creator :px 400 :py -150}
            ;{:name :input-load-reducer :px 400 :py -325}
            {:name :operator-executor :px -350 :py -150}
            {:name :event-buffer :px 200 :py 150}
            {:name :general-inferencer :px 400 :py 300}
            {:name :derived-load-reducer :px 400 :py 150}
            {:name :event-selector :px 600 :py 150}
            {:name :event-bag :px 775 :py 150}
            {:name :concept-selector :px 600 :py 450}
            {:name :concept-bag :px 775 :py 450}
            {:name :output :px 600 :py -400}])

(def vertices [{:from :concept-manager :to :task-dispatcher}
               {:from :concepts :to :concept-manager}
               {:from :task-dispatcher :to :event-buffer :unidirectional true}
               {:from :concepts :to :general-inferencer :unidirectional true}
               {:from :concepts :to :operator-executor :unidirectional true}
               {:from :operator-executor :to :task-creator :unidirectional true}
               {:from :event-selector :to :general-inferencer :unidirectional true}
               {:from :concept-selector :to :general-inferencer :unidirectional true}
               {:from :sentence-parser :to :task-creator :unidirectional true}
               ;{:from :sentence-parser :to :input-load-reducer}
               ;{:from :input-load-reducer :to :task-creator}
               {:from :general-inferencer :to :derived-load-reducer :unidirectional true}
               {:from :derived-load-reducer :to :task-creator :unidirectional true}
               {:from :task-creator :to :task-dispatcher :unidirectional true}
               {:from :concept-selector :to :concepts} :unidirectional true])

(def graph-actors [nodes vertices actor-level-width actor-level-height])