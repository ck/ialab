(ns ialab.web
  (:require [clojure.tools.logging :refer [debug info]]
            [immutant.web          :as web])
  (:use [ring.util.response :only [response]]))

;;; Our main web request handler
(defn handler
  [request]
  (debug (format "web [%s]" (:path-info request)))
  (response "OK\n")
  ;(response (format "messages=%s, jobs=%s\n" (:messages cache) (:jobs
  ;cache)))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Lifecycle

(defn start
  [system]
  (info "Initializing web application.")
  (assoc system :web (web/start handler)))

(defn stop [system]
  (info "Terminating web application.")
  (web/stop "/")
  (dissoc system :web))
