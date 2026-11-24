---
name: read-user-story
description: "Read and extract a user story from a confluence prd (id =13271042), so the Requirements Agent can turn it into structured requirements. Use at the start of Step 1 of the SDLC pipeline, or whenever a new user story must be ingested before writing requirements."
user-invocable: true
---

# Skill: Read User Story
Use this skill to reliably ingest a user story from any supported source and extract the fields the Requirements Agent needs.

## Sources and how to read them

### 1. Confluence page
Use the Atlassian MCP tools (e.g. `confluence_get_page`) with the page ID or title. Extract the story body and any acceptance-criteria / business-rules tables.

### 2. Local `.docx` file (fallback, e.g. `userstory.docx`)
Use when no Confluence access is available. Extract the raw text by unzipping the `.docx` and stripping XML tags from its `word/document.xml` part:

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path 'userstory.docx'))
$entry = $zip.Entries | Where-Object { $_.FullName -eq 'word/document.xml' }
$reader = New-Object System.IO.StreamReader($entry.Open())
$xml = $reader.ReadToEnd(); $reader.Close(); $zip.Dispose()
[System.Text.RegularExpressions.Regex]::Replace($xml, '<[^>]+>', ' ') `
  -replace '\s+', ' '
```
## Fields to extract
- **Title** and the "As a … I want … so that …" statement
- **Description / context**
- **Acceptance criteria** (Given / When / Then)
- **Business rules and constraints** (security, compliance, HTTPS, masking, etc.)
- **Priority**
- **Dependencies** (other features, services, APIs, modules)
- **Actors / stakeholders**

## Output
Hand the extracted content to the Requirements Agent as structured notes. Flag anything missing, vague, or conflicting as an **open question** — never invent business-critical behavior. Preserve source wording for acceptance criteria so they stay traceable.