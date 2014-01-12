(ns gameoflife.core
  (:require-macros [dommy.macros :refer [node sel sel1]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [dommy.core :as dommy]
            [cljs.core.async :refer [<! timeout]]
            [monet.canvas :as canvas]))

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

(defn grid-dimensions [cells]
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

(defn set-size! [canvas {:keys [w h]}]
  (dommy/set-attr! canvas :width w)
  (dommy/set-attr! canvas :height h))

(defn clear! [ctx {:keys [w h]}]
  (canvas/clear-rect ctx {:x 0 :y 0 :w w :h h}))

(defn render! [ctx {:keys [w h]} cells]
  (let [{[x1 x2] :x [y1 y2] :y} (:dimensions (adjust-dimensions (grid-dimensions cells)))
        xs (range x1 (inc x2))
        ys (range y1 (inc y2))
        cell-width (Math/floor (/ w (- (inc x2) x1)))
        cell-height (Math/floor (/ h (- (inc y2) y1)))]
    (clear! ctx {:w w :h h})
    (doseq [x xs
            y ys
            :let [x-start (* cell-width (- x x1))
                  y-start (* cell-height (- y y1))]
            :when (cells [x y])]
      (-> ctx
          (canvas/fill-style "#a00")
          (canvas/fill-rect {:x x-start :y y-start :w cell-width :h cell-height})))))

(defn start [first]
  (let [canvas-dimensions {:w 500
                           :h 500}
        canvas (sel1 :.game-of-life)
        ctx (.getContext canvas "2d")]
    (set-size! canvas canvas-dimensions)
    (go-loop [cells first]
             (render! ctx canvas-dimensions cells)
             (<! (timeout (/ 1000 2.0)))
             (recur (next-gen cells)))))

(start #{[1 1] [0 3] [1 2] [1 3] [3 2] [5 3] [6 3] [7 3]})
