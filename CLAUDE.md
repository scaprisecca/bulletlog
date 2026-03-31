# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

Bulletlog is a personal fork of Logseq (the open-source knowledge management app). This fork **intentionally preserves markdown files as the storage format** and does not adopt the upstream SQLite/DB-based graph system. All work should remain compatible with file-based graphs only. Do not suggest or introduce SQLite storage, DB-version graph handlers, or RTC sync code into new features.

## Build & Dev Commands

```bash
# Start development server (browser)
yarn watch

# Start Electron dev
yarn electron-watch

# Start mobile dev (Capacitor)
yarn mobile-watch

# Run all tests
yarn test

# Run a single test (filter by regex)
yarn cljs:test && node static/tests.js -r "YOUR_NAMESPACE_OR_TEST_PATTERN" -e fix-me

# ClojureScript lint
clojure -M:clj-kondo --parallel --lint src

# Full lint suite
bb lint:dev

# CSS lint
yarn style:lint

# Production builds
yarn release           # browser
yarn release-app       # Electron
yarn release-mobile    # mobile
```

Tests live in `src/test/frontend/**/*_test.cljs`. The nREPL port for the shadow-cljs watcher is **8701**.

## Architecture Overview

### Stack
- **Language**: ClojureScript (with some Clojure for scripts/tooling)
- **UI**: Rum (thin React wrapper), React components
- **In-memory DB**: DataScript (Datalog-based, used for all querying)
- **Markdown parsing**: mldoc (OCaml parser compiled to JS via `deps/graph-parser`)
- **Styling**: Tailwind CSS
- **Mobile**: Capacitor (`util/capacitor?` detects mobile at runtime)
- **Desktop**: Electron

### Directory Structure

```
src/main/frontend/   — Main ClojureScript app (UI, handlers, DB layer)
src/main/electron/   — Electron main process
src/main/mobile/     — Mobile-specific UI code
src/main/logseq/     — Public plugin API (logseq.api)
src/test/frontend/   — Tests
deps/graph-parser/   — Parses markdown/org-mode files → DataScript datoms
deps/db/             — Core DataScript schema, queries, Datalog rules
deps/outliner/       — Block tree editing operations
deps/common/         — Shared utilities (used across frontend and deps)
deps/shui/           — UI component library (Storybook)
```

Each `deps/` subdirectory is an independent Clojure library with its own `deps.edn` and tests. The main app references them as local roots.

### Data Flow (File-Based Graph — the only kind used here)

1. **Load**: Markdown files are read from disk via FilesAPI (browser), Electron's Node fs, or Capacitor's Filesystem plugin
2. **Parse**: `logseq.graph-parser.mldoc` runs mldoc to produce an AST
3. **Extract**: `logseq.graph-parser.extract` converts AST → blocks, properties, page refs
4. **Transact**: Datoms are written into the DataScript in-memory database (`frontend.db.conn`)
5. **Query**: UI components query via DataScript Datalog rules in `logseq.db.frontend.rules` and model functions in `frontend.db.model`
6. **React**: `frontend.db.react` wires DataScript reactive queries to Rum component re-renders
7. **Persist**: Edits flow back through `frontend.handler.editor` → `frontend.handler.file` → file system write

### Key Namespace Groups

| Group | Namespace prefix | Purpose |
|---|---|---|
| Handlers | `frontend.handler.*` | All user-action logic (editor, page, file, events) |
| DB queries | `frontend.db.*` | DataScript queries, transactions, reactive subscriptions |
| Components | `frontend.components.*` | Rum/React UI components |
| Format | `frontend.format.*` | Markdown/org-mode format utilities |
| Graph parser | `logseq.graph-parser.*` | File parsing pipeline (in `deps/graph-parser`) |
| DB schema | `logseq.db.*` | DataScript schema and rules (in `deps/db`) |
| Outliner | `logseq.outliner.*` | Block-level tree operations (in `deps/outliner`) |

### Platform Detection

```clojure
(util/capacitor?)    ; true on Android/iOS (Capacitor native shell)
(util/electron?)     ; true on desktop Electron
```

Mobile uses `@capacitor/filesystem` for file I/O; Electron uses Node's `fs`. Browser uses IndexedDB-backed virtual filesystem.

### State Management

Global app state lives in `frontend.state` (a Rum atom). DataScript reactions (`frontend.db.react`) handle data-driven UI updates. The two systems are kept distinct — UI/UX state goes in `frontend.state`, content/graph data goes in DataScript.
