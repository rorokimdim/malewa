(ns malewa.utils
  (:require [cljs-time.core :as t]
            [cljs.pprint :as p]))

(defn current-year
  "Gets current year."
  []
  (t/year (t/now)))

(defn format-number-with-commas
  "Formats number N with commas."
  [n]
  (p/cl-format nil "~:d" n))

(defn format-number-as-abbreviated
  "Formats number as abbreviated string such as 1K, 10M."
  [n]
  (let [thousands (/ n 1000)
        millions (/ thousands 1000)
        billions (/ millions 1000 )
        trillions (/ billions 1000)]
    (cond
      (>= trillions 1) (str trillions "T")
      (>= billions 1) (str billions "B")
      (>= millions 1) (str millions "M")
      (>= thousands 1) (str thousands "K")
      :else (str n))))

(defn format-number-as-pct
  "Formats number representing a percentage."
  [n]
  (p/cl-format nil "~,2f %" (* 100.0 n)))

(defn value-by-id
  "Gets value of a dom element by id."
  [id default]
  (let [e (.getElementById js/document id)]
    (if e (.-value e) default)))

(defn float-value-by-id
  "Gets float value of a dom element by id."
  [id]
  (js/parseFloat (value-by-id id 0)))

(defn filter-positive-values
  "Filters DICTS with positive values for KEYS."
  [dicts keys]
  (loop [ikeys keys
         result dicts]
    (if (seq ikeys)
      (recur (rest ikeys)
             (filter #(pos? ((first ikeys) %)) result))
      result)))

(defn find-max-value
  "Finds max of values of given KEYS in DICTS."
  [keys dicts]
  (loop [ikeys keys
         result nil]
    (if (seq ikeys)
      (recur (rest ikeys)
             (apply max (conj (map (first ikeys) dicts) result)))
      result)))

(defn find-min-value
  "Finds min of values of given KEYS in DICTS."
  [keys dicts]
  (loop [ikeys keys
         result nil]
    (if (seq ikeys)
      (recur (rest ikeys)
             (let [candidates (map (first ikeys) dicts)]
               (if (nil?  result)
                 (apply min candidates)
                 (apply min (conj candidates result)))))
      result)))

(defn positive-or-zero
  "Returns N if it is non-negative, else returns 0."
  [n]
  (if (neg? n) 0 n))

(defn zero-if-nan
  "Returns N if it is a number, else returns 0."
  [n]
  (if (js/isNaN n) 0 n))

(defn abs
  "Returns absolute value of a number."
  [n]
  (if (neg? n) (* -1 n) n))
