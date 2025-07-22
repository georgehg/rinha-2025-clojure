(ns rinha-2025.processors-client 
   (:require
    [cheshire.core :as json]
    [clj-http.client :as client]
    [clj-http.conn-mgr :as conn-mgr]
    [clj-http.core :as http]))

(def ^:private conn-manager
  (conn-mgr/make-reusable-conn-manager {:timeout 5 :threads 10 :default-per-route 5}))

(def ^:private http-client
  (http/build-http-client {} false conn-manager))

(defn create-processors-client
  [config]
  {:default-request {:socket-timeout     10000
                     :connection-timeout 10000
                     :throw-exceptions   false
                     :http-client        http-client
                     :connection-manager conn-manager}
   :default-processor-url  (-> config :payment-processors :default-host)
   :fallback-processor-url (-> config :payment-processors :fallback-host)
   :health-endpoint        (-> config :payment-processors :endpoints :health)
   :payment-endpoint       (-> config :payment-processors :endpoints :payment)})

(defn get-default-processor-health
  [{:keys [default-processor-url health-endpoint default-request]}]
  (-> (client/get (str default-processor-url health-endpoint)
              (assoc default-request
                     :accept :json
                     :as     :json))
      (select-keys [:status :body])))

(defn get-fallback-processor-health
  [{:keys [fallback-processor-url health-endpoint default-request]}]
  (-> (client/get (str fallback-processor-url health-endpoint)
              (assoc default-request
                     :accept :json
                     :as     :json))
      (select-keys [:status :body])))

(defn send-payment-default
  [{:keys [default-processor-url payment-endpoint default-request]} payment-data]
  (-> (client/post (str default-processor-url payment-endpoint)
               (assoc default-request
                      :body         (json/encode payment-data)
                      :content-type :json))
      (select-keys [:status :body])))

(defn send-payment-fallback
  [{:keys [fallback-processor-url payment-endpoint default-request]} payment-data]
  (-> (client/post (str fallback-processor-url payment-endpoint)
                   (assoc default-request
                          :body         (json/encode payment-data)
                          :content-type :json))
      (select-keys [:status :body])))
