^{:clay {:category    :clojure
         :date        "2026-03-07"
         :description "Documenting thi.ng/geom through examples"
         :quarto      {:author :respatialized}
         :tags        [:geometry :graphics]
         :title       "thi.ng/geom Documentation"}
  :kindly/hide-code true}
(ns geom.docs
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.svg.adapter]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.svg.adapter :as adapt]
            [thi.ng.geom.vector :as v :refer [vec2]]
            [thi.ng.color.core :as col]
            [thi.ng.geom.rect :as rect]
            [thi.ng.geom.line :as line]
            [thi.ng.geom.triangle :as tri]
            [thi.ng.geom.types :as types]
            [thi.ng.color.gradients :as grad]
            [thi.ng.math.core :as m]
            [thi.ng.math.macros :as mm]
            [fastmath.easings :as easings]
            ;   [respatialized.geometry :as geometry]
            [clojure.string :as str]
            [clojure.pprint :as pprint]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]
            [clojure.set :as set])
  (:import [thi.ng.geom.types Path2 Bezier2 Circle2 Ellipse2 Line2 LineStrip2
            Polygon2 Rect2 Triangle2]
           [thi.ng.geom.vector Vec2]))

(declare tri-svg-animation)

^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:style #_(list [:.annotation {:display "none"}])
 ".annotation {display: none;}
.annotation:hover {display: content;}
.doc-grid {
 display: grid;
 grid-auto-columns: minmax(20px, 25ch);
}
.doc-grid h1,h2,h3,h4,h5,h6 {grid-column: 1 / span 5;}
.doc-grid p {grid-column: 1 / span 3;}
.doc-grid dl {display: grid; grid-template-columns: subgrid;
grid-column: 1 / span 5;}

"]

^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:h1 {:class "big wide"} "thi.ng/geom"]


^{:kindly/hide-code true :kindly/kind :kind/hiccup} tri-svg-animation

(defn display-expr [e] (kind/code (with-out-str (pprint/pprint e))))
;; thing-geom test

(defn var-meta->hiccup
  [{:keys [arglists name ns line column file] :as var-meta} src-url]
  (list "arguments:"
        [:br]
        (display-expr (vec arglists))
        #_[:br]
        [:span "source: "
         [:a {:href (str src-url "/" file "#L" line) :target "_blank"}
          (str file " L" line)]]))

;; the centroid of a 2d vector is the vector
(extend-protocol g/ICenter
 Vec2
   #_(center ([_] _))
   (centroid ([_] _)))

(extend-protocol g/IRotate
 clojure.lang.PersistentVector
   (rotate ([v theta] (mapv (fn [g] (g/rotate g theta)) v))))

(defn translate-from
  [g-obj dist bearing]
  (let [[x y] (g/centroid g-obj)
        Δx    (* dist (Math/sin bearing))
        Δy    (* dist (Math/cos bearing))]
    (g/translate g-obj (v/vec2 Δx Δy))))

(defn get-literal
  [r]
  (let [r-type (symbol (.getName (type r)))]
    (tagged-literal r-type (into {} r))))

(defmethod pprint/simple-dispatch thi.ng.geom.types.Rect2
  [g]
  (pprint/pprint (get-literal g)))


(defn evenly-space-up-to
  [n max pct]
  (let [ext       (* max pct)
        range-ext (map #(/ % n) (range 0 (inc n)))]
    (map (fn [r] (mm/mix 0 ext r)) range-ext)))




(def github-src-url "https://github.com/thi-ng/geom/tree/feature/no-org/src")

(def hex-colors
  {:white     "#e2e2e2"
   :cool-grey "#597d8e"
   :teal      "#5abeb1"
   :blue      "#5abeb1"
   :pink      "#e788ea"
   :red       "#ff5549"
   :yellow    "#ecd248"
   :black     "#1a1a1a"})

(def thing-colors (update-vals hex-colors col/hex->int))


(def color-gradient-2
  (grad/cosine-gradient 2
                        (grad/cosine-coefficients (:red thing-colors)
                                                  (:blue thing-colors))))
^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:figure
 (let [rect (rect/rect 5 15 900 30)]
   (svg/serialize (svg/svg
                   {:width 930 :height 45 :style {:grid-column "1 / -1"}}
                   (svg/defs (update (apply svg/linear-gradient
                                            "gradient"
                                            {}
                                            (map-indexed (fn [ix c] [ix c])
                                                         color-gradient-2))
                                     1   assoc
                                     :x1 0
                                     :x2 1
                                     :y1 0
                                     :y2 0))
                   (-> rect
                       (adapt/all-as-svg)
                       (assoc-in [1 :fill] "url(#gradient)")))))]

;; computational geometry with Clojure


^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:h4 {:class "wide"} "thi.ng.geom.rect"]

