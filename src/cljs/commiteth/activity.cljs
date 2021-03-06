(ns commiteth.activity
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]))


(defn item-description [{:keys [display-name
                                balance
                                issue-title
                                item-type
                                repo-owner
                                repo-name
                                issue-number] :as item}]
  (let [issue-link [:a
                    {:href (issue-url repo-owner repo-name issue-number)}
                    issue-title]]
    (case item-type
      "new-bounty" [:div "New bounty opened for issue " issue-link]
      "claim-payout" [:div "Received ETH " balance
                      " for " issue-link]
      "open-claim" [:div "Submitted a claim for " issue-link]
      "balance-update" [:div issue-link " bounty increased to ETH " balance]
      "")))


(defn activity-item [{:keys [avatar-url
                             display-name
                             updated
                             balance
                             issue-title
                             item-type] :as  item}]
  [:div.item.activity-item
   [:div.ui.mini.circular.image
    [:img {:src avatar-url}]]
   [:div.content
    [:div.header.display-name display-name]
    [:div.description
     [item-description item]]
    [:div.footer-row
     (when-not (= item-type "new-bounty")
       [:div.balance-badge (str "ETH " balance )])
     [:div.time (moment-timestamp updated)]]]])



(defn activity-list [activity-items]
      [:div.ui.container
       (if (empty? activity-items)
         [:div.ui.text "No data"]
         (into [:div.ui.items]
               (for [item activity-items]
                 [activity-item item])))]  )

(defn activity-page []
  (let [activity-items (rf/subscribe [:activity-feed])
        activity-feed-loading? (rf/subscribe [:get-in [:activity-feed-loading?]])]
    (fn []
      (if @activity-feed-loading?
        [:div
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader "Loading"]]]
        [activity-list @activity-items]))))
