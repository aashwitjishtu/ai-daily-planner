# Architecture Overview

## System Design

```
┌─────────────────────────────────────────────────────────────┐
│                    DESKTOP USER                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
         ┌───────────────────────────────┐
         │   Tkinter GUI (main.py)       │
         │   ┌──────────────────────┐    │
         │   │ Task Management UI   │    │
         │   │ - Add Tasks          │    │
         │   │ - View Tasks         │    │
         │   │ - Delete Tasks       │    │
         │   │ - Local Suggestions  │    │
         │   └──────────────────────┘    │
         │   ┌──────────────────────┐    │
         │   │ API Integration      │    │
         │   │ - Get Recommendations│    │
         │   │ - Log Events         │    │
         │   │ - Get Analytics      │    │
         │   └──────────────────────┘    │
         └──────────────────┬─────────────┘
                            │
                    ┌───────┴──────────┐
                    │                  │
                    ▼                  ▼
         ┌──────────────────┐  ┌──────────────────┐
         │  Local Storage   │  │   FastAPI Backend│
         │                  │  │ (Optional)       │
         │ ┌──────────────┐ │  │                  │
         │ │tasks.json    │ │  │ ┌──────────────┐ │
         │ │(JSON format) │ │  │ │/recommendations│
         │ └──────────────┘ │  │ ├──────────────┤ │
         │ ┌──────────────┐ │  │ │/tasks         │ │
         │ │Auto-created  │ │  │ ├──────────────┤ │
         │ │on first run  │ │  │ │/events        │ │
         │ └──────────────┘ │  │ ├──────────────┤ │
         │                  │  │ │/analytics     │ │
         └──────────────────┘  │ └──────────────┘ │
                               │                  │
                               │ Python/FastAPI  │
                               │ (uvicorn)       │
                               │ Port: 8000      │
                               └──────────────────┘
                                       │
                                       ▼
                          ┌──────────────────────┐
                          │  Future Integrations:│
                          │ - Firebase Auth      │
                          │ - Firestore (DB)     │
                          │ - Cloud Functions    │
                          │ - Analytics          │
                          └──────────────────────┘
```

## Data Flow

### Create Task
```
User Input (GUI)
    ↓
Validate (planner/utils.py)
    ↓
Create Task object (planner/scheduler.py)
    ↓
Save to JSON (data/tasks.json)
    ↓
Refresh UI (gui/main_ui.py)
```

### Get AI Suggestion
```
User clicks "Suggest Next Task"
    ↓
Load all tasks from JSON
    ↓
AI Suggester analyzes (planner/ai_suggester.py)
    ├─ Filter overdue/today's tasks
    ├─ Sort by priority
    └─ Return highest priority task
    ↓
Display in GUI
```

### Get Recommendations (Backend)
```
User clicks "Get AI Recommendations"
    ↓
GUI sends POST request to backend
    ├─ User ID
    ├─ Limit (5)
    └─ Context
    ↓
FastAPI Server (backend/app/main.py)
    ↓
Routes handler (backend/app/routes.py)
    ↓
Recommendation Logic (backend/app/recommendations.py)
    ├─ Load seed items
    ├─ Score items
    └─ Return ranked list
    ↓
GUI displays results
```

## Module Responsibilities

### planner/scheduler.py
- **Task Class**: Represents individual task
- **Scheduler Class**: Manages all tasks
- **Functions**: 
  - `load_tasks()` - Load from JSON
  - `save_tasks()` - Save to JSON
  - `add_task()` - Add new task
  - `delete_task()` - Remove task
  - `get_tasks_sorted()` - Return sorted tasks

### planner/ai_suggester.py
- **AISuggester Class**: AI recommendation engine
- **Functions**:
  - `suggest_next_task()` - Smart next task (overdue first, then priority)
  - `suggest_schedule()` - Full day schedule (priority + due date)

### planner/utils.py
- `validate_date()` - Validate YYYY-MM-DD format
- `format_date()` - Convert to readable format (July 21, 2025)

### gui/main_ui.py
- **PlannerGUI Class**: Main UI window
- **Widgets**:
  - Header with user info
  - Task input fields
  - Action buttons
  - Task list (Treeview)
  - Suggestion/recommendation labels
- **Methods**:
  - `create_widgets()` - Build UI
  - `add_task()` - Handle task creation
  - `delete_task()` - Handle task deletion
  - `refresh_task_list()` - Update display
  - `show_suggestion()` - Show next task
  - `get_recommendations()` - Fetch from API

### backend/app/main.py
- FastAPI app setup
- CORS middleware configuration
- Route registration
- Health check endpoint

### backend/app/routes.py
- `/recommendations` - Get AI recommendations
- `/events` - Log user events
- `/analytics/me` - Get user analytics
- `/tasks` - Task CRUD operations
- `/health` - Server health check

### backend/app/schemas.py
- Pydantic models for data validation
- `RecommendationRequest/Response`
- `EventIn/Out`
- `AnalyticsOut`
- `TaskCreate/TaskOut`

### backend/app/recommendations.py
- `get_recommendations()` - Generate recommendations
- Seed item database
- Scoring algorithm

## Data Models

### Task (Local JSON)
```json
{
  "title": "Complete project",
  "due_date": "2026-06-25",
  "priority": 1,
  "created_at": "2026-06-23 10:30:45"
}
```

### Recommendation Item (API)
```json
{
  "item_id": "t1",
  "title": "Deep Work Sprint",
  "category": "focus",
  "tags": ["work", "focus"],
  "estimated_minutes": 45,
  "score": 0.92,
  "reasons": ["Matches preferences"]
}
```

## Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Frontend** | Tkinter + ttkbootstrap | Desktop GUI |
| **Task Logic** | Python (planner/) | Core business logic |
| **Backend** | FastAPI | REST API |
| **Server** | Uvicorn | ASGI server |
| **Local Storage** | JSON | Simple persistence |
| **Data Validation** | Pydantic | Schema validation |
| **HTTP Client** | requests | API calls |
| **Auth (Future)** | Firebase Admin SDK | User authentication |
| **DB (Future)** | Firestore/PostgreSQL | Persistent storage |

## Deployment Paths

### Path 1: Desktop App Only ✓ (Current)
- No backend needed
- All data local
- Runs offline

### Path 2: Desktop + Backend (Optional)
- Backend provides recommendations
- Still uses local storage
- Optional cloud features

### Path 3: Full Cloud (Future)
- Desktop app + Cloud backend
- Database in cloud (Firebase/PostgreSQL)
- Authentication enabled
- Multi-user support

## Performance Characteristics

| Operation | Time | Storage |
|-----------|------|---------|
| Add Task | ~10ms | Per task: 200 bytes |
| Suggest Next | ~1ms | -  |
| Load 100 tasks | ~50ms | ~20KB |
| Get Recommendation (API) | ~500ms | - |

## Security Considerations

### Current Implementation
- Local storage (no network exposure)
- No authentication
- CORS open for development

### Production Recommendations
- Enable Firebase authentication
- Use HTTPS/TLS
- Validate all API inputs
- Rate limit API endpoints
- Encrypt local storage
- Implement permission checks

---

## Extension Points

### Add New Features
1. **Database**: Replace JSON with SQLAlchemy
2. **Authentication**: Integrate Firebase
3. **AI Model**: Replace rule-based with ML model
4. **Notifications**: Add system notifications
5. **Sync**: Add cloud sync
6. **Mobile**: Create Flutter/React Native app
7. **Analytics**: Advanced analytics dashboard
8. **Integrations**: Calendar, email, Slack
