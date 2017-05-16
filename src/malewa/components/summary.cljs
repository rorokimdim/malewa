(ns malewa.components.summary
  (:require [malewa.utils :as u]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config]]))

(defn summary-comp [computations]
  "Builds summary component."
  (let [config (get-config)
        expenses-per-year (:expenses-per-year-during-retirement config)
        broke-year-c (some #(when (>= expenses-per-year (:balance %)) %) computations)
        before-broke-year (max (if broke-year-c (dec (:year broke-year-c)) 0) 0)
        before-broke-year-c (nth computations before-broke-year)]
    [:div
     [:p "You plan to retire in " (:target-retirement-after-years config) " years."
      " You expect your expenses to be less than "
      (u/format-with-commas (js/parseInt (:expenses-per-year-during-retirement config)))
      " per year."
      ]
     [:p "You currently have "
      (u/format-with-commas (js/parseInt (:current-balance config)))
      " invested in non-retirement accounts, and "
      (u/format-with-commas (js/parseInt (:retirement-account-current-balance config)))
      " in retirement accounts, both earning about "
      (u/format-as-pct (:interest-per-year config))
      " interest per year. If you cash out your retirement accounts before "
      (f/retirement-account-early-withdrawal-penalty-tax-years config)
      " years, you will incur a 10 % penalty tax (in addition to income-tax on"
      " any non-roth retirement accounts)."]
     (if broke-year-c
       [:p "You will have enough money till year "
        (dec (:year broke-year-c))
        ", when you will be " (+ (:year broke-year-c) (- (u/current-year) (:birth-year config)))
        " years old. After that you will have to cash our your retirement account balance ("
        (let [[pre-tax post-tax] (:retirement-account-balance before-broke-year-c)]
          (str "pre/post tax: " (u/format-with-commas (js/parseInt pre-tax))
               " / " (u/format-with-commas (js/parseInt post-tax))))
        ")."
        ]
       [:p "Congratulations! You will have enough money to last forever!"
        " You should retire earlier or live more lavishly during retirement!"])
     [:p
      "The following chart shows your balance in non-retirement accounts over years; starting with 0, the current year."
      " Green bars indicate a positive balance. Red bars indicate a negative balance."
      " The bar highlighted in black indicates the year you plan to retire."
      " The bar highlighted in blue indicates the year you can withdraw from your retirement accounts without penalty."]
     ]))
