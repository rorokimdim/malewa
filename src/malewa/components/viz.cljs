(ns malewa.components.viz
  (:require [rid3.core :as d3]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config get-window-width get-window-height]]))

(def BAR-COLORS {:balance "#31a354"
                 :retirement-account-balance-pre-tax "#316395"
                 :retirement-account-balance-post-tax "#ff9900"})
(def BAR-OFFSETS {:balance 0
                  :retirement-account-balance-pre-tax -0.1
                  :retirement-account-balance-post-tax 0.1})

(def TARGET-YEAR-STROKE-COLOR "black")
(def RETIREMENT-WITHDRAWAL-YEAR-STROKE-COLOR "blue")

(def CHART-BAR-WIDTH 5)
(def CHART-HEIGHT 200)
(def CHART-PADDING-HEIGHT 50)
(def CHART-PADDING-WIDTH 80)
(def CHART-WIDTH-PCT 0.80)

(def SVG-HEIGHT (+ CHART-HEIGHT CHART-PADDING-HEIGHT))

(defn chart-width []
  "Gets chart width."
  (* CHART-WIDTH-PCT (get-window-width)))

(defn svg-width []
  "Gets SVG width."
  (+ (chart-width) CHART-PADDING-WIDTH))

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

(defn svg-did-mount [node ratom]
  "Mount function for svg component."
  (-> node
      (.attr "width" (svg-width))
      (.attr "height" SVG-HEIGHT)))

(defn svg-did-update [node ratom]
  "Update function for svg component."
  (-> node
      (.attr "width" (svg-width))
      (.attr "height" SVG-HEIGHT)))

(defn main-container-did-mount [node ratom]
  "Mount function for main-container."
  (-> node
      (.attr "transform" "translate(50, 0)")))

(defn create-x-scale [computations]
  "Creates x-scale for plotting given computations."
  (-> js/d3
      .scaleLinear
      (.domain #js [0 (count computations)])
      (.range #js [0 (chart-width)])))

(defn create-y-scale [keys computations]
  "Creates y-scale for plotting given computations."
  (-> js/d3
      .scaleLinear
      (.domain #js [(find-min-value keys computations)
                    (find-max-value keys computations)])
      (.range #js [CHART-HEIGHT 0])))

(defn x-axis [keys node ratom]
  "Builds x-axis."
  (let [computations @ratom
        x-scale (create-x-scale computations)]
    (-> node
        (.attr "transform" (str "translate(" (/ CHART-BAR-WIDTH 2) "," CHART-HEIGHT ")"))
        (.call (.axisBottom js/d3 x-scale)))
    (-> node
        (.select "path")
        (.style "stroke" "none"))))

(defn y-axis [keys node ratom]
  "Builds y-axis."
  (let [computations (filter-positive-values @ratom keys)
        y-scale (create-y-scale keys computations)]
    (-> node
        (.call (-> (.axisLeft js/d3 y-scale)
                   (.ticks 5)
                   (.tickFormat (fn [d]
                                  (let [thousands (/ d 1000)
                                        millions (/ d 1000000)]
                                    (if (>= millions 1) (str millions "M")
                                        (str thousands "K")))))
                   (.tickSizeInner #js [(- 5)]))))
    (-> node
        (.select "path")
        (.style "stroke" "none"))))

(defn chart-label [text node ratom]
  "Builds a label with TEXT."
  (-> node
      (.attr "x" (/ (chart-width) 2))
      (.attr "y" (- SVG-HEIGHT 10))
      (.attr "text-anchor" "middle")
      (.attr "font-size" 14)
      (.attr "font-family" "sans-serif")
      (.attr "fill" "#5D6971")
      (.text text)))

(defn positive-or-zero [n]
  "Returns N if it is non-negative, else returns 0."
  (if (neg? n) 0 n))

(defn zero-if-nan [n]
  "Returns N if it is a number, else returns 0."
  (if (js/isNaN n) 0 n))

(defn bars [keys key node ratom]
  "Builds SVG bars."
  (let [config (get-config)
        target-year (:target-retirement-after-years config)
        retirement-withdrawal-year (f/retirement-account-early-withdrawal-penalty-tax-years
                                    config)
        computations @ratom
        positive-computations (filter-positive-values computations keys)
        x-scale (create-x-scale computations)
        y-scale (create-y-scale keys positive-computations)]
    (-> node
        (.attr "fill" (key BAR-COLORS))
        (.style "stroke" (fn [d]
                           (let [year (aget d "year")]
                             (cond
                               (= year target-year) TARGET-YEAR-STROKE-COLOR
                               (= year retirement-withdrawal-year)
                               RETIREMENT-WITHDRAWAL-YEAR-STROKE-COLOR
                               :else nil))))
        (.attr "x" (fn [d] (x-scale (+ (key BAR-OFFSETS) (aget d "year")))))
        (.attr "y" (fn [d] (zero-if-nan (y-scale (positive-or-zero (aget d (name key)))))))
        (.attr "height" (fn [d] (positive-or-zero
                                 (zero-if-nan (- CHART-HEIGHT
                                                 (y-scale (positive-or-zero (aget d (name key)))))))))
        (.attr "width" CHART-BAR-WIDTH))))

(defn viz-comp [keys label ratom]
  "Builds visualization component for values of given KEYS."
  (chart-width) ;; Hack to update this component when window is resized
  [d3/viz
   {:id (apply str (map name keys))
    :ratom ratom
    :svg {:did-mount svg-did-mount
          :did-update svg-did-update}
    :main-container {:did-mount main-container-did-mount}
    :pieces (concat [{:kind :container
                      :class "x-axis"
                      :children
                      [{:kind :container
                        :class  "x-axis"
                        :did-mount (partial x-axis keys)}
                       {:kind :container
                        :class  "y-axis"
                        :did-mount (partial y-axis keys)}
                       ]}
                     {:kind :elem
                      :class "chart-label"
                      :tag "text"
                      :did-mount (partial chart-label label)}]
                    (for [key keys]
                      {:kind :elem-with-data
                       :class (name key)
                       :tag "rect"
                       :prepare-dataset (fn [ratom] (-> @ratom clj->js))
                       :did-mount (partial bars keys key)
                       :did-update (partial bars keys key)}))}])
