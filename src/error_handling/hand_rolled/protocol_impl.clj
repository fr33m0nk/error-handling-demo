(ns error-handling.hand-rolled.protocol-impl
  (:require [clojure.string :as str]))

(defprotocol IResult
  (then [not-exception f] "Invoke f with not-exception as arg")
  (else [exception f] "Invoke f with exception as arg"))

(extend-protocol IResult
  Exception
  (then [this _] this)
  (else [this f]
    (try
      (f this)
      (catch Exception e
        e)))

  Object
  (then [this f]
    (try
      (f this)
      (catch Exception ex
        ex)))
  (else [this _]
    this))

(defn switch
  [x success-fn failure-fn]
  (if (instance? Exception x)
    (failure-fn x)
    (success-fn x)))


(comment

  ;; Success
  (-> 10
      (then inc)
      (then #(do
               (println "Executed when previous ops does not throw exception")
               (inc %)))
      (else #(ex-message %)))

  ;; Error
  ;; Short circuiting when error occurs
  (-> 10
      (then inc)
      (then #(/ % 0))
      (then #(do
               (println "Executed when previous ops does not throw exception")
               (inc %)))
      (else #(do
               (println "Do whatever with exception")
               (ex-message %))))

  ;; ROP
  ;; Error and recover and then process the rest of pipeline
  (-> 10
      (then inc)
      (then #(/ % 0))
      (then #(do
               (println "Executed when previous ops does not throw exception")
               (inc %)))
      (else #(ex-message %))
      (then str/lower-case))

  )
