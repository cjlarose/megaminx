(ns megaminx.gl-util
  (:import goog.math.Matrix))

(defn make-frustrum [left right bottom top znear zfar]
  (let [x (* 2 (/ znear (- right left)))
        y (* 2 (/ znear (- top bottom)))
        a (/ (+ right left) (- right left))
        b (/ (+ top bottom) (- top bottom))
        c (/ (- (+ zfar znear)) (- zfar znear))
        d (/ (* -2 zfar znear) (- zfar znear))]
    (Matrix. #js [#js [x 0  a 0]
                  #js [0 y b 0]
                  #js [0 0 c d]
                  #js [0 0 -1 0]])))

(defn make-perspective [fovy aspect znear zfar]
  (let [ymax (* znear (js/Math.tan (/ (* fovy js/Math.PI) 360.0)))
        ymin (- ymax)
        xmin (* ymin aspect)
        xmax (* ymax aspect)]
    (make-frustrum xmin xmax ymin ymax znear zfar)))
