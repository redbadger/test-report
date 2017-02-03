(ns test-report.core
  (:require [clojure.test]
            [robert.hooke :refer [add-hook remove-hook]]))

(def message-types
  (->
    (make-hierarchy)
    (derive :pass :result)
    (derive :fail :result)
    (derive :error :result)))

(defn- result? [message]
  (isa? message-types (:type message) :result))

(defn- add-time [message]
  (assoc message :time (System/nanoTime)))

(defn- add-context [message]
  (assoc message :context clojure.test/*testing-contexts*))

(defmulti enrich :type :hierarchy #'message-types)

(defmethod enrich :result [message]
  (-> message add-time add-context))

(defmethod enrich :default [message]
  (-> message add-time))

(defn- apply-all [functions & args]
  (->
   (apply juxt functions)
   (apply args)))

(defn- report [options messages message]
  (let [message ((:enrich options) message)]
    (dosync (alter messages conj message))
    (apply-all (:report options) message)))

(defn- collect-messages [options f & args]
  (let [messages (ref [])
        options (merge {:enrich enrich
                        :report [clojure.test/report]
                        :summarize []} options)]
    (binding [clojure.test/report (partial report options messages)]
      (let [result (apply f args)]
        (apply-all (:summarize options) @messages)
        result))))

(defn activate [options]
  (remove-hook #'clojure.test/run-tests :test-report)
  (add-hook #'clojure.test/run-tests :test-report (partial collect-messages options)))

(defn- map-nested-between [f begin-pred end-pred values]
  (loop [values values
         acc []]
    (let [[begin & after-begin] (drop-while (complement begin-pred) values)
          [between [end & after-end]] (split-with (complement end-pred) after-begin)]
      (if begin
        (recur after-end (conj acc (f begin end between)))
        acc))))

(defn- duration [begin end]
  (- (:time end) (:time begin)))

(defn- transform-test-var [begin-test-var end-test-var contents]
  {:var (:var begin-test-var)
   :time (duration begin-test-var end-test-var)
   :results (->> contents
                 (filter result?)
                 (map #(dissoc % :time)))})

(defn- count-results [test-vars]
  (let [result-types (->> test-vars
                          (mapcat :results)
                          (map :type))]
    (merge {:test (count test-vars)
            :assertion (count result-types)}
           (zipmap (descendants message-types :result) (repeat 0))
           (frequencies result-types))))

(defn- add-result-counts [test-ns]
 (assoc test-ns :summary (-> test-ns :tests count-results)))

(defn- transform-test-ns [begin-test-ns end-test-ns contents]
  (add-result-counts
   {:ns (:ns begin-test-ns)
    :time (duration begin-test-ns end-test-ns)
    :tests (map-nested-between transform-test-var
                               #(= :begin-test-var (:type %))
                               #(= :end-test-var (:type %))
                               contents)}))


(defn- sum-result-counts [test-nses]
  (->> test-nses
       (map :summary)
       (apply merge-with +)))

(defn- add-overall-result-counts [output]
  (assoc output :summary (-> output :namespaces sum-result-counts)))

(defn summarize [messages]
  (add-overall-result-counts
   {:namespaces (map-nested-between transform-test-ns
                                    #(= :begin-test-ns (:type %))
                                    #(= :end-test-ns (:type %))
                                    messages)}))
