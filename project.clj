(defproject narjure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.logic "0.8.10"]]
  :main ^:skip-aot narjure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
