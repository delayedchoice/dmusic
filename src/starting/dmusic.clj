(ns starting.dmusic
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:import [java.util.concurrent Executors TimeUnit])
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.envelope :as envel]
            [overtone.sc.bus :as bus]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.music.time :as time]
            [overtone.sc.buffer :as buf]
            [overtone.sc.info :as info]   
            [overtone.core :as core]   
            [starting.core :as app]
            ;[clojure.pprint :as pp]
            ))

(def G3 67)
(def B3 71)
(def D4 74)
(def E4 76)
(def G4 79)
(def C5 84)
(def D5 86)
(def E5 88)
(def G5 91)
(def A5 93)

;(defonce right-echo-bus (bus/audio-bus))
;(defonce left-echo-bus (bus/audio-bus))
;
;(defonce main-group (n/group "echo"))
;(defonce right-input-group (n/group "right-input-group" :head main-group))
;(defonce left-input-group (n/group "left-input-group" :head main-group))
;(defonce right-effect-group (n/group "right-effect-group" :after right-input-group))
;(defonce left-effect-group (n/group "left-effect-group" :after left-input-group))

;(n/node-active? nil)
;(bus/bus? nil)
(def server-resources (atom {}))
;(pp/pprint left-input-group )
;(n/node-status main-group)

(defn get-new-audio-bus [old-bus]
  (bus/free-bus old-bus)
  (bus/audio-bus))

(defn get-new-group [old-group nm loc parent]
  (n/node-free old-group)
  (if (keyword? loc)
    (n/group nm loc parent) 
    (n/group nm)))

(defn init []
  (do
    (swap! server-resources assoc :right-echo-bus (bus/audio-bus))
    (swap! server-resources assoc :left-echo-bus (bus/audio-bus))
    (swap! server-resources assoc :main-group (n/group "echo"))
    (swap! server-resources assoc :right-input-group (n/group "right-input-group" :head (@server-resources :main-group)))                        
    (swap! server-resources assoc :left-input-group (n/group "left-input-group" :head (@server-resources :main-group)))
    (swap! server-resources assoc :right-effect-group (n/group "right-effect-group" :after (@server-resources :right-input-group)))
    (swap! server-resources assoc :left-effect-group (n/group "left-effect-group" :after (@server-resources :left-input-group)))))
;RecordBuf.ar(sig, b, recLevel: 1, preLevel: 0.5, loop: 1); 
;sig = PlayBuf.ar(1, b, BufRateScale.kr(b), loop: 1) * 0.25 ! 2;
;(init)
(sy/defsynth echo
  "RecordBuf.ar(sig, b, recLevel: 1, preLevel: 0.5, loop: 1); 
  sig = PlayBuf.ar(1, b, BufRateScale.kr(b), loop: 1) * 0.25 ! 2;
  "
  [
   dur      1.0
   input-bus  0  
   output-bus 0
   factor 1/8
   ]
  (let [sample-rate (info/server-sample-rate)
        b (buf/buffer (* sample-rate  1))
        phs (u/lf-saw :freq (/ 1 (* 5 (:duration b))) :iphase 1)
        writ (u/play-buf:ar :num-channels 1 
                              :bufnum b 
                              :start-pos 0 
                              :loop 1 
                              ;;:loop 1 
                              ;; :trigger 1
                              )
        sig (as-> (u/in:ar input-bus) $
              (+ $ (u/comb-n:ar $ 6 6 (* 16 37 factor) ))
              ;(u/buf-rd:ar :num-channels 1 :bufnum b :phase phs)
              (* $ 0.5)
              )]
    (u/out output-bus sig)))

