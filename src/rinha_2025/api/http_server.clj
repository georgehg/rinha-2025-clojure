(ns rinha-2025.api.http-server
  (:require
   [cheshire.core :as json]
   [io.pedestal.connector :as conn]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.cors :as cors]
   [io.pedestal.http.jetty :as jetty]
   [io.pedestal.http.route :as route]
   [io.pedestal.interceptor :as interceptor]
   [io.pedestal.log :as logger]
   [io.pedestal.service.interceptors :as interceptors]
   [rinha-2025.api.routes :as routes])
  (:import
   [java.util.concurrent Executors]
   [org.eclipse.jetty.util.thread QueuedThreadPool]))

(defn- inject-dependencies
  [dependencies]
  (interceptor/interceptor
   {:name  :dependencies-injection
    :enter (fn [context]
             (assoc context :dependencies dependencies))}))

(def ^:private json-output-encoder
  (interceptor/interceptor
   {:name  :json-output-encoder
    :leave (fn [context]
             (-> context
                 (update-in [:response :body] json/encode)
                 (update-in [:response :headers] merge {"Content-Type" "application/json"})))}))

(def ^:private service-interceptors
  [interceptors/log-request
   (cors/allow-origin (constantly true))
   interceptors/not-found
   route/query-params
   (body-params/body-params)
   json-output-encoder])

(defn- create-http-server
  [config deps]
  (let [thread-pool (QueuedThreadPool.)
        _           (.setVirtualThreadsExecutor thread-pool
                                                (Executors/newVirtualThreadPerTaskExecutor))
        {:keys [host port]} (:server config)]
    (-> (conn/default-connector-map host (read-string port))
        (conn/with-default-interceptors)
        (conn/with-interceptors service-interceptors)
        (conn/with-interceptor (inject-dependencies deps))
        (conn/with-routes routes/routes)
        (jetty/create-connector {:container-options {:thread-pool thread-pool}}))))

(defonce *connector (atom nil))

(defn start-server
  [config dependencies]
  (reset! *connector
          (conn/start! (create-http-server config dependencies)))
  (logger/info :started :http-server :port (-> config :server :port)))

(defn stop-server
  []
  (when @*connector
    (conn/stop! @*connector)
    (reset! *connector nil)))
