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
                   [:div {:key i
                          :style {:border-style "solid"
                                  :margin-left (str (- b) "em")
                                  :margin-top (str (/ h -2) "em")
                                  :transform (str "rotateZ(" (* i (central-angle 5)) "rad) translateY(" (/ h 2) "em)")
                                  :border-color (str "transparent transparent " color)
                                  :opacity 0.5
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
      (regular-pentagon s "#00ff00" (str "rotateX(90deg) translateZ(" r "em)")) ;; top
      (regular-pentagon s "#800080" (str "rotateY(0deg)   rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#0000ff" (str "rotateY(72deg)  rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#ffff00" (str "rotateY(144deg) rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#ff0000" (str "rotateY(216deg) rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#ffffff" (str "rotateY(288deg) rotateZ(180deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#8bc34a" (str "rotateX(-90deg) translateZ(" r "em)")) ;; base
      (regular-pentagon s "#ff6600" (str "rotateY(36deg)   rotateZ(0deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#666666" (str "rotateY(108deg)   rotateZ(0deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#ffc0cb" (str "rotateY(180deg)   rotateZ(0deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#0000ff" (str "rotateY(252deg)   rotateZ(0deg) rotateX(" r-x "rad) translateZ(" r "em)"))
      (regular-pentagon s "#999900" (str "rotateY(324deg)   rotateZ(0deg) rotateX(" r-x "rad) translateZ(" r "em)"))]))

(defn scene []
  [:div {:style {:transform "rotateX(-30deg)"}}
    (dodecahedron 7.5)])

(reagent/render-component [scene]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
