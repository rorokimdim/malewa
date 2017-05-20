(ns malewa.components.config
  (:require [malewa.dao :refer [MAX-VALID-TARGET-RETIREMENT-YEAR
                                get-config reset-config! update-config! reset-validation!]]))

(defn update-config-on-change [key event]
  "Updates state variable for KEY on EVENT."
  (let [value (-> event
                  .-target
                  .-value)
        float-value (js/parseFloat value)]
    (if (not (js/isNaN float-value))
      (update-config! key float-value)
      (reset-validation! 1))))

(defn inc-key! [key maximum event]
  "Increments value of a key by 1."
  (let [c (get-config)
        current-target (key c)]
    (when (< current-target maximum)
      (update-config! key (inc current-target)))))

(defn dec-key! [key minimum event]
  "Decrements value of a key by 1 if value is > minimum."
  (let [c (get-config)
        current-target (key c)]
    (when (> current-target minimum)
      (update-config! key (dec current-target)))))

(defn config-text-input-comp [key]
  "Builds a text input component."
  [:input {:type "text"
           :id key
           :style {:text-align "right"}
           :default-value (key (get-config))
           :on-change (partial update-config-on-change key)}])

(defn config-slider-input-comp [key min max]
  "Builds a slider input component."
  [:input {:type "range"
           :id key
           :value (key (get-config))
           :min min
           :max max
           :on-change (partial update-config-on-change key)}])

(defn config-comp []
  "Builds configuration component."
  (let [config (get-config)
        birth-year (:birth-year config)]
    [:div
     [:h3 "Configuration"
      [:input {:type "button"
               :style {:margin-left "10px"}
               :value "RESET"
               :on-click #(do (reset-config!)
                              (.reload (.-location js/window)))}]]
     [:table.config
      [:tbody
       [:tr [:td.title {:colSpan "2"} "Non-retirement-account Investment"]]
       [:tr
        [:td "Current investment balance"]
        [:td.value [config-text-input-comp :current-balance]]]
       [:tr
        [:td "Investment per year"]
        [:td.value [config-text-input-comp :investment-per-year]]]
       [:tr [:td.title {:colSpan "2"} "Retirement Account Investments (401Ks, Traditional IRAs)"]]
       [:tr
        [:td "Current investment balance"]
        [:td.value [config-text-input-comp :retirement-account-current-balance]]]
       [:tr
        [:td "Investment per year"]
        [:td.value [config-text-input-comp :retirement-account-investment-per-year]]]
       [:tr
        [:td "Tax at withdrawal"]
        [:td.value [config-text-input-comp :retirement-account-tax-at-withdrawal]]]
       [:tr
        [:td "Penalty tax at early withdrawal"]
        [:td.value [config-text-input-comp :retirement-account-penalty-tax-at-early-withdrawal]]]
       [:tr
        [:td "Birth Year (to determine when penalty tax applies)"]
        [:td.value [config-text-input-comp :birth-year]]]
       [:tr [:td.title {:colSpan "2"} "Earnings from investments"]]
       [:tr
        [:td "Interest on investments per year"]
        [:td.value [config-text-input-comp :interest-per-year]]]
       [:tr
        [:td "Long term capital gain tax on selling investments"]
        [:td.value [config-text-input-comp :long-term-capital-gain-tax]]]
       [:tr [:td.title {:colSpan "2"} "Retirement Goals"]]
       [:tr
        [:td "Expenses per year during retirement"]
        [:td.value [config-text-input-comp :expenses-per-year-during-retirement]]]
       [:tr
        [:td
         [:span {:style {:padding-right "15px"}} "Target retirement after"]
         [config-slider-input-comp :target-retirement-after-years 0
          MAX-VALID-TARGET-RETIREMENT-YEAR]]
        [:td.value
         [:input {:type "button" :value "<<"
                  :on-click (partial dec-key! :target-retirement-after-years 0)}]
         [:span {:style {:font-size "30px" :margin "5px"}}
          (:target-retirement-after-years config) " Years"]
         [:input {:type "button" :value ">>"
                  :on-click (partial inc-key! :target-retirement-after-years
                                     MAX-VALID-TARGET-RETIREMENT-YEAR)}]]]]]]))
