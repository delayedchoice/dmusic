(ns starting.core
  (:require ;[overtone.core :as o]
            ;[overtone.live]
            [overtone.sc.node :as n]
            [overtone.sc.machinery.server.connection :as conn]
            ))


(defn reset-server-connection []
 (conn/shutdown-server)
 (conn/connect "127.0.0.1" 57110)
)
(conn/connect "127.0.0.1" 57110)

(comment 
  (reset-server-connection))

