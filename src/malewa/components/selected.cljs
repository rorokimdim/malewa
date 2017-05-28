(ns malewa.components.selected
  (:require [malewa.utils :as u]
            [malewa.dao :refer [get-config SELECTED-VALUE]]))

(defn selected-comp [computations]
  "Builds selected component."
  (let [config (get-config)
        svalue @SELECTED-VALUE]
    [:div [:p.selected {:id "selected"} (or svalue "")]]))
