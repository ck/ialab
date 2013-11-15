(ns ialab.server
  (:require [clojure.tools.logging :refer [error
                                           info
                                           warn]]
            [clj-time.core :as t]
            [gloss.core :refer [string]]
            [lamina.core :refer [enqueue-and-close
                                 pipeline
                                 read-channel
                                 run-pipeline
                                 restart
                                 complete]]
            [aleph.tcp :refer [start-tcp-server]]
            [immutant.messaging :as msg]
            [immutant.registry :as registry]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; TCP Server

(defn- trace
  [id]
  (fn [{:keys [remote-ip received-at raw-data] :as m}]
    (info (str "[" id "] Remote IP   : " remote-ip))
    (info (str "[" id "] Received At : " received-at))
    (info (str "[" id "] Data        : " raw-data))
    m))

(defn- read-data
  "Read the data from the channel and
   adds it to :raw-data in the passed in map."
  [channel]
  (pipeline
   (fn [m] (run-pipeline (read-channel channel)
                         #(assoc m :raw-data %)))))

(defn- process-data
  [queue-prefix queue-count]
  (fn [{:keys [raw-data] :as m}]
    (let [msg-size (count raw-data)
          queue    (str queue-prefix (mod msg-size queue-count))]
      (info "Publish message" raw-data "to queue" queue ".")
      (msg/publish queue raw-data))
    m))

(defn- send-response
  [channel response]
  (fn [m]
    (enqueue-and-close channel response)))

(defn- make-handler
  [{:keys [queue-prefix queue-count] :as system}]
  (fn
    [channel client-info]
    (run-pipeline {:remote-ip   (client-info :address) ; client IP address
                   :received-at (t/now)                ; receive datetime
                   :raw-data    nil}                   ; raw data

                  {:error-handler (fn [ex] (error "error:" ex))}

                  (read-data channel)
                  (trace "DATA")
                  (process-data queue-prefix queue-count)
                  (send-response channel "OK\n"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Lifecycle

(defn start
  "Takes a system with a server-port, returns a running server."
  [{:keys [server-port] :as system}]
  (info "Initializing server mode on port" server-port ".")
  (let [handler (make-handler system)
        server  (start-tcp-server handler
                                  {:port server-port
                                   :frame (string :utf-8 :delimiters ["\n"])})]
    (info "Server started.")
    (assoc system :server server)))

(defn stop
  [{:keys [server] :as system}]
  (if server
    (do
      (info "Terminating server.")
      (server) ; Calling Aleph server with no args, stops the server
      (info "Server terminated.")
      (dissoc system :server))
    system))
