(ns gui.actors)

(def actor-level-width 175)
(def actor-level-height 100)

(def nodes [{:name :concept-manager :px 0 :py 0}
            {:name :concepts :px 0 :py 300}
            {:name :task-dispatcher :px 200 :py 0}
            {:name :sentence-parser :px 400 :py -300}
            {:name :task-creator :px 400 :py -150}
            {:name :operator-executor :px -350 :py -150}
            {:name :event-buffer :px 200 :py 150}
            {:name :general-inferencer :px 400 :py 300}
            {:name :event-selector :px 600 :py 150}
            {:name :concept-selector :px 600 :py 450}])

(def vertices [{:from :concept-manager :to :task-dispatcher}
               {:from :concepts :to :concept-manager}
               {:from :task-dispatcher :to :event-buffer}
               {:from :concepts :to :general-inferencer}
               {:from :concepts :to :operator-executor}
               {:from :operator-executor :to :task-creator}
               {:from :event-selector :to :general-inferencer}
               {:from :concept-selector :to :general-inferencer}
               {:from :sentence-parser :to :task-creator}
               {:from :general-inferencer :to :task-creator}
               {:from :task-creator :to :task-dispatcher}])

(def graph-actors [nodes vertices actor-level-width actor-level-height])