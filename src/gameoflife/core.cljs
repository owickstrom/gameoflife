(ns gameoflife.core
  (:require-macros [dommy.macros :refer [node sel sel1]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [dommy.core :as dommy]
            [cljs.core.async :refer [<! timeout]]
            [monet.canvas :as canvas]))

(def state (atom {:dimensions {:x [0 0]
                               :y [0 0]}
                  :canvas-size {:w 500
                                :h 500}}))

(defn cell-at? [cells x y]
  (some #(and (= x (:x %1)) (= y (:y %1))) cells))

(defn neighbors [{:keys [x y]}]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not (= dx dy 0))]
    {:x (+ dx x) :y (+ dy y)}))

(defn next-gen [cells]
  (let [freqs (frequencies (mapcat neighbors cells))
        neighbor-cells (map
                        (fn [[{:keys [x y] :as cell} c]]
                          (cond (= c 3) (merge cell {:state :born})
                                (and (= c 2) (cell-at? cells x y)) (merge cell {:state :alive})
                                :else nil))
                        freqs)]
    (set (filter (complement nil?) neighbor-cells))))

(defn generations [cells]
  (iterate next-gen cells))

(defn grid-dimensions [cells]
  (let [xs (map :x cells)
        ys (map :y cells)]
    {:x [(apply min xs) (apply max xs)]
     :y [(apply min ys) (apply max ys)]}))

;; old = {:x [0 2] :y [1 5]}
;; new = {:x [1 4] :y [0 3]}
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

(defn set-size! [canvas {:keys [w h] :as size}]
  (.log js/console "Setting size to " w h)
  (swap! state assoc-in [:canvas-size] size)
  (dommy/set-attr! canvas :width w)
  (dommy/set-attr! canvas :height h))

(defn adjust-to-window-size! [canvas]
  (.log js/console "Adjusting size")
  (set-size! canvas {:w (aget js/window "innerWidth")
                      :h (aget js/window "innerHeight")}))

(defn listen-for-window-resize! [canvas]
  (dommy/listen! js/window :resize #(adjust-to-window-size! canvas)))

(defn clear! [ctx {:keys [w h]}]
  (canvas/clear-rect ctx {:x 0 :y 0 :w w :h h}))

(defn render! [ctx cells]
  (let [{:keys [w h]} (:canvas-size @state)
        {[x1 x2] :x [y1 y2] :y} (:dimensions (adjust-dimensions (grid-dimensions cells)))
        cell-width (Math/floor (/ w (- (inc x2) x1)))
        cell-height (Math/floor (/ h (- (inc y2) y1)))]
    (.log js/console w h)
    (clear! ctx {:w w :h h})
    (doseq [{:keys [x y state]} cells
            :let [x-start (* cell-width (- x x1))
                  y-start (* cell-height (- y y1))]]
      (-> ctx
          (canvas/fill-style (if (= state :alive) "#a00" "#f00"))
          (canvas/fill-rect {:x x-start :y y-start :w cell-width :h cell-height})))))

(defn start [first]
  (let [canvas (sel1 :.game-of-life)
        ctx (.getContext canvas "2d")]
    (listen-for-window-resize! canvas)
    (adjust-to-window-size! canvas)
    (go-loop [cells first]
             (render! ctx cells)
             (<! (timeout (/ 1000 2.0)))
             (recur (next-gen cells)))))

(start #{{:x 1 :y 1}
         {:x 0 :y 3}
         {:x 1 :y 2}
         {:x 1 :y 3}
         {:x 3 :y 2}
         {:x 5 :y 3}
         {:x 6 :y 3}
         {:x 7 :y 3}})
