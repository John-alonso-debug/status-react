(ns status-im.ui.screens.group.views
  (:require-macros [status-im.utils.views :as views])
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.tabbar.styles :as main-tabs.styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.constants :as constants]
            [status-im.utils.platform :as utils.platform]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.search-input.view :as search]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.group.styles :as styles]))

(defn- render-contact [contact]
  [contact/contact-view {:contact             contact
                         :style               styles/contact
                         :accessibility-label :chat-member-item}])

(defn- on-toggle [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-contact public-key allow-new-users?])

   ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-contact public-key allow-new-users?])))

(defn- on-toggle-participant [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-participant public-key allow-new-users?])

   ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-participant public-key allow-new-users?])))

(defn- group-toggle-contact [allow-new-users? contact]
  [contact/toggle-contact-view
   contact
   :is-contact-selected?
   (partial on-toggle allow-new-users?)
   (and (not (:is-contact-selected? contact))
        (not allow-new-users?))])

(defn- group-toggle-participant [allow-new-users? contact]
  [contact/toggle-contact-view
   contact
   :is-participant-selected?
   (partial on-toggle-participant allow-new-users?)
   ;; Disable if not-checked and we don't allow new users
   (and (not (:is-participant-selected? contact))
        (not allow-new-users?))])

(defn- handle-invite-friends-pressed []
  (if utils.platform/desktop?
    (re-frame/dispatch [:navigate-to :new-contact])
    (list-selection/open-share {:message (i18n/label :t/get-status-at)})))

(defn toggle-list [{:keys [contacts render-fn]}]
  [react/scroll-view {:flex 1}
   (if utils.platform/desktop?
     (for [contact contacts]
       ^{:key (:public-key contact)}
       (render-fn contact))
     [list/flat-list {:style                     styles/contacts-list
                      :data                      contacts
                      :key-fn                    :address
                      :render-fn                 render-fn
                      :keyboardShouldPersistTaps :always}])])

(defn no-contacts []
  [react/view {:style styles/no-contacts}
   [react/text
    {:style styles/no-contact-text}
    (i18n/label :t/group-chat-no-contacts)]
   (when-not platform/desktop?
     [button/button
      {:type     :secondary
       :on-press handle-invite-friends-pressed
       :label    :t/invite-friends}])])

(views/defview bottom-container [{:keys [on-press disabled on-press-prev label accessibility-label]}]
  [react/view {:style styles/bottom-container}
   (when on-press-prev
     [react/view
      [button/button
       {:type                :previous
        :accessibility-label (or accessibility-label :previous-button)
        :label               (i18n/label :t/back)
        :on-press            on-press-prev}]])
   [react/view {:style components.styles/flex}]
   [react/view
    [button/button
     {:type                :next
      :accessibility-label (or accessibility-label :next-button)
      :label               label
      :disabled?           disabled
      :on-press            on-press}]]])

(defn filter-contacts [filter-text contacts]
  (let [lower-filter-text (string/lower-case (str filter-text))
        filter-fn         (fn [{:keys [name alias]}]
                            (or
                             (string/includes? (string/lower-case (str name)) lower-filter-text)
                             (string/includes? (string/lower-case (str alias)) lower-filter-text)))]
    (if filter-text
      (filter filter-fn contacts)
      contacts)))

