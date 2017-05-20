(ns malewa.components.root
  (:require [malewa.components.config :refer [config-comp]]
            [malewa.components.summary :refer [summary-comp]]
            [malewa.components.computations :refer [computations-comp]]
            [malewa.components.viz :refer [viz-comp]]
            [malewa.finance :as f]
            [malewa.dao :refer [COMPUTATIONS
                                MAX-VALID-TARGET-RETIREMENT-YEAR
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
       (let [computations (reset-computations! (f/compute config MAX-VALID-TARGET-RETIREMENT-YEAR))]
         [:div
          [:h3 "Summary"]
          [summary-comp computations]
          [viz-comp [:balance] "Balance in non-retirement accounts over years" COMPUTATIONS]
          [viz-comp [:retirement-account-balance-pre-tax :retirement-account-balance-post-tax]
           "Balance in retirement accounts over years" COMPUTATIONS]
          [:h3 "Computations"]
          [computations-comp computations]]))]))
