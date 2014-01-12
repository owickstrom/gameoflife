(ns gameoflife.core
  (:require-macros [dommy.macros :refer [node sel sel1]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [dommy.core :as dommy]
            [cljs.core.async :refer [<! timeout]]))

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
        ys (map second cells)
        x-axis (range (apply min xs) (inc (apply max xs)))
        y-axis (range (apply min ys) (inc (apply max ys)))]
    [x-axis y-axis]))

(defn html-table [cells]
  (let [[xs ys] (table-dimensions cells)]
    (node [:table
           (map (fn [y]
                  [:tr (map (fn [x]
                              [:td (if (cells [x y]) "☺" " ")])
                            xs)])
                ys)])))

(defn render-table [cells]
  (dommy/set-html! (sel1 :.game-of-life) "")
  (dommy/append!
   (sel1 :.game-of-life)
   (html-table cells)))

(defn start [first]
  (go-loop [cells first]
           (render-table cells)
           (<! (timeout (/ 1000 3.0)))
           (recur (next-gen cells))))

(start #{[1 1] [1 2] [1 3] [1 4] [1 5]})
