(ns status-im.ui.screens.group.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.styles :as styles]
            [status-im.utils.platform :as platform]))

(def tabs-height
  (cond
    platform/android? 52
    platform/ios? 52
    platform/desktop? 36))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color colors/white})

(def contact
  {:padding-left 0})

(def contacts-list
  {:background-color colors/white})

(def no-contact-text
  {:margin-bottom     20
   :margin-horizontal 50
   :text-align        :center
   :color             colors/gray})

(def number-of-participants-disclaimer
  {:margin-top        20
   :margin-bottom     5
   :font-size         12
   :margin-horizontal 17})

(def bottom-container
  {:align-items      :center
   :justify-content  :space-between
   :flex-direction   :row
   :height           tabs-height
   :background-color colors/white
   :border-top-width 1
   :border-top-color colors/gray-lighter})

(def bottom-container-center
  {:align-items     :center
   :justify-content :center
   :flex            1})

(def toolbar-header-container
  {:align-items :center})

(def toolbar-sub-header
  {:color colors/gray})

(def no-contacts
  {:flex 1
   :justify-content :center
   :align-items :center})

(def search-container
  {:border-bottom-color colors/gray-lighter
   :border-bottom-width 1})

(def members-title
  {:color         colors/gray
   :margin-top    14
   :margin-bottom 4})
