(ns hxgm30.httpd.kit.request
  (:require
    [hxgm30.httpd.components.config :as config]
    [org.httpkit.client :as httpc]
    [taoensso.timbre :as log])
  (:refer-clojure :exclude [get]))

(def version-format "application/vnd.%s%s+%s")
(def user-agent
  "Hexagram30/1.0 (+https://github.com/hexagram30/httpd)")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Header Support   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-header
  [req field]
  (get-in req [:headers field]))

(defn add-header
  ([field value]
    (add-header {} field value))
  ([req field value]
    (assoc-in req [:headers field] value)))

(defn add-accept
  ([value]
    (add-accept {} value))
  ([req value]
    (add-header req "Accept" value)))

(defn add-token-header
  ([token]
    (add-token-header {} token))
  ([req token]
    (add-header req "Echo-Token" token)))

(defn add-content-type
  ([ct]
    (add-content-type {}))
  ([req ct]
    (add-header req "Content-Type" ct)))

(defn add-form-ct
  ([]
    (add-form-ct {}))
  ([req]
    (add-content-type req "application/x-www-form-urlencoded")))

(defn add-payload
  ([data]
    (add-payload {} data))
  ([req data]
    (assoc req :body data)))

(defn add-user-agent
  ([]
    (add-user-agent {}))
  ([req]
    (add-header req "User-Agent" user-agent)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   HTTP Client Support   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn options
  [req & opts]
  (apply assoc (concat [req] opts)))

(defn request
  [method url req options callback]
  ;; WARNING: Don't switch the order of options/req below, otherwise
  ;;          request processing will break!
  (httpc/request (-> options
                     (merge req)
                     (assoc :url url :method method)
                     ((fn [x] (log/trace "Options to httpc:" x) x)))
                  callback))

(defn async-get
  ([url]
    (async-get url {}))
  ([url req]
    (async-get url req {}))
  ([url req options]
    (async-get url req options nil))
  ([url req options callback]
    (request :get url req options callback)))

(defn async-post
  ([url]
    (async-post url {:body nil}))
  ([url req]
    (async-post url req {}))
  ([url req options]
    (async-post url req options nil))
  ([url req options callback]
    (request :post url req options callback)))

(defn get
  [& args]
  @(apply async-get args))

(defn post
  [& args]
  @(apply async-post args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Accept Header/Version Support   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def accept-pattern
  "The regular expression for the `Accept` header that may include version
  and parameter information splits into the following groups:
  * type: everything before the first '/' (slash)
  * subtype: everything after the first '/'

  The subtype is then further broken down into the following groups:
  * vendor
  * version (with and without the '.'
  * content-type (with and without the '+' as well as the case where no
    vendor is supplied))

  All other groups are unused."
  (re-pattern "(.+)/((vnd\\.([^.+]+)(\\.(v[0-9.]+))?(\\+(.+))?)|(.+))"))

(def accept-pattern-keys
  [:all
   :type
   :subtype
   :vendor+version+content-type
   :vendor
   :.version
   :version
   :+content-type
   :content-type
   :no-vendor-content-type])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Accept Header/Version Support   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn default-accept
  [system]
  (format version-format
          (config/api-vendor system)
          (config/api-version-dotted system)
          (config/default-content-type system)))

(defn parse-accept
  [system req]
  (->> (or (get-in req [:headers :accept])
           (get-in req [:headers "accept"])
           (get-in req [:headers "Accept"])
           (default-accept system))
       (re-find accept-pattern)
       (zipmap accept-pattern-keys)))

(defn accept-api-version
  [system req]
  (let [parsed (parse-accept system req)
        version (or (:version parsed) (config/api-version system))]
    version))

(defn accept-media-type
  [system req]
  (let [parsed (parse-accept system req)
        vendor (or (:vendor parsed) (config/api-vendor system))
        version (or (:.version parsed) (config/api-version-dotted system))]
    (str vendor version)))

(defn accept-format
  [system req]
  (let [parsed (parse-accept system req)]
    (or (:content-type parsed)
        (:no-vendor-content-type parsed)
        (config/default-content-type system))))
