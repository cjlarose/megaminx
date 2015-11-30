(ns megaminx.component
  (:require [clojure.string :refer [join]]
            [reagent.core :as reagent]))

(defn rect [{:keys [w h]} styles]
  [:div {:style (merge {:width (str w "em")
                        :height (str h "em")
                        :margin-left (str (- (/ w 2)) "em")
                        :margin-top (str (- (/ h 2)) "em")}
                       styles)}])

(defn isosceles-triangle [{:keys [b h color]} styles]
  [:div {:style (merge {:border-style "solid"
                        :margin-left (str (- (/ b 2)) "em")
                        :margin-top (str (/ h -2) "em")
                        :border-color (str "transparent transparent " color)
                        :opacity 0.5
                        :border-width (str "0 " (/ b 2) "em " h "em")}
                       styles)}])

(defn transform-string [{:keys [name args]}]
  (str name "(" (join "," args) ")"))

(defn transforms-string [transforms]
  (join " " (map transform-string transforms)))

(defn transform [transforms & children]
  [:div
   {:style {:transform (transforms-string transforms)}}
   (map-indexed #(with-meta %2 {:key %1}) children)])
