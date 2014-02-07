(ns ialab.messaging
  (:require [clojure.tools.logging :refer (debug info)]
            [clj-time.core      :as tc]
            [immutant.daemons   :as daemon]
            [immutant.messaging :as msg]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Queues

(defn- start-queues [queue-prefix queue-count]
  (let [queues (map #(str queue-prefix %) (range queue-count))]
    (doseq [q queues] (msg/start q))
    queues))

(defn- stop-queues [queues]
  (doseq [q queues] (msg/stop q))
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Workers

(def ^:private workers (atom {}))

;; current gateway code
(defrecord QueueWorker [queue worker-fn]
  daemon/Daemon
  (start [_]
    (swap! workers assoc queue (msg/listen queue worker-fn :xa false)))
  (stop [_]
    (info "Stopping" queue "worker")
    (let [listener (get @workers queue)]
      (msg/unlisten listener))
    (swap! workers assoc queue nil)
    (info "Stopped" queue "worker")))

;; Register the daemon

(defn- start-workers [queues f]
  (doall (map-indexed (fn [i queue]
                        (let [worker-name (str "queue-worker-" i)]
                          (info "Create Queue Worker" worker-name)
                          (daemon/create worker-name
                                         (->QueueWorker queue f)
                                         :singleton true)))
                      queues)))

(defn- stop-workers [workers]
  (doseq [w workers] (daemon/stop w))
  nil)

;; Work functions (simulating actual work)
(defn- log-message
  "Log message and sleep for 3 seconds."
  [message]
  (info "Received: " message)
  (Thread/sleep 3000))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Lifecycle

(defn start
  "Start"
  [{:keys [queue-count queue-prefix] :as system}]
  (info "Initializing messaging.")
  (let [queues  (start-queues queue-prefix queue-count)
        workers (start-workers queues log-message)]
    (info "Messaging started.")
    (-> system
        (assoc :messaging {:queues queues :workers workers}))))

(defn stop
  [{:keys [messaging] :as system}]
  (info "Terminating messaging.")
  (stop-workers (:workers messaging))
  (stop-queues (:queues messaging))
  (dissoc system :messaging))
