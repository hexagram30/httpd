(ns hxgm30.httpd.config
  (:require
   [hxgm30.common.file :as file]
   [hxgm30.common.util :as util]))

(def config-file "hexagram30-config/httpd.edn")

(defn data
  ([]
    (data config-file))
  ([filename]
   (file/read-edn-resource filename)))
