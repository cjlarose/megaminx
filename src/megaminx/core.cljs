(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]
            [megaminx.gl-util :as gl-util])
  (:import goog.math.Matrix))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:rotate-y "Hello world!"
                          :rotate-x ""}))

(defonce gl-context (atom nil))
(defonce vertex-buffer (atom nil))
(defonce vertex-color-buffer (atom nil))
(defonce gl-program (atom nil))
(defonce t (atom (js/Date.)))

(def vertex-shader
  "attribute vec3 aVertexPosition;
   attribute vec4 aVertexColor;

   uniform mat4 uMVMatrix;
   uniform mat4 uPMatrix;

   varying lowp vec4 vColor;

   void main(void) {
     gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
     vColor = aVertexColor;
   }")

(def fragment-shader
  "varying lowp vec4 vColor;

   void main(void) {
     gl_FragColor = vColor;
     // gl_FragColor = vec4(1.0, 0.5, 1.0, 1.0);
   }")

(defn set-shader [gl shader-type source]
  (let [shader-type-constant (if (= shader-type :fragment)
                               (.-FRAGMENT_SHADER gl)
                               (.-VERTEX_SHADER gl))
        shader (.createShader gl shader-type-constant)]
    (doto gl
      (.shaderSource shader source)
      (.compileShader shader))
    (if-not (.getShaderParameter gl shader (.-COMPILE_STATUS gl))
      (let [err (.getShaderInfoLog gl shader)]
        (.error js/console (str "Error compiling shaders: " err))))
    shader))

(defn init-shaders [gl vs fs]
  (let [program (.createProgram gl)]
    (doto gl
      (.attachShader program vs)
      (.attachShader program fs)
      (.linkProgram program))
    (if-not (.getProgramParameter gl program (.-LINK_STATUS gl))
      (.error js/console "Unable to initialize shader program"))
    (.useProgram gl program)
    program))

(defn init-buffer [gl arr]
  (let [buffer (.createBuffer gl)]
    (doto gl
      (.bindBuffer (.-ARRAY_BUFFER gl) buffer)
      (.bufferData (.-ARRAY_BUFFER gl) arr (.-STATIC_DRAW gl)))
    buffer))

(defn init-vertex-buffer [gl]
  (let [vertices (js/Float32Array. #js [ 1.0  1.0 0.0
                                        -1.0  1.0 0.0
                                         1.0 -1.0 0.0
                                        -1.0 -1.0 0.0])]
    (init-buffer gl vertices)))

(defn init-vertex-color-buffer [gl]
  (let [colors (js/Float32Array. #js [1.0 1.0 1.0 1.0
                                      1.0 0.0 0.0 1.0
                                      0.0 1.0 0.0 1.0
                                      0.0 0.0 1.0 1.0])]
    (init-buffer gl colors)))

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

(defn flatten-matrix [m]
  (.apply js/Array.prototype.concat (js/Array.) (.toArray (.getTranspose (Matrix. (.toArray m))))))

(defn set-matrix-uniforms [gl program perspective-matrix mv-matrix]
  (let [p-uniform (.getUniformLocation gl program "uPMatrix")
        mv-uniform (.getUniformLocation gl program "uMVMatrix")]
    (doto gl
      (.uniformMatrix4fv p-uniform false (js/Float32Array. (flatten-matrix perspective-matrix)))
      (.uniformMatrix4fv mv-uniform false (js/Float32Array. (flatten-matrix mv-matrix))))))

(defn on-update [component]
  (let [gl @gl-context
        vertex-pos (.getAttribLocation gl @gl-program "aVertexPosition")
        vertex-color (.getAttribLocation gl @gl-program "aVertexColor")
        square-rotation (/ @t 1000)]
    (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))
    (.bindBuffer gl (.-ARRAY_BUFFER gl) @vertex-buffer)
    (.vertexAttribPointer gl vertex-pos 3 (.-FLOAT gl) false 0 0)
    (.bindBuffer gl (.-ARRAY_BUFFER gl) @vertex-color-buffer)
    (.vertexAttribPointer gl vertex-color 4 (.-FLOAT gl) false 0 0)
    (let [perspective-matrix (gl-util/make-perspective 45 (/ 640.0 480) 0.1 100.0)
          mv-matrix (-> (.createIdentityMatrix Matrix 4)
                        (.multiply (translation-matrix -0.0 0.0 -6.0))
                        (.multiply (rotate-x-matrix square-rotation)))]
      (set-matrix-uniforms gl @gl-program perspective-matrix mv-matrix))
    (.drawArrays gl (.-TRIANGLE_STRIP gl) 0 4)
    (reset! t (js/Date.))))

(defn on-mount [component]
  (let [canvas (reagent/dom-node component)
        gl (.getContext canvas "webgl")
        v-buffer (init-vertex-buffer gl)
        color-buffer (init-vertex-color-buffer gl)
        program (init-shaders
                  gl
                  (set-shader gl :vertex vertex-shader)
                  (set-shader gl :fragment fragment-shader))
        a-vertex-position (.getAttribLocation gl program "aVertexPosition")
        a-vertex-color (.getAttribLocation gl program "aVertexColor")]
    (doto gl
      (.clearColor 0.0 0.0 0.0 1.0)
      (.clearDepth 1.0)
      (.enable (.-DEPTH_TEST gl))
      (.depthFunc (.-LEQUAL gl))
      (.enableVertexAttribArray a-vertex-color)
      (.enableVertexAttribArray a-vertex-position))
    (reset! gl-context gl)
    (reset! gl-program program)
    (reset! vertex-buffer v-buffer)
    (reset! vertex-color-buffer color-buffer)
    (reset! t (js/Date.))))

(defn scene []
  [:canvas {:width 640
            :height 480} @t])

(reagent/render-component [(with-meta scene {:component-did-mount on-mount
                                             :component-did-update on-update})]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
