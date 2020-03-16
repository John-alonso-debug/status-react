(ns status-im.utils.theme
  (:require [status-im.react-native.js-dependencies :as js-dependencies]
            [oops.core :refer [oget]]))

(def event-emitter (oget js-dependencies/react-native-dark-mode "eventEmitter"))
(def initial-mode (atom (oget js-dependencies/react-native-dark-mode "initialMode")))

(defn add-mode-change-listener [callback]
  (.on event-emitter "currentModeChanged" #(do (reset! initial-mode %)
                                               (callback (keyword %)))))

(defn is-dark-mode []
  (= @initial-mode "dark"))