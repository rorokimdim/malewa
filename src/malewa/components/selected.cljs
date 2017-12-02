(ns malewa.components.selected
  (:require [malewa.utils :as u]
            [malewa.dao :refer [get-config SELECTED-VALUE]]))

(defn selected-comp
  "Builds selected component."
  [computations]
  (let [config (get-config)
        svalue @SELECTED-VALUE]
    [:div [:p.selected {:id "selected"} (or svalue "")]]))
