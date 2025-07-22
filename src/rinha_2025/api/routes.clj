(ns rinha-2025.api.routes
  (:require
   [rinha-2025.processors-client :refer [send-payment-default]]))

(defn- handle-payments-processing-fn
  [context]
  (let [payment-data          (-> context :request :json-params)
        {:keys [status body]} (send-payment-default payment-data)]

    (assoc context :response
           {:status status
            :body  body ;"payment received successfully"
            })))

(defn- handle-get-payments-summary-fn
  [context]
  (assoc context :response
         {:status 200
          :body   {:default {:totalRequests  43236
                             :totalAmount    415542345.98}
                   :fallback {:totalRequests 423545
                              :totalAmount   329347.34}}}))

(defn- handle-post-purge-payments-fn
  [context]
  (assoc context :response
         {:status 200
          :body  "payments database purged successfully"}))

(def handle-payments-processing
  {:name :handle-payments-processing
   :enter handle-payments-processing-fn})

(def handle-get-payments-summary
  {:name :handle-get-payments-summary
   :enter handle-get-payments-summary-fn})

(def handle-post-purge-payments
  {:name :handle-post-purge-payments
   :enter handle-post-purge-payments-fn})

(def routes #{["/payments"         :post handle-payments-processing  :route-name :post-payments-processing]
              ["/payments-summary" :get  handle-get-payments-summary :route-name :get-payments-summary]
              ["/purge-payments"   :post handle-post-purge-payments  :route-name :post-purge-payments]})
