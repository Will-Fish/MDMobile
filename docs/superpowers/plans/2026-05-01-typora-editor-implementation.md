# Typora-like Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the plain Markdown write pane with a Compose-native Typora-like editor that keeps raw Markdown as the saved source.

**Architecture:** Add a pure Kotlin styling layer that scans Markdown into line and token ranges, then use a `VisualTransformation` in a new `TyporaEditorPane` so the editable text keeps identity offsets. `ReaderScreen` keeps all document state, saving, preview, split view, and toolbar ownership.

**Tech Stack:** Kotlin, Jetpack Compose, `BasicTextField`, `VisualTransformation`, JUnit 4.

---

### Task 1: Markdown Edit Styler

**Files:**
- Create: `app/src/main/java/com/example/mdmobile/ui/components/MarkdownEditStyler.kt`
- Test: `app/src/test/java/com/example/mdmobile/MarkdownEditStylerTest.kt`

- [ ] Write failing tests for active line detection, headings, lists, blockquotes, fenced code, inline emphasis/code/link/image, and malformed fallback.
- [ ] Run `gradle testDebugUnitTest --tests com.example.mdmobile.MarkdownEditStylerTest` and verify the tests fail because the styler does not exist.
- [ ] Implement focused line scanning and bounded inline token detection.
- [ ] Run the styler test class and verify it passes.

### Task 2: Compose Typora Editor Pane

**Files:**
- Create: `app/src/main/java/com/example/mdmobile/ui/components/TyporaEditorPane.kt`

- [ ] Create `TyporaEditorPane` with the same public inputs as the old private `EditorPane`.
- [ ] Use `BasicTextField` with identity `VisualTransformation` built from `MarkdownEditStyler`.
- [ ] Preserve placeholder, scroll, focus-lost saving, cursor brush, and keyboard options.
- [ ] Style active-line markers muted and inactive common Markdown structures toward rendered appearance.

### Task 3: ReaderScreen Wiring

**Files:**
- Modify: `app/src/main/java/com/example/mdmobile/ui/screens/ReaderScreen.kt`

- [ ] Import `TyporaEditorPane`.
- [ ] Replace both `EditorPane` call sites with `TyporaEditorPane`.
- [ ] Remove the old private `EditorPane` function and now-unused imports.
- [ ] Keep preview and split mode behavior unchanged.

### Task 4: Verification

**Commands:**
- `gradle testDebugUnitTest --tests com.example.mdmobile.MarkdownEditStylerTest`
- `gradle testDebugUnitTest --tests com.example.mdmobile.MarkdownRendererHtmlTest`
- `gradle assembleDebug`

**Known baseline:** Full `testDebugUnitTest` currently has pre-existing failures in `MarkdownCompatibilityTest`; do not use it as the success gate for this change unless those old tests are fixed separately.
