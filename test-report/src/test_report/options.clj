(ns test-report.options)

(defn- options->bindings [options]
  `(dissoc (zipmap (map #(or (ns-resolve ~*ns* (symbol (str "*" (name %) "*")))
                             (throw (IllegalArgumentException. (str "Unknown option " %))))
                        (keys ~options))
                   (vals ~options))
           nil))

(defmacro with-options
  "Executes the body with options bound to their corresponding dynamic vars.
  Equivalent to clojure.core/with-bindings, except the map keys are given as
  :foo rather than #'*foo*."
  [options & body]
  `(with-bindings ~(options->bindings options)
     ~@body))
