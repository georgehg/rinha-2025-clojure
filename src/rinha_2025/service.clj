(ns rinha-2025.service
  (:require
   [rinha-2025.db.postgres :refer [insert-payment-processing-request
                                   payments-processing-summary
                                   purge-payments-processing-data]]
   [rinha-2025.processors-client :refer [send-payment-default]])
  (:import
   [java.time ZonedDateTime]))

(defn request-payment-processing
  [payment-data]
  (insert-payment-processing-request payment-data)
  (send-payment-default payment-data))

(defn payments-summary-by-processor
  [start-date end-date]
  (->> (payments-processing-summary (str (ZonedDateTime/parse start-date))
                                    (str (ZonedDateTime/parse end-date)))
       (reduce (fn [summary totals]
                 (assoc summary
                        (-> totals :processor keyword)
                        {:totalRequests (:total_requests totals)
                         :totalAmount   (:total_amount totals)}))
               {})))

(defn purge-payments-processing
  []
  (purge-payments-processing-data))
