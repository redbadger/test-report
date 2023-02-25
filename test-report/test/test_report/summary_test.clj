(ns test-report.summary-test
  (:require [clojure.test :refer :all]
            [test-report.summary :refer :all]))

(create-ns 'example.first-test)
(intern 'example.first-test 'passing (fn []))
(intern 'example.first-test 'also-passing (fn []))
(intern 'example.first-test 'failing (fn []))
(intern 'example.first-test 'erroring (fn []))
(create-ns 'example.second-test)
(intern 'example.second-test 'passing (fn []))
(create-ns 'example.empty-since-fixture-failed-test)
(intern 'example.empty-since-fixture-failed-test 'erroring (fn []))

(deftest summarize-messages
  (let [messages [{:type :begin-test-ns
                   :ns (find-ns 'example.first-test)
                   :time 116894627141655}
                  {:type :begin-test-var
                   :var #'example.first-test/passing
                   :time 116894630092598}
                  {:type :pass
                   :time 116894630602954
                   :foo "bar"}
                  {:type :pass
                   :time 116894631113311
                   :baz "qux"}
                  {:type :end-test-var
                   :var #'example.first-test/passing
                   :time 116894631324095}
                  {:type :begin-test-var
                   :var #'example.first-test/also-passing
                   :time 116894631395748}
                  {:type :pass
                   :time 116894631557068
                   :quux "corge"}
                  {:type :end-test-var
                   :var #'example.first-test/also-passing
                   :time 116894631670019}
                  {:type :begin-test-var
                   :var #'example.first-test/erroring
                   :time 116894631727955}
                  {:type :error
                   :time 116894632069958
                   :grault "garply"}
                  {:type :end-test-var
                   :var #'example.first-test/erroring
                   :time 116894649400363}
                  {:type :begin-test-var
                   :var #'example.first-test/failing
                   :time 116894649549433}
                  {:type :fail
                   :time 116894650628514
                   :waldo "fred"}
                  {:type :end-test-var
                   :var #'example.first-test/failing
                   :time 116894652657467}
                  {:type :end-test-ns
                   :ns (find-ns 'example.first-test)
                   :time 116894652854284}
                  {:type :begin-test-ns
                   :ns (find-ns 'example.second-test)
                   :time 116894653100547}
                  {:type :begin-test-var
                   :var #'example.second-test/passing
                   :time 116894653734493}
                  {:type :pass
                   :time 116894654062493
                   :plugh "xyzzy"}
                  {:type :end-test-var
                   :var #'example.second-test/passing
                   :time 116894654195961}
                  {:type :end-test-ns
                   :ns (find-ns 'example.second-test)
                   :time 116894654251062}
                  {:type :begin-test-ns
                   :ns (find-ns 'example.empty-since-fixture-failed-test)
                   :time 116894653100547}
                  {:type :error
                   :message "smth"
                   :time 116894653734493}
                  {:type :end-test-ns
                   :ns (find-ns 'example.empty-since-fixture-failed-test)
                   :time 116894654251062}
                  {:type :summary
                   :test 5
                   :pass 4
                   :fail 1
                   :error 2
                   :time 116894654397872}]]
    (is (= {:namespaces [{:ns (find-ns 'example.first-test)
                          :time 25712629
                          :tests [{:var #'example.first-test/passing
                                   :time 1231497
                                   :results [{:type :pass, :foo "bar"}
                                             {:type :pass, :baz "qux"}]}
                                  {:var #'example.first-test/also-passing
                                   :time 274271
                                   :results [{:type :pass, :quux "corge"}]}
                                  {:var #'example.first-test/erroring
                                   :time 17672408
                                   :results [{:type :error, :grault "garply"}]}
                                  {:var #'example.first-test/failing
                                   :time 3108034
                                   :results [{:type :fail, :waldo "fred"}]}]
                          :summary {:test 4
                                    :assertion 5
                                    :pass 3
                                    :fail 1
                                    :error 1}}
                         {:ns (find-ns 'example.second-test)
                          :time 1150515
                          :tests [{:var #'example.second-test/passing
                                   :time 461468
                                   :results [{:type :pass, :plugh "xyzzy"}]}]
                          :summary {:test 1
                                    :assertion 1
                                    :pass 1
                                    :fail 0
                                    :error 0}}
                         {:ns (find-ns 'example.empty-since-fixture-failed-test)
                          :time 1150515
                          :tests [{:results [{:message "smth" :type :error}]}]
                          :summary {:test 1
                                    :assertion 1
                                    :pass 0
                                    :fail 0
                                    :error 1}}]
            :summary {:test 6
                      :assertion 7
                      :pass 4
                      :fail 1
                      :error 2}}
           (summarize messages)))))
