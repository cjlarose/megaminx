(ns megaminx.model)

(defn rotate-x [angle]
  {:name "rotateX"
   :args [(str angle "rad")]})

(defn rotate-y [angle]
  {:name "rotateY"
   :args [(str angle "rad")]})

(defn rotate-z [angle]
  {:name "rotateZ"
   :args [(str angle "rad")]})

(defn skew-x [angle]
  {:name "skewX"
   :args [(str angle "rad")]})

(defn skew-y [angle]
  {:name "skewY"
   :args [(str angle "rad")]})

(defn scale-x [s]
  {:name "scaleX"
   :args [(str s)]})

(defn scale-y [s]
  {:name "scaleY"
   :args [(str s)]})

(defn scale-z [s]
  {:name "scaleZ"
   :args [(str s)]})

(defn translate-x [t]
  {:name "translateX"
   :args [(str t "em")]})

(defn translate-y [t]
  {:name "translateY"
   :args [(str t "em")]})

(defn translate-z [t]
  {:name "translateZ"
   :args [(str t "em")]})
