(ns malewa.core
  (:require [reagent.core :as reagent]
            [malewa.dao :refer [update-window-dims!]]
            [malewa.components.root :refer [root-comp]]))

(enable-console-print!)

(defn on-window-resize
  "Updates window dimensions on browser resize."
  [event]
  (update-window-dims!))

(reagent/render-component
 [root-comp]
 (. js/document (getElementById "app"))
 (.addEventListener js/window "resize" on-window-resize))
