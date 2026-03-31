# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Calendar icon button in the header toolbar that opens a popup calendar for quick journal navigation from any page
- Native journal calendar widget for desktop sidebar and mobile home tab, displaying journal entries for the current month with day-by-day indicators and navigation
- `get-journal-days-for-month` database query function to efficiently fetch all journal pages in a given month using the journal-day datoms index

### Changed
- Left sidebar now includes a collapsible calendar section (between Navigations and Favorites) when journals are enabled
- Mobile home tab now displays an expandable calendar at the top of the journals feed

---