(comment 
  (meta #'overtone.core/group)
  (ru/odoc u/buf-dur)
  )

(sy/defsynth tone
  "i'm just a tone"
  [note             60
   scaled-duration  1.0
   output-bus       0  
   gate             1
   ]
  (let [envl  (u/env-gen:ar
                (envel/envelope [0.1 1 1 0.001 0]
                                [0.2 scaled-duration 5 1]
                                [1 1 -10 1])
                :action u/FREE)

        sig (as-> note $
              (u/midicps $) 
              (- $ (* 1 (u/sin-osc:kr 0.5) )) ;vib
              (+ (u/var-saw $ :width 0.9) (u/sin-osc $)) 
              (* $ envl) ;adsr
              (u/free-verb $ :mix 0.4 :room 0.7)
              (u/lpf $ 800) ;low pass filter
              (u/softclip $)
              (* $ 0.4)
              )]
    (u/out output-bus sig)))

(defn play-phrase [phrase offset channel input-group time-factor]
  (let [
        first-note (-> phrase first first)
        scaled-first-note (- first-note 12)
        total-duration (reduce + (map second phrase))
        continuations (rest phrase)
        continuations-with-waits (map #(vector (first %1) %2 ) 
                                      continuations
                                      (reductions + (map second phrase))) ;offsets?
        the-synth (tone [:tail input-group] 
                        :note scaled-first-note 
                        :scaled-duration (* time-factor total-duration) 
                        :output-bus channel)
        ] 
    (doseq [[note wait] continuations-with-waits]
      (srv/at (+ (time/now) (* 1000 time-factor (+ wait offset))) 
              (n/ctl the-synth :note (- note 12) )))))

;(defn schedule-phrase [phrase offset channel input-group]
;  (let [time-factor 1/8] 
;    (srv/at (+ (time/now) (* 1000 time-factor offset)) 
;            (play-phrase phrase offset channel input-group time-factor ))))
(defn schedule-phrase [channel input-group phrase offset ]
  (let [time-factor 1/8] 
    (srv/at (+ (time/now) (* 1000 time-factor offset)) 
            (play-phrase phrase offset channel input-group time-factor ))))


(defn play-tune [] 
  (let [{right-echo-bus     :right-echo-bus         
         left-echo-bus      :left-echo-bus         
         right-input-group  :right-input-group 
         left-input-group   :left-input-group } @server-resources]

    (schedule-phrase [[D4 16] [E4 8]] 80 right-echo-bus right-input-group)
    (schedule-phrase [[B3 8] [G3 8] ] 186  right-echo-bus right-input-group)
    (schedule-phrase [[G4 8] ] 376 right-echo-bus right-input-group)

    (schedule-phrase [[C5 8] [D5 16]] 0 left-echo-bus left-input-group)
    (schedule-phrase [[E4 8] ] 96 left-echo-bus left-input-group)
    (schedule-phrase [[G4 8] ] 184 left-echo-bus left-input-group)
    (schedule-phrase [[E5 6][G5 6][A5 6][G5 14]] 304 left-echo-bus left-input-group)
    ))

(defn setup [] 
  (let [
       {right-echo-bus     :right-echo-bus         
         left-echo-bus      :left-echo-bus         
         right-effect-group :right-effect-group 
         left-effect-group  :left-effect-group } @server-resources 
        ]
    (echo [:tail right-effect-group] :dur 1 :input-bus right-echo-bus  :output-bus 1)
    (echo [:tail left-effect-group] :dur 1 :input-bus left-echo-bus  :output-bus 0)) )
(srv/sc-debug-on)
(comment
  (setup)
  (doseq [it (range 1)]
    (let [scheduler (Executors/newScheduledThreadPool 1)
          tune (.scheduleAtFixedRate scheduler 
                                     play-tune 
                                     0 
                                     (* 37 16 1/8) 
                                     TimeUnit/SECONDS)] 
      (.schedule scheduler ^Runnable #(.cancel tune true) (long (* 37 16 1/8 2)) TimeUnit/SECONDS)))
  
(doseq [it (range 2)] 
   (srv/at (+ (time/now) (* it 1000  5)) 
           (play-tune))) 
  ) 

(defn discreet-music [] 
  (let    [
           right-echo-bus     (bus/audio-bus)
           left-echo-bus      (bus/audio-bus)
           main-group         (n/group "echo")
           right-input-group  (n/group "right-input-group" :head main-group)
           left-input-group   (n/group "left-input-group" :head main-group)
           right-effect-group (n/group "right-effect-group" :after right-input-group)
           left-effect-group  (n/group "left-effect-group" :after left-input-group)
           right-phrase       [[[[D4 16] [E4 8]] 80] 
                               [[[B3 8][G3 8]] 186] 
                               [[[G4 8]] 376]] 
           left-phrase        [[[[C5 8] [D5 16]] 0]
                               [[[E4 8]] 96] 
                               [[[G4 8]] 184] 
                               [[[E5 6][G5 6][A5 6][G5 14]] 304]] 
           right-player       (partial schedule-phrase right-echo-bus right-input-group) 
           left-player        (partial schedule-phrase left-echo-bus  left-input-group)
           _                  (echo [:tail right-effect-group] :dur 1 :input-bus right-echo-bus  :output-bus 1)
           _                  (echo [:tail left-effect-group] :dur 1 :input-bus left-echo-bus  :output-bus 0)    
           play-tune          #((do (dorun (map (fn [[phrase offset]] (right-player phrase offset)) right-phrase))
                                    (dorun (map (fn [[phrase offset]] (left-player phrase offset)) left-phrase))))
           scheduler          (Executors/newScheduledThreadPool 1)
           tune               (.scheduleAtFixedRate scheduler 
                                                    play-tune 
                                                    0 
                                                    (* 37 16 1/8) 
                                                    TimeUnit/SECONDS) 
           stop-and-clean     (fn [] (do (.cancel tune true)
                                         (bus/free-bus right-echo-bus)
                                         (bus/free-bus left-echo-bus)
                                         (n/node-free right-effect-group)
                                         (n/node-free left-effect-group)
                                         (n/node-free right-effect-group)
                                         (n/node-free left-input-group)
                                         (n/node-free main-group)))
           ] 
       (.schedule scheduler ^Runnable stop-and-clean (long (* 37 16 1/8 2)) TimeUnit/SECONDS)))

(comment 
  (discreet-music)
)
(comment 
  (play-tune)
)
(comment 
  (srv/stop)
  (app/reset-server-connection)
)
