(ns megaminx.component
  (:require [clojure.string :refer [join]]
            [reagent.core :as reagent]))

(defn rect [{:keys [w h]} styles]
  [:div {:style (merge {:width (str w "em")
                        :height (str h "em")
                        :margin-left (str (- (/ w 2)) "em")
                        :margin-top (str (- (/ w 2)) "em")}
                       styles)}])

(defn isosceles-triangle [{:keys [b h color]} styles]
  [:div {:style (merge {:border-style "solid"
                        :margin-left (str (- b) "em")
                        :margin-top (str (/ h -2) "em")
                        :border-color (str "transparent transparent " color)
                        :opacity 0.5
                        :border-width (str "0 " b "em " h "em")}
                       styles)}])

(declare shape)

(defn composite-shape [{:keys [children]} styles]
  (apply vector :div {:style styles} (map (fn [c] [shape c]) children)))

(defn transform-string [{:keys [name args]}]
  (str name "(" (join "," args) ")"))

(defn transforms-string [transforms]
  (join " " (map transform-string transforms)))

(defn shape [{:keys [name attrs styles transforms]}]
  (let [t (transforms-string transforms)
        styles (merge styles {:transform t})]
    (case name
      :rect [rect attrs styles]
      :isosceles-triangle [isosceles-triangle attrs styles]
      :composite [composite-shape attrs styles])))
