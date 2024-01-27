(ns starting.core
  (:require ;[overtone.core :as o]
            ;[overtone.live]
            [overtone.sc.machinery.server.connection :as conn]
            ))

;(o/demo (o/sin-osc))

;(o/definst foo [] (o/saw 220))
;(foo)
;(o/kill foo)


;(o/odoc o/saw)
(defonce c (conn/connect "127.0.0.1" 57110))
(comment 
  (o/stop)

(defn reset []
 (conn/shutdown-server)
 (conn/connect "127.0.0.1" 57110)
 )

(o/definst trem [freq 440 depth 10 rate 6 length 3]
    (* 0.3
       (o/line:kr 0 1 length o/FREE)
       (o/saw (+ freq (* depth (o/sin-osc:kr rate))))))
;(trem)

(o/definst sawzall [freq 261.63 length 1]
  (* 1 #_(o/env-gen (o/perc 0.1 0.8) :action o/FREE)
     (o/line:kr 1 1 length o/FREE)
     (o/saw freq)))

;(sawzall)


(macroexpand (o/definst saw-wave [freq 261.3 attack 0.005 decay 0.1 sustain 0.9 release 1 vol 0.4] 
                (* (o/env-gen (o/adsr attack decay sustain release) 1 1 0 1 o/FREE)
                   (o/saw freq)
                   vol)))
  )

;(saw-wave)

