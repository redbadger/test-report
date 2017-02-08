(ns test-report.options-test
  (:require [clojure.test :refer :all]
            [test-report.options :refer :all]))

(def ^:dynamic *foo* 42)
(def ^:dynamic *bar* 50)

(deftest with-options-executes-body-with-bound-dynamic-vars
  (testing "with no options"
    (with-options {}
      (is (= 42 *foo*))
      (is (= 50 *bar*))))
  (testing "with only one option"
    (with-options {:foo 66}
      (is (= 66 *foo*))
      (is (= 50 *bar*))))
  (testing "with all options"
    (with-options {:foo 66, :bar 99}
      (is (= 66 *foo*))
      (is (= 99 *bar*)))))
