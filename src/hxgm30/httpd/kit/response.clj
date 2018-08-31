(ns hxgm30.httpd.kit.response
  "This namespace defines a default set of transform functions suitable for use
  in presenting results to HTTP clients.

  Note that ring-based middleeware may take advantage of these functions either
  by single use or composition."
  (:require
    [cheshire.core :as json]
    [cheshire.generate :as json-gen]
    [clojure.string :as string]
    [hxgm30.httpd.results.errors :as errors]
    [ring.util.http-response :as response]
    [taoensso.timbre :as log])
  (:import
    (java.lang.ref SoftReference))
  (:refer-clojure :exclude [error-handler]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn soft-reference->json!
  "Given a soft reference object and a Cheshire JSON generator, write the
  data stored in the soft reference to the generator as a JSON string.

  Note, however, that sometimes the value is not a soft reference, but rather
  a raw value from the response. In that case, we need to skip the object
  conversion, and just do the realization."
  [obj json-generator]
  (let [data @(if (isa? obj SoftReference)
                (.get obj)
                obj)
        data-str (json/generate-string data)]
    (log/trace "Encoder got data: " data)
    (.writeString json-generator data-str)))

(defn stream->str
  [body]
  (if (string? body)
    body
    (slurp body)))

(defn parse-json-body
  [body]
  (let [str-data (stream->str body)
        json-data (json/parse-string str-data true)]
    (log/trace "str-data:" str-data)
    (log/trace "json-data:" json-data)
    json-data))

(defn json-errors
  [body]
  (:errors (parse-json-body body)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Global operations   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; This adds support for JSON-encoding the data cached in a SoftReference.
(json-gen/add-encoder SoftReference soft-reference->json!)


(defn ok
  [_request & args]
  (response/ok args))

(defn not-found
  [_request]
  (response/content-type
   (response/not-found "Not Found")
   "text/html"))

(defn cors
  [request response]
  (case (:request-method request)
    :options (-> response
                 (response/content-type "text/plain; charset=utf-8")
                 (response/header "Access-Control-Allow-Origin" "*")
                 (response/header "Access-Control-Allow-Methods" "POST, PUT, GET, DELETE, OPTIONS")
                 (response/header "Access-Control-Allow-Headers" "Content-Type")
                 (response/header "Access-Control-Max-Age" "2592000"))
    (response/header response "Access-Control-Allow-Origin" "*")))

(defn add-header
  [response field value]
  (assoc-in response [:headers (if (string? field) field (name field))] value))


(defn version-media-type
  [response value]
  (add-header response :x-media-type value))

(defn errors
  [errors]
  {:errors errors})

(defn error
  [error]
  (errors [error]))

(defn not-allowed
  ([message]
   (not-allowed message []))
  ([message other-errors]
   (-> (conj other-errors message)
       errors
       json/generate-string
       response/forbidden
       (response/content-type "application/json"))))

(defn error-handler
  ([status headers body]
    (error-handler
      status
      headers
      body
      (format "Unexpected error (%s)." status)))
  ([status headers body default-msg]
    (let [ct (:content-type headers)]
      (log/trace "Headers:" headers)
      (log/trace "Content-Type:" ct)
      (log/trace "Body:" body)
      (cond (nil? ct)
            (do
              (log/error body)
              {:errors [body]})

            (string/starts-with? ct "application/json")
            (let [errs (json-errors body)]
              (log/error errs)
              {:errors errs})

            :else
            {:errors [default-msg]}))))

(defn client-handler
  ([response]
    (client-handler response error-handler))
  ([response err-handler]
    (client-handler response err-handler identity))
  ([{:keys [status headers body error]} err-handler parse-fn]
    (log/debug "Handling client response ...")
    (cond error
          (do
            (log/error error)
            {:errors [error]})

          (>= status 400)
          (err-handler status headers body)

          :else
          (-> body
              stream->str
              ((fn [x] (log/trace "headers:" headers)
                       (log/trace "body:" x) x))
              parse-fn))))

(def json-handler #(client-handler % error-handler parse-json-body))

(defn process-ok-results
  [data]
  {;; :headers {...}
   :status 200})

(defn process-err-results
  [data]
  (cond (errors/any-server-errors? data)
        {:status errors/server-error-code}

        (errors/any-client-errors? data)
        {:status errors/client-error-code}

        :else
        {:status errors/default-error-code}))

(defn process-results
  [data]
  (if (:errors data)
    (process-err-results data)
    (process-ok-results data)))

(defn json
  [_request data]
  (log/trace "Got data for JSON:" data)
  (-> data
      process-results
      (assoc :body (json/generate-string data))
      (response/content-type "application/json")))

(defn text
  [_request data]
  (-> data
      process-results
      (assoc :body data)
      (response/content-type "text/plain")))

(defn html
  [_request data]
  (-> data
      process-results
      (assoc :body data)
      (response/content-type "text/html")))
