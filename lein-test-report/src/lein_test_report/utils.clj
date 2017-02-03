(ns lein-test-report.utils
  (:require [leiningen.core.project :as project]))

(defn add-profile [project profile]
  (if (some #{profile} (-> project meta :included-profiles))
    project
    (project/merge-profiles project [profile])))
