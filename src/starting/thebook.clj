(ns starting.thebook
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

(sy/defsynth hello-world
  "hello world."
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

;(sy/demo 10 (u/sin-osc (+ 1000 (* 600 (u/lf-noise0:kr 12))) 0.3))
(sy/demo 10 (u/rlpf (u/dust [12 15]) (+ 1600 (* 1500 (u/lf-noise1 [1/3, 1/4]))) 0.02 ))
(sy/demo 20 (let [sines 5
                 speed 6]
             (* (mix/mix
                 (map #(u/pan2 (* (u/sin-osc (* % 100))
                                  (max 0 (+ (u/lf-noise1:kr speed) (u/line:kr 1 -1 30))))
                               (- (clojure.core/rand 2) 1))
                      (range sines)))
                (/ 1 sines))))
(comment 
  (meta #'overtone.core/mix)
  (ru/odoc u/pan2)
  )


(defn restart []
  (do 
    (n/kill @the-synth)
    (reset! the-synth (pulse-test))))

(comment 
  (restart)
  (n/ctl @the-synth :amp-hz 4)
  (n/ctl @the-synth :width 0.25)
  (n/ctl @the-synth :fund  100)
  (n/ctl @the-synth :max-partial 8)
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

(sy/demo 2 
         (as-> (u/lf-saw:ar 1) $
               (line/lin-lin:ar $ -1 1 0 500)
               (* 0.01 (u/lf-pulse:ar (+ 200 $) 0.5)))
               (u/pan2 $ 0))
