(ns malewa.finance
  (:require [malewa.utils :as u]))

(defn expense
  "Computes expense at start of Nth year."
  [config n]
  (let [target (:target-retirement-after-years config)
        capital-gain-tax (:long-term-capital-gain-tax config)
        expense-per-year (:expenses-per-year-during-retirement config)]
    (if (> n target)
      (/ expense-per-year (- 1 capital-gain-tax))
      0)))

(defn investment
  "Computes investment by end of Nth year in non-retirement accounts."
  [config n]
  (let [target (:target-retirement-after-years config)
        inv (:investment-per-year config)]
    (if (> n target) 0 inv)))

(defn retirement-account-investment
  "Computes investment by end of Nth year in retirement accounts."
  [config n]
  (let [target (:target-retirement-after-years config)
        inv (:retirement-account-investment-per-year config)]
    (if (> n target) 0 inv)))

(defn balance
  "Computes balance at start of Nth year."
  [config n f]
  (let [current-balance (:current-balance config)
        i (:interest-per-year config)]
    (if (= n 0)
      current-balance
      (-> (f config (dec n) f)
          (* (+ 1 i))
          (+ (investment config (dec n)))
          (- (expense config n))))))

(defn retirement-account-early-withdrawal-penalty-tax-years
  "Computes years til when early withdrawal penalty tax applies on retirement accounts."
  [config]
  (- 60 (- (u/current-year) (:birth-year config))))

(defn retirement-account-balance
  "Computes pre-tax/post-tax balance in Nth year in retirement accounts."
  [config n f]
  (let [current-balance (:retirement-account-current-balance config)
        i (:interest-per-year config)
        penalty-tax (:retirement-account-penalty-tax-at-early-withdrawal config)
        penalty-tax-until-years (retirement-account-early-withdrawal-penalty-tax-years config)
        income-tax (:retirement-account-tax-at-withdrawal config)
        tax (if (< n penalty-tax-until-years) (+ income-tax penalty-tax) income-tax)]
    (if (= n 0)
      [current-balance
       (* (- 1 tax) current-balance)]
      (let [pre-tax (-> (f config (dec n) f)
                        first
                        (* (+ 1 i))
                        (+ (retirement-account-investment config (dec n))))
            post-tax (* (- 1 tax) pre-tax)]
        [pre-tax post-tax]))))

(defn compute
  "Performs computations for N years."
  [config n]
  (let [memoized-balance (memoize balance)
        memoized-retirement-account-balance (memoize retirement-account-balance)
        birth-year (:birth-year config)]
    (for [y (range (inc n))]
      (let [rbalance (memoized-retirement-account-balance
                      config y
                      memoized-retirement-account-balance)
            [retirement-account-balance-pre-tax
             retirement-account-balance-post-tax] rbalance]
        {:year y
         :investment (investment config y)
         :balance (memoized-balance config y memoized-balance)
         :expense (expense config y)
         :retirement-account-investment (retirement-account-investment config y)
         :retirement-account-balance-pre-tax retirement-account-balance-pre-tax
         :retirement-account-balance-post-tax retirement-account-balance-post-tax}))))


(defn get-broke-year-computation
  "Gets computation record for year when there is insufficient funds
   to cover expenses."
  [config computations]
  (let [target-year (:target-retirement-after-years config)
        expenses-per-year (:expenses-per-year-during-retirement config)]
    (some #(when (and
                  (<= target-year (:year %))
                  (>= expenses-per-year (:balance %))) %) computations)))
