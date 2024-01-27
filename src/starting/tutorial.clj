(ns starting.tutorial
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.core]

            ))

(def the-synth (atom nil))

(def foo
  (sy/synth [freq 440 noiseHz 8] 
    (u/out 0
           (* (-> (u/lf-noise1:kr 12)
                  (u/lin-exp:kr -1 1 0.02 1))
              (-> 
                  (u/lf-noise0:kr noiseHz)
                  (u/lin-exp:kr -1 1 200 1000)
                  u/sin-osc)))))

(defn restart []
  (do 
    (n/kill @the-synth)
    (reset! the-synth (foo))))

(comment 
  (restart)
  (n/ctl @the-synth :noiseHz 4)
  (reset! the-synth (foo))
  (n/kill @the-synth)
  )

(comment 
  (srv/stop)
  )

(comment 
  (meta #'overtone.core/kill)
  (ru/odoc n/ctl)
  )








