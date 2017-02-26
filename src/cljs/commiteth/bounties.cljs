(ns commiteth.bounties
  (:require [re-frame.core :as rf]))



(defn pr-url [{owner :owner_login
               pr-number :pr_number
               repo :repo_name}]
  (str "https://github.com/" owner "/" repo "/pull/" pr-number))

(defn claim-card [bounty claim]
  (let [{pr-state :pr_state
         user-name :user_name
         avatar-url :user_avatar_url
         issue-id :issue_id
         issue-title :issue_title} claim
        merged? (= 1 (:pr_state claim))
        paid? (not-empty (:payout_hash claim))
        confirming? (:confirming? bounty)]
    (println "paid?" paid? "merged?" merged? (and merged? ((comp not) paid?)))
    [:div.activity-item
     [:div.ui.grid.container
      [:div.left-column
       [:div.ui.circular.image
        [:img {:src avatar-url}]]]
      [:div.content
       [:div.header user-name]
       [:div.description "Submitted a claim for " [:a {:href (pr-url claim)}
                                                   issue-title]]
       [:div.description (if paid?
                           "(paid)"
                           (str "(" (if merged? "merged" "open") ")"))]
       [:div.time "1 day ago"]   ;; TODO: claim timestamp
       [:button.ui.button
        (merge (if (and merged? (not paid?))
                 {}
                 {:disabled true})
               {:on-click #(rf/dispatch [:confirm-payout claim])}
               (when confirming?
                 {:class "busy loading" :disabled true}))
        (if paid?
          "Signed off"
          "Confirm")]]]]))


(defn claim-list [bounties]
  (if (empty? bounties)
    [:div.ui.text "No items"]
    (into [:div.activity-item-container]

          (for [bounty bounties
                claim (:claims bounty)]
            ;; TODO: for paid bounties, only show the winning claim
            [claim-card bounty claim]))))


(defn bounties-page []
  (let [owner-bounties (rf/subscribe [:owner-bounties])]
    (fn []
      (let [bounties (vals @owner-bounties)
            unpaid? #(empty? (:payout_hash %))
            paid? #(not-empty (:payout_hash %))
            unpaid-bounties (filter unpaid? bounties)
            paid-bounties (filter paid? bounties)]
        [:div.ui.container
         [:h3 "New claims"]
         [claim-list unpaid-bounties]
         [:h3 "Old claims"]
         [claim-list paid-bounties]]))))
