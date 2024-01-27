(ns starting.synth
  (:use [overtone.live]))

(defsynth sawbble-synth
  "a detuned and stereo-separated saw synth with a low-pass-filter and
   low-pass-filter LFO."
  [note                {:default 60   :min 0   :max 127   :step 1}
   ;; adding amp for (midi-poly-player)
   amp                 {:default 1.0  :min 0.0 :max 1.0   :step 0.1}
   amp-konst           {:default 1.0  :min 1.0 :max 10.0  :step 0.1}
   note-slew           {:default 15.0 :min 1.0 :max 50.0  :step 1.0}
   separation-delay-ms {:default 5.0  :min 0   :max 30.0  :step 0.1}
   separation-phase    {:default 1    :min -1  :max 1     :step 2}
   lpf-lfo-freq        {:default 4.1  :min 0.0 :max 10.0  :step 0.01}
   lpf-min-freq-ratio  {:default 2.0  :min 0.2 :max 100.0 :step 0.1}
   lpf-max-freq-ratio  {:default 10.0 :min 0.2 :max 100.0 :step 0.1}
   lpf-res             {:default 0.1  :min 0.0 :max 1.0   :step 0.05}
   lfo-level-ratio     {:default 0.001 :min 0.0 :max 0.1  :step 0.001}
   lfo-freq            {:default 1.8  :min 0.0 :max 10.0  :step 0.1}
   adsr-attack-time    {:default 0.1 :min 0.0  :max 1.0   :step 0.01}
   adsr-decay-time     {:default 0.1 :min 0.0  :max 1.0   :step 0.01}
   adsr-sustain-level  {:default 0.5 :min 0.0  :max 1.0   :step 0.01}
   adsr-release-time   {:default 0.1 :min 0.0  :max 1.0   :step 0.01}
   adsr-peak-level     {:default 0.9 :min 0.0  :max 1.0   :step 0.01}
   adsr-curve          {:default -4  :min -5   :max 5     :step 1}
   gate                {:default 1.0 :min 0.0  :max 1.0   :step 1}
   out-bus             {:default 0   :min 0    :max 128   :step 1}]
  (let [pitch-midi       (slew:kr note note-slew note-slew) ;; interesting lag to changing notes
        pitch-freq       (midicps pitch-midi)
        lfo-out          (* lfo-level-ratio pitch-freq
                            (sin-osc lfo-freq))
        saws-out         (mix (saw [pitch-freq (+ pitch-freq lfo-out)]))
        separation-delay (/ separation-delay-ms 1000.0)
        saws-out-2ch     [saws-out (delay-c (* separation-phase saws-out)
                                            1.0 separation-delay)]
        lpf-min-freq     (* lpf-min-freq-ratio pitch-freq)
        lpf-max-freq     (* lpf-max-freq-ratio pitch-freq)
        lpf-freq         (lin-lin (sin-osc lpf-lfo-freq)
                                  -1 1
                                  lpf-min-freq lpf-max-freq)
        lpf-out-2ch      (moog-ff saws-out-2ch lpf-freq lpf-res)
        env-out          (env-gen (adsr adsr-attack-time   adsr-decay-time
                                        adsr-sustain-level adsr-release-time
                                        adsr-peak-level    adsr-curve)
                                  :gate gate :action FREE)]
    (out out-bus (* 1 amp amp-konst env-out ))))

;; 72bpm
(def bpm0 72)
(defn spb [beats tempo]
  (let [bps (/ tempo 60)]
    (/ beats bps)))
(defn bps [beats tempo]
  (/ 1.0 (spb beats tempo)))
;; This is pretty damn close to that Vangelis Blade Runner sound

(comment
 (def mpp (
            sawbble-synth
            :amp-konst           12.0
            :separation-delay-ms 8.0
            :separation-phase    -1
            :lpf-lfo-freq        (bps 2 bpm0)
            :lpf-min-freq-ratio  10.0
            :lpf-max-freq-ratio  20.0
            :lpf-res             0.4
            :lfo-level-ratio     0.005
            :lfo-freq            (bps 2 bpm0)
            :adsr-attack-time    (spb 0.5 bpm0)
            :adsr-decay-time     (spb 0.25 bpm0)
            :adsr-sustain-level  0.8
            :adsr-release-time   (spb 3 bpm0)
            :adsr-peak-level     1.0
            :adsr-curve          4))
 )
;(midi-player-stop)
;;(synth-controller sawbble-synth)

;; new in 0.9 graphviz output
;(show-graphviz-synth sawbble-synth)

;; ======================================================================
;; some things to play with...
(def sawbble (sawbble-synth))
;(stop)
;(ctl sawbble :note 35 :gate 1)
;(ctl sawbble :note 32)
;(ctl sawbble :note 30)
;(ctl sawbble :lfo-level 1.5 :lfo-freq 2.0)
;(ctl sawbble :note-slew 25.0)
;(ctl sawbble :lpf-freq-lo 4000.0 :lpf-freq-hi 4000.0 :lpf-lfo-freq 1.5 :lpf-res 0.9)
;(ctl sawbble :lp-res 0.75)
;(ctl sawbble :sep-delay 2.0)
;(ctl sawbble :gate 0)

;(use 'overtone.live)
;
;; Define your desired instrument
;; Using saw-wave from: https://github.com/overtone/overtone/wiki/Chords-and-scales
;(definst saw-wave [freq 440 attack 0.01 sustain 0.4 release 0.1 vol 0.4] 
;  (* (env-gen (env-lin attack sustain release) 1 1 0 1 FREE)
;     (saw freq)
;     vol))
;
;(defn play [note ms]
;  (saw-wave (midi->hz note))
;  (Thread/sleep ms))
;
;(doseq [note (scale :c4 :major)] (play note 500))


;(let [env (envelope [0  1] [2] :sqr)]
;  (demo (sin-osc :freq (+ 200 (* 200 (env-gen env :action FREE))))))

;(demo (* (env-gen (lin 0.1 1 1 0.25) :action FREE) (sin-osc)))
