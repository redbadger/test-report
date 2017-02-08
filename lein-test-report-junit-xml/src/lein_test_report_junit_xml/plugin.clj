(ns lein-test-report-junit-xml.plugin
  (:require [lein-test-report.utils :refer [add-profile]]))

(defn middleware [project]
  (let [options (:test-report-junit-xml project {})
        output-path (or (System/getenv "TEST_REPORT_JUNIT_XML_OUTPUT_PATH")
                        (:output-path options)
                        "target/TESTS-TestSuites.xml")]
    (add-profile project {:dependencies [['test-report-junit-xml "0.1.0-SNAPSHOT"]]
                          :plugins [['lein-test-report "0.1.0-SNAPSHOT"]]
                          :injections `[(require 'test-report-junit-xml.core)
                                        (require 'clojure.java.io)]
                          :test-report {:summarizers `[#(with-open [writer# (clojure.java.io/writer ~output-path)]
                                                          (test-report-junit-xml.core/write writer# % ~options))]}})))
