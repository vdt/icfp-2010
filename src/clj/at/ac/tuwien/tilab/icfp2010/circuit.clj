(ns at.ac.tuwien.tilab.icfp2010.circuit
  )

(def sample-circuit
  {:input [0 :l]
   :outputs {0 {:r :x-in
		:l [0 :r]}}})

(defn assign-input [inputs output input]
  (if (= :x-in input)
    inputs
    (let [input-gate (first input)]
      (assoc inputs input-gate (assoc (inputs input-gate) (second input) output)))))

(defn gate-inputs [circuit]
  (let [input (:input circuit)]
    (loop [inputs {(first input) {(second input) :x-out}}
	   outputs (:outputs circuit)]
      (if (empty? outputs)
	inputs
	(let [output (first outputs)
	      gate (key output)
	      wires (val output)]
	  (recur (-> inputs
		     (assign-input [gate :l] (:l wires))
		     (assign-input [gate :r] (:r wires)))
		 (rest outputs)))))))

(defn lookup-input [gate wire circuit-inputs inputs]
  (inputs ((circuit-inputs gate) wire)))

(defn gate-function [l r]
  (let [ln (if (= :undef l) 0 l)
	rn (if (= :undef r) 0 1)]
    [ln (mod (+ ln rn) 3)]))

(defn wire-delayed? [circuit gate wire]
  (let [output (((:outputs circuit) gate) wire)]
    (if (= :x-in output)
      false
      (>= (first output) gate))))

(defn undef-inputs [circuit]
  (into {} (mapcat (fn [[gate outputs]]
		     (mapcat (fn [[wire input]]
			       (if (or (= input :x-in) (not (wire-delayed? circuit gate wire)))
				 []
				 [[input :undef]]))
			     outputs))
		   (:outputs circuit))))

(defn circuit-step [circuit circuit-inputs inputs]
    (loop [outputs (:outputs circuit)
	   inputs inputs
	   delayed {}
	   changed false]
      (if (empty? outputs)
	[inputs delayed changed]
	(let [gate (key (first outputs))
	      gate-outputs (val (first outputs))
	      l-input (inputs [gate :l])
	      r-input (inputs [gate :r])]
	  (if (and l-input r-input)
	    (let [[l-output r-output] (gate-function l-input r-input)
		  l-delayed (wire-delayed? circuit gate :l)
		  r-delayed (wire-delayed? circuit gate :r)]
	      (let [delayed* (if l-delayed
			       (assoc delayed (:l gate-outputs) l-output)
			       delayed)
		    delayed** (if r-delayed
				(assoc delayed* (:r gate-outputs) r-output)
				delayed*)
		    inputs* (if l-delayed
			      inputs
			      (assoc inputs (:l gate-outputs) l-output))
		    inputs** (if r-delayed
			       inputs*
			       (assoc inputs* (:r gate-outputs) r-output))]
		(recur (rest outputs) inputs** delayed** true)))
	    (recur (rest outputs) inputs delayed changed))))))

(defn simulate-circuit [circuit input-stream]
  (let [circuit-inputs (gate-inputs circuit)]
    (loop [input-stream input-stream
	   last-inputs (undef-inputs circuit)
	   output-stream []]
      (if (empty? input-stream)
	output-stream
	(let [[inputs delayed changed] (circuit-step circuit circuit-inputs (assoc last-inputs (:input circuit) (first input-stream)))]
	  (println {:inputs inputs :delayed delayed :changed changed})
	  (recur (rest input-stream)
		 delayed
		 (conj output-stream (:x-in inputs))))))))