;; Rectangular shape generation

(defn geom->hiccup
  [g]
  (let [lit (get-literal g)]
    [:pre
     [:code {:style {:color "#ff5549" :font-weight 700 :font-size "1.05em"}} "#"
      (:tag lit)] [:br] (display-expr (:form lit))]))

^:kind/hiccup (geom->hiccup (rect/rect 0 0 30 30))

^{:kindly/hide-code true :kindly/kind :kind/hiccup}
(let [pct-range
      #_[0.25 0.3 0.35 0.45 0.67 0.75 0.95 0.99]
      (map (fn [i] (easings/cubic-in (/ i 25))) (range 15 24 0.8))
      rect-count 13
      col-count (count pct-range)
      w 900
      h 700
      max-y (* h 0.95)
      r-width (/ (* w 0.9) col-count)
      r-height 10]
  (svg/serialize
   (svg/svg {:width w :height h}
            (for [[ix pct] (map-indexed vector pct-range)
                  y        (evenly-space-up-to rect-count max-y pct)]
              (let [x (* (/ (* 1.0 w) col-count) ix)
                    r-row-height (/ (* (* max-y pct) 0.75) rect-count)]
                (let [rect-geom (rect/rect x y r-width r-row-height)]
                  [:g
                   (-> rect-geom
                       adapt/all-as-svg
                       (update 1       assoc
                               :stroke "#e6e6e6"
                               :fill   "#1a1a1a"
                               :class  "annotated"
                               :stroke-dasharray "6,2"))
                   [:foreignObject
                    {:class "annotation" :x 15 :y 450 :width 450 :height 300}
                    [:div {:xmlns "http://www.w3.org/1999/xhtml"}
                     (geom->hiccup rect-geom)]]]))))))


