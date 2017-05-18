(ns malewa.components.viz
  (:require [rid3.core :as d3]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config get-window-width get-window-height]]))
(def POSITIVE-BAR-COLOR "#31a354")
(def NEGATIVE-BAR-COLOR "#e6550d")
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

(defn abs [n] (if (neg? n) (* -1 n) n))

(defn find-abs-max-value [key computations]
  "Finds max of absolute values of given KEY in COMPUTATIONS."
  (abs (:balance (apply max-key #(abs (:balance %1)) computations))))

(defn find-abs-min-value [key computations]
  "Finds min of absolute values of given KEY in COMPUTATIONS."
  (abs (:balance (apply min-key #(abs (:balance %1)) computations))))

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
  (-> js/d3
      .scaleLinear
      (.domain #js [0 (count computations)])
      (.range #js [0 (chart-width)])))

(defn create-y-scale [computations]
  (-> js/d3
      .scaleLinear
      (.domain #js [(find-abs-min-value :balance computations)
                    (find-abs-max-value :balance computations)])
      (.range #js [CHART-HEIGHT 0])))

(defn x-axis [node ratom]
  (let [computations @ratom
        x-scale (create-x-scale computations)]
    (-> node
        (.attr "transform" (str "translate(" (/ CHART-BAR-WIDTH 2) "," CHART-HEIGHT ")"))
        (.call (.axisBottom js/d3 x-scale)))
    (-> node
        (.select "path")
        (.style "stroke" "none"))))

(defn y-axis [node ratom]
  (let [computations @ratom
        y-scale (create-y-scale computations)]
    (-> node
        (.call (-> (.axisLeft js/d3 y-scale)
                   (.ticks 5)
                   (.tickFormat (fn [d]
                                  (let [thousands (/ d 1000)
                                        millions (/ d 1000000)]
                                    (if (> millions 1) (str millions "M")
                                        (str thousands "K")))))
                   (.tickSizeInner #js [(- 5)]))))
    (-> node
        (.select "path")
        (.style "stroke" "none"))))

(defn chart-label [node ratom]
  (-> node
      (.attr "x" (/ (chart-width) 2))
      (.attr "y" (- SVG-HEIGHT 10))
      (.attr "text-anchor" "middle")
      (.attr "font-size" 14)
      (.attr "font-family" "sans-serif")
      (.attr "fill" "#5D6971")
      (.text "Balance in non-retirement accounts over years")))

(defn bars [node ratom]
  "Builds SVG bars."
  (let [config (get-config)
        target-year (:target-retirement-after-years config)
        retirement-withdrawal-year (f/retirement-account-early-withdrawal-penalty-tax-years
                                    config)
        computations @ratom
        x-scale (create-x-scale computations)
        y-scale (create-y-scale computations)]
    (-> node
        (.attr "fill" (fn [d] (let [b (aget d "balance")]
                                (if (> b 0) POSITIVE-BAR-COLOR NEGATIVE-BAR-COLOR))))
        (.style "stroke" (fn [d]
                           (let [year (aget d "year")]
                             (cond
                               (= year target-year) TARGET-YEAR-STROKE-COLOR
                               (= year retirement-withdrawal-year)
                               RETIREMENT-WITHDRAWAL-YEAR-STROKE-COLOR
                               :else nil))))
        (.attr "x" (fn [d] (x-scale (aget d "year"))))
        (.attr "y" (fn [d] (y-scale (abs (aget d "balance")))))
        (.attr "height" (fn [d] (- CHART-HEIGHT (y-scale (abs (aget d "balance"))))))
        (.attr "width" CHART-BAR-WIDTH))))

(defn viz-comp [ratom]
  "Builds visualization component."
  (chart-width) ;; Hack to update this component when window is resized
  [d3/viz
   {:id "barchart"
    :ratom ratom
    :svg {:did-mount svg-did-mount
          :did-update svg-did-update}
    :main-container {:did-mount main-container-did-mount}
    :pieces [{:kind :container
              :class "x-axis"
              :children
              [{:kind :container
                :class  "x-axis"
                :did-mount x-axis}
               {:kind :container
                :class  "y-axis"
                :did-mount y-axis}
               ]}
             {:kind :elem
              :class "chart-label"
              :tag "text"
              :did-mount chart-label}
             {:kind :elem-with-data
              :class "bars"
              :tag "rect"
              :prepare-dataset (fn [ratom] (-> @ratom clj->js))
              :did-mount bars
              :did-update bars}]}])
