(ns lein-test-report-junit-xml.plugin
  (:require [lein-test-report.utils :refer [add-profile]]))

(defn middleware [project]
  (add-profile project {:dependencies [['test-report-junit-xml "0.1.0-SNAPSHOT"]]
                        :plugins [['lein-test-report "0.1.0-SNAPSHOT"]]
                        :injections `[(require 'test-report-junit-xml.core)]
                        :test-report {:summarize ['test-report-junit-xml.core/summarize]}}))
