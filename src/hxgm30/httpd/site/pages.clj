(ns hxgm30.httpd.site.pages
  "The functions of this namespace are specifically responsible for returning
  ready-to-serve pages."
  (:require
   [hxgm30.httpd.site.data :as data]
   [ring.util.response :as response]
   [selmer.parser :as selmer]
   [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Page Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn render-template
  "A utility function for preparing templates."
  [template page-data]
  (response/response
   (selmer/render-file template page-data)))

(defn render-html
  "A utility function for preparing HTML templates."
  [template page-data]
  (response/content-type
   (render-template template page-data)
   "text/html"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   HTML page-genereating functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn home
  "Prepare the home page template."
  [request data]
  (render-html
   "templates/home.html"
   (data/base-dynamic data)))

(defn not-found
  "Prepare the home page template."
  ([request]
    (not-found request {:base-url "/"}))
  ([request data]
    (render-html
     "templates/not-found.html"
     (data/base-dynamic data))))
