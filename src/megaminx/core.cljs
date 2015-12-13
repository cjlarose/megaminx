(ns megaminx.core
  (:require [gamma.api :as g]
            [gamma.program :as p]
            [gamma-driver.api :as gd]
            [gamma-driver.drivers.basic :as driver]
            [thi.ng.math.core :as math]
            [thi.ng.geom.core :as geom]
            [thi.ng.geom.core.matrix :as mat]
            [thi.ng.geom.gmesh :as gmesh]
            [thi.ng.geom.types.utils :as tu]
            [thi.ng.geom.aabb :refer [aabb]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def initial-app-state {:last-rendered 0
                        :translate-z 0
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

(def ^:const τ math/TWO_PI)

;; spherical coordinates are in the physics convention
;; (radial distance, polar angle, azimuthal)
(defn ->cartesian [[r theta phi]]
  [(* r (js/Math.sin theta) (js/Math.cos phi))
   (* r (js/Math.sin theta) (js/Math.sin phi))
   (* r (js/Math.cos theta))])

(defn dodecahedron [s]
  (let [r (* math/SQRT3 math/PHI s 0.5)
        theta (js/Math.asin (/ (* math/SQRT3 math/PHI)))
        central-angle (/ τ 5)
        l0 (map #(vector r (* 2 theta) (* % central-angle)) (range 5))
        l1 (map #(vector r (* 4 theta) (* % central-angle)) (range 5))
        l2 (map #(vector r (* 5 theta) (+ (* % central-angle) (/ central-angle 2))) (range 5))
        l3 (map #(vector r (* 7 theta) (+ (* % central-angle) (/ central-angle 2))) (range 5))
        spherical-coords (concat l0 l1 l2 l3)
        cart-coords (map ->cartesian spherical-coords)
        [b g l h c a k q m d f p r i e z t s n j] cart-coords
        faces [[a b c d e] [k g b a f] [q l g k p] [m h l q r] [d c h m i] [l h c b g]
               [z f a e j] [t p k f z] [s r q p t] [n i m r s] [j e d i n] [t z j n s]]]
    (tu/into-mesh (gmesh/gmesh) gmesh/add-face faces)))

(defn ->color-vec [hex]
  (let [f #(-> (apply str %)
               (js/parseInt 16)
               (/ 255))]
    (map f (partition 2 hex))))

(defn dodecahedron-buffers [s]
  (let [red         (->color-vec "F44336FF")
        pink        (->color-vec "E91E63FF")
        purple      (->color-vec "9C27B0FF")
        deep-purple (->color-vec "673AB7FF")
        indigo      (->color-vec "3F51B5FF")
        blue        (->color-vec "2196F3FF")
        light-blue  (->color-vec "03A9F4FF")
        cyan        (->color-vec "00BCD4FF")
        teal        (->color-vec "009688FF")
        green       (->color-vec "4CAF50FF")
        light-green (->color-vec "8BC34AFF")
        lime        (->color-vec "CDDC39FF")]
    {:vertices {:id :dodecahedron-vertices
                :data (->> (dodecahedron s)
                           (geom/tessellate)
                           (geom/faces)
                           (apply concat))
                :immutable? true}
     :colors {:id :dodecahedron-colors
              :data [red red red
                     pink pink pink
                     purple purple purple
                     deep-purple deep-purple deep-purple
                     indigo indigo indigo
                     blue blue blue
                     deep-purple deep-purple deep-purple
                     light-blue light-blue light-blue
                     cyan cyan cyan
                     teal teal teal
                     green green green
                     teal teal teal
                     light-blue light-blue light-blue
                     red red red
                     light-green light-green light-green
                     cyan cyan cyan
                     red red red
                     deep-purple deep-purple deep-purple
                     green green green
                     light-green light-green light-green
                     indigo indigo indigo
                     indigo indigo indigo
                     blue blue blue
                     lime lime lime
                     light-green light-green light-green
                     light-blue light-blue light-blue
                     green green green
                     lime lime lime
                     pink pink pink
                     purple purple purple
                     teal teal teal
                     pink pink pink
                     lime lime lime
                     purple purple purple
                     blue blue blue
                     cyan cyan cyan]
              :immutable? true}}))

(defn get-program-data [p mv vertices colors]
  {u-p-matrix p
   u-mv-matrix mv
   a-position vertices
   a-vertex-color colors})

(defn draw-scene [gl driver program]
  (fn [state]
    (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))
    (let [rot-x (js/Math.sin (:rot-x state))
          rot-y (* 2 (js/Math.sin (:rot-y state)))
          translate-z (- (js/Math.sin (:translate-z state)) 6)
          buffers (dodecahedron-buffers 1)
          perspective-matrix (mat/perspective 45 (/ 640.0 480) 0.1 100.0)
          mv-matrix (-> (mat/matrix44)
                        (geom/translate [0 0 translate-z])
                        (geom/rotate-x rot-x)
                        (geom/rotate-y rot-y))
          program-data (get-program-data
                         perspective-matrix
                         mv-matrix
                         (:vertices buffers)
                         (:colors buffers))
          bindings (gd/bind driver program program-data)]
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
        d-translate-z (* dt 0.001)
        dx (* dt 0.002)
        dy (* dt 0.001)]
    (-> state
        (update :rot-x + dx)
        (update :rot-y + dy)
        (update :translate-z + d-translate-z)
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
