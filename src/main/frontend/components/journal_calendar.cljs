(ns frontend.components.journal-calendar
  "Shared journal calendar widget used in both desktop sidebar and mobile home tab.

   Investigation notes (Task 1):
   - 1.1 Mobile injection point: `mobile.components.app/home` (app.cljs line 40-45) is the
         correct target. No intermediate wrapper needed.
   - 1.2 shui/calendar modifier API: The Calendar component is a thin wrapper over react-day-picker's
         DayPicker. All extra props (including `modifiers` and `modifiersClassNames`) are passed
         straight through after kebab->camelCase conversion by adapt-class. The `modifiers` prop
         accepts a map from modifier-name -> matcher, where a matcher can be a `(fn [^js Date] bool)`
         predicate OR an array of `js/Date` objects. We use a predicate set-membership fn here.
   - 1.3 `frontend.date/js-date->journal-title` calls `(journal-name (t/to-default-time-zone date))`
         which uses `(state/get-date-formatter)` — it correctly produces the user-configured journal
         page name for all built-in date formatters. No wrapper needed.
   - 1.4 `logseq.common.util.date-time/date->int` already exists and converts a js/Date to a
         YYYYMMDD integer. Used directly; no new utility needed.

   NOTE: rum/reactive and db-mixins/query produce a class component via rum/build_class.
   React hooks (rum/use-state) cannot be called inside class component renders.
   Use rum/defcs + rum/local instead — rum/local stores an atom in class component state
   and is fully compatible with class-based mixins."
  (:require [frontend.date :as date]
            [frontend.db :as db]
            [frontend.db-mixins :as db-mixins]
            [frontend.handler.page :as page-handler]
            [frontend.handler.route :as route-handler]
            [logseq.common.util.date-time :as date-time-util]
            [logseq.db :as ldb]
            [logseq.shui.ui :as shui]
            [promesa.core :as p]
            [rum.core :as rum]))

(defn navigate-to-journal-day!
  "Navigate to the journal page for the given js/Date, creating it if needed."
  [^js js-date]
  (let [page-name (date/js-date->journal-title js-date)]
    (if-let [journal (db/get-page page-name)]
      (route-handler/redirect-to-page! (:block/uuid journal))
      (p/let [page (page-handler/<create! page-name {:redirect? false})]
        (route-handler/redirect-to-page! (:block/uuid page))))))

(rum/defcs journal-calendar < rum/reactive db-mixins/query
  (rum/local (js/Date.) ::displayed-month)
  (rum/local nil ::cached-journal-days)
  [state & [{:keys [on-day-click]}]]
  (let [displayed-month* (::displayed-month state)
        cached*          (::cached-journal-days state)
        displayed-month  @displayed-month*
        year             (.getFullYear displayed-month)
        ;; js/Date month is 0-based; :block/journal-day months are 1-based
        month            (inc (.getMonth displayed-month))
        journal-days     (let [[cy cm cdays] (or @cached* [])]
                           (if (and (= cy year) (= cm month) cdays)
                             cdays
                             (when-let [db (db/get-db)]
                               (let [days (ldb/get-journal-days-for-month db year month)]
                                 (reset! cached* [year month days])
                                 days))))
        has-journal?     (fn [^js d]
                           (contains? journal-days (date-time-util/date->int d)))]
    [:div.journal-calendar-widget
     (shui/calendar
      {:mode                  "single"
       :month                 displayed-month
       :on-month-change       #(reset! displayed-month* %)
       :on-day-click          (or on-day-click navigate-to-journal-day!)
       :modifiers             {:has-journal has-journal?}
       :modifiers-class-names {:has-journal "day-has-journal"}})]))
