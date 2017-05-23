(ns malewa.components.computations
  (:require [malewa.utils :as u]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config]]))

(defn computations-comp [computations]
  "Builds computations component."
  (let [config (get-config)]
    [:table.computations
     [:thead>tr
      [:th "Year"]
      [:th.currency "Investment (by end of year)"]
      [:th.currency "Balance (at start of the year)"]
      [:th.currency "Retirement Expense + tax"]
      [:th.currency "Retirement Acc. Investment"]
      [:th.currency "Retirement Acc. Balance (pre/post tax)"]]
     [:tbody
      (doall
       (for [c computations]
         ^{:key (:year c)}
         [:tr {:class (if (= (:year c)
                             (f/retirement-account-early-withdrawal-penalty-tax-years config))
                        "highlighted" "regular")}
          [:td (:year c)]
          [:td.currency (u/format-number-with-commas (js/parseInt (:investment c)))]
          (let [b (js/parseInt (:balance c))
                bf (u/format-number-with-commas b)]
            (if (< b 0)
              [:td.currency.negative bf]
              [:td.currency bf]))
          [:td.currency (u/format-number-with-commas (js/parseInt (:expense c)))]
          [:td.currency (u/format-number-with-commas (js/parseInt (:retirement-account-investment c)))]
          [:td.currency
           (u/format-number-with-commas (js/parseInt (:retirement-account-balance-pre-tax c)))
           " / "
           (u/format-number-with-commas (js/parseInt (:retirement-account-balance-post-tax c)))]]))]]))
