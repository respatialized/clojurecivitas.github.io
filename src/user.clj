(ns user
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.clay.v2.snippets :as snippets]))

(defn dev!
  "Start Clay using `scicloj.clay.v2.snippets/watch!`.

 Uses default arguments if no options are provided."
  ([opts] (snippets/watch! opts))
  ([] (dev! {})))
