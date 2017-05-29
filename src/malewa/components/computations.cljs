(ns malewa.components.computations
  (:require [malewa.utils :as u]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config]]))

(defn computations-comp [computations]
  "Builds computations component."
  (let [config (get-config)]
    [:div
     [:p.small "* At start of year"
      [:br] "** By end of year"]
     [:div.table.computations
      [:div.row.header.blue
       [:div.cell.right "Year"]
       [:div.cell.currency "Investment" [:sup "**"]]
       [:div.cell.currency "Balance (after expenses)" [:sup "*"]]
       [:div.cell.currency "Retirement Expense + tax" [:sup "*"]]
       [:div.cell.currency "Retirement Acc. Investment" [:sup "**"]]
       [:div.cell.currency "Retirement Acc. Balance (pre/post tax)" [:sup "*"]]]
      (doall
       (for [c computations]
         ^{:key (:year c)}
         [:div {:class (if (= (:year c)
                              (f/retirement-account-early-withdrawal-penalty-tax-years config))
                         "row highlighted" "row regular")}
          [:div.cell.right (:year c)]
          [:div.cell.currency (u/format-number-with-commas (js/parseInt (:investment c)))]
          (let [b (js/parseInt (:balance c))
                bf (u/format-number-with-commas b)]
            (if (< b (:expense c))
              [:div.cell.currency.negative bf]
              [:div.cell.currency bf]))
          [:div.cell.currency (u/format-number-with-commas (js/parseInt (:expense c)))]
          [:div.cell.currency (u/format-number-with-commas
                               (js/parseInt (:retirement-account-investment c)))]
          [:div.cell.currency
           (u/format-number-with-commas
            (js/parseInt (:retirement-account-balance-pre-tax c)))
           " / "
           (u/format-number-with-commas
            (js/parseInt (:retirement-account-balance-post-tax c)))]]))]]))
