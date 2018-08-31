(ns hxgm30.httpd.results.core)

(defrecord Results
  [;; The number of results returned
   hits
   ;; Number of milleseconds elapsed from start to end of call
   took
   ;; The actual items in the result set
   items
   ;; Any non-error messages that need to be returned
   warnings])

(defn create
  [results & {:keys [elapsed warnings]}]
  (map->Results
    (merge {:hits (count results)
            :took elapsed
            :items results}
           warnings)))

(defn elided
  [results]
  (when (seq results)
    (assoc results :items [(first (:items results) )"..."])))

(defn remaining-items
  [results]
  (when (seq results)
    (rest (:items results))))
