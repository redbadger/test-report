(ns lein-test-report.plugin
  (:require [lein-test-report.utils :refer [add-profile]]))

(defn middleware [project]
  (let [options (:test-report project {})]
    (add-profile project {:dependencies [['test-report "0.2.0"]]
                          :injections `[(require 'test-report.core)
                                        (test-report.core/activate ~options)]})))
