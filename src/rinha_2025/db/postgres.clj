(ns rinha-2025.db.postgres
  (:require
   [io.pedestal.log :as logger]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql])
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

(defonce *connection (atom nil))

(defn db-connect
  [config]
  (let [db-spec (config->db-spec (:postgres config))
        connection (connection/->pool HikariDataSource db-spec)]
    (execute-migration (config->migration-spec connection (:migration config)))
    (logger/info :started :postgres-connection :database (-> config :postgres :database))
    (reset! *connection connection)))

(defn db-disconnect
  []
  (when @*connection
    (.close @*connection)
    (reset! *connection nil)
    (logger/info :stopped :postgres-connection)))

(defn insert-payment-processing-request
  [{:keys [correlationId amount]}]
  (sql/insert! @*connection
               :payments_processing
               {:correlation_id correlationId
                :amount         amount
                :status         "REQUESTED"}))

(defn update-payment-processing-status
  [id processor status]
  (sql/update! @*connection
               :payments_processing
               {:status status
                :processor processor}
               {:processing_id id}))

(defn payments-processing-summary
  [start-date end-date]
  (sql/query @*connection
             ["select processor, count(*) as total_requests, sum(amount) as total_amount
               from payments_processing
               where requested_at between ?::timestamp and ?::timestamp
               group by processor"
              start-date end-date]
             {:builder-fn rs/as-unqualified-lower-maps}))

(defn purge-payments-processing-data
  []
  (sql/query @*connection
             ["truncate table payments_processing"]))
