# YouTrack Workflow Analyzer

When a YouTrack workflow rule blocks an action, users see cryptic error messages. This tool uses GPT-4 to explain what went wrong and links directly to the problematic workflow rules.

## What it does

You send it an error message like "Cannot move DEMO-42 to In Progress" along with the issue ID. The analyzer:

1. Fetches the actual issue data from YouTrack (status, assignee, priority, etc.)
2. Grabs all workflow rules for that project
3. Builds an enriched prompt with real context and sends it to GPT-4
4. Returns a human-readable explanation with clickable links to the specific workflow rules


## Tech stack

- **Kotlin** + **Ktor** for the REST API
- **Koog 0.5.2** for LLM integration (wraps OpenAI SDK)
- **GPT-4** for analysis
- **Repository pattern** for YouTrack API access
- **Redis** for caching (infrastructure exists, didn't have time to implement)

## Quick start

1. **Set up environment**:

```bash
# Create .env file
YOUTRACK_BASE_URL=https://your-instance.myjetbrains.com/youtrack
YOUTRACK_API_TOKEN=perm:your-token-here
OPENAI_API_KEY=sk-your-key-here
```

2. **Run it**:

```bash
./gradlew run
```

3. **Test with a real issue**:

```bash
curl -X POST http://localhost:8080/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "errorMessage": "Cannot move to In Progress",
    "issueId": "DEMO-42"
  }'
```

Response includes:
- Plain English explanation of what went wrong
- List of workflow rules that likely blocked the action
- Direct URLs to those rules in YouTrack admin UI

## API

### `POST /analyze`

**With issue context** (recommended):
```json
{
  "errorMessage": "Cannot transition to Done",
  "issueId": "DEMO-42"
}
```

The analyzer fetches the issue, detects its project, and includes all relevant workflow rules in the LLM prompt.

**With project context only**:
```json
{
  "errorMessage": "State change blocked",
  "projectId": "DEMO"
}
```

Gets workflow rules for the project but can't provide issue-specific context.

**Basic analysis** (no YouTrack data):
```json
{
  "errorMessage": "Workflow error occurred"
}
```

Falls back to generic analysis without real rule data.

## Response format

```json
{
  "explanation": "The transition requires an assignee to be set before moving to In Progress state.",
  "workflowRules": [
    {
      "name": "Require Assignee for In Progress",
      "description": "State machine rule that enforces assignee requirement",
      "ruleUrl": "https://youtrack.../admin/workflows/rules/123-456-789"
    }
  ],
  "suggestedActions": [
    "Assign the issue to a team member first",
    "Then retry the state transition"
  ]
}
```

## How it works internally

**WorkflowAnalyzer** orchestrates everything:

1. Takes `issueId` or `projectId` from the request
2. Fetches issue data via `IssueRepository` (if issueId provided)
3. Fetches workflow rules via `WorkflowRepository`
4. Builds enriched prompt with actual YouTrack data
5. Sends to GPT-4 (via Koog)
6. Parses LLM response for rule names mentioned
7. Maps those names to actual `WorkflowRule` objects using fuzzy matching
8. Generates admin URLs for each matched rule

**The prompt matters most**. I include:
- Issue ID, summary, current state, assignee, priority
- Up to 15 workflow rules with their guards, actions, and requirements
- The user's error description

GPT-4 sees the full context and identifies which rules likely caused the problem.

## Project structure

```
app/src/main/kotlin/com/youtrack/analyzer/
├── Main.kt                           # Ktor server setup, dependency wiring
├── ai/
│   └── WorkflowAnalyzer.kt          # Core business logic
├── api/
│   ├── dto/                         # Request/response models
│   └── routes/
│       └── AnalysisRoutes.kt        # POST /analyze endpoint
├── domain/
│   ├── model/                       # Issue, Workflow, WorkflowRule
│   └── repository/                  # Repository interfaces
└── infrastructure/
    ├── config/                      # Configuration loading
    ├── repository/                  # YouTrack repository implementations
    └── youtrack/                    # YouTrack API client, DTOs, mappers
```

Clean layering: API → Service → Repository → External API

## Development

**Build**:
```bash
./gradlew build
```

**Run tests**:
```bash
./gradlew test
```

**Run locally**:
```bash
./gradlew run
```

Server starts on `http://localhost:8080`

## Testing with real YouTrack data

1. Make sure you have a YouTrack instance with issues and workflow rules
2. Create an API token with read permissions (Issues, Projects, Workflows)
3. Add credentials to `.env`
4. Pick an issue that has workflow rules in its project
5. Try to analyze an error for that issue

The analyzer will fetch real data and show you which rules are relevant.


## Requirements

- **JDK 17+**
- **YouTrack instance** (cloud or on-premise)
- **OpenAI API key** with GPT-4 access
- **Gradle** (wrapper included)

## Troubleshooting

**"Issue not found" error**:
- Check that `YOUTRACK_BASE_URL` is correct
- Verify the issue ID exists in YouTrack
- Ensure API token has Read Issue permission

**"Missing issueId or projectId" error**:
- The API requires at least one of these parameters
- Without them, the analyzer can't fetch YouTrack data

**LLM timeout or errors**:
- Verify `OPENAI_API_KEY` is valid
- Check your OpenAI account has credits
- GPT-4 API access is required

## License

JetBrains internship assessment task
