# Tasks: Native Journal Calendar Widget

**PRD:** `build-plan/prd-journal-calendar-widget.md`  
**Branch:** `calendar-feature`  
**Status:** In Progress

---

## Relevant Files

| File | Status | Notes |
|------|--------|-------|
| `src/main/frontend/components/journal_calendar.cljs` | **Create** | New shared calendar widget component (desktop + mobile) |
| `src/main/frontend/components/left_sidebar.cljs` | Modify | Add `sidebar-journal-calendar` between Navigations and Favorites |
| `src/main/mobile/components/app.cljs` | Modify | Wrap `home` component to render calendar above `all-journals` |
| `deps/db/src/logseq/db/common/initial_data.cljs` | Modify | Add `get-journal-days-for-month` query function |
| `deps/db/src/logseq/db.cljs` | Modify | Re-export `get-journal-days-for-month` from the public DB namespace |
| `src/main/frontend/components/container.css` | Modify | Add `.day-has-journal` dot styles and mobile touch-target overrides |

### Notes

- ClojureScript tests live alongside source in `src/test/`. There is no Jest in this project — the test runner is `cljs-test-runner` invoked via `yarn test`.
- The codebase uses **Rum** (a ClojureScript React wrapper). Components are defined with `rum/defc`. Reactive components that need to re-render on DB changes use the `rum/reactive` mixin and `db-mixins/query`.
- `shui/calendar` is a **react-day-picker** wrapper. Its `modifiers` prop accepts either a JS Date array or a predicate function. Verify which form is accepted during Task 1 investigation before implementing Task 3.
- State for collapsible sidebar sections is stored at `[:ui/navigation-item-collapsed? "class-name"]` and toggled via `state/toggle-navigation-item-collapsed!`. This same pattern is reused for the calendar.

---

## Tasks

- [x] 1.0 Investigate & Resolve Open Questions
  - [x] 1.1 Confirmed: `home` component in `app.cljs` (line 40–45) is the correct target. No intermediate wrapper.
  - [x] 1.2 Confirmed: `shui/calendar` passes all props straight through to react-day-picker's DayPicker (via `adapt-class`/`map-keys->camel-case`). Both predicate functions and `js/Date` arrays are valid for `modifiers`. Using predicate (set-membership fn). Documented in `journal_calendar.cljs`.
  - [x] 1.3 Confirmed: `js-date->journal-title` uses `(journal-name (t/to-default-time-zone date))` with `state/get-date-formatter` — handles all configured formats correctly. No wrapper needed.
  - [x] 1.4 Found: `logseq.common.util.date-time/date->int` already exists. No new utility needed.

- [x] 2.0 DB Layer: Add Month Journal Query
  - [x] 2.1 Added `get-journal-days-for-month` to `deps/db/src/logseq/db/common/initial_data.cljs`.
  - [x] 2.2 Not needed — `date-time-util/date->int` already exists.
  - [x] 2.3 Re-exported from `deps/db/src/logseq/db.cljs` (line after `get-latest-journals`).
  - [ ] 2.4 Manually verify in REPL/browser (runtime check — pending manual test).

- [x] 3.0 Build Shared `journal-calendar` Widget Component
  - [x] 3.1 Created `src/main/frontend/components/journal_calendar.cljs`.
  - [x] 3.2 Added `navigate-to-journal-day!` private helper.
  - [x] 3.3 Uses `date-time-util/date->int` directly (no new helper needed).
  - [x] 3.4 Defined `journal-calendar` component with `rum/reactive db-mixins/query`, month state, predicate-based modifiers.
  - [x] 3.5 Month state change triggers re-render which re-computes `journal-days` with new year/month.
  - [x] 3.6 Wrapped in `[:div.journal-calendar-widget ...]`.

- [x] 4.0 Desktop Integration: Left Sidebar
  - [x] 4.1 Added require for `journal-calendar` in `left_sidebar.cljs`.
  - [x] 4.2 Defined `sidebar-journal-calendar` component using `sidebar-content-group` with class `"journal-calendar"` and `collapsable? true`.
  - [x] 4.3 Inserted `(sidebar-journal-calendar)` in `sidebar-container` between navigations and favorites, guarded with `enable-journals?`.
  - [x] 4.4 Added `"journal-calendar" true` to `:ui/navigation-item-collapsed?` default map in `state.cljs`.
  - [ ] 4.5 Manually test on desktop (pending runtime test).

- [x] 5.0 Mobile Integration: Home Tab
  - [x] 5.1 Added require for `journal-calendar` in `app.cljs`.
  - [x] 5.2 Created `mobile-journal-calendar-section` component with collapsible header and chevron.
  - [x] 5.3 Modified `home` to render `[:div#mobile-home (mobile-journal-calendar-section) (component-with-restoring ...)]`.
  - [x] 5.4 Added `"mobile-journal-calendar" true` to `:ui/navigation-item-collapsed?` default map (same edit as 4.4).
  - [ ] 5.5 Native top-bar button unaffected — `open-journal-calendar!` in `header.cljs` is untouched (verify at runtime).
  - [ ] 5.6 Manually test on Android (pending runtime test).

- [x] 6.0 Styling: Dot Indicators & Mobile Touch Targets
  - [x] 6.1 Added `.day-has-journal::after` dot rule to `container.css`.
  - [x] 6.2 Added `.journal-calendar-widget` with `max-width: 100%; overflow-x: hidden`.
  - [x] 6.3 Added `@media (hover: none)` override for `min-height/min-width: 44px`.
  - [ ] 6.4 Visually review today highlight vs dot (pending runtime test).
  - [ ] 6.5 Test light/dark themes (pending runtime test).
