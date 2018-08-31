(defn get-banner
  []
  (try
    (str
      (slurp "resources/text/banner.txt")
      ;(slurp "resources/text/loading.txt")
      )
    ;; If another project can't find the banner, just skip it;
    ;; this function is really only meant to be used by Dragon itself.
    (catch Exception _ "")))

(defn get-prompt
  [ns]
  (str "\u001B[35m[\u001B[34m"
       ns
       "\u001B[35m]\u001B[33m λ\u001B[m=> "))

(defproject hexagram30/httpd "0.1.0-SNAPSHOT"
  :description "Web service components, utilities, and wrappers for use by hexagram30 projects"
  :url "https://github.com/hexagram30/httpd"
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [cheshire "5.8.0"]
    [clojusc/system-manager "0.3.0-SNAPSHOT"]
    [clojusc/trifl "0.3.0"]
    [clojusc/twig "0.3.3"]
    [com.stuartsierra/component "0.3.2"]
    [http-kit "2.3.0"]
    [hexagram30/common "0.1.0-SNAPSHOT"]
    [metosin/reitit-core "0.1.3"]
    [metosin/reitit-ring "0.1.3"]
    [metosin/ring-http-response "0.9.0"]
    [org.clojure/clojure "1.9.0"]
    [ring/ring-core "1.6.3"]
    [ring/ring-codec "1.1.1"]
    [ring/ring-defaults "0.3.2"]
    [selmer "1.12.0"]]
  :profiles {
    :ubercompile {
      :aot :all
      :source-paths ["test"]}
    :dev {
      :dependencies [
        [clojusc/trifl "0.3.0"]
        [org.clojure/tools.namespace "0.2.11"]]
      :plugins [
        [lein-shell "0.5.0"]
        [venantius/ultra "0.5.2"]]
      :source-paths ["dev-resources/src"]
      :repl-options {
        :init-ns hxgm30.httpd.repl
        :prompt ~get-prompt
        :init ~(println (get-banner))}}
    :lint {
      :source-paths ^:replace ["src"]
      :test-paths ^:replace []
      :plugins [
        [jonase/eastwood "0.2.9"]
        [lein-ancient "0.6.15"]
        [lein-kibit "0.1.6"]]}
    :test {
      :dependencies [
        [clojusc/ltest "0.3.0"]]
      :plugins [
        [lein-ltest "0.3.0"]]
      :test-selectors {
        :unit #(not (or (:integration %) (:system %)))
        :integration :integration
        :system :system
        :default (complement :system)}}}
  :aliases {
    ;; Dev & Testing Aliases
    "repl" ["do"
      ["clean"]
      ["repl"]]
    "ubercompile" ["with-profile" "+ubercompile" "compile"]
    "check-vers" ["with-profile" "+lint" "ancient" "check" ":all"]
    "check-jars" ["with-profile" "+lint" "do"
      ["deps" ":tree"]
      ["deps" ":plugin-tree"]]
    "check-deps" ["do"
      ["check-jars"]
      ["check-vers"]]
    "kibit" ["with-profile" "+lint" "kibit"]
    "eastwood" ["with-profile" "+lint" "eastwood" "{:namespaces [:source-paths]}"]
    "lint" ["do"
      ["kibit"]
      ;["eastwood"]
      ]
    "ltest" ["with-profile" "+test" "ltest"]
    ;; Documentation and static content
    ;; Build tasks
    "build-lite" ["do"
      ["ltest" ":unit"]]
    "build" ["do"
      ["clean"]
      ["ltest" ":unit"]
      ["ubercompile"]
      ["uberjar"]]})
