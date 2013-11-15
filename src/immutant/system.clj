(ns immutant.system
  (:require [clojure.tools.logging :refer (info)]
            [clj-time.core     :as tc]
            [immutant.registry :as registry]
            [ialab.messaging   :as msg]
            [ialab.server      :as server]
            [ialab.web         :as web]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; System Definitions

(defmulti system
  (fn [] (keyword (:environment (registry/get :config)))))

(defmethod system :development
  []
  {:server-port  4444
   :queue-prefix "/queue/ial-"
   :queue-count  3})

(defmethod system :default
  []
  {:server-port  8888
   :queue-prefix "/queue/ial-"
   :queue-count  3})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Lifecycle

(defn start
  "Start up the system.
   Returns an updated instance of the gateway application."
  [system]
  (let [mode (:environment (registry/get :config))
        reload (= "development" mode)]
    (info "Starting system in" mode "mode.")
    (-> system
        (assoc :started-at (tc/now))
        msg/start
        server/start
        web/start)))

(defn stop
  "Cleanly shutdown the system.
   Returns an updated instance of the gateway application."
  [system]
  (let [mode (:environment (registry/get :config))]
    (info "Terminating system in" mode "mode.")
    (-> system
        web/stop
        server/stop
        msg/stop
        (dissoc :started-at))))

(defn init
  "Initialize system."
  []
  (start (system)))
