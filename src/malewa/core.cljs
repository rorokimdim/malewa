(ns malewa.core
  (:require [reagent.dom :as rdom]
            [malewa.dao :refer [update-window-dims!]]
            [malewa.components.root :refer [root-comp]]))

(enable-console-print!)

(defn on-window-resize
  "Updates window dimensions on browser resize."
  [event]
  (update-window-dims!))

(defn ^:export run []
  (rdom/render
   [root-comp]
   (. js/document (getElementById "app")))
  (.addEventListener js/window "resize" on-window-resize))
