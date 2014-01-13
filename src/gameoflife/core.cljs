(ns gameoflife.core
  (:require-macros [dommy.macros :refer [node sel sel1]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [dommy.core :as dommy]
            [cljs.core.async :refer [<! put! chan mult tap timeout]]
            [monet.canvas :as canvas]))

(def state (atom {:dimensions {:x [0 0]
                               :y [0 0]}
                  :canvas-size {:w 500
                                :h 500}
                  :cell-size 16
                  :speed 10}))

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

(defn debounce [src ms]
  (let [out (chan)]
    (go-loop []
             (let [first (<! src)
                   done (timeout ms)]
               (loop [msg first]
                 (let [[next-msg ch] (alts! [src done])]
                   (if (= ch src)
                     (recur next-msg)
                     (put! out msg)))))
             (recur))
    out))

(def ^:dynamic *arrow-left* 37)
(def ^:dynamic *arrow-up* 38)
(def ^:dynamic *arrow-right* 39)
(def ^:dynamic *arrow-down* 40)

(def keydowns (chan))
(def keydowns-mult (mult keydowns))

(defn publish-keydowns []
  (dommy/listen! js/window :keydown #(put! keydowns (.-which %))))

(defn adjust-cell-size! []
  (let [c (chan)
        min-size 1
        max-size 128]
    (tap keydowns-mult c)
    (go-loop []
             (let [old-size (:cell-size @state)
                   key (<! c)
                   new-size (cond
                             (= key *arrow-up*) (* old-size 2)
                             (= key *arrow-down*) (/ old-size 2))]
               (when (and new-size (>= new-size min-size) (<= new-size max-size))
                 (swap! state assoc-in [:cell-size] new-size))
               (recur)))))

(defn adjust-speed! []
  (let [c (chan)
        min-speed 1
        max-speed 60]
    (tap keydowns-mult c)
    (go-loop []
             (let [old-speed (:speed @state)
                   key (<! c)
                   new-speed (cond
                              (= key *arrow-left*) (- old-speed 2)
                              (= key *arrow-right*) (+ old-speed 2))]
               (when (and new-speed (>= new-speed min-speed) (<= new-speed max-speed))
                 (swap! state assoc-in [:speed] new-speed))
               (recur)))))

(def window-resizes (chan))

(defn set-canvas-size! [canvas {:keys [w h] :as size}]
  (swap! state assoc-in [:canvas-size] size)
  (dommy/set-attr! canvas :width w)
  (dommy/set-attr! canvas :height h))

(defn adjust-to-window-size! [canvas]
  (let [resizes (debounce window-resizes 500)]
    (go-loop []
             (let [new-size (<! resizes)]
               (set-canvas-size! canvas new-size)
               (recur)))))

(defn get-window-size []
  {:w (aget js/window "innerWidth")
   :h (aget js/window "innerHeight")})

(defn publish-window-resizes []
  (dommy/listen! js/window :resize #(put! window-resizes (get-window-size))))

(defn clear! [ctx {:keys [w h]}]
  (canvas/clear-rect ctx {:x 0 :y 0 :w w :h h}))

(defn render! [ctx cells]
  (let [{:keys [w h]} (:canvas-size @state)
        x-center (Math/round (/ w 2))
        y-center (Math/round (/ h 2))
        cell-size (:cell-size @state)]
    (clear! ctx {:w w :h h})
    (doseq [{:keys [x y state]} cells
            :let [x-start (+ x-center (* cell-size x))
                  y-start (+ y-center (* cell-size y))]]
      (-> ctx
          (canvas/fill-style (if (= state :alive) "#555" "#f00"))
          (canvas/fill-rect {:x x-start :y y-start :w cell-size :h cell-size})))))

(defn start [first]
  (let [canvas (sel1 :.game-of-life)
        ctx (.getContext canvas "2d")]
    (publish-keydowns)
    (adjust-cell-size!)
    (adjust-speed!)
    (publish-window-resizes)
    (set-canvas-size! canvas (get-window-size))
    (adjust-to-window-size! canvas)
    (go-loop [cells first]
             (render! ctx cells)
             (<! (timeout (/ 1000 (:speed @state))))
             (recur (next-gen cells)))))

(start #{{:x -1 :y -1}
         {:x -2 :y 1}
         {:x -1 :y 0}
         {:x -1 :y 1}
         {:x 1 :y 0}
         {:x 3 :y 1}
         {:x 4 :y 1}
         {:x 5 :y 1}})
