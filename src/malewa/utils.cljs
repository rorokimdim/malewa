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
