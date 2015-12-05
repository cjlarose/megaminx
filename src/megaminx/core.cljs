(ns megaminx.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:rotate-y "Hello world!"
                          :rotate-x ""}))

(defn on-mount [component]
  (let [canvas (reagent/dom-node component)
        gl (.getContext canvas "webgl")]
    (doto gl
      (.clearColor 0.0 0.0 0.0 1.0)
      (.enable (.-DEPTH_TEST gl))
      (.depthFunc (.-LEQUAL gl))
      (.clear (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl))))))

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
