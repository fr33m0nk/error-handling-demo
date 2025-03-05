(ns error-handling.flow
  (:require
    [fmnoise.flow :as f]
    [clojure.string :as str]))

(comment

  ;; Success
  (->> 10
       ;; f/then is for a function that does not throw
       (f/then inc)
       (f/then #(do
                  (println "Executed when previous ops does not throw exception")
                  (inc %)))
       (f/else #(ex-message %)))

  ;; Error
  ;; Short circuiting when error occurs
  (->> 10
       ;; f/then is for a function that does not throw
       (f/then inc)
       ;; f/then-call is for a function that can throw
       (f/then-call #(/ % 0))
       (f/then #(do
                  (println "Executed when previous ops does not throw exception")
                  (inc %)))
       (f/else #(do
                  (println "Do whatever with exception")
                  (ex-message %))))

  ;; ROP
  ;; Error and recover and then process the rest of pipeline
  (->> 10
       (f/then inc)
       (f/then-call #(/ % 0))
       (f/then #(do
                  (println "Executed when previous ops does not throw exception")
                  (inc %)))
       (f/else #(ex-message %))
       (f/then str/lower-case))

  ;; thru
  (->> 10
       (f/then inc)
       (f/then-call #(/ % 0))
       (f/then #(do
                  (println "Executed when previous ops does not throw exception")
                  (inc %)))
       ;; there's f/thru-call for fns that can throw
       (f/thru #(do (println "I will always be called ") %))
       (f/else #(do (println "Exception handler called")  (ex-message %)))
       (f/then str/lower-case))

  ;; Exception handling
  (->> "10"
       (f/then-call inc)
       (f/then #(do
                  (println "Executed when previous ops does not throw exception")
                  (inc %)))
       ;; there's f/thru-call for fns that can throw
       (f/thru #(do (println "I will always be called ") %))
       (f/else-if ClassCastException #(do (println "ClassCastException handler called")  (ex-message %)))
       ;; there's f/else-call for fns that can throw
       (f/else #(do (println "Fallback called")  (ex-message %)))
       (f/then str/lower-case))

  ;; Not a fan of threading macro or then and then-call?
  (defn working-with-flet
    [x]
    (f/flet [a (inc x)
             b (inc a)]
      (println "Executed when previous ops does not throw exception")
      (inc b)))

  (let [result (working-with-flet 10)]
    (if (f/fail? result)
      ;; Handle, throw, pass on
      (str "Fail " (ex-message result))
      (str "Success " result)))

  ;; Want some switch like stuff
  (f/switch  {:ok  #(str "Success " %)
              :err #(str "Fail " (ex-message %))}
             (working-with-flet "10"))

  ;; Create errors
  (->> "10"
       (f/then-call inc)
       (f/then #(do
                  (println "Executed when previous ops does not throw exception")
                  (inc %)))
       (f/thru #(do (println "I will always be called ") %))
       (f/else-if ClassCastException
                  #(do (println "ClassCastException handler called")
                       ;; f/fail-with returns exception as value
                       ;; f/fail-with! throws exception
                       (f/fail-with!
                         {:msg "User not found" :data {:id 1} :cause %})))
       (f/else #(do (println "Fallback called") (ex-message %)))
       (f/then str/lower-case))


  ;; Combine constructs
  ;; Write functions that return failures
  (defn validate-email [email]
    (if (re-matches #".+@.+\..+" email)
      email
      (f/fail-with {:msg (format "Invalid email address (got %s)" email)})))

  (defn validate-not-empty [s]
    (if (empty? s)
      (f/fail-with {:msg "Empty string provided"})
      s))

  (defn validate-data
    [data]
    (f/flet [email (validate-email (:email data))
             username (validate-not-empty (:username data))
             id (parse-long (:id data))]
      {:email email
       :username username}))

  (defn logger
    [x]
    (f/switch {:ok (partial println "logging success - ")
               :err (comp (partial println "logging failure - ") ex-message)}
              x))

  ;; Some handler
  (->> {:email "jason@bourne.com"
        :username "jason"
        :id "10" #_:10}
       validate-data
       (f/then (partial hash-map :status 200 :data))
       (f/thru logger)
       (f/else #(assoc {} :status 400 :error (ex-message %))))
  )

;; Closing remark: draw parallel to js Promise
