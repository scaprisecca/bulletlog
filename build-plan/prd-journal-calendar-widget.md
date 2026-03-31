# PRD: Native Journal Calendar Widget

**Feature Branch:** `calendar-feature`  
**Status:** Draft  
**Last Updated:** 2026-03-30

---

## 1. Introduction / Overview

Bulletlog is a markdown-first note-taking app (forked from Logseq) with a daily journaling workflow at its core. Users currently have no at-a-glance way to see which days have journal entries or quickly jump to a specific day's journal without typing the date.

The logseq-journals-calendar plugin solves this on desktop but requires a separate plugin installation and does not work on Android/iOS. This feature bakes an equivalent calendar widget **natively into the app** so it works out of the box on all platforms.

The widget shows a monthly calendar. Days with existing journal entries display a dot indicator. Clicking any day navigates to that day's journal — creating the page if it does not yet exist.

---

## 2. Goals

1. Let users see at a glance which days have journal entries (dot indicators per day).
2. Let users navigate to any day's journal with a single tap/click.
3. Work natively on **desktop** (left sidebar) and **Android/iOS** (home screen).
4. Require zero configuration — the widget should be immediately usable with no setup.
5. Not break or replace any existing functionality (editor date-picker, native top-bar calendar button).

---

## 3. User Stories

| # | Story |
|---|-------|
| US-1 | As a desktop user, I want a calendar in the left sidebar so I can see which days I have journal entries without leaving my current page. |
| US-2 | As a mobile user, I want a calendar at the top of my Journals home screen so I can quickly jump to a specific day's journal. |
| US-3 | As any user, I want a dot on calendar days that have journal entries so I can see my journaling history at a glance. |
| US-4 | As any user, I want clicking/tapping a day to take me directly to that day's journal (and create it automatically if it doesn't exist yet) so I don't have to type the date manually. |
| US-5 | As any user, I want to be able to navigate to future dates and pre-create journal pages so I can plan ahead. |
| US-6 | As a desktop user, I want the calendar section to be collapsible so I can hide it when I need more sidebar space for other sections. |

---

## 4. Functional Requirements

### 4.1 Shared (Desktop + Mobile)

| # | Requirement |
|---|-------------|
| FR-1 | The calendar **must** display one month at a time in a standard grid layout (Sun–Sat or Mon–Sun based on locale). |
| FR-2 | The calendar **must** show prev/next month navigation controls. |
| FR-3 | The calendar **must** default to the current month when first rendered. |
| FR-4 | The calendar **must** display today's date with a distinct highlight (accent color). |
| FR-5 | Any day that has an existing journal entry in the DataScript database **must** show a small dot indicator below its date number. |
| FR-6 | The dot indicators **must** update reactively — if the user creates a journal entry while the calendar is visible, the dot for that day must appear without a page reload. |
| FR-7 | Tapping/clicking a day **must** navigate to that day's journal page. |
| FR-8 | If no journal page exists for the selected day, the app **must** automatically create one and then navigate to it (no confirmation prompt). |
| FR-9 | The calendar **must** query only the currently displayed month's journal data (not all journals) for performance. |
| FR-10 | The calendar **must** respect the user's configured journal date formatter (set in app settings) when generating journal page names for navigation. |
| FR-11 | All future dates **must** be tappable/clickable (same behavior as present and past dates). |

### 4.2 Desktop — Left Sidebar

| # | Requirement |
|---|-------------|
| FR-12 | The calendar **must** be rendered as a new collapsible section in the left sidebar. |
| FR-13 | The section header **must** be labeled **"Calendar"** and follow the same visual pattern as existing sections ("Favorites", "Recent Pages"). |
| FR-14 | The calendar section **must** be positioned **between** the Navigations section and the Favorites section. |
| FR-15 | The calendar section **must default to collapsed** on first launch. |
| FR-16 | The collapsed/expanded state **must** be persisted to localStorage so it survives app restarts. |
| FR-17 | The calendar section **must not** be rendered when journals are disabled in app settings (`enable-journals?` = false). |

### 4.3 Mobile — Home Tab

| # | Requirement |
|---|-------------|
| FR-18 | The calendar **must** be rendered as a collapsible section **at the top of the Journals/Home tab**, above the journal entries feed. |
| FR-19 | The calendar section **must default to collapsed** on first launch. |
| FR-20 | The collapsed/expanded state **must** be persisted (localStorage) across sessions. |
| FR-21 | All tap targets (day cells, prev/next arrows) **must** be at least **44×44 px** to meet mobile accessibility standards. |
| FR-22 | The existing native top-bar "calendar" button (which opens the system date picker) **must continue to work** alongside the new widget — the two are complementary, not redundant. |
| FR-23 | The calendar **must** work on both Android and iOS (no platform-specific code paths needed for the widget itself). |

---

## 5. Non-Goals (Out of Scope)

- **Week view** — monthly view only.
- **Journal content preview on hover/long-press** — future feature.
- **Sync status indicators** on calendar days (e.g., unsynced changes).
- **Replacing the editor date-picker** — the calendar widget is for navigation only; the inline date-picker used when writing `[[date]]` references is unchanged.
- **Replacing the native top-bar date picker** on mobile.
- **Multi-graph calendar** — only the active graph's journals are shown.
- **Custom dot colors** or any per-day metadata beyond existence.

