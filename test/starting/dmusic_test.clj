(ns starting.dmusic-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require 
    
    [clojure.test :as test]
    [starting.dmusic :as d] 
    [overtone.sc.ugens :as u]
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
  (conn/connect "127.0.0.1" 57110 )
  (conn/shutdown-server))
(comment
  (app/reset))

(comment 
  (meta #'overtone.core/group)
  (ru/odoc u/comb-n)
)

(def the-node (atom (one :note (- C5 12) :output-bus 1 :scaled-duration 10 )))

(n/ctl @the-node :note G3)

(comment
  (srv/at (+ (time/now) (* 1000 4)) (one))
  (one :wait 4)
  (one))


(comment
  (d/play-phrase  [[d/E5 6][d/G5 6][d/A5 6] [d/G5 14]] 0 d/echo-bus)
  (d/echo [:tail d/effect-group] :dur 1 :input-bus d/echo-bus  :ouput-bus 0)
  ;(play-phrase [[B3 8] [G3 8] ] 0 10)
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


