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
            [overtone.music.time :as time]
            [starting.core :as app]
            [overtone.at-at :as at]
            ))
(def the-synth (atom nil))
(comment
  (conn/connect "127.0.0.1" 57110 )
  (conn/shutdown-server))
(comment
  (app/reset))

(def my-pool (at/mk-pool))
(comment 
  (meta #'overtone.core/now)
  (ru/odoc srv/at)
  )

(sy/defsynth one
  "first class of https://www.youtube.com/playlist?list=PLPYzvS8A_rTZmJZjUtMG6GJ2QkLUEaY4Q."
  [note                {:default 60   :min 0   :max 127   :step 1}
   dur                 {:default 1.0  :min 0.0 :max 10.0   :step 0.1}
   fade                {:default 1 :min 0 :max 1 :step 1}
   ]
  (let [env (u/env-gen (env/adsr 0 0 0.9 4))
        fade-target (if fade 0 1)
        _ (println-str "fade-target: " fade-target)
        sig (as-> note $
              (u/midicps $) 
              (- $ (* 1 (u/sin-osc 0.5) )) ;vib
              (+ (u/saw $) (u/sin-osc $)) 
              (* $ env) ;adsr
              (u/softclip $)
              (* $ (u/line 1 fade-target (+ 1.5 dur) :action u/FREE )) ;fade
              (u/lpf $ 800) ;low pass filter
              (* $ 0.5)
              [$  $])]
    (u/out 0 sig)))

(comment
  (srv/at (+ (time/now) (* 1000 4)) (one))
  (one :wait 4))

(defn play-phrase [phrase]
  (let [phrase-with-waits (map #(vector (first %1) (second %1) %2 %3) phrase (into [0] (map second phrase)) (into (map #(* 0 (first %) ) phrase) [1]) ) ] 
    (doseq [[note dur wait fade] phrase-with-waits]
      (srv/at (+ (time/now) (* 1000 wait)) (one :note note :dur dur :fade fade))
     )))

(play-phrase [[62 1] [64 4]])
;siren
;env (overtone/env-gen (overtone/adsr attack decay sustain release level curve)
;                              :gate gate
;                              :action overtone/FREE)]
( #(true? true) )
(comment
  (sy/demo 8 (let [dur 4
                  env (abs (u/lf-saw :freq (/ 1 dur)  ))]
              ;(u/line:kr 0 1 dur :action u/FREE)
              (* env (u/saw 220)))))

(comment
  (sy/demo 4
          (let [env (u/env-gen (env/adsr 0 0 0.9 1)
                              ; :gate 1
                              ; :action u/FREE
                               )
                ] 

          )))

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
