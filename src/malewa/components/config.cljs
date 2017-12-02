(ns malewa.components.config
  (:require [malewa.dao :refer [MAX-VALID-TARGET-RETIREMENT-YEAR
                                get-config reset-config! update-config! reset-validation!]]))

(defn update-config-on-change
  "Updates state variable for KEY on EVENT."
  [key event]
  (let [value (-> event
                  .-target
                  .-value)
        float-value (js/parseFloat value)]
    (if (not (js/isNaN float-value))
      (update-config! key float-value)
      (reset-validation! 1))))

(defn inc-key!
  "Increments value of a key by 1."
  [key maximum event]
  (let [c (get-config)
        current-target (key c)]
    (when (< current-target maximum)
      (update-config! key (inc current-target)))))

(defn dec-key!
  "Decrements value of a key by 1 if value is > minimum."
  [key minimum event]
  (let [c (get-config)
        current-target (key c)]
    (when (> current-target minimum)
      (update-config! key (dec current-target)))))

(defn config-text-input-comp
  "Builds a text input component."
  [key]
  [:input {:type "text"
           :id key
           :style {:text-align "right"}
           :default-value (key (get-config))
           :on-change (partial update-config-on-change key)}])

(defn config-slider-input-comp
  "Builds a slider input component."
  [key min max]
  [:input {:type "range"
           :id key
           :value (key (get-config))
           :min min
           :max max
           :on-change (partial update-config-on-change key)}])

(defn config-comp
  "Builds configuration component."
  []
  (let [config (get-config)
        birth-year (:birth-year config)]
    [:div
     [:h3 "Configuration"
      [:input {:type "button"
               :style {:margin-left "10px"}
               :value "RESET"
               :on-click #(do (reset-config!)
                              (.reload (.-location js/window)))}]]
     [:div {:class "table config"}
      [:div.row.header.blue
       [:div.cell.title "Non-retirement-account Investment"]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Current investment balance"]
       [:div.cell.value [config-text-input-comp :current-balance]]]
      [:div.row
       [:div.cell "Investment per year"]
       [:div.cell.value [config-text-input-comp :investment-per-year]]]
      [:div {:class "row header blue"}
       [:div.cell.title "Retirement Account Investments" [:span.small " (401Ks, Traditional IRAs)"]]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Current investment balance"]
       [:div.cell.value [config-text-input-comp :retirement-account-current-balance]]]
      [:div.row
       [:div.cell "Investment per year"]
       [:div.cell.value [config-text-input-comp :retirement-account-investment-per-year]]]
      [:div.row
       [:div.cell "Tax at withdrawal"]
       [:div.cell.value [config-text-input-comp :retirement-account-tax-at-withdrawal]]]
      [:div.row
       [:div.cell "Penalty tax at early withdrawal"]
       [:div.cell.value [config-text-input-comp :retirement-account-penalty-tax-at-early-withdrawal]]]
      [:div.row
       [:div.cell "Birth Year (determines when penalty tax applies)"]
       [:div.cell.value [config-text-input-comp :birth-year]]]
      [:div {:class "row header blue"}
       [:div.cell.title "Earnings from investments"]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Interest on investments per year"]
       [:div.cell.value [config-text-input-comp :interest-per-year]]]
      [:div.row
       [:div.cell "Long term capital gain tax on selling investments"]
       [:div.cell.value [config-text-input-comp :long-term-capital-gain-tax]]]
      [:div {:class "row header blue"}
       [:div.cell.title "Retirement Goals"]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Expenses per year during retirement"]
       [:div.cell.value [config-text-input-comp :expenses-per-year-during-retirement]]]
      [:div.row
       [:div.cell
        [:span {:style {:padding-right "15px"}} "Target retirement after"]
        [config-slider-input-comp :target-retirement-after-years 0
         (dec MAX-VALID-TARGET-RETIREMENT-YEAR)]]
       [:div.cell.value
        [:input {:type "button" :value "←"
                 :on-click (partial dec-key! :target-retirement-after-years 0)}]
        [:span {:style {:font-size 14 :display "inline-block" :margin "5px"}}
         (:target-retirement-after-years config) " Years"]
        [:input {:type "button" :value "→"
                 :on-click (partial inc-key! :target-retirement-after-years
                                    (dec MAX-VALID-TARGET-RETIREMENT-YEAR))}]]]]]))
