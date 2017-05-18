(ns malewa.dao
  (:require [reagent.core :as r]
            [alandipert.storage-atom :as ls]
            [malewa.utils :as u]))

(def MAX-VALID-TARGET-RETIREMENT-YEAR 100)

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
  (let [yob (:birth-year (get-config))]
    (cond
      (not (and (> yob 1900) (< yob (u/current-year)))) "Invalid Birth Year"
      :else nil)))
