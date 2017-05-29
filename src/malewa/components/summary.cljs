(ns malewa.components.summary
  (:require [malewa.utils :as u]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config]]))

(defn summary-comp [computations]
  "Builds summary component."
  (let [config (get-config)
        target-year (inc (:target-retirement-after-years config))
        balance-at-retirement (:balance (nth computations target-year))
        expenses-per-year (:expenses-per-year-during-retirement config)
        broke-year-c (f/get-broke-year-computation config computations)]
    [:div
     [:p "You plan to retire after " (dec target-year) " years."
      " You expect your expenses to be less than "
      (u/format-number-with-commas (js/parseInt (:expenses-per-year-during-retirement config)))
      " per year."]
     [:p "You currently have "
      (u/format-number-with-commas (js/parseInt (:current-balance config)))
      " invested in non-retirement accounts, and "
      (u/format-number-with-commas (js/parseInt (:retirement-account-current-balance config)))
      " in retirement accounts, both earning about "
      (u/format-number-as-pct (:interest-per-year config))
      " interest per year. If you cash out your retirement accounts before "
      (f/retirement-account-early-withdrawal-penalty-tax-years config)
      " years, you will incur a 10 % penalty tax (in addition to income-tax on"
      " any non-roth retirement accounts)."]
     (if broke-year-c
       [:p "At the time you retire, you will have "
        (u/format-number-with-commas (js/parseInt balance-at-retirement))
        ". That will last you till year "
        (dec (:year broke-year-c))
        ", when you will be " (+ (:year broke-year-c) (- (u/current-year) (:birth-year config)))
        " years old. After that you will have to cash our your retirement account balance ("
        (let [pre-tax (:retirement-account-balance-pre-tax broke-year-c)
              post-tax (:retirement-account-balance-post-tax broke-year-c)]
          (str "pre/post tax: " (u/format-number-with-commas (js/parseInt pre-tax))
               " / " (u/format-number-with-commas (js/parseInt post-tax))))
        ")."
        ]
       [:p "Congratulations! You will have enough money to last forever!"
        " You should retire earlier or live more lavishly during retirement!"])
     [:p
      "The first chart below shows your balance in non-retirement accounts over years; starting with 0, the current year."
      " Similarly, the second chart shows your balance in retirement accounts; blue is pre-tax balance, orange is post-tax balance."
      " Years without any bars imply that you have run out of money."
      " The bar highlighted in black indicates the year you plan to retire."
      " The bar highlighted in blue indicates the year you can withdraw from your retirement accounts without penalty."
      " The bar highlighted in red indicates the year when you have insufficient funds and will need to cash your retirement accounts."]
     ]))
