(ns malewa.utils
  (:require [cljs-time.core :as t]
            [cljs.pprint :as p]))

(defn current-year []
  "Gets current year."
  (t/year (t/now)))

(defn format-with-commas [n]
  "Formats number N with commas."
  (p/cl-format nil "~:d" n))

(defn format-as-pct [n]
  "Formats number representing a percentage."
  (p/cl-format nil "~,2f %" (* 100.0 n)))

(defn value-by-id [id default]
  "Gets value of a dom element by id."
  (let [e (.getElementById js/document id)]
    (if e (.-value e) default)))

(defn float-value-by-id [id]
  "Gets float value of a dom element by id."
  (js/parseFloat (value-by-id id 0)))

(defn filter-positive-values [dicts keys]
  "Filters DICTS with positive values for KEYS."
  (loop [ikeys keys
         result dicts]
    (if (seq ikeys)
      (recur (rest ikeys)
             (filter #(pos? ((first ikeys) %)) result))
      result)))

(defn find-max-value [keys dicts]
  "Finds max of values of given KEYS in DICTS."
  (loop [ikeys keys
         result nil]
    (if (seq ikeys)
      (recur (rest ikeys)
             (apply max (conj (map (first ikeys) dicts) result)))
      result)))

(defn find-min-value [keys dicts]
  "Finds min of values of given KEYS in DICTS."
  (loop [ikeys keys
         result nil]
    (if (seq ikeys)
      (recur (rest ikeys)
             (let [candidates (map (first ikeys) dicts)]
               (if (nil?  result)
                 (apply min candidates)
                 (apply min (conj candidates result)))))
      result)))

(defn positive-or-zero [n]
  "Returns N if it is non-negative, else returns 0."
  (if (neg? n) 0 n))

(defn zero-if-nan [n]
  "Returns N if it is a number, else returns 0."
  (if (js/isNaN n) 0 n))
