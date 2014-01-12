(ns gameoflife.core
  (:require-macros [dommy.macros :refer [node sel sel1]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [dommy.core :as dommy]
            [cljs.core.async :refer [<! timeout]]))

(def state (atom {:dimensions {:x [0 0]
                               :y [0 0]}}))

(defn neighbors [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not (= dx dy 0))]
    [(+ dx x) (+ dy y)]))

(defn next-gen [cells]
  (set
   (for [[loc c] (frequencies (mapcat neighbors cells))
             :when (or (= c 3)
                       (and (= c 2) (cells loc)))]
         loc)))

(defn generations [cells]
  (iterate next-gen cells))

(defn table-dimensions [cells]
  (let [xs (map first cells)
        ys (map second cells)]
    {:x [(apply min xs) (apply max xs)]
     :y [(apply min ys) (apply max ys)]}))

;; old = {:x [0 2] :y [1 5]}]
;; new = {:x [1 4] :y [0 3]}]
;; res = {:x [0 4] :y [0 5]}
(defn expand-dimensions [old new]
  (into {}
        (map (fn [[key [f1 t1]] [_ [f2 t2]]]
               {key [(min f1 f2) (max t1 t2)]})
             old new)))

(defn adjust-dimensions [dimensions]
  (swap! state assoc-in [:dimensions]
         (expand-dimensions (get-in @state [:dimensions])
                            dimensions)))

(def ^:dynamic *canvas* (sel1 :.game-of-life))
(def ^:dynamic *canvas-context* (.getContext *canvas* "2d"))
(def ^:dynamic *canvas-width* 500)
(def ^:dynamic *canvas-height* 500)

(defn init-canvas! []
  (.log js/console (pr-str *canvas*))
  (dommy/set-attr! *canvas* :width *canvas-width*)
  (dommy/set-attr! *canvas* :height *canvas-height*))

(defn clear-canvas! []
  (.clearRect *canvas-context* 0 0 *canvas-width* *canvas-height*))

(defn draw-rect [x y width height fill-style stroke-style]
  (when fill-style
    (aset *canvas-context* "fillStyle" fill-style)
    (.fillRect *canvas-context* x y width height))
  (when stroke-style
    (aset *canvas-context* "strokeStyle" stroke-style)
    (.strokeRect *canvas-context* x y width height)))

(defn render-canvas! [cells]
  (let [cell-dimensions (table-dimensions cells)
        {[x1 x2] :x [y1 y2] :y :as adjusted} (:dimensions (adjust-dimensions cell-dimensions))
        xs (range x1 (inc x2))
        ys (range y1 (inc y2))
        cell-width (/ *canvas-width* (- (inc x2) x1))
        cell-height (/ *canvas-height* (- (inc y2) y1))]
    (clear-canvas!)
    (doseq [x xs
            y ys
            :let [x-start (* cell-width (- x x1))
                  y-start (* cell-height (- y y1))]]
      (draw-rect x-start y-start cell-width cell-height (if (cells [x y]) "#a00" "#ccc") "black"))))

(defn start [first]
  (init-canvas!)
  (go-loop [cells first]
           (render-canvas! cells)
           (<! (timeout (/ 1000 2.0)))
           (recur (next-gen cells))))

(start #{[1 1] [0 3] [1 2] [1 3] [3 2] [5 3] [6 3] [7 3]})