;; Set name of new group-chat
(views/defview new-group []
  (views/letsubs [contacts   [:selected-group-contacts]
                  group-name [:new-chat-name]]
    (let [save-btn-enabled? (and (spec/valid? :global/not-empty-string group-name) (pos? (count contacts)))]
      [react/keyboard-avoiding-view (merge {:behavior :padding}
                                           styles/group-container)
       [toolbar/toolbar {}
        toolbar/default-nav-back
        [react/view {:style styles/toolbar-header-container}
         [react/view
          [react/text (i18n/label :t/new-group-chat)]]
         [react/view
          [react/text {:style styles/toolbar-sub-header}
           (i18n/label :t/group-chat-members-count
                       {:selected (inc (count contacts))
                        :max      constants/max-group-chat-participants})]]]]
       [react/view {:style {:padding-vertical 16
                            :flex             1}}
        [react/view {:style {:padding-horizontal 16}}
         [react/view add-new.styles/input-container
          [react/text-input
           {:auto-focus          true
            :on-change-text      #(re-frame/dispatch [:set :new-chat-name %])
            :default-value       group-name
            :placeholder         (i18n/label :t/set-a-topic)
            :style               add-new.styles/input
            :accessibility-label :chat-name-input}]]
         [react/text {:style styles/members-title}
          (i18n/label :t/members-title)]]
        [react/view {:style {:margin-top 8}}
         [list/flat-list {:data                         contacts
                          :key-fn                       :address
                          :render-fn                    render-contact
                          :bounces                      false
                          :keyboard-should-persist-taps :always
                          :enable-empty-sections        true}]]]
       [react/view {:style styles/bottom-container}
        [react/view
         [button/button
          {:type                :previous
           :accessibility-label :previous-button
           :label               (i18n/label :t/back)
           :on-press            #(re-frame/dispatch [:navigate-back])}]]
        [react/view {:style components.styles/flex}]
        [react/view
         [button/button
          {:type                :secondary
           :container-style     {:padding-horizontal 16}
           :text-style          {:font-weight "500"}
           :accessibility-label :create-group-chat-button
           :label               (i18n/label :t/create-group-chat)
           :disabled?           (not save-btn-enabled?)
           :on-press            #(re-frame/dispatch [:group-chats.ui/create-pressed group-name])}]]]])))

(defn searchable-contact-list []
  (let [search-value (reagent/atom nil)]
    (fn [{:keys [contacts toggle-fn allow-new-users?]}]
      [react/view {:style {:flex 1}}
       [react/view {:style styles/search-container}
        [search/search-input {:on-cancel #(reset! search-value nil)
                              :on-change #(reset! search-value %)}]]
       [react/view {:style {:flex             1
                            :padding-vertical 8}}
        (if (seq contacts)
          [toggle-list {:contacts  (filter-contacts @search-value contacts)
                        :render-fn (partial toggle-fn allow-new-users?)}]
          [no-contacts])]])))

;; Start group chat
(views/defview contact-toggle-list []
  (views/letsubs [contacts                [:contacts/active]
                  selected-contacts-count [:selected-contacts-count]]
    [react/keyboard-avoiding-view {:style styles/group-container}
     [toolbar/toolbar {:border-bottom-color :white}
      toolbar/default-nav-back
      [react/view {:style styles/toolbar-header-container}
       [react/view
        [react/text (i18n/label :t/new-group-chat)]]
       [react/view
        [react/text {:style styles/toolbar-sub-header}
         (i18n/label :t/group-chat-members-count
                     {:selected (inc selected-contacts-count)
                      :max      constants/max-group-chat-participants})]]]]
     [searchable-contact-list
      {:contacts         contacts
       :toggle-fn        group-toggle-contact
       :allow-new-users? (< selected-contacts-count
                            (dec constants/max-group-chat-participants))}]
     [react/view {:style styles/bottom-container}
      [react/view {:style components.styles/flex}]
      [react/view
       [button/button
        {:type                :next
         :accessibility-label :next-button
         :label               (i18n/label :t/next)
         :disabled?           (zero? selected-contacts-count)
         :on-press            #(re-frame/dispatch [:navigate-to :new-group])}]]]]))

;; Add participants to existing group chat
(views/defview add-participants-toggle-list []
  (views/letsubs [contacts                        [:contacts/all-contacts-not-in-current-chat]
                  {:keys [name] :as current-chat} [:chats/current-chat]
                  selected-contacts-count         [:selected-participants-count]]
    (let [current-participants-count (count (:contacts current-chat))]
      [react/keyboard-avoiding-view {:style styles/group-container}
       [toolbar/toolbar {:border-bottom-color :white}
        toolbar/default-nav-back
        [react/view {:style styles/toolbar-header-container}
         [react/view
          [react/text (i18n/label :t/add-members)]]
         [react/view
          [react/text {:style styles/toolbar-sub-header}
           (i18n/label :t/group-chat-members-count
                       {:selected (+ current-participants-count selected-contacts-count)
                        :max      constants/max-group-chat-participants})]]]]
       [searchable-contact-list
        {:contacts         contacts
         :toggle-fn        group-toggle-participant
         :allow-new-users? (< (+ current-participants-count
                                 selected-contacts-count)
                              constants/max-group-chat-participants)}]
       [react/view {:style styles/bottom-container}
        [react/view {:style styles/bottom-container-center}
         [button/button
          {:type                :secondary
           :accessibility-label :next-button
           :label               (i18n/label :t/add)
           :disabled?           (zero? selected-contacts-count)
           :on-press            #(re-frame/dispatch [:group-chats.ui/add-members-pressed])}]]]])))

(defonce new-group-chat-name (atom nil))

(views/defview edit-group-chat-name []
  (views/letsubs [{:keys [name chat-id] :as current-chat} [:chats/current-chat]]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:title       :t/edit-group
       :modal?      true}]
     [react/view add-new.styles/new-chat-container
      [react/view add-new.styles/new-chat-input-container
       [react/text-input
        {:on-change-text #(reset! new-group-chat-name %)
         :default-value name
         :on-submit-editing #(when (seq @new-group-chat-name)
                               (re-frame/dispatch [:group-chats.ui/name-changed chat-id @new-group-chat-name]))
         :placeholder         (i18n/label :t/enter-contact-code)
         :style               add-new.styles/input
         ;; This input is fine to preserve inputs
         ;; so its contents will not be erased
         ;; in onWillBlur navigation event handler
         :preserve-input?     true
         :accessibility-label :enter-contact-code-input
         :return-key-type     :go}]]
      [react/view {:width 16}]]]))
