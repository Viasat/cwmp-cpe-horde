(ns viasat.cwmp-cpe.basic-cpe-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [viasat.cwmp-cpe.basic-cpe :as basic-cpe]
            [viasat.cwmp-cpe.messages :as messages]
            [viasat.cwmp-cpe.messages.inform-events :as informs]
            [viasat.cwmp-cpe.stateful-device.atom :as stateful-device-atom]
            [viasat.cwmp-cpe.util.tr069 :as tr069-util]
            [viasat.cwmp-cpe.util.value-finders :as value-finders]
            [viasat.cwmp-cpe.util.xml :as xml-util]))

(defn handle-fake-acs [stateful-device cpe-message]
  (let [device-id (:device-id @stateful-device)
        ;; XXX add more event flows
        acs-response-body (cond
                            ;; Respond to BOOTSTRAP with FactoryReset.
                            (value-finders/find-value (partial = "0 BOOTSTRAP") cpe-message)
                            (do
                              (swap! stateful-device update :messages-received conj :BOOTSTRAP)
                              (messages/soap-envelope (tr069-util/generate-msg-id "FR" device-id) [:cwmp:FactoryReset]))

                            ;; Respond to FactoryResetResponse with empty body, which is an offer to end the session.
                            (value-finders/find-value (partial = :cwmp:FactoryResetResponse) cpe-message)
                            (do
                              (swap! stateful-device update :messages-received conj :FactoryResetReponse)
                              nil)

                            ;; The CPE has offered to end the TR-069 session.
                            ;; We have no further ACS messages to send, so respond
                            ;; with an empty body, which ends the session.
                            (empty? cpe-message) nil

                            :else (throw (ex-info (format "giving up on handle-fake-acs: %s" cpe-message)
                                                  {:cause :handle-fake-acs-unhandled
                                                   :cpe-message :cpe-message})))]
    (if acs-response-body
      {:status 200 :body (xml-util/xml->map-xml acs-response-body)}
      ;; offer to end the session
      {:status 204})))

(deftest inform-session!-test
  (let [use-cases [{:description "bootstrap session with noop ACS"
                    :inform-body-fn informs/compose-bootstrap-boot
                    :send-request-fn (constantly {:status 204})
                    :expected-stateful-device {}}
                   {:description "bootstrap session with basic ACS"
                    :inform-body-fn informs/compose-bootstrap-boot
                    :expected-stateful-device {:messages-received [:FactoryResetReponse :BOOTSTRAP]}}]]
    (doseq [{:keys [description inform-body-fn send-request-fn expected-stateful-device]} use-cases
            :let [device (stateful-device-atom/stateful-device-atom "fefefe012345" {} {:acs-url "fake-acs-url"})
                  acs-state (atom {})
                  send-request-fn (or send-request-fn
                                      (fn send-request-fn* [_device-id _url {:keys [body]}]
                                        (handle-fake-acs acs-state body)))]]
      (testing description
        (is (= :inform-session!-done
               (basic-cpe/inform-session! device
                                          "inform-session-test-url"
                                          (inform-body-fn device)
                                          {:send-request-fn send-request-fn})))
        (is (match? expected-stateful-device @acs-state))))))

