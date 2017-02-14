(defproject test-report "0.2.0-SNAPSHOT"
  :description "Library providing hooks to customise clojure.test output"
  :url "https://github.com/redbadger/test-report"
  :scm {:dir ".."}
  :license {:name "Eclipse Public License", :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-shade "0.2.0"]]
  :profiles {:uberjar {:aot :all}
             :provided {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :shaded {:dependencies [[robert/hooke "1.3.0"]]
                      :shade {:namespaces [robert]}}
             :default [:leiningen/default :shaded]}
  :aliases {"deploy" ["deploy-shaded-jar" "clojars"]})
