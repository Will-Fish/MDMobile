# Typora-like Editor Design

## Context

MDMobile already has a Markdown workspace with edit, preview, split view, outline, export, image insertion, undo/redo, autosave, and WebView-based preview rendering. The current editing pane is a plain `BasicTextField`, while preview is a separate rendered surface. This creates a repeated repair loop: editing and preview are two different experiences, so rendering problems or formatting surprises are discovered only after switching views.

The selected direction is to keep the existing write/preview/split modes, but make the write mode itself feel close to Typora. The preview and split modes remain available for final rendering checks and large-screen comparison.

## Goal

Create a comfortable Markdown writing surface where the document mostly looks rendered while the source Markdown remains the single source of truth. The active cursor line reveals Markdown syntax markers in a muted style. Non-active lines prioritize readable formatted text.

## Non-Goals

- Do not replace the app with a full HTML `contenteditable` editor.
- Do not create a block editor that changes the Markdown text mental model.
- Do not attempt full CommonMark layout parity in the editable surface.
- Do not remove preview or split mode.
- Do not change file storage format; files remain plain Markdown.

## Recommended Approach

Refactor `EditorPane` into a Compose-native `TyporaEditorPane`.

The editor continues to own a `TextFieldValue`, autosave still writes `editorValue.text`, and toolbar actions still insert Markdown snippets into the source text. A display layer builds an `AnnotatedString` from the source text and current selection. It applies Markdown-aware visual styling without changing the underlying text.

The active line is detected from `TextFieldValue.selection`. On that line, syntax markers such as heading hashes, list bullets, blockquote markers, emphasis delimiters, inline code backticks, and link/image delimiters stay visible but use a low-contrast color. On inactive lines, common Markdown structures are styled toward their rendered appearance.

## Editing Surface Behavior

Supported in the first implementation:

- Headings: larger font, heavier weight, clear vertical rhythm; heading markers muted outside the active line.
- Paragraphs: normal body text using the user font size preference.
- Bold and italic: inline text styling for simple `**text**`, `__text__`, `*text*`, and `_text_` ranges.
- Inline code: monospace font, subtle background-colored span where Compose support allows; otherwise monospace and accent color.
- Lists: bullet or number markers muted, content indented enough to scan comfortably.
- Blockquotes: muted `>` marker and quoted text color; if feasible, add a subtle left indicator through row decoration.
- Fenced code blocks: monospace styling across the block; fence lines are muted, especially when inactive.
- Links and images: link text emphasized, URL delimiters muted outside active line.

Fallback behavior:

- Tables, raw HTML, footnotes, nested edge cases, malformed Markdown, and complex mixed syntax remain readable as text with light syntax coloring.
- Preview mode remains the source of truth for exact CommonMark/WebView rendering.

## Architecture

Add a small editor-rendering layer under `ui/components`:

- `TyporaEditorPane.kt`: Compose component replacing the current plain `EditorPane`.
- `MarkdownEditStyler.kt`: pure Kotlin functions that analyze the source text and return line/token style ranges.
- `MarkdownEditStyles.kt` or equivalent local model types: data classes for line ranges, token ranges, and active-line metadata.

`ReaderScreen.kt` keeps ownership of file loading, saving, undo/redo, toolbar actions, outline selection, mode switching, and export. It should delegate editor visual behavior to `TyporaEditorPane` instead of growing more editor-specific parsing code inside the screen.

`MarkdownRenderer.kt` remains responsible for final preview HTML. The editable display layer can share small slug or heading helpers only if that reduces duplication without coupling the editor to WebView rendering.

## Data Flow

1. `ReaderScreen` loads Markdown into `TextFieldValue`.
2. `TyporaEditorPane` receives the value and current font size.
3. `MarkdownEditStyler` computes active line and style ranges from `value.text` and `value.selection`.
4. The editor displays an annotated text representation while preserving the original text and selection.
5. User input calls `onValueChange`, updating the original `TextFieldValue`.
6. Autosave, undo/redo, outline extraction, preview, split view, export, and sharing continue to use raw Markdown text.

## Error Handling

The styler must be defensive. Malformed Markdown should never prevent typing. If a construct is incomplete or ambiguous, style only the clearly recognized part and leave the rest as normal text. Regex parsing must operate line-by-line or in bounded ranges so large documents do not cause noticeable typing lag.

## Performance

The first implementation should avoid reparsing the full document into an AST on every keystroke. A lightweight line scanner is acceptable because source text is already held in memory. Styling should be memoized with `remember(value.text, value.selection, fontSize, colorScheme)` at the component boundary. If large documents show typing lag, the next step is to style only visible text or nearby line windows.

## Testing

Unit tests should cover the pure styling layer first:

- active line detection from cursor position and selected ranges
- heading marker ranges and content ranges
- list and blockquote marker ranges
- fenced code block range detection, including blank lines inside fences
- simple bold, italic, inline code, link, and image token ranges
- malformed/incomplete Markdown does not throw

Existing preview HTML tests stay in place. Add only focused UI tests where Compose can verify integration: typing updates source text, toolbar insertion still edits raw Markdown, and preview/split mode can still receive the updated content.

## Acceptance Criteria

- Write mode visually resembles rendered Markdown for common notes.
- The active line reveals Markdown markers in a muted style.
- Raw `.md` content is preserved exactly when typing, saving, undoing, redoing, inserting toolbar snippets, exporting, or opening preview.
- Preview and split modes still work.
- Existing Markdown renderer HTML tests pass.
- New styler tests cover the common formatting cases and malformed input fallback.
