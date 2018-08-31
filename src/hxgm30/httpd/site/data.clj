(ns hxgm30.httpd.site.data
  "The functions of this namespace are specifically responsible for generating
  data structures to be consumed by site page templates.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Data Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-title "Generic Hexagram30 Web Site")
(def default-data {:app-title default-title})

(defn base-dynamic
  "Data that all pages have in common.

  Note that dynamic pages need to provide the base-url."
  ([]
   (base-dynamic {}))
  ([data]
   (merge default-data
          data)))
