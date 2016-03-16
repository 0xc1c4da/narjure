(defproject narjure "0.1.0-SNAPSHOT"
  :description "A Clojure implementation of the Non-Axiomatic Reasoning System proposed by Pei Wang."
  :url "https://github.com/jarradh/narjure"
  :license {:name "GNU General Public License 2.0"
            :url  "http://www.gnu.org/licenses/old-licenses/gpl-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.logic "0.8.10"]
                 [instaparse "1.4.1"]
                 [com.rpl/specter "0.9.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/data.priority-map "0.0.7"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.unify "0.5.5"]]
  :main ^:skip-aot narjure.core
  :plugins [[lein-cloverage "1.0.6"]
            [jonase/eastwood "0.2.3"]
            [lein-kibit "0.1.2"]
            [cider/cider-nrepl "0.11.0-SNAPSHOT"]
            [michaelblume/lein-marginalia "0.9.0"]]
  :eastwood {:exclude-namespaces [nal.rules]}
  :target-path "target/%s"
  :repl-options {:init-ns          narjure.repl
                 :nrepl-middleware [narjure.repl/narsese-handler]}
  :profiles {:uberjar {:aot :all}}
  :marginalia { :javascript ["http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML"]})
