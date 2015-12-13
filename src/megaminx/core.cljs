(ns megaminx.core
  (:require [megaminx.gl-util :as gl-util]
            [gamma.api :as g]
            [gamma.program :as p]
            [gamma-driver.api :as gd]
            [gamma-driver.drivers.basic :as driver])
  (:import goog.math.Matrix))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def initial-app-state {:last-rendered 0
                        :rot-x 0
                        :rot-y 0})
(defonce anim-loop (atom nil))

(def u-p-matrix
  (g/uniform "uPMatrix" :mat4))

(def u-mv-matrix
  (g/uniform "uMVMatrix" :mat4))

(def a-position
  (g/attribute "aVertexPosition" :vec3))

(def a-vertex-color
  (g/attribute "aVertexColor" :vec4))

(def v-color
  (g/varying "vColor" :vec4 :lowp))

(def program-source
  (p/program
    {:vertex-shader {(g/gl-position) (-> u-p-matrix
                                         (g/* u-mv-matrix)
                                         (g/* (g/vec4 a-position 1.0)))
                     v-color a-vertex-color}
     :fragment-shader {(g/gl-frag-color) v-color}}))

(defn rectangular-prism [w h d]
  (let [[x y z]      (map (partial * 0.5) [w h d])
        front-color  [0.0 0.0 1.0 1.0]
        back-color   [0.0 1.0 0.0 1.0]
        right-color  [0.0 1.0 1.0 1.0]
        left-color   [1.0 0.0 0.0 1.0]
        top-color    [1.0 0.0 1.0 1.0]
        bottom-color [1.0 1.0 0.0 1.0]]
    {:vertices {:id :prism-vertices
                :data [;; front
                       [x y z]
                       [(- x) y z]
                       [x (- y) z]
                       [(- x) y z]
                       [x (- y) z]
                       [(- x) (- y) z]
                       ;; back
                       [x y (- z)]
                       [(- x) y (- z)]
                       [x (- y) (- z)]
                       [(- x) y (- z)]
                       [x (- y) (- z)]
                       [(- x) (- y) (- z)]
                       ;; right
                       [x y (- z)]
                       [x y z]
                       [x (- y) (- z)]
                       [x y z]
                       [x (- y) (- z)]
                       [x (- y) z]
                       ;; left
                       [(- x) y (- z)]
                       [(- x) y z]
                       [(- x) (- y) (- z)]
                       [(- x) y z]
                       [(- x) (- y) (- z)]
                       [(- x) (- y) z]
                       ;; top
                       [x y (- z)]
                       [(- x) y (- z)]
                       [x y z]
                       [(- x) y (- z)]
                       [x y z]
                       [(- x) y z]
                       ;; bottom
                       [x (- y) (- z)]
                       [(- x) (- y) (- z)]
                       [x (- y) z]
                       [(- x) (- y) (- z)]
                       [x (- y) z]
                       [(- x) (- y) z]]
                :immutable? true}
     :colors {:id :prism-colors
              :data [front-color
                     front-color
                     front-color
                     front-color
                     front-color
                     front-color
                     back-color
                     back-color
                     back-color
                     back-color
                     back-color
                     back-color
                     right-color
                     right-color
                     right-color
                     right-color
                     right-color
                     right-color
                     left-color
                     left-color
                     left-color
                     left-color
                     left-color
                     left-color
                     top-color
                     top-color
                     top-color
                     top-color
                     top-color
                     top-color
                     bottom-color
                     bottom-color
                     bottom-color
                     bottom-color
                     bottom-color
                     bottom-color]
              :immutable? true}}))

(defn translation-matrix [dx dy dz]
  (doto (.createIdentityMatrix Matrix 4)
    (.setValueAt 3 0 dx)
    (.setValueAt 3 1 dy)
    (.setValueAt 3 2 dz)))

(defn rotate-x-matrix [angle]
  (let [c (js/Math.cos angle)
        s (js/Math.sin angle)]
    (doto (.createIdentityMatrix Matrix 4)
      (.setValueAt 1 1 c)
      (.setValueAt 2 1 (- s))
      (.setValueAt 1 2 s)
      (.setValueAt 2 2 c))))

(defn rotate-y-matrix [angle]
  (let [c (js/Math.cos angle)
        s (js/Math.sin angle)]
    (doto (.createIdentityMatrix Matrix 4)
      (.setValueAt 0 0 c)
      (.setValueAt 0 2 s)
      (.setValueAt 2 0 (- s))
      (.setValueAt 2 2 c))))

(defn get-program-data [p mv vertices colors]
  {u-p-matrix p
   u-mv-matrix mv
   a-position vertices
   a-vertex-color colors})

(defn draw-scene [gl driver program]
  (fn [state]
    (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))
    (let [square (rectangular-prism 2 2.5 1.5)
          perspective-matrix (-> (gl-util/make-perspective 45 (/ 640.0 480) 0.1 100.0)
                                 (.toArray)
                                 (js->clj))
          mv-matrix (-> (.createIdentityMatrix Matrix 4)
                        (.multiply (rotate-x-matrix (:rot-x state)))
                        (.multiply (rotate-y-matrix (:rot-y state)))
                        (.multiply (translation-matrix -0.0 0.0 -6.0))
                        (.toArray)
                        (js->clj))
          bindings (gd/bind driver program (get-program-data perspective-matrix mv-matrix (:vertices square) (:colors square)))]
      (gd/draw-arrays driver bindings {:draw-mode :triangles}))))

(defn animate [draw-fn step-fn current-value]
  (let [cb (fn [t]
             (let [next-value (step-fn t current-value)]
               (draw-fn next-value)
               (animate draw-fn step-fn next-value)))]
    (reset! anim-loop (js/requestAnimationFrame cb))))

(defn tick [t state]
  (let [old-t (:last-rendered state)
        dt (- t old-t)
        dx (* (js/Math.sin (* t 0.001)) 0.1)
        dy (* (js/Math.sin (* t 0.001)) 0.01)]
    (-> state
        (update :rot-x + dx)
        (update :rot-y + dy)
        (assoc :last-rendered t))))

(defn main []
  (if @anim-loop
    (js/cancelAnimationFrame @anim-loop))
  (let [canvas (.getElementById js/document "scene")
        gl (.getContext canvas "webgl")
        driver (driver/basic-driver gl)
        program (gd/program driver program-source)]
    (doto gl
      (.clearColor 0.0 0.0 0.0 1.0)
      (.clearDepth 1.0)
      (.enable (.-DEPTH_TEST gl))
      (.depthFunc (.-LEQUAL gl)))
    (animate (draw-scene gl driver program) tick initial-app-state)))

(main)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
