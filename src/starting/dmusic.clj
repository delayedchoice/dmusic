(ns starting.dmusic
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.envelope :as env]
            [overtone.sc.bus :as bus]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.algo.scaling :as scale]
            ;[overtone.core]
            [overtone.inst.synth :as toy]
            [overtone.sc.cgens.mix :as mix]
            [overtone.sc.cgens.line :as line]
            [overtone.sc.machinery.server.connection :as conn]
            [overtone.music.time :as time]
            [starting.core :as app]
            ))

(comment 
  (meta #'overtone.core/synth)
  (ru/odoc u/comb-n)
)

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

(defonce echo-bus (bus/audio-bus))

(defonce main-group (n/group "get-on-the-bus echo"))
(defonce input-group (n/group "input-group" :head main-group))
(defonce effect-group (n/group "effect-group" :after input-group))

(comment 
  (meta #'overtone.core/audio-bus)
  (ru/odoc u/comb-n)
  )

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

(sy/defsynth one
  "first class of https://www.youtube.com/playlist?list=PLPYzvS8A_rTZmJZjUtMG6GJ2QkLUEaY4Q."
  [note     84
   dur      1.0
   fade     1
   output-bus  0  
   ]
  (let [env (u/env-gen (env/adsr 2 0 0.9 4))
        fade-target (if fade 0 1)
        sig (as-> note $
              (u/midicps $) 
              (- $ (* 1 (u/sin-osc 0.5) )) ;vib
              (+ (u/saw $) (u/sin-osc $)) 
              (* $ env) ;adsr
              (u/softclip $)
              (* $ (u/line 1 fade-target (+ 1.5 dur) :action u/FREE )) ;fade
              (u/lpf $ 800) ;low pass filter
              (+ $ (u/comb-l:ar $ ))
              (* $ 0.5)
              )]
    (u/out output-bus sig)))
(one :note C5 )
(comment
  (srv/at (+ (time/now) (* 1000 4)) (one))
  (one :wait 4)
  (one))

(defn play-phrase [phrase offset channel]
  (let [factor 1/8
        phrase-with-waits (map #(vector (first %1) (second %1) %2 %3) 
                                  (map #(vector (first %1) (* factor (second %1))) phrase) 
                                  ;phrase
                                  (into [0] (reductions + (map second phrase))) 
                                  (into (map #(* 0 (first %) ) phrase) [1]) ) ] 
    (doseq [[note dur wait fade] phrase-with-waits]
      (let [_ (print-str "note: " note " dur: " dur " :wait " wait " :fade " fade)]
       (srv/at (+ (time/now) (* 1000 factor (+ wait offset))) 
               (one [:tail input-group] 
                    :note note 
                    :dur dur 
                    :fade fade 
                    :output-bus channel)))
     )))
(comment
  ;this is the tune
  (echo [:tail effect-group] :dur 1 :input-bus echo-bus  :ouput-bus 0)
  (play-phrase [[D4 16] [E4 8]] 80 echo-bus )
  (play-phrase [[B3 8] [G3 8] ] 186  echo-bus)
  (play-phrase [[G4 8] ] 376 echo-bus)

  (play-phrase [[C5 8] [D5 16]] 0 echo-bus)
  (play-phrase [[E4 8] ] 96 echo-bus)
  (play-phrase [[G4 8] ] 184 echo-bus)
  (play-phrase [[E5 6][G5 6][A5 6][G5 14]] 304 echo-bus)


 )
(comment 
  (srv/stop)
)
;/ Trigger D4 after 5 measures and hold for 1 full measure + two 1/4 notes
;  rightSynth.triggerAttackRelease('D4', '1:2', '+5:0');
;  // Switch to E4 after one more measure
;  rightSynth.setNote('E4', '+6:0');
;
;  // Trigger B3 after 11 measures + two 1/4 notes + two 1/16 notes. Hold for one measure
;  rightSynth.triggerAttackRelease('B3', '1m', '+11:2:2');
;  // Switch to G3 after a 1/2 note more
;  rightSynth.setNote('G3', '+12:0:2');
;se
;  // Trigger G4 after 23 measures + two 1/4 notes. Hold for a half note.
;  rightSynth.triggerAttackRelease('G4', '0:2', '+23:2');
;-----------------------------------------------------
;  leftSynth.triggerAttackRelease('C5', '1:2', time);
;  leftSynth.setNote('D5', '+0:2');
;
;  leftSynth.triggerAttackRelease('E4', '0:2', '+6:0');
;
;  leftSynth.triggerAttackRelease('G4', '0:2', '+11:2');
;
;  leftSynth.triggerAttackRelease('E5', '2:0', '+19:0');
;  leftSynth.setNote('G5', '+19:1:2');
;  leftSynth.setNote('A5', '+19:3:0');
;  leftSynth.setNote('G5', '+19:4:2');


