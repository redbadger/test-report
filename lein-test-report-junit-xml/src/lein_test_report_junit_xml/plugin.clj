(ns lein-test-report-junit-xml.plugin
  (:require [lein-test-report.utils :refer [add-profile]]))

(defn middleware [project]
  (let [output-path (or (System/getenv "TEST_REPORT_JUNIT_XML_OUTPUT_PATH")
                        (-> project :test-report-junit-xml :output-path)
                        "target/TESTS-TestSuites.xml")]
    (add-profile project {:dependencies [['test-report-junit-xml "0.1.0-SNAPSHOT"]]
                          :plugins [['lein-test-report "0.1.0-SNAPSHOT"]]
                          :injections `[(require 'test-report-junit-xml.core)
                                        (require 'clojure.java.io)]
                          :test-report {:summarize `[#(with-open [writer# (clojure.java.io/writer ~output-path)]
                                                        (test-report-junit-xml.core/summarize writer# %))]}})))
