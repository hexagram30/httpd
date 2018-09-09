(ns hxgm30.httpd.components.server
  (:require
    [com.stuartsierra.component :as component]
    [hxgm30.httpd.app.core :as app]
    [hxgm30.httpd.components.config :as config]
    [org.httpkit.server :as server]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord HTTPD [
  server])

(defn start
  [this]
  (log/info "Starting httpd component ...")
  (let [port (config/http-port this)
        server (server/run-server (app/main this) {:port port})]
    (log/debugf "HTTPD is listening on port %s" port)
    (log/debug "Started httpd component.")
    (assoc this :server server)))

(defn stop
  [this]
  (log/info "Stopping httpd component ...")
  (if-let [server (:server this)]
    (server))
  (assoc this :server nil))

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend HTTPD
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (map->HTTPD {}))
