# test-report

[![CircleCI](https://circleci.com/gh/redbadger/test-report.svg?style=svg)](https://circleci.com/gh/redbadger/test-report)

test-report is a tool for customizing clojure.test output.

## Installation

The easiest way to get started is to add the lein-test-report plugin to your [Leiningen](https://github.com/technomancy/leiningen) project map. As it's only used in tests, it's best to add it only to the `:test` profile:

```clojure
:profiles {:test {:plugins [[lein-test-report "0.1.0"]]}}
```

## Usage

test-report hooks into the [`clojure.test/run-tests`](https://clojure.github.io/clojure/clojure.test-api.html#clojure.test/run-tests) function, so will automatically be included when running `lein test`.

By default, the configuration is identical to the clojure.test defaults, so you won't see any difference.
To start customizing the output, you'll need to provide some reporters or summarizers.

### Reporters

As your tests run, clojure.test passes messages to [`clojure.test/report`](https://clojure.github.io/clojure/clojure.test-api.html#clojure.test/report).
The default implementation prints relevant details to stdout.
Leiningen [enhances the output](https://github.com/technomancy/leiningen/blob/2.7.1/src/leiningen/test.clj#L96-L111) by including commands that can be used to re-run individual tests.

To override this output, you can provide a collection of reporters to test-report, which are called sequentially with each message reported during the test run.
For example, to pretty-print the message to stdout, add the following to the `:test` profile:

```clojure
:injections [(require 'clojure.pprint)]
:test-report {:reporters [clojure.pprint/pprint]}
```

Notice how namespaces not already used in your tests need to be required in an injection.

The resulting output, which might be useful when developing your own reporters, looks something like

```console
$ lein test
{:type :begin-test-ns,
 :ns #object[clojure.lang.Namespace 0x66fdec9 "example.test"],
 :time 45906184724820}
{:type :begin-test-var,
 :var #'example.test/arithmetic,
 :time 45906212298330}
{:type :pass,
 :expected (= 4 (+ 2 2)),
 :actual
 (#object[clojure.core$_EQ_ 0x3016fd5e "clojure.core$_EQ_@3016fd5e"]
  4
  4),
 :message nil,
 :time 45906216409445,
 :context ("with positive integers" "addition")}
{:type :pass,
 :expected (= 7 (+ 3 4)),
 :actual
 (#object[clojure.core$_EQ_ 0x3016fd5e "clojure.core$_EQ_@3016fd5e"]
  7
  7),
 :message nil,
 :time 45906232042175,
 :context ("with positive integers" "addition")}
{:type :end-test-var,
 :var #'example.test/arithmetic,
 :time 45906241926006}
{:type :end-test-ns,
 :ns #object[clojure.lang.Namespace 0x66fdec9 "example.test"],
 :time 45906243742590}
{:test 1,
 :pass 0,
 :fail 0,
 :error 0,
 :type :summary,
 :time 45906248308974}
```

Note that the final summary message does not have accurate counts; the original implementation of `clojure.test/report` is not being called, so the counters are not being incremented.
When implementing your own reporter, you may need to call [`clojure.test/inc-report-counter`](https://clojure.github.io/clojure/clojure.test-api.html#clojure.test/inc-report-counter) yourself.

The messages passed to the reporters have some extra information compared to those originally passed to `clojure.test/report`.
A nanosecond offset `:time` is added to all messages, which can be used to calculate the test execution time.
Test results (`:pass`, `:fail`, or `:error` messages) also have `:context`, which is the list of strings passed to [`clojure.test/testing`](https://clojure.github.io/clojure/clojure.test-api.html#clojure.test/testing), ordered from innermost to outermost.

### Summarizers

To produce output based on aggregate test results (for example, a [JUnit XML report](https://github.com/redbadger/test-report-junit-xml)), reporters would have to keep track of previous messages in a mutable data structure.
In this case, it may be simpler to use a summarizer.
Each summarizer is called at the end of the test run, and passed a vector of all the messages that were reported.
For example, to pretty-print the messages to stdout:

```clojure
:injections [(require 'clojure.pprint)]
:test-report {:summarizers [clojure.pprint/pprint]}
```

```console
$ lein test

Testing example.test

Ran 1 tests containing 2 assertions.
0 failures, 0 errors.
[{:type :begin-test-ns,
  :ns #object[clojure.lang.Namespace 0x1b73be9f "example.test"],
  :time 46558525944477}
 {:type :begin-test-var,
  :var #'example.test/arithmetic,
  :time 46558529380772}
 {:type :pass,
  :expected (= 4 (+ 2 2)),
  :actual
  (#object[clojure.core$_EQ_ 0x5ccbeb64 "clojure.core$_EQ_@5ccbeb64"]
   4
   4),
  :message nil,
  :time 46558532202149,
  :context ("with positive integers" "addition")}
 {:type :pass,
  :expected (= 7 (+ 3 4)),
  :actual
  (#object[clojure.core$_EQ_ 0x5ccbeb64 "clojure.core$_EQ_@5ccbeb64"]
   7
   7),
  :message nil,
  :time 46558533416008,
  :context ("with positive integers" "addition")}
 {:type :end-test-var,
  :var #'example.test/arithmetic,
  :time 46558534111792}
 {:type :end-test-ns,
  :ns #object[clojure.lang.Namespace 0x1b73be9f "example.test"],
  :time 46558534219611}
 {:test 1,
  :pass 2,
  :fail 0,
  :error 0,
  :type :summary,
  :time 46558534317550}]
```

The `test-report.summary/summarize` function collates the messages into a more usable form:

```clojure
:injections [(require 'clojure.pprint 'test-report.summary)]
:test-report {:summarizers [(comp clojure.pprint/pprint test-report.summary/summarize)]}
```

```console
$ lein test

Testing example.test

Ran 1 tests containing 2 assertions.
0 failures, 0 errors.
{:namespaces
 [{:ns
   #object[clojure.lang.Namespace 0x31000e60 "example.test"],
   :time 7656556,
   :tests
   [{:var #'example.test/arithmetic,
     :time 2690626,
     :results
     ({:type :pass,
       :expected (= 4 (+ 2 2)),
       :actual
       (#object[clojure.core$_EQ_ 0x42f8285e "clojure.core$_EQ_@42f8285e"]
        4
        4),
       :message nil,
       :context ("with positive integers" "addition")}
      {:type :pass,
       :expected (= 7 (+ 3 4)),
       :actual
       (#object[clojure.core$_EQ_ 0x42f8285e "clojure.core$_EQ_@42f8285e"]
        7
        7),
       :message nil,
       :context ("with positive integers" "addition")})}],
   :summary {:test 1, :assertion 2, :fail 0, :error 0, :pass 2}}],
 :summary {:test 1, :assertion 2, :fail 0, :error 0, :pass 2}}
```

## License

Copyright © 2017 Red Badger

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
