(ns viasat.cwmp-cpe.stateful-device.specs
  (:require [clojure.spec.alpha :as s]))

(s/def :viasat.cwmp-cpe/tr069-parameter-name string?)
(s/def :viasat.cwmp-cpe/tr069-parameter-value any?)
(s/def :viasat.cwmp-cpe/tr069-parameter-value-source
  #{:cpe :acs})

(s/def :viasat.cwmp-cpe.stateful-device/spvs
  (s/map-of :viasat.cwmp-cpe/tr069-parameter-name
            :viasat.cwmp-cpe/tr069-parameter-value))
(s/def :viasat.cwmp-cpe.stateful-device/spv-sources
  (s/map-of :viasat.cwmp-cpe/tr069-parameter-name
            :viasat.cwmp-cpe/tr069-parameter-value-source))
