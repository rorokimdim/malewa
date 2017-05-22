(ns malewa.dao
  (:require [reagent.core :as r]
            [alandipert.storage-atom :as ls]
            [malewa.utils :as u]))

(def MAX-VALID-TARGET-RETIREMENT-YEAR 50)
(def MAX-ALLOWED-VALUE 1000000000000000)

(defn describe-config-key [key]
  "Gets description for a configuration key."
  (case key
    :birth-year "Birth Year"
    :current-balance "Non-retirement - Current investment balance"
    :investment-per-year "Non-retirement - Investment per year"
    :interest-per-year "Interest on investments per year"
    :target-retirement-after-years "Target retirement after"
    :expenses-per-year-during-retirement "Expenses per year during retirement"
    :long-term-capital-gain-tax "Long term capital gain tax on selling investments"
    :retirement-account-current-balance "Retirement - Current investment balance"
    :retirement-account-investment-per-year "Retirement - Investment per year"
    :retirement-account-tax-at-withdrawal "Retirement - Tax at withdrawal"
    :retirement-account-penalty-tax-at-early-withdrawal "Retirement - Penalty tax at early withdrawal"
    key))

(def APP-STATE
  (ls/local-storage
   (r/atom
    {:config {:birth-year 2000
              :current-balance 100000
              :interest-per-year 0.05
              :investment-per-year (* 12 1000)
              :target-retirement-after-years 20
              :expenses-per-year-during-retirement (* 2000 12)
              :long-term-capital-gain-tax 0.20
              :retirement-account-current-balance 100000
              :retirement-account-investment-per-year (+ (* 18000 2) (* 5500 2))
              :retirement-account-tax-at-withdrawal 0.30
              :retirement-account-penalty-tax-at-early-withdrawal 0.10}})
   :APP-STATE))

(defonce COMPUTATIONS (r/atom []))
(defonce SELECTED-VALUE (r/atom nil)) ;; Atom to indicate any selected value
(defonce VALIDATION (r/atom 0)) ;; Atom to notify that we need to re-validate configuration data

(defonce WINDOW-DIMS
  (r/atom
   {:width (.-innerWidth js/window)}
   {:height (.-innerHeight js/window)}))

(defn get-config []
  "Gets current configuration."
  (:config @APP-STATE))

(defn get-window-width []
  "Gets current window width."
  (:width @WINDOW-DIMS))

(defn get-window-height []
  "Gets current window height."
  (:height @WINDOW-DIMS))

(defn reset-computations! [computations]
  "Resets computations to new value."
  (reset! COMPUTATIONS computations))

(defn reset-validation! [flag]
  (reset! VALIDATION (+ @VALIDATION flag)))

(defn reset-selected-value! [v]
  "Updates SELECTED-VALUE atom to V."
  (reset! SELECTED-VALUE v))

(defn update-window-dims! []
  "Updates window dimensions to current window dimensions."
  (reset! WINDOW-DIMS
          {:width (.-innerWidth js/window)}
          {:height (.-innerHeight js/window)}))

(defn update-config! [key value]
  "Updates configuration for KEY with VALUE."
  (swap! APP-STATE assoc-in [:config key] value))

(defn reset-config! []
  "Resets configuration to default values."
  (ls/clear-local-storage!))

(defn get-validation-error []
  "Gets the first configuration error found."
  (let [config (get-config)
        invalid-value-key (some #(when (js/isNaN (u/float-value-by-id (name %))) %) (keys config))
        too-big-value-key (some #(when (< MAX-ALLOWED-VALUE
                                          (u/abs (u/float-value-by-id (name %)))) %) (keys config))
        inv (:current-balance config)
        yob (:birth-year config)]
    (cond
      invalid-value-key (str (describe-config-key invalid-value-key) " must be a number.")
      too-big-value-key (str (describe-config-key too-big-value-key) " is extremely large.")
      (not (and (> yob 1900) (< yob (u/current-year)))) "Invalid value for Birth Year."
      :else nil)))
