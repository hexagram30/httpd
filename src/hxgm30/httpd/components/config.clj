(ns hxgm30.httpd.components.config
  (:require
    [clojure.string :as string]
    [com.stuartsierra.component :as component]
    [hxgm30.httpd.config :as config]
    [taoensso.timbre :as log])
  (:import
    (clojure.lang Symbol)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- get-cfg
  [system]
  (->> [:config :data]
       (get-in system)
       (into {})))

(defn resolve-fully-qualified-fn
  [^Symbol fqfn]
  (when fqfn
    (try
      (let [[name-sp fun] (mapv symbol (string/split (str fqfn) #"/"))]
        (require name-sp)
        (var-get (ns-resolve name-sp fun)))
      (catch  Exception _
        (log/warn "Couldn't resolve one or more of" fqfn)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Config Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn api-version
  [system]
  (get-in (get-cfg system) [:httpd :api :version]))

(defn api-version-dotted
  [system]
  (str "." (api-version system)))

(defn api-vendor
  [system]
  (get-in (get-cfg system) [:httpd :api :vendor]))

(defn default-content-type
  [system]
  (get-in (get-cfg system) [:httpd :default-content-type]))

(defn http-assets
  [system]
  (get-in (get-cfg system) [:httpd :assets]))

(defn http-docs
  [system]
  (get-in (get-cfg system) [:httpd :docs]))

(defn http-port
  [system]
  (get-in (get-cfg system) [:httpd :port]))

(defn http-index-dirs
  [system]
  (get-in (get-cfg system) [:httpd :index-dirs]))

(defn http-skip-static
  [system]
  (get-in (get-cfg system) [:httpd :skip-static]))

(defn api-routes
  [system]
  (resolve-fully-qualified-fn
    (get-in (get-cfg system) [:httpd :route-fns :api])))

(defn site-routes
  [system]
  (resolve-fully-qualified-fn
    (get-in (get-cfg system) [:httpd :route-fns :site])))

(defn log-color?
  [system]
  (get-in (get-cfg system) [:logging :color]))

(defn log-level
  [system]
  (get-in (get-cfg system) [:logging :level]))

(defn log-nss
  [system]
  (get-in (get-cfg system) [:logging :nss]))

(defn streaming-heartbeat
  [system]
  (get-in (get-cfg system) [:streaming :heartbeat]))

(defn streaming-timeout
  [system]
  (get-in (get-cfg system) [:streaming :timeout]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Config [data])

(defn start
  [this]
  (log/info "Starting config component ...")
  (log/debug "Started config component.")
  (let [cfg (config/data)]
    (log/trace "Built configuration:" cfg)
    (assoc this :data cfg)))

(defn stop
  [this]
  (log/info "Stopping config component ...")
  (log/debug "Stopped config component.")
  this)

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend Config
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (map->Config {}))