(def geom-core-protocols
  (filter
   (fn [[sym var]]
     (let [value (var-get var)]
       (and (map? value) (not (sorted? value)) (some? (:on-interface value)))))
   (ns-publics 'thi.ng.geom.core)))


^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:div {:class "doc-grid"}
 [:h5 [:span {:class "wide"} "Core type:"] [:br]
  [:code
   [:a
    {:href (str github-src-url "/thi/ng/geom/types.cljc#L24") :target "_blank"}
    "thi.ng.geom.types.Rect2"]]]
 [:p
  "a 2d plane with a position, width, and height - specified either by coordinate points or vectors"]
 [:h4
  [:code
   [:a {:href (str github-src-url "/thi/ng/geom/rect.cljc") :target "_blank"}
    "thi.ng.geom.rect"]] " namespace"] [:h5 {:class "wide"} "Functions"]
 [:dl
  (for [[sym v] (ns-publics 'thi.ng.geom.rect)]
    (list [:dt {:style {:grid-column "1 / span 1"}} [:code sym]]
          [:dd {:style {:grid-column "span 3"}}
           (var-meta->hiccup (meta v) github-src-url)
           #_[:pre (display-expr (meta v))]]))]
 [:h5 [:code "thi.ng.geom.core"]
  [:span {:class "wide"} " implemented protocols"]]
 [:dl
  (for [[sym var] (filter (fn [[sym var]]
                            (let [value (var-get var)]
                              (contains? (:impls value)
                                         thi.ng.geom.types.Rect2)))
                          geom-core-protocols)]
    (list [:dt {:style {:grid-column "1 / span 1"}} (display-expr sym)]
          (let [v (var-get var)]
            [:dt {:style {:grid-column "span 3"}}
             (-> v
                 (select-keys [:on :sigs :arglists])
                 (display-expr))])))]]


(comment)



^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:figure {:grid-column "1 / span 4"}
 [:figcaption #_{:style (style {:align-self "end"})}
  [:h4 {:class "wide"} "Grid elements deformed"]]
 (let [ratio     1.618
       rows      20
       grid-w    85
       grid-h    45
       cols      (int (* rows ratio))
       h         1.2
       w         (* h ratio)
       rect-grid (for [x (range 0 grid-w (/ grid-w cols))
                       y (range 0 grid-h (/ grid-h rows))]
                   (rect/rect x y w h))
       r-attrs   {:fill (:white hex-colors)}
       filter    [:filter
                  {:id "texture-filter-1"
                   :filterUnits "userSpaceOnUse"
                   :color-interpolation-filters "sRGB"}
                  [:feImage
                   {:href "/media/7142992537_63cdc95211_o.jpg" :result "FEIMG"}]
                  [:feDisplacementMap
                   {:scale 1.5
                    :in    "SourceGraphic"
                    :xChannelSelector "B"
                    :yChannelSelector "R"}]]
       filter-2  [:filter
                  {:id "texture-filter-2"
                   :filterUnits "userSpaceOnUse"
                   :color-interpolation-filters "sRGB"}
                  [:feImage
                   {:href "/media/7142992537_63cdc95211_o.jpg" :result "FEIMG"}]
                  [:feDisplacementMap
                   {:scale 1.5
                    :in    "SourceGraphic"
                    :xChannelSelector "R"
                    :yChannelSelector "G"}]]]
   (svg/serialize
    (svg/svg {:width   "auto"
              :height  "auto"
              :style   {:grid-column "1 / span 3"}
              :viewBox "0 0 100 100"}
             [:defs filter filter-2]
             (->> rect-grid
                  adapt/all-as-svg
                  (map #(update % 1 merge {:fill (:red hex-colors)}))
                  (reduce conj
                          [:g {:style {:filter "url(#texture-filter-1)"}}]))
             (->> rect-grid
                  adapt/all-as-svg
                  (map #(update % 1 merge {:fill (:teal hex-colors)}))
                  (reduce conj
                          [:g
                           {:style     {:filter "url(#texture-filter-2)"}
                            :transform "translate(2.5  2.5)"}])))))]


^{:kindly/hide-code true :kindly/kind :kind/hiccup}
[:h4 {:class "wide"} "thi.ng.geom.triangle"]

;;2-dimensional and 3-dimensional triangle generation

^{:kindly/hide-code true :kindly/hide-result true}
(def tri-svg-animation
  (let [line      (->> {:pt (vec2 0 0) :dist 18 :bearing (m/radians 270)}
                       (iterate (fn [{:keys [pt dist bearing]}]
                                  {:pt      (translate-from pt dist bearing)
                                   :dist    (+ dist 0)
                                   :bearing (- bearing (m/radians 120))}))
                       (map :pt)
                       (take 3)
                       (types/->LineStrip2))
        line-ct   6
        triangles (map-indexed (fn [ix l]
                                 (let [angle (* (* (/ 1.0 line-ct) ix)
                                                (* Math/PI 2))]
                                   (-> l
                                       (g/center)
                                       (g/rotate angle)
                                       (translate-from 22 angle)
                                       (g/translate (vec2 50 50))
                                       (g/sample-uniform 3 false)
                                       (#(map (fn [pt]
                                                (tri/equilateral2
                                                 pt
                                                 (translate-from pt 18 angle)))
                                              %)))))
                               (repeat line-ct line))]
    (svg/serialize
     (svg/svg {:width   "auto"
               :height  "auto"
               :style   {:grid-column      "1 / span 3"
                         :background-color (:red hex-colors)}
               :viewBox "0 0 100 100"}
              (->> triangles
                   (mapcat identity)
                   (adapt/all-as-svg)
                   (map (fn [t]
                          (update t
                                  1
                                  merge
                                  {:fill            (:red hex-colors)
                                   :stroke          (:black hex-colors)
                                   :stroke-dasharray "12 2"
                                   :stroke-linecap  "round"
                                   :stroke-linejoin "round"
                                   :stroke-width    0.9})))
                   (reduce conj
                           [:g
                            [:animate
                             {:attributeName "stroke-dashoffset"
                              :values        "0;2000"
                              :additive      "sum"
                              :dur           "360s"
                              :repeatCount   "indefinite"}]]))))))

;; more to come!
