(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def central-angle (/ (* Math.PI 2) 5))

(defn regular-pentagon [r color transform]
  (let [theta (/ central-angle 2)
        b (* r (Math/sin theta))
        h (* r (Math/cos theta))
        triangle (fn [i]
                   [:div {:style {:border-style "solid"
                                  :margin-left (str (- b) "em")
                                  :margin-top (str (/ h -2) "em")
                                  :transform (str "rotateZ(" (* i central-angle) "rad) translateY(" (/ h 2) "em)")
                                  :border-color (str "transparent transparent " color)
                                  :border-width (str "0 " b "em " h "em")}}])]
    [:div {:style {:transform transform}}
     (map triangle (range 5))]))

(defn dodecahedron [r]
  (let [r 5.7
        r-x -26]
    [:div
      ;(regular-pentagon 5 "#000" "")
      (regular-pentagon 5 "#0f0" (str "rotateX(90deg) translateZ(3.6em)"))
      (regular-pentagon 5 "#f0f" (str "rotateY(0deg)   rotateZ(180deg) translateZ(" r "em) rotateX(" r-x "deg)"))
      (regular-pentagon 5 "#00f" (str "rotateY(72deg)  rotateZ(180deg) translateZ(" r "em) rotateX(" r-x "deg)"))
      (regular-pentagon 5 "#ff0" (str "rotateY(144deg) rotateZ(180deg) translateZ(" r "em) rotateX(" r-x "deg)"))
      (regular-pentagon 5 "#f00" (str "rotateY(216deg) rotateZ(180deg) translateZ(" r "em) rotateX(" r-x "deg)"))
      (regular-pentagon 5 "#fff" (str "rotateY(288deg) rotateZ(180deg) translateZ(" r "em) rotateX(" r-x "deg)"))]))

(defn scene []
  [:div {:style {:transform "rotateX(-30deg)"}}
    [:h1 (:text @app-state)]
    (dodecahedron 5)])

(reagent/render-component [scene]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
