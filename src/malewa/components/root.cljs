(ns malewa.components.root
  (:require [malewa.components.config :refer [config-comp]]
            [malewa.components.summary :refer [summary-comp]]
            [malewa.components.computations :refer [computations-comp]]
            [malewa.components.viz :refer [viz-comp]]
            [malewa.finance :as f]
            [malewa.dao :refer [COMPUTATIONS
                                get-config
                                get-validation-error
                                reset-computations!]]))

(defn root-comp []
  "Builds root component."
  (let [config (get-config)]
    [:div
     [config-comp]
     (if-let [e (get-validation-error)]
       [:p.error "Bad configuration: " e]
       (let [computations (reset-computations! (f/compute config))]
         [:div
          [:h3 "Summary"]
          [summary-comp computations]
          [viz-comp COMPUTATIONS]
          [:h3 "Computations"]
          [computations-comp computations]]))]))
