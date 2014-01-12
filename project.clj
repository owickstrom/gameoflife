(defproject gameoflife "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]
                 [prismatic/dommy "0.1.2"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [rm-hull/monet "0.1.9"]]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild {
              :builds {:dev {:source-paths ["src"]
                             :compiler {:output-to "gameoflife-debug.js"
                                        :output-dir "out"
                                        :optimizations :none
                                        :source-map true}}
                       :prod {:source-paths ["src"]
                              :compiler {:output-to "gameoflife.js"
                                         :optimizations :advanced
                                         :pretty-print false}}}})
