(ns status-im.ui.components.contact.contact
  (:require [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.contact.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(defn desktop-extended-options [options]
  [react/view {}
   (doall (for [{:keys [label action]} options]
            ^{:key label}
            [react/touchable-highlight
             {:on-press action}
             [react/view {}
              [react/text label]]]))])

(defn format-name [{:keys [ens-verified name public-key]}]
  (if ens-verified
    (str "@" (or (stateofus/username name) name))
    (gfycat/generate-gfy public-key)))

(defn contact-view
  [{:keys [style contact extended? on-press extend-options
           info show-forward? accessibility-label inner-props]
    :or   {accessibility-label :contact-item}}]
  [react/touchable-highlight (merge {:accessibility-label accessibility-label}
                                    (when-not extended?
                                      {:on-press (when on-press #(on-press contact))}))
   [react/view {:style styles/contact-container}
    [chat-icon/contact-icon-contacts-tab contact]
    [react/view styles/info-container-to-refactor
     [react/text (merge {:style           styles/name-text
                         :number-of-lines 1}
                        inner-props)
      (or (format-name contact) (i18n/label :t/chat-name))]]
    (when show-forward?
      [react/view styles/forward-btn
       [vector-icons/icon :main-icons/next]])
    (when info
      [react/text {:style styles/info-text}
       info])
    (when (and extended? extend-options)
      [react/view
       [react/touchable-highlight {:on-press            extend-options
                                   :accessibility-label :menu-option}
        [vector-icons/icon :main-icons/more {:accessibility-label :options}]]])]])

(views/defview toggle-contact-view
  [{:keys [public-key] :as contact} selected-key on-toggle-handler disabled?]
  (views/letsubs [checked [selected-key public-key]]
    [react/view {:accessibility-label :contact-item}
     [list/list-item-with-checkbox
      {:checked?        checked
       :disabled?       disabled?
       :on-value-change #(on-toggle-handler checked public-key)}
      [list/item
       [chat-icon/contact-icon-contacts-tab contact]
       [react/view styles/info-container
        [react/text {:style           styles/name-text
                     :number-of-lines 1}
         (or (format-name contact) (i18n/label :t/chat-name))]]]]]))
