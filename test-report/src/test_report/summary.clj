(ns test-report.summary
  (:require [test-report.message :as message]))

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
                 (filter message/result?)
                 (map #(dissoc % :time)))})

(defn- count-results [test-vars]
  (let [result-types (->> test-vars
                          (mapcat :results)
                          (map :type))]
    (merge {:test (count test-vars)
            :assertion (count result-types)}
           (zipmap (descendants message/types :result) (repeat 0))
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

(defn summarize
  "Collates the given messages into a nested data structure of the following
  form:

  {:namespaces [{:ns <test namespace>
                 :time <test namespace execution time in nanoseconds>
                 :tests [{:var <test var>
                          :time <test var execution time in nanoseconds>
                          :results [<message of type :pass :fail :error>
                                    ...]}
                          ...]
                 :summary {:test <number of tests executed in this namespace>
                           :assertion <number of assertions executed>
                           :pass <number of assertions passed>
                           :fail <number of assertions failed>
                           :error <number of errors thrown>}}
                 ...]
   :summary {:test <total number of test vars executed>
             :assertion <total number of assertions executed>
             :pass <total number of assertions passed>
             :fail <total number of assertions failed>
             :error <total number of errors thrown>}}"
  [messages]
  (add-overall-result-counts
   {:namespaces (map-nested-between transform-test-ns
                                    #(= :begin-test-ns (:type %))
                                    #(= :end-test-ns (:type %))
                                    messages)}))
