(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}

 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.473"]
                 [adzerk/boot-cljs "1.7.228-2"]
                 [pandeiro/boot-http "0.7.6"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"] ;;required in order to make boot-http works
                 [adzerk/boot-reload "0.5.1"]
                 [adzerk/boot-cljs-repl "0.3.3"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [weasel "0.7.0" :scope "test"]

                 [cider/cider-nrepl "0.16.0-SNAPSHOT"]
                 [refactor-nrepl "2.4.0-SNAPSHOT"]

                 [org.clojure/core.async  "0.3.442" :exclusions [org.clojure/tools.reader]]
                 [reagent "0.6.0"]
                 [alandipert/storage-atom "2.0.1"]
                 [cljsjs/d3 "4.3.0-5"]
                 [rid3 "0.1.0-SNAPSHOT"]
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[cider.tasks :refer [add-middleware]])

(task-options! add-middleware {:middleware '[cider.nrepl.middleware.apropos/wrap-apropos
                                             cider.nrepl.middleware.version/wrap-version]})


;; define dev task as composition of subtasks
(deftask dev
  "Launch Immediate Feedback Development Environment"
  []
  (comp
   (serve :dir "target")
   (watch)
   (reload)
   (cljs-repl) ;; before cljs task
   (cljs)
   (target :dir #{"target"})))
