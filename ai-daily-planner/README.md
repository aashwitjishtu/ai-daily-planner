# AI Daily Planner

A **hybrid application** combining a **modern FastAPI backend** with a **user-friendly Tkinter desktop GUI** for intelligent task management and AI-powered recommendations.

## Architecture

### 🎯 Features

- **Desktop GUI**: Task management with priority levels and due dates
- **AI Suggestions**: Smart next-task recommendations based on priority and urgency
- **FastAPI Backend**: REST API for recommendations and analytics
- **Hybrid Design**: GUI uses local JSON storage + connects to backend for AI features
- **Firebase Ready**: Backend configured for Firebase authentication and Firestore integration

## Project Structure

```
ai-daily-planner/
├── main.py                          # Desktop app entry point
├── planner/                         # Task management & AI logic
│   ├── __init__.py
│   ├── scheduler.py                # Task storage and scheduling
│   ├── ai_suggester.py            # AI-powered task suggestions
│   └── utils.py                    # Utility functions (date validation, etc.)
│
├── gui/                            # Tkinter desktop interface
│   ├── __init__.py
│   └── main_ui.py                 # Main GUI window and widgets
│
├── backend/                        # FastAPI server
│   ├── app/
│   │   ├── main.py                # FastAPI app setup
│   │   ├── routes.py              # API endpoints
│   │   ├── schemas.py             # Pydantic models
│   │   ├── recommendations.py     # Recommendation logic
│   │   └── __init__.py
│   └── pyproject.toml
│
├── data/                          # Local data storage
│   └── tasks.json                # User tasks (local JSON)
│
├── requirements.txt               # Python dependencies
└── README.md                      # This file
```

## Tech Stack

- **Frontend**: Tkinter + ttkbootstrap (modern theming)
- **Backend**: FastAPI + Uvicorn
- **Task Storage**: JSON (local)
- **Authentication**: Firebase Admin SDK (configured)
- **Data**: Pydantic models
- **API Client**: requests library

## Installation & Setup

### 1. Install Dependencies

```bash
cd ai-daily-planner
pip install -r requirements.txt
```

### 2. Run the Desktop Application

```bash
python main.py
```

This will launch the Tkinter GUI with full task management capabilities.

### 3. Run the Backend Server (Optional)

In a separate terminal:

```bash
cd backend
uvicorn app.main:app --reload --port 8000
```

The backend provides:
- `/recommendations` - AI recommendations
- `/tasks` - Task management API
- `/analytics` - User analytics
- `/events` - Event logging
- `/health` - Health check

## Usage

### Desktop GUI

1. **Add a Task**
   - Enter task description
   - Set priority (1-5, where 1 is highest)
   - Optionally set due date (YYYY-MM-DD format)
   - Click "Add Task"

2. **View Tasks**
   - Tasks are automatically sorted by priority and due date
   - Tasks due today appear first

3. **Get Suggestions**
   - Click "Suggest Next Task" to see the next task to focus on
   - Click "Get AI Recommendations" to connect to the backend (requires running backend server)

4. **Delete Tasks**
   - Select a task and click "Delete Selected Task"

### Backend API

Example: Get recommendations
```bash
curl -X POST http://localhost:8000/recommendations \
  -H "Content-Type: application/json" \
  -d '{"user_id": "user123", "limit": 5}'
```

## Key Components

### 1. Task Scheduling (`planner/scheduler.py`)
- Manages task CRUD operations
- Stores tasks in JSON format
- Sorts tasks by priority and due date

### 2. AI Suggester (`planner/ai_suggester.py`)
- `suggest_next_task()` - Recommends the most urgent task
- `suggest_schedule()` - Provides a full day's schedule
- Logic: Overdue/today's tasks > high priority > due date

### 3. Desktop GUI (`gui/main_ui.py`)
- Modern Tkinter interface with ttkbootstrap theming
- Real-time task list with sorting
- Integration with backend recommendations (when server is running)

### 4. FastAPI Backend (`backend/app/`)
- RESTful API for task and recommendation management
- Firebase-ready authentication
- Analytics and event tracking endpoints

## Data Models

### Task (Local)
```json
{
  "title": "Complete project report",
  "due_date": "2026-06-25",
  "priority": 1,
  "created_at": "2026-06-23 10:30:45"
}
```

### Recommendation Item (API)
```json
{
  "item_id": "t1",
  "title": "Deep Work Sprint (45 min)",
  "category": "focus",
  "tags": ["work", "focus"],
  "estimated_minutes": 45,
  "score": 0.92,
  "reasons": ["Matches your preferences", "High acceptance probability"]
}
```

## Configuration

### Change Backend API URL

Edit `gui/main_ui.py`:
```python
API_BASE_URL = "http://localhost:8000"  # Change this
```

### Change Local Data Path

Edit `planner/scheduler.py`:
```python
TASKS_FILE = "data/tasks.json"  # Change this
```

## Future Enhancements

- [ ] Database integration (PostgreSQL/MongoDB)
- [ ] Cloud storage for tasks (Firebase/AWS)
- [ ] Mobile app integration
- [ ] Advanced AI model for recommendations
- [ ] Multi-user support with authentication
- [ ] User preference learning
- [ ] Notification system
- [ ] Dark mode theme

## License

MIT License - See LICENSE file

## Author

Created as a hybrid task management system combining desktop convenience with cloud-ready architecture.

