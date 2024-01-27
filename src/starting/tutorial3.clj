(ns starting.tutorial3
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}
                                 :invalid-arity {:level :off}}}}  
  (:require [overtone.sc.ugens :as u]
            [overtone.sc.server :as srv]
            [overtone.sc.synth :as sy]
            [overtone.sc.node :as n]
            [overtone.repl.ugens :as ru]
            [overtone.algo.scaling :as scale]
            [overtone.core]

            ))
(def the-synth (atom nil))

(sy/defsynth pulse-test
  "tutorial 3."
  [amp-hz         {:default 4   :min 0   :max 64    :step 1}
   fund           {:default 40  :min 20  :max 100   :step 1}
   max-partial    {:default 4   :min 2   :max 10    :step 1}
   width          {:default 0.5 :min 0.0 :max 10.0  :step 0.1}
]
  (let [
        amp1 (u/lf-pulse:kr amp-hz 0 0.12) 
        ;amp2 (u/lf-pulse:kr amp-hz 0 0.12) 
        freq1 (* (-> 
                    (u/lf-pulse:kr 8)
                    (+ 1)
                    )
                 (-> 
                    (u/lf-noise0:kr 4)
                    (u/lin-exp:kr -1 1 fund (* fund max-partial))
                    (u/round fund)))
        freq2 (* (-> 
                    (u/lf-pulse:kr 6)
                    (+ 1)
                    )
                 (-> 
                    (u/lf-noise0:kr 4)
                    (u/lin-exp:kr -1 1 fund (* fund max-partial))
                    (u/round fund)))
        sig1     (-> 
                   (u/pulse:ar freq1 width)
                   (u/mul-add:ar (* 0.75 (u/lf-pulse:kr amp-hz 0 0.12)) 0)
                   (u/free-verb 0.7 0.8 0.25))
        sig2     (->
                   (u/pulse:ar freq2 width)
                   (u/mul-add:ar (* 0.75 (u/lf-pulse:kr amp-hz 0.5 0.12)) 0)
                   (u/free-verb 0.7 0.8 0.25))
        ]
    (u/out 0 sig1)
    (u/out 1 sig2)))


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
  (reset! the-synth (pulse-test))
  (n/kill @the-synth)
  )

(comment 
  (srv/stop)
  )

(comment 
  (meta #'overtone.core/pulse)
  (ru/odoc u/mul-add)
  )








