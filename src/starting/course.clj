(ns starting.course
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.algo.scaling :as scale]
            [overtone.core]
            [overtone.sc.cgens.mix :as mix]
            [overtone.sc.cgens.line :as line]

            ))
(def the-synth (atom nil))

(sy/defsynth one
  "first class of https://www.youtube.com/playlist?list=PLPYzvS8A_rTZmJZjUtMG6GJ2QkLUEaY4Q."
  [ ]
  (let [
        sig (-> 
                    (u/lf-noise0:kr 12)
                    (* 600)
                    (+ 1000)
                    (u/sin-osc 0.3)
                    #_(u/pan2 0))
        ]
    (u/out 0 sig)))
;siren
(sy/demo 2 
         (as-> (u/lf-saw:ar 1) $
               (line/lin-lin:ar $ -1 1 0 500)
               (* 0.01 (u/lf-pulse:ar (+ 200 $) 0.5))))


(comment 
  (meta #'overtone.core/lin-lin)
  (ru/odoc u/lf-saw)
  )


(defn restart []
  (do 
    (n/kill @the-synth)
    (reset! the-synth (one))))

(comment 
  (restart)
  (reset! the-synth (hello-world))
  (n/kill @the-synth)
  )

(comment 
  (srv/stop)
  )

(comment 
  (meta #'overtone.core/mix)
  (ru/odoc u/mul-add)
  )
