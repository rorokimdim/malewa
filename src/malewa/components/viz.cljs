(ns malewa.components.viz
  (:require [rid3.core :as d3]
            [malewa.utils :as u]
            [malewa.finance :as f]
            [malewa.dao :refer [get-config get-window-width get-window-height
                                reset-selected-value!]]))

(def BAR-COLORS {:balance "#31a354"
                 :retirement-account-balance-pre-tax "#316395"
                 :retirement-account-balance-post-tax "#ff9900"})
(def BAR-OFFSETS {:balance 0
                  :retirement-account-balance-pre-tax -0.25
                  :retirement-account-balance-post-tax 0.25})
(def BAR-STROKE-WIDTH 2)

(def BROKE-YEAR-STROKE-COLOR "red")
(def TARGET-YEAR-STROKE-COLOR "black")
(def RETIREMENT-WITHDRAWAL-YEAR-STROKE-COLOR "blue")

(def MAX-CHART-BAR-WIDTH 8)
(def CHART-HEIGHT 200)
(def CHART-PADDING-HEIGHT 50)
(def CHART-PADDING-WIDTH 80)
(def CHART-WIDTH-PCT 0.80)

(def SVG-HEIGHT (+ CHART-HEIGHT CHART-PADDING-HEIGHT))

(defn chart-width []
  "Gets chart width."
  (* CHART-WIDTH-PCT (get-window-width)))

(defn chart-bar-width []
  "Gets with of each bar for bar charts."
  (min MAX-CHART-BAR-WIDTH (/ (chart-width) 100)))

(defn svg-width []
  "Gets SVG width."
  (+ (chart-width) CHART-PADDING-WIDTH))

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
      (.domain #js [(u/find-min-value keys computations)
                    (u/find-max-value keys computations)])
      (.range #js [CHART-HEIGHT 0])))

(defn x-axis [keys node ratom]
  "Builds x-axis."
  (let [computations @ratom
        x-scale (create-x-scale computations)]
    (-> node
        (.attr "transform" (str "translate(" (/ (chart-bar-width) 2) "," CHART-HEIGHT ")"))
        (.call (.axisBottom js/d3 x-scale)))
    (-> node
        (.select "path")
        (.style "stroke" "none"))))

(defn y-axis [keys node ratom]
  "Builds y-axis."
  (let [computations (u/filter-positive-values @ratom keys)
        y-scale (create-y-scale keys computations)]
    (-> node
        (.call (-> (.axisLeft js/d3 y-scale)
                   (.ticks 5)
                   (.tickFormat u/format-number-as-abbreviated)
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

(defn bars [keys key node ratom]
  "Builds SVG bars."
  (let [config (get-config)
        target-year (:target-retirement-after-years config)
        retirement-withdrawal-year (f/retirement-account-early-withdrawal-penalty-tax-years
                                    config)
        computations @ratom
        expenses-per-year (:expenses-per-year-during-retirement config)
        broke-year-c (f/get-broke-year-computation config computations)
        positive-computations (u/filter-positive-values computations keys)
        x-scale (create-x-scale computations)
        y-scale (create-y-scale keys positive-computations)]
    (-> node
        (.attr "fill" (key BAR-COLORS))
        (.style "cursor" "hand")
        (.style "stroke" (fn [d]
                           (let [year (aget d "year")]
                             (cond
                               (= year target-year) TARGET-YEAR-STROKE-COLOR
                               (= year retirement-withdrawal-year)
                               RETIREMENT-WITHDRAWAL-YEAR-STROKE-COLOR
                               (and (not (nil? broke-year-c))
                                    (= year (:year broke-year-c))) BROKE-YEAR-STROKE-COLOR
                               :else nil))))
        (.style "stroke-width" BAR-STROKE-WIDTH)
        (.attr "x" (fn [d] (x-scale (+ (key BAR-OFFSETS) (aget d "year")))))
        (.attr "y" (fn [d] (u/zero-if-nan (y-scale (u/positive-or-zero (aget d (name key)))))))
        (.attr "height" (fn [d] (u/positive-or-zero
                                 (u/zero-if-nan (- CHART-HEIGHT
                                                   (y-scale (u/positive-or-zero (aget d (name key)))))))))
        (.attr "width" (chart-bar-width))
        (.on "click" (fn [d]
                       (let [year (aget d "year")
                             value (aget d (name key))
                             formatted-value (u/format-number-with-commas (js/parseInt value))]
                         (reset-selected-value! (str "Year " year ": " formatted-value))
                         (-> (js/d3.select "#selected")
                             (.style "left" (str (- js/d3.event.pageX 50) "px"))
                             (.style "top" (str (- js/d3.event.pageY 100) "px"))
                             (.style "display" "inline-block")))))
        (.on "mouseover" (fn []
                           (this-as this
                             (-> (js/d3.select this)
                                 (.style "fill" "brown")))))
        (.on "mouseout" (fn [d]
                          (this-as this
                            (-> (js/d3.select this)
                                (.style "fill" (key BAR-COLORS))))
                          (-> (js/d3.select "#selected") (.style "display" "none"))
                          (reset-selected-value! nil))))))

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
                      :class "axis"
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
