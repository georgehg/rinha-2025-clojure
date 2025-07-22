(ns rinha-2025.db.postgres
  (:require
   [io.pedestal.log :as logger]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection])
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(defn- config->migration-spec
  [connection migration-config]
  (merge {:db {:datasource (jdbc/get-datasource connection)}}
         migration-config
         {:store :database}))

(defn- config->db-spec
  [{:keys [database host port username password socket-timeout-seconds maximum-pool-size]}]
  {:dbtype   "postgresql"
   :dbname   database
   :host     host
   :port     port
   :username username
   :password password
   :dataSourceProperties {:socketTimeout   socket-timeout-seconds
                          :maximumPoolSize maximum-pool-size}})

(defn- execute-migration [config]
  (migratus/init config)
  (as-> (migratus/migrate config) completed
    (logger/info :migration-completed (or completed :successful))))

(defonce *database (atom nil))

(defn db-connect
  [config]
  (let [db-spec (config->db-spec (:postgres config))
        connection (connection/->pool HikariDataSource db-spec)]
    (execute-migration (config->migration-spec connection (:migration config)))
    (logger/info :started :postgres-connection :database (-> config :postgres :database))
    (reset! *database connection)))

(defn db-disconnect
  []
  (when @*database
    (.close @*database)
    (reset! *database nil)
    (logger/info :stopped :postgres-connection)))
