(ns test-report-junit-xml.core
  (:require [clojure.pprint :refer [pprint]]
            [test-report.core :as test-report]))

(defn summarize [messages]
  (pprint (test-report/summarize messages)))
