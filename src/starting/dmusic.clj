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

(sy/defsynth echo
  " "
  [
   dur        1.0
   input-bus  0  
   output-bus 0
   factor     1/8
   ]
  (let [sig (as-> (u/in:ar input-bus) $
              (+ $ (u/comb-n:ar $ 6 6 (* 16 37 factor) ))
              (* $ 0.5)
              )]
    (u/out output-bus sig)))

(sy/defsynth tone
  "i'm just a tone"
  [note             60
   scaled-duration  1.0
   output-bus       0  
   gate             1
   ]
  (let [envl  (u/env-gen:ar
                (envel/envelope [0.1 1 1 0.001 0]
                                [0.2 scaled-duration 20 1]
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
(tone)
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

(defn schedule-phrase [channel input-group phrase offset base-offset ]
  (let [time-factor 1/8
        offset (+ base-offset offset)] 
    (srv/at (+ (time/now) (* 1000 time-factor offset)) 
            (play-phrase phrase offset channel input-group time-factor ))))

;(srv/sc-debug-on)
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
           play-tune          #(doseq [it (range 3)] 
                                    (dorun (map (fn [[phrase offset]] (right-player phrase offset (* it 37 16))) right-phrase))
                                    (dorun (map (fn [[phrase offset]] (left-player phrase offset (* it 37 16))) left-phrase)))
           ] 
       (play-tune)))

(comment 
  (discreet-music)
)

(comment 
  (srv/stop)
  (app/reset-server-connection)
)
