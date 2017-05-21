(ns malewa.components.selected
  (:require [malewa.utils :as u]
            [malewa.dao :refer [get-config SELECTED-VALUE]]))

(defn selected-comp [computations]
  "Builds selected component."
  (let [config (get-config)
        svalue @SELECTED-VALUE]
    [:div
     [:p {:id "selected"
          :style {:text-align "center"
                  :font-size 14
                  :position "absolute"
                  :height "auto"
                  :background "none repeat scroll 0 0 #ffffff"
                  :border "1px solid #6F257F"
                  :display "none"
                  :padding "14px"
                  :font-family "sans-serif"
                  :color "#5D6971"}} (or svalue "")]]))
