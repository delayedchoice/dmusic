(ns starting.dmusic
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.envelope :as envel]
            [overtone.sc.bus :as bus]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.music.time :as time]
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

(defonce right-echo-bus (bus/audio-bus))
(defonce left-echo-bus (bus/audio-bus))

(defonce main-group (n/group "echo"))
(defonce right-input-group (n/group "right-input-group" :head main-group))
(defonce left-input-group (n/group "left-input-group" :head main-group))
(defonce right-effect-group (n/group "right-effect-group" :after right-input-group))
(defonce left-effect-group (n/group "left-effect-group" :after left-input-group))

(sy/defsynth echo
  ""
  [
   dur      1.0
   input-bus  0  
   output-bus 0
   ]
  (let [
        sig (as-> (u/in:ar input-bus) $
              (+ $ (u/comb-n:ar $ 6 6 600 ))
              (* $ 0.5)
              )]
    (u/out output-bus sig)))

(comment 
  (meta #'overtone.core/synth)
  (ru/odoc u/free-verb2)
)

(sy/defsynth tone
  "first class of https://www.youtube.com/playlist?list=PLPYzvS8A_rTZmJZjUtMG6GJ2QkLUEaY4Q."
  [note        60
   scaled-duration  1.0
   output-bus  0  
   gate        1
   ]
  (let [envl  (u/env-gen:ar
               (envel/envelope [0.1 1 1 0.001 0]
                               [0.2 scaled-duration 7 1]
                               [1 1 :exponential 1])
               :action u/FREE)
         ;(u/env-gen (envel/adsr 2 0 0.9 4) :gate gate :action u/FREE)
        ;fade-target (if fade 0 1)
        sig (as-> note $
              (u/midicps $) 
              (- $ (* 1 (u/sin-osc 0.5) )) ;vib
              (+ (u/var-saw $ :width 0.9) #_(u/sin-osc $)) 
              (* $ envl) ;adsr
              (u/lpf $ 800) ;low pass filter
              (u/free-verb $ :mix 0.4 :room 1)
              (u/softclip $)
              (* $ 0.5)
              )]
    (u/out output-bus sig)))

(defn play-phrase [phrase offset channel input-group time-factor]
  (let [
        phrase-with-waits (map #(vector (first %1) %2 ) 
                                  phrase
                                  (into [0] (reductions + (map second phrase))) ;offsets?
                                  ) 
        total-duration (reduce + (map second phrase))
        first-note (-> phrase first first)
        scaled-first-note (- first-note 12)
        the-synth (tone [:tail input-group] 
                        :note scaled-first-note 
                        :scaled-duration (* time-factor total-duration) 
                        :output-bus channel)
        ] 
    (doseq [[note wait] (rest phrase-with-waits)]
       (srv/at (+ (time/now) (* 1000 time-factor (+ wait offset))) 
               (n/ctl the-synth :note (- note 12) )))))

(defn schedule-phrase [phrase offset channel input-group]
  (let [time-factor 1/8] 
    (srv/at (+ (time/now) (* 1000 time-factor offset)) 
      (play-phrase phrase offset channel input-group time-factor ))))

(comment
  ;this is the tune
  (doall
   (echo [:tail right-effect-group] :dur 1 :input-bus right-echo-bus  :output-bus 1)
   (echo [:tail left-effect-group] :dur 1 :input-bus left-echo-bus  :output-bus 0)
   (schedule-phrase [[D4 16] [E4 8]] 80 right-echo-bus right-input-group)
   (schedule-phrase [[B3 8] [G3 8] ] 186  right-echo-bus right-input-group)
   (schedule-phrase [[G4 8] ] 376 right-echo-bus right-input-group)

   (schedule-phrase [[C5 8] [D5 16]] 0 left-echo-bus left-input-group)
   (schedule-phrase [[E4 8] ] 96 left-echo-bus left-input-group)
   (schedule-phrase [[G4 8] ] 184 left-echo-bus left-input-group)
   (schedule-phrase [[E5 6][G5 6][A5 6][G5 14]] 304 left-echo-bus left-input-group)
   ) )

(comment 
  (srv/stop)
)

