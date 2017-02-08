(ns test-report.core
  (:require [clojure.test]
            [robert.hooke :refer [add-hook remove-hook]]
            [test-report.message :as message]
            [test-report.options :refer [with-options]]))

(def ^:dynamic *reporters* [clojure.test/report])
(def ^:dynamic *summarizers* [])

(defn- add-time [message]
  (assoc message :time (System/nanoTime)))

(defn- add-context [message]
  (assoc message :context clojure.test/*testing-contexts*))

(defmulti ^:private enrich :type :hierarchy #'message/types)

(defmethod enrich :result [message]
  (-> message add-time add-context))

(defmethod enrich :default [message]
  (-> message add-time))

(defn- apply-all [functions & args]
  (when (seq functions)
    (-> (apply juxt functions)
        (apply args))))

(defn- report [messages message]
  (let [message (enrich message)]
    (dosync (alter messages conj message))
    (apply-all *reporters* message)))

(defn- collect-messages [f & args]
  (let [messages (ref [])]
    (binding [clojure.test/report (partial report messages)]
      (let [result (apply f args)]
        (apply-all *summarizers* @messages)
        result))))

(defn activate
  "Sets up the test reporter by hooking into clojure.test/run-tests.

  Output may be configured by supplying the following options (or by binding the
  corresponding dynamic vars):
  :reporters   - a collection of functions called sequentially to process each
                 message during a test run
                 (default [clojure.test/report])
  :summarizers - A collection of functions called sequentially to process all
                 messages at the end of a test run
                 (default [])"
  ([] (activate {}))
  ([options]
   (remove-hook #'clojure.test/run-tests :test-report)
   (add-hook #'clojure.test/run-tests :test-report #(with-options options (apply collect-messages %&)))))
