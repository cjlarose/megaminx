(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]
            [megaminx.gl-util :as gl-util])
  (:import goog.math.Matrix))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:rotate-y "Hello world!"
                          :rotate-x ""}))

(def vertex-shader
  "attribute vec3 aVertexPosition;

   uniform mat4 uMVMatrix;
   uniform mat4 uPMatrix;

   void main(void) {
     gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
   }")

(def fragment-shader
  "void main(void) {
     gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
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
      (.linkProgram program)
      (.useProgram program))
    program))

(defn init-buffers [gl]
  (let [vertices (js/Float32Array. #js [ 1.0  1.0 0.0
                                        -1.0  1.0 0.0
                                         1.0 -1.0 0.0
                                        -1.0 -1.0 0.0])
        buffer (.createBuffer gl)]
    (.bindBuffer gl (.-ARRAY_BUFFER gl) buffer)
    (.bufferData gl (.-ARRAY_BUFFER gl) vertices (.-STATIC_DRAW gl))
    buffer))

(defn translation-matrix [dx dy dz]
  (doto (.createIdentityMatrix Matrix 4)
    (.setValueAt 0 3 dx)
    (.setValueAt 1 3 dy)
    (.setValueAt 2 3 dz)))

(defn flatten-matrix [m]
  (.apply js/Array.prototype.concat (js/Array.) (.toArray m)))

(defn set-matrix-uniforms [gl program perspective-matrix mv-matrix]
  (let [p-uniform (.getUniformLocation gl program "uPMatrix")
        mv-uniform (.getUniformLocation gl program "uMVMatrix")]
    (doto gl
      (.uniformMatrix4fv p-uniform false (js/Float32Array. (flatten-matrix perspective-matrix)))
      (.uniformMatrix4fv mv-uniform false (js/Float32Array. (flatten-matrix mv-matrix))))))

(defn draw-scene [gl vertex-buffer program]
  (let [vertex-pos-attr (.getAttribLocation gl program "aVertexPosition")]
    (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))
    (.bindBuffer gl (.-ARRAY_BUFFER gl) vertex-buffer)
    (.vertexAttribPointer gl vertex-pos-attr 3 (.-FLOAT gl) false 0 0)
    (let [perspective-matrix (gl-util/make-perspective 45 (/ 640.0 480) 0.1 100.0)
          mv-matrix (doto (.createIdentityMatrix Matrix 4)
                      (.multiply (translation-matrix -0.0 0.0 -6.0)))]
      (set-matrix-uniforms gl program perspective-matrix mv-matrix))
    (.drawArrays gl (.-TRIANGLE_STRIP gl) 0 4)))

(defn on-mount [component]
  (let [canvas (reagent/dom-node component)
        gl (.getContext canvas "webgl")
        vertex-buffer (init-buffers gl)
        program (init-shaders
                  gl
                  (set-shader gl :vertex vertex-shader)
                  (set-shader gl :fragment fragment-shader))]
    (doto gl
      (.clearColor 0.0 0.0 0.0 1.0)
      (.clearDepth 1.0)
      (.enable (.-DEPTH_TEST gl))
      (.depthFunc (.-LEQUAL gl))
      (.enableVertexAttribArray gl (.getAttribLocation gl program "aVertexPosition")))
    (.setInterval js/window (partial draw-scene gl vertex-buffer program) 1000)))

(defn scene []
  [:canvas {:width 640
            :height 480}])

(reagent/render-component [(with-meta scene {:component-did-mount on-mount})]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
