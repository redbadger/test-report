(ns test-report-junit-xml.core
  (:require [test-report.core :as test-report]
            [clojure.data.xml :as xml]
            [clojure.string :as string]
            [clojure.stacktrace :as stacktrace]
            [clojure.pprint :refer [pprint]]))

(defn- join-non-blanks [delimiter & strings]
  (->> strings (remove string/blank?) (string/join delimiter)))

(defn- context [result]
  (->> result :context reverse (string/join " ")))

(defn- error-message [error]
  (when (instance? Throwable error)
    (.getMessage error)))

(defn- error-details [error]
  (if (instance? Throwable error)
    (with-out-str (stacktrace/print-cause-trace error))
    (prn-str error)))

(defmulti result-details :type)

(defmethod result-details :fail [result]
  (let [message (join-non-blanks ": " (context result) (:message result))]
    {:tag :failure
     :attrs {:message message}
     :content (join-non-blanks "\n"
                               message
                               (str "expected: " (-> result :expected prn-str)
                                    "  actual: " (-> result :actual prn-str)
                                    "      at: " (:file result) ":" (:line result)))}))

(defmethod result-details :error [result]
  (let [message (join-non-blanks ": " (context result) (:message result) (-> result :actual error-message))]
    {:tag :error
     :attrs {:message message}
     :content (join-non-blanks "\n"
                               message
                               (str "expected: " (-> result :expected prn-str)
                                    "  actual: " (-> result :actual error-details)))}))

(defmethod result-details :default [result])

(defn- nanos->secs [nanos]
  (->> nanos (* 1e-9) (format "%.3g")))

(defn- testcase [test-var]
  (let [var-meta (-> test-var :var meta)]
    {:tag :testcase
     :attrs {:classname (-> var-meta :ns ns-name)
             :name (:name var-meta)
             :time (-> test-var :time nanos->secs)}
     :content (keep result-details (:results test-var))}))

(defn- testsuite [id test-ns]
  (let [counts (:summary test-ns)]
    {:tag :testsuite
     :attrs {:id id
             :name (-> test-ns :ns ns-name)
             :tests (:test counts)
             :errors (:error counts)
             :failures (:fail counts)
             :time (-> test-ns :time nanos->secs)}
     :content (map testcase (:tests test-ns))}))

(defn- testsuites [summary]
  {:tag :testsuites
   :content (map-indexed testsuite (:namespaces summary))})

(defn summarize [writer messages]
  (-> messages
      test-report/summarize
      testsuites
      (xml/emit writer)))
