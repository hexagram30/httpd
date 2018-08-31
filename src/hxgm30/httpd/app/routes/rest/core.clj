(ns hxgm30.httpd.app.routes.rest.core
  (:require
    [hxgm30.httpd.app.handler.core :as core-handler]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   REST API Routes   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn admin-api
  [httpd-component]
  [["/api/health" {
    :get (core-handler/health httpd-component)
    :options core-handler/ok}]
   ["/api/ping" {
    :get {:handler core-handler/ping
          :roles #{:admin}}
    :post {:handler core-handler/ping
           :roles #{:admin}}
    :options core-handler/ok}]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Testing Routes   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def testing
  [["/api/testing/401" {:get (core-handler/status :unauthorized)}]
   ["/api/testing/403" {:get (core-handler/status :forbidden)}]
   ["/api/testing/404" {:get (core-handler/status :not-found)}]
   ["/api/testing/405" {:get (core-handler/status :method-not-allowed)}]
   ["/api/testing/500" {:get (core-handler/status :internal-server-error)}]
   ["/api/testing/503" {:get (core-handler/status :service-unavailable)}]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Assembled Routes   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn all
  [httpd-component api-version]
  (concat
   (admin-api httpd-component)
   testing))
