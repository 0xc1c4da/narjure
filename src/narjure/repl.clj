(ns narjure.repl
  (:require [clojure.string :as s]))

(defn- wrap-code [code]
  (str "(narjure.narsese/parse \"" code "\")"))

(defn- narsese? [code]
  (#{\< \$} (first code)))

(defn narsese-handler [handler]
  (fn [{:keys [code] :as args} & tail]
    (if (narsese? code)
      (apply handler [(update args :code wrap-code)])
      (apply handler (cons args tail)))))
