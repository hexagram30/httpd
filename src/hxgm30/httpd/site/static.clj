(ns hxgm30.httpd.site.static
  "The functions of this namespace are specifically responsible for generating
  the static resources of the top-level and site pages and sitemaps."
  (:require
   [clojure.java.io :as io]
   [clojusc.twig :as logger]
   [com.stuartsierra.component :as component]
   [hxgm30.httpd.components.config :as config]
   [hxgm30.httpd.components.core :as components]
   [hxgm30.httpd.site.data :as data]
   [selmer.parser :as selmer]
   [taoensso.timbre :as log]
   [trifl.java :as trifl])
  (:gen-class))

(logger/set-level! '[hxgm30.httpd] :info)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn generate
  "This is the function used by default to render templates, given data that
  the template needs to render."
  [target template-file data]
  (log/debug "Rendering data from template to:" target)
  (log/debug "Template:" template-file)
  (log/debug "Data:" data)
  (io/make-parents target)
  (->> data
       (selmer/render-file template-file)
       (spit target)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Content Generators   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn generate-all
  "A convenience function that pulls together all the static content generators
  in this namespace. This is the function that should be called in the parent
  static generator namespace."
  [docs-source docs-dir base-url]
  (log/debug "Generating static site files ...")
  ;; Add function calls here ...
  )

(defn -main
  [& args]
  (let [system-init (components/init :basic)
        system (component/start system-init)]
    (trifl/add-shutdown-handler #(component/stop system))
    ;; (generate-all
    ;;   (config/http-... system)
    ;;   (config/http-... system)
    ;;   (config/http-... system))
    ))
