# Markdown Style Guide

This document outlines the markdown formatting standards for this project to ensure consistency and compliance with markdownlint.

## General Rules

1. **One H1 per document**: Each file should have exactly one H1 heading (`#`) at the top
2. **Blank lines**: Add blank lines before and after:
   - Headings
   - Code blocks
   - Lists
   - Blockquotes
3. **No trailing spaces**: Remove all trailing whitespace
4. **Consistent line endings**: Use Unix-style line endings (LF)

## Headings

### Correct

```markdown
# Document Title

## Section Title

### Subsection Title
```

### Incorrect

```markdown
#Document Title
##Section Title
### Subsection Title
```

## Lists

### Unordered Lists

- Use `-` for bullets (not `*` or `+`)
- Maintain consistent indentation (2 spaces)
- Add blank line before and after list

```markdown
Here is a list:

- First item
- Second item
  - Nested item
  - Another nested item
- Third item

More content follows.
```

### Ordered Lists

```markdown
Steps to follow:

1. First step
2. Second step
3. Third step

Next section.
```

## Code Blocks

### Fenced Code Blocks

Always specify the language:

```markdown
```java
public class Example {
    // Code here
}
```

### Inline Code

Use single backticks:

```markdown
Use the `getInstance()` method to get the singleton.
```

## Links and References

### Inline Links

```markdown
Visit the [official documentation](https://example.com) for more info.
```

### Reference Links

```markdown
Visit the [official documentation][docs] for more info.

[docs]: https://example.com
```

## Tables

```markdown
| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Data 1   | Data 2   | Data 3   |
| Data 4   | Data 5   | Data 6   |
```

## Emphasis

- **Bold**: Use `**text**` (not `__text__`)
- *Italic*: Use `*text*` (not `_text_`)
- ***Bold Italic***: Use `***text***`

## Common Markdownlint Rules

### MD001: Heading levels should only increment by one level at a time

✅ Correct:

```markdown
# Title
## Section
### Subsection
```

❌ Incorrect:

```markdown
# Title
### Subsection (skipped h2)
```

### MD003: Heading style should be consistent (ATX style)

✅ Correct: `## Heading`

❌ Incorrect: `Heading\n------`

### MD009: No trailing spaces

✅ Correct: `This is text.`

❌ Incorrect: `This is text.` (with trailing spaces)

### MD010: No hard tabs

✅ Correct: Use spaces for indentation

❌ Incorrect: Using tab characters

### MD012: No multiple consecutive blank lines

✅ Correct:

```markdown
Paragraph 1

Paragraph 2
```

❌ Incorrect:

```markdown
Paragraph 1


Paragraph 2
```

### MD022: Headings should be surrounded by blank lines

✅ Correct:

```markdown
Previous content.

## Heading

Next content.
```

### MD031: Fenced code blocks should be surrounded by blank lines

✅ Correct:

```markdown
Here is some code:

```java
// Code
```

More text.

### MD032: Lists should be surrounded by blank lines

✅ Correct:

```markdown
Here is a list:

- Item 1
- Item 2

More content.
```

## File Naming

- Use lowercase with hyphens: `my-document.md`
- For project docs, UPPERCASE is acceptable: `README.md`, `LICENSE.md`
- Be descriptive but concise

## Tools

To check markdown files:

```bash
# Install markdownlint-cli
npm install -g markdownlint-cli

# Check all markdown files
markdownlint '**/*.md'

# Fix auto-fixable issues
markdownlint '**/*.md' --fix
```

## VS Code Extensions

Recommended extensions:

- **markdownlint** by David Anson
- **Markdown All in One** by Yu Zhang

## Configuration

Create `.markdownlint.json` in project root:

```json
{
  "default": true,
  "MD013": false,
  "MD033": false,
  "no-hard-tabs": true
}
```
