(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]
            [megaminx.util :refer [central-angle dihedral-angle circumradius phi apothem]]
            [megaminx.component :refer [shape]]
            [megaminx.model :as m]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn regular-pentagon [s color]
  (let [b (/ s 2)
        h (apothem 5 s)]
    (apply m/composite-shape
      (map (fn [i] (->> (m/isosceles-triangle b h color)
                        (m/rotate-z (* i (central-angle 5)))
                        (m/translate-y (/ h 2))))
           (range 5)))))

(defn dodecahedron [s]
  (let [a (apothem 5 s)
        c (central-angle 5)
        R (circumradius 5 s)
        r-x (- (/ js/Math.PI 2) dihedral-angle)
        r (/ (* s (js/Math.pow phi 3)) (* 2 (js/Math.sqrt (+ (js/Math.pow phi 2) 1))))]
    (apply m/composite-shape
      (->> (regular-pentagon s "#00ff00")
           (m/rotate-x (/ js/Math.PI 2))
           (m/translate-z r))
      (->> (regular-pentagon s "#8bc34a")
           (m/rotate-x (- (/ js/Math.PI 2)))
           (m/translate-z r))
      (concat
        (map-indexed (fn [i color] (->> (regular-pentagon s color)
                                        (m/rotate-y (* c i))
                                        (m/rotate-z js/Math.PI)
                                        (m/rotate-x r-x)
                                        (m/translate-z r)))
                     ["#800080" "#0000ff" "#ffff00" "#ff0000" "#ffffff"])
        (map-indexed (fn [i color] (->> (regular-pentagon s color)
                                        (m/rotate-y (+ (* c i) (/ c 2)))
                                        (m/rotate-x r-x)
                                        (m/translate-z r)))
                     ["#ff6600" "#666666" "#ffc0cb" "#0000ff" "#999900"])))))

(defn rhombohedron [s]
  (->> (m/composite-shape
         (->> (m/square s {:background-color "#000000"})
              (m/rotate-x (/ js/Math.PI 2))
              (m/translate-z (/ s 2)))
         (->> (m/square s {:background-color "#000000"})
              (m/rotate-x (- (/ js/Math.PI 2)))
              (m/translate-z (/ s 2)))
         (->> (m/square s {:background-color "#000000"})
              (m/rotate-y 0)
              ; (m/skew-x (- dihedral-angle (/ js/Math.PI 2)))
              (m/translate-z (/ s 2)))
         (->> (m/square s {:background-color "#ff6600"})
              (m/rotate-y (/ js/Math.PI 2))
              (m/translate-z (/ s 2)))
         (->> (m/square s {:background-color "#ff00ff"})
              (m/rotate-y js/Math.PI)
              ; (m/skew-x (- dihedral-angle (/ js/Math.PI 2)))
              (m/translate-z (/ s 2)))
         (->> (m/square s {:background-color "#ff0000"})
              (m/rotate-y (* js/Math.PI (/ 3 2)))
              (m/translate-z (/ s 2))))
       (m/skew-x (- dihedral-angle (/ js/Math.PI 2)))
       (m/scale-y (js/Math.cos (- dihedral-angle (/ js/Math.PI 2))))))

(defn scene []
  [:div {:style {:transform "rotateX(-30deg)"}}
   [shape (dodecahedron 7.5)]
   [shape (rhombohedron 7.5)]])

(reagent/render-component [scene]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
