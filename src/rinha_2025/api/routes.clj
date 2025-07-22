(ns rinha-2025.api.routes
  (:require
   [rinha-2025.service :as service]))

(defn- handle-payments-processing-fn
  [context]
  (let [payment-data (-> context :request :json-params)]
    (service/request-payment-processing payment-data)
    (assoc context :response
           {:status 200
            :body   "payment received successfully"})))

(defn- handle-get-payments-summary-fn
  [context]
  (let [{:keys [from to]} (-> context :request :query-params)
        result (service/payments-summary-by-processor from to)]
    (assoc context :response
           {:status 200
            :body   result
            #_{:default {:totalRequests  43236
                         :totalAmount    415542345.98}
               :fallback {:totalRequests 423545
                          :totalAmount   329347.34}}})))

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
