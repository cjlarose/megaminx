(ns megaminx.model)

(defn rect [w h & [styles]]
  {:name :rect
   :attrs {:w w :h h}
   :styles (if styles styles {})
   :transforms []})

(defn square [s & args]
  (apply rect s s args))

(defn isosceles-triangle [b h color & [styles]]
  {:name :isosceles-triangle
   :attrs {:b b :h h :color color}
   :styles (if styles styles {})
   :transforms []})

(defn rotate-x [angle shape]
  (update shape :transforms conj {:name "rotateX"
                                  :args [(str angle "rad")]}))

(defn rotate-y [angle shape]
  (update shape :transforms conj {:name "rotateY"
                                  :args [(str angle "rad")]}))

(defn rotate-z [angle shape]
  (update shape :transforms conj {:name "rotateZ"
                                  :args [(str angle "rad")]}))

(defn translate-y [t shape]
  (update shape :transforms conj {:name "translateY"
                                  :args [(str t "em")]}))

(defn translate-z [t shape]
  (update shape :transforms conj {:name "translateZ"
                                  :args [(str t "em")]}))

(defn composite-shape [& children]
  {:name :composite
   :transforms []
   :styles {}
   :attrs {:children children}})
