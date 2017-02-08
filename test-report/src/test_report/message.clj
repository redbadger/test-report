(ns test-report.message)

(def types
  "Hierarchy of clojure.test/report message types."
  (->
    (make-hierarchy)
   (derive :pass :result)
    (derive :fail :result)
    (derive :error :result)))

(defn result?
  "Checks if the given message contains a test result."
  [message]
  (isa? types (:type message) :result))
