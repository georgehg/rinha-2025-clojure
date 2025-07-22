(ns rinha-2025.core
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [rinha-2025.api.http-server :as server]
   [rinha-2025.db.postgres :as db]
   [rinha-2025.processors-client :as client]))

(defn- config-file
  [env]
  (read-config (io/resource "config.edn") {:profile env}))

(defn- get-config
  ([]
   (let [profile (or (keyword (System/getenv "ENV"))
                     (keyword (System/getProperty "config.profile"))
                     :local)
         config (config-file profile)]
     config))
  ([opt-profile]
   (config-file opt-profile)))

(defn start-service
  []
  (let [config  (get-config)
        db-conn (db/db-connect config)
        client  (client/setup-processors-client config)]
    (server/start-server config
                         {:processors-client client
                          :database          db-conn})))

(defn stop-service
  []
  (db/db-disconnect)
  (client/shutdown-client)
  (server/stop-server))

(defn -main [& _args]
  (java.util.TimeZone/setDefault (java.util.TimeZone/getTimeZone "UTC"))
  (start-service)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(stop-service))))

(comment
  (do   (java.util.TimeZone/setDefault (java.util.TimeZone/getTimeZone "UTC"))
        (load-file "src/rinha_2025/core.clj")
        (stop-service)
        (start-service)
        nil))
