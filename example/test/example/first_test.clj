(ns example.first-test
  (:require [clojure.test :refer :all]))

(deftest passing
  (is (= 0 0))
  (is (= 0 0)))

(deftest nested
  (testing "outer"
    (testing "inner"
      (is (= 0 0)))))

(deftest failing
  (is (= 0 1)))

(deftest erroring
  (is (= 0 (/ 0 0))))
