(ns starting.dmusic-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require 
    
    [clojure.test :as test]
    [starting.dmusic :as :dmusic] 
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


(comment
  (dmusic/play-phrase  [[dmusic/E5 6][dmusic/G5 6][dmusic/A5 6] [dmusic/G5 14]] 0 dmusic/echo-bus)
  (dmusic/echo [:tail dmusic/effect-group] :dur 1 :input-bus dmusic/echo-bus  :ouput-bus 0)
  ;(play-phrase [[B3 8] [G3 8] ] 0 10)
  )
