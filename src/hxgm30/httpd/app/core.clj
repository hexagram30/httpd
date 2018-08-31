(ns hxgm30.httpd.app.core
  (:require
    [clojure.java.io :as io]
    [hxgm30.httpd.app.handler.core :as handler]
    [hxgm30.httpd.app.middleware :as middleware]
    [hxgm30.httpd.components.config :as config]
    [ring.middleware.defaults :as ring-defaults]
    [reitit.ring :as ring]
    [taoensso.timbre :as log]))

(defn main
  [httpd-component]
  (let [api-routes (config/api-routes httpd-component)
        site-routes (config/site-routes httpd-component)]
    (-> httpd-component
        (site-routes)
        (middleware/wrap-api-version-dispatch
           httpd-component
           api-routes
           (middleware/reitit-auth httpd-component))
        middleware/wrap-log-request
        (ring-defaults/wrap-defaults ring-defaults/api-defaults)
        (middleware/wrap-resource httpd-component)
        middleware/wrap-trailing-slash
        middleware/wrap-cors
        (middleware/wrap-not-found httpd-component))))
