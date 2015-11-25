(ns megaminx.util)

(def phi (/ (+ 1 (js/Math.sqrt 5)) 2))

(defn central-angle [n]
  (/ (* Math.PI 2) n))

(defn interior-angle [n]
  (* (- n 2) js/Math.PI (/ n)))

(defn apothem [n s]
  (let [theta (/ js/Math.PI n)]
    (/ s (* 2 (js/Math.tan theta)))))

(defn circumradius [n s]
  (let [theta (/ js/Math.PI n)]
    (/ s (* 2 (js/Math.sin theta)))))

(def dihedral-angle (- js/Math.PI (js/Math.atan 2)))
