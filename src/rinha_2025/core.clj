(ns rinha-2025.core)

(defn start-service
  []
  (println "starting service"))

(defn stop-service
  []
  (println "stopping service"))

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
