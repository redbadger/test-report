(ns test-report.message-test
  (:require [clojure.test :refer :all]
            [test-report.message :refer :all]))

(deftest pass-is-a-result
  (is (result? {:type :pass})))

(deftest fail-is-a-result
  (is (result? {:type :fail})))

(deftest error-is-a-result
  (is (result? {:type :error})))
