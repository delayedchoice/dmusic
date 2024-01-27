(ns starting.dmusic
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.envelope :as env]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.algo.scaling :as scale]
            [overtone.core]
            [overtone.inst.synth :as toy]
            [overtone.sc.cgens.mix :as mix]
            [overtone.sc.cgens.line :as line]
            [overtone.sc.machinery.server.connection :as conn]
            ))
(def the-synth (atom nil))
(comment
  (conn/connect)
  (conn/shutdown-server))

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

;attack: 0.005,
;decay: 0.1,
;release: 1,
;sustain: 0.9,
;siren
;env (overtone/env-gen (overtone/adsr attack decay sustain release level curve)
;                              :gate gate
;                              :action overtone/FREE)]

(comment
  (sy/demo 8 (let [dur 4
                  env (abs (u/lf-saw :freq (/ 1 dur)  ))]
              ;(u/line:kr 0 1 dur :action u/FREE)
              (* env (u/saw 220)))))

(comment
  (sy/demo 4
          (let [env (u/env-gen (env/adsr 0 0 0.9 1)
                               :gate 1
                               :action u/FREE)
                vib (u/sin-osc:kr 0.5)] 
            (as-> 60 $
              (u/midicps $) 
              (- $ (* 1 (u/sin-osc 0.5) ))
              (+ (u/saw $) (u/sin-osc $)) 
              (* $ env)
              (u/softclip $)
              (* $ (u/line 1 0 4 ))
              ( u/lpf $ 800)
              [$  $]))
          ))
(comment
  (sy/demo 4 (let [dur 4
                  env (u/lf-saw :freq (/ 1 dur) :iphase -2 :mul 0.5 :add 1)]
              (u/line:kr 0 1 dur :action u/FREE)
              (* 0.1 env (u/saw 220)))))

(comment 
  (sy/demo 2 (toy/ping)))

(comment
  (sy/demo 20 (let [sines 5
                   speed 6]
               (* 10 (mix/mix
                       (map #(u/pan2 (* (u/sin-osc (* % 100))
                                        (max 0 (+ (u/lf-noise1:kr speed) (u/line:kr 1 -1 20))))
                                     (- (clojure.core/rand 2) 1))
                            (range sines)))
                  (/ 1 sines)))))

(comment 
  (meta #'overtone.core/shutdown-server)
  (ru/odoc u/line)
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
