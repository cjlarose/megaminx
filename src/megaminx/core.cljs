(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]
            [megaminx.util :refer [central-angle
                                   interior-angle
                                   dihedral-angle
                                   circumradius
                                   phi
                                   hypotenuse
                                   apothem]]
            [megaminx.component :refer [transform] :as component]
            [megaminx.model :as m]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:rotate-y "Hello world!"
                          :rotate-x ""}))

(defn regular-pentagon [s color]
  (let [h (apothem 5 s)]
    [:div
     (map (fn [i] (with-meta
                    [transform
                     [(m/rotate-z (* i (central-angle 5)))
                      (m/translate-y (/ h 2))]
                     (component/isosceles-triangle {:b s :h h :color color} {})]
                    {:key i}))
          (range 5))]))

(defn pentagonal-pyramid [s h color]
  (let [c (central-angle 5)
        a (apothem 5 s)
        lateral-height (hypotenuse a h)
        base-angle (js/Math.atan (/ h a))]
    [:div
     [transform
      [(m/rotate-x (- (/ js/Math.PI 2)))
       (m/translate-z (/ h 2))]
      [regular-pentagon s color]]
     (map-indexed (fn [i] (with-meta
                            [transform
                             [(m/rotate-y (+ (* i c) (/ c 2)))
                              (m/translate-z (/ a 2))
                              (m/rotate-x (- (/ js/Math.PI 2) base-angle))]
                             [component/isosceles-triangle {:b s :h lateral-height :color color} {}]]
                            {:key i}))
                  (range 5))]))

(defn dodecahedron [s]
  (let [a (apothem 5 s)
        c (central-angle 5)
        R (circumradius 5 s)
        r-x (- (/ js/Math.PI 2) dihedral-angle)
        r (/ (* s (js/Math.pow phi 3)) (* 2 (js/Math.sqrt (+ (js/Math.pow phi 2) 1))))]
    [:div
     [transform
       [(m/rotate-x (/ js/Math.PI 2))
        (m/translate-z r)]
       [regular-pentagon s "#00ff00"]]
     [transform
       [(m/rotate-x (- (/ js/Math.PI 2)))
        (m/translate-z r)]
       [regular-pentagon s "#8bc34a"]]
     (map-indexed (fn [i color] (with-meta
                                  [transform
                                   [(m/rotate-y (* c i))
                                    (m/rotate-z js/Math.PI)
                                    (m/rotate-x r-x)
                                    (m/translate-z r)]
                                   [regular-pentagon s color]]
                                  {:key (+ i 2)}))
                  ["#800080" "#0000ff" "#ffff00" "#ff0000" "#ffffff"])
     (map-indexed (fn [i color] (with-meta
                                  [transform
                                   [(m/rotate-y (+ (* c i) (/ c 2)))
                                    (m/rotate-x r-x)
                                    (m/translate-z r)]
                                   [regular-pentagon s color]]
                                  {:key (+ i 7)}))
                  ["#ff6600" "#666666" "#ffc0cb" "#0000ff" "#999900"])]))

(defn rhombohedron [s]
  (let [alpha (/ js/Math.PI 10) ;(- (interior-angle 5) (/ js/Math.PI 2))
        translate 5.0]; (/ s 2)]
        ; alpha (* (/ 1 5) js/Math.PI)]
    [transform
     [(m/translate-z 7)
      (m/translate-y -7.0)]
     [transform
      [(m/skew-x (- alpha))
       (m/scale-y (js/Math.cos (- alpha)))
       (m/rotate-y 0)
       (m/translate-z translate)
       (m/rotate-x (- (- (/ js/Math.PI 2) dihedral-angle)))]
      (component/rect {:w s :h s} {:background-color "#006699" :opacity 0.5})]
     [transform
      [(m/rotate-y (- (central-angle 5)))
       (m/skew-x alpha)
       (m/scale-y (js/Math.cos alpha))
       (m/translate-z translate)
       (m/rotate-x (- (- (/ js/Math.PI 2) dihedral-angle)))]
      (component/rect {:w s :h s} {:background-color "#00ff00" :opacity 0.5})]]))

(defn scene []
  (let [rotate (atom {:x (- (/ js/Math.PI 6)) :y 0})
        start-pos (atom nil)
        record-pos (fn [e] (reset! start-pos [[(.-pageX e) (.-pageY e)] @rotate]))
        update-rotate (fn [e] (if @start-pos
                                (let [current-pos   [(.-pageX e) (.-pageY e)]
                                      [dx dy]       (map - (first @start-pos) current-pos)
                                      {:keys [x y]} (second @start-pos)]
                                  (reset! rotate {:y (+ y (* dx 0.001)) :x (+ x (* dy 0.001))}))))
        clear-pos #(reset! start-pos nil)]
    (fn []
      [:div {:id "scene"
             :on-mouse-down record-pos
             :on-mouse-move update-rotate
             :on-mouse-up clear-pos}
       [transform
        [(m/rotate-x (:x @rotate))
         (m/rotate-y (:y @rotate))]
        [dodecahedron 7.5]
        [rhombohedron 3.75]]])))

(reagent/render-component [scene]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
