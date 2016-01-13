(defproject narjure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.logic "0.8.10"]
                 [instaparse "1.4.1"]
                 [com.rpl/specter "0.9.1"]]
  :main ^:skip-aot narjure.core
  :plugins [[lein-cloverage "1.0.6"]]
  :target-path "target/%s"
  :repl-options {:init-ns narjure.narsese
                 :nrepl-middleware [narjure.repl/narsese-handler]}
  :profiles {:uberjar {:aot :all}})
