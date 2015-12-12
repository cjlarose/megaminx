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

(def initial-app-state {:last-rendered 0})
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

(def square
  {:vertices {:id :square-vertices
              :data [[ 1.0  1.0 0.0]
                     [-1.0  1.0 0.0]
                     [ 1.0 -1.0 0.0]
                     [-1.0 -1.0 0.0]]
              :immutable? true}
   :colors {:id :square-colors
            :data [[1.0 1.0 1.0 1.0]
                   [1.0 0.0 0.0 1.0]
                   [0.0 1.0 0.0 1.0]
                   [0.0 0.0 1.0 1.0]]
            :immutable? true}})

(defn translation-matrix [dx dy dz]
  (doto (.createIdentityMatrix Matrix 4)
    (.setValueAt 0 3 dx)
    (.setValueAt 1 3 dy)
    (.setValueAt 2 3 dz)))

(defn rotate-x-matrix [angle]
  (doto (.createIdentityMatrix Matrix 4)
    (.setValueAt 1 1 (js/Math.cos angle))
    (.setValueAt 1 2 (- (js/Math.sin angle)))
    (.setValueAt 2 1 (js/Math.sin angle))
    (.setValueAt 2 2 (js/Math.cos angle))))

(defn get-program-data [p mv vertices colors]
  {u-p-matrix p
   u-mv-matrix mv
   a-position vertices
   a-vertex-color colors})

(defn draw-scene [gl driver program]
  (fn [state]
    (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))
    (let [square-rotation (/ (:last-rendered state) 1000)
          perspective-matrix (-> (gl-util/make-perspective 45 (/ 640.0 480) 0.1 100.0)
                                 (.toArray)
                                 (js->clj))
          mv-matrix (-> (.createIdentityMatrix Matrix 4)
                        (.multiply (translation-matrix -0.0 0.0 -6.0))
                        (.multiply (rotate-x-matrix square-rotation))
                        (.toArray)
                        (js->clj))
          bindings (gd/bind driver program (get-program-data perspective-matrix mv-matrix (:vertices square) (:colors square)))]
      (gd/draw-arrays driver bindings {:draw-mode :triangle-strip}))))

(defn animate [draw-fn step-fn current-value]
  (let [cb (fn [t]
             (let [next-value (step-fn t current-value)]
               (draw-fn next-value)
               (animate draw-fn step-fn next-value)))]
    (reset! anim-loop (js/requestAnimationFrame cb))))

(defn tick [t state]
  (assoc state :last-rendered t))

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
