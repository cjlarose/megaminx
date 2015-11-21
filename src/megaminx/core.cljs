(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def central-angle (/ (* Math.PI 2) 5))

(defn regular-pentagon [r]
  (let [theta (/ central-angle 2)
        b (* r (Math/sin theta))
        h (* r (Math/cos theta))
        triangle (fn [i]
                   [:div {:style {:border-style "solid"
                                  :margin-left (str (- b) "em")
                                  :margin-top (str (/ h -2) "em")
                                  :transform (str "rotateZ(" (* i central-angle) "rad) translateY(" (/ h 2) "em)")
                                  :border-color "transparent transparent #f00"
                                  :border-width (str "0 " b "em " h "em")}}])]
    [:div {}
     (map triangle (range 5))]))

(defn scene []
  [:div
    [:h1 (:text @app-state)]
    (regular-pentagon 5)])

(reagent/render-component [scene]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
