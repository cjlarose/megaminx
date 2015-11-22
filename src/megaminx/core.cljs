(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def phi (/ (+ 1 (js/Math.sqrt 5)) 2))

(defn central-angle [n]
  (/ (* Math.PI 2) n))

(defn apothem [n s]
  (let [theta (/ js/Math.PI n)]
    (/ s (* 2 (js/Math.tan theta)))))

(defn circumradius [n s]
  (let [theta (/ js/Math.PI n)]
    (/ s (* 2 (js/Math.sin theta)))))

(defn regular-pentagon [s color transform]
  (let [b (/ s 2)
        h (apothem 5 s)
        triangle (fn [i]
                   [:div {:style {:border-style "solid"
                                  :margin-left (str (- b) "em")
                                  :margin-top (str (/ h -2) "em")
                                  :transform (str "rotateZ(" (* i (central-angle 5)) "rad) translateY(" (/ h 2) "em)")
                                  :border-color (str "transparent transparent " color)
                                  :border-width (str "0 " b "em " h "em")}}])]
    [:div {:style {:transform transform}}
     (map triangle (range 5))]))

(defn dodecahedron [s]
  (let [a (apothem 5 s)
        R (circumradius 5 s)
        dihedral-angle (- js/Math.PI (js/Math.atan 2))
        r-x (- (/ js/Math.PI 2) dihedral-angle)
        r (/ (* s (js/Math.pow phi 3)) (* 2 (js/Math.sqrt (+ (js/Math.pow phi 2) 1))))]
    [:div
      dihedral-angle "rad"
      ;(regular-pentagon 5 "#000" "")
      (regular-pentagon s "#0f0" (str "rotateX(90deg) translateZ(" r "em)"))
      (regular-pentagon s "#f0f" (str "rotateY(0deg)   rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#00f" (str "rotateY(72deg)  rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#ff0" (str "rotateY(144deg) rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#f00" (str "rotateY(216deg) rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#fff" (str "rotateY(288deg) rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))]))

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
