# Exception handling in Clojure

- Java style handling 
  ```clojure
  (try .... 
    (catch ExceptionClass ex 
      ....))
  ```
- Handling Exception as value
  - [Hand roll own implementation](./src/error_handling/hand_rolled/protocol_impl.clj)
  - Clojure libraries
    - [Flow](https://github.com/fmnoise/flow)
      - [Demo](./src/error_handling/flow.clj)
    - [Failjure](https://github.com/adambard/failjure)
- Advanced error handling techniques
  - Interceptors 
    - [Pedestal interceptors](https://pedestal.io/pedestal/0.6/guides/what-is-an-interceptor.html)
    - [Exoscale interceptor](https://github.com/exoscale/interceptor)
    - [Metosin Sieppari](https://github.com/metosin/sieppari)
    - [Papilon](https://github.com/lambda-toolshed/papillon)
      - [YT Talk](https://www.youtube.com/watch?v=bDN898hu_wQ)