---

## 6. Design Considerations

### Visual Style

- Follow existing Bulletlog/Logseq design tokens (Tailwind CSS + CSS custom properties).
- The collapsible section header must match the `.sidebar-content-group` pattern already used in `left_sidebar.cljs`.
- On desktop, the calendar must fit within the sidebar width without horizontal scrolling.
- Dot indicator: a small filled circle (≈4–6 px diameter) centered below the day number, using the app's accent color (`var(--ls-accent-color)` or equivalent).
- On mobile, use the same Tailwind classes and `shui` component primitives to keep visual consistency with the rest of the mobile UI.

### Collapsed State Appearance

When collapsed, only the section header row ("Calendar") is visible with the standard chevron icon indicating it can be expanded.

### Component Reuse

The `shui/calendar` (react-day-picker) already exists in the codebase and is used by the editor date-picker. This feature should use the same base component with `modifiers` and `modifiersClassNames` props to render the dot indicators, rather than building a custom calendar from scratch.

---

## 7. Technical Considerations

### New File

- `src/main/frontend/components/journal_calendar.cljs` — The standalone calendar widget component, shared between desktop and mobile.

### Files to Modify

| File | Change |
|------|--------|
| `src/main/frontend/components/left_sidebar.cljs` | Add `sidebar-journal-calendar` component call between Navigations and Favorites sections |
| `deps/db/src/logseq/db.cljs` or `src/main/frontend/db/model.cljs` | Add `get-journal-days-for-month` query — returns a set of YYYYMMDD integers for a given year+month |
| `src/main/mobile/components/home.cljs` (or equivalent journals home view) | Add collapsible calendar section above the journals list |
| `src/main/frontend/state.cljs` | Add state key `:ui/journal-calendar-collapsed?` (or reuse existing navigation-item-collapsed pattern) |
| CSS (container.css or equivalent) | Add `.journal-calendar-widget` and `.day-has-journal` dot styles |

### DB Query Design

```clojure
;; Returns a set of YYYYMMDD integers for all journal pages in the given year+month
(defn get-journal-days-for-month [db year month]
  (let [start (int (str year (util/zero-pad month) "01"))
        end   (int (str year (util/zero-pad month) "31"))]
    (->> (d/datoms db :avet :block/journal-day)
         (keep (fn [d]
                 (let [v (:v d)]
                   (when (and (>= v start) (<= v end))
                     v))))
         (set))))
```

### Dot Rendering via react-day-picker Modifiers

```clojure
;; Inside journal-calendar component
(let [journal-days-set (get-journal-days-for-month db year month)
      has-journal? (fn [^js date]
                     (let [d (date-util/js-date->int date)]
                       (contains? journal-days-set d)))]
  (shui/calendar
   {:modifiers {:has-journal has-journal?}
    :modifiers-class-names {:has-journal "day-has-journal"}
    ;; ... other props
    }))
```

### Navigation on Day Click

```clojure
(defn- navigate-to-journal-day! [^js js-date]
  (let [page-name (date/js-date->journal-title js-date)]
    (if-let [existing (db/get-page page-name)]
      (route-handler/redirect-to-page! (:block/uuid existing))
      (p/let [page (page-handler/<create! page-name {:redirect? false})]
        (route-handler/redirect-to-page! (:block/uuid page))))))
```

### Reactive Subscription Pattern

The component should use `rum/reactive` + a DataScript subscription (via `db-mixins/query`) so that dot indicators update automatically when new journal pages are created in the current month.

### Mobile Home Location

Investigate `src/main/mobile/` for the component that renders the journals list on the Home tab. The calendar section should be injected at the top of that component's render output, wrapped in the same collapsible pattern as the desktop version.

---

## 8. Success Metrics

| Metric | Target |
|--------|--------|
| Feature works on desktop (Electron/web) | All FR-1 through FR-17 pass manual testing |
| Feature works on Android | All FR-18 through FR-23 pass on Android emulator or device |
| Feature works on iOS | All FR-18 through FR-23 pass on iOS simulator or device |
| No regression on existing date-picker | Editor `[[date]]` picker continues to work identically |
| No regression on native mobile top-bar calendar | Native date picker still opens on calendar icon tap |
| Performance | Month data query completes in < 50 ms on a graph with 1000+ journal pages |

---

## 9. Open Questions

| # | Question | Owner |
|---|----------|-------|
| OQ-1 | What is the exact component/file that renders the mobile Home tab journals list? Needs investigation to find the correct injection point. | Dev |
| OQ-2 | Does `shui/calendar` (react-day-picker) support a function-based `modifiers` value, or must it be an array of Date objects? Needs a quick test. | Dev |
| OQ-3 | Should the calendar section also appear in the right sidebar (shift+click on Journals)? Not in scope for v1 but worth noting. | Product |
| OQ-4 | The app supports multiple date format configurations. Does `js-date->journal-title` already handle all configured formats, or do we need additional handling? | Dev |
