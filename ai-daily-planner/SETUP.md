# Setup Guide: AI Daily Planner

## Quick Start (Desktop App Only)

```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Run the desktop app
python main.py
```

That's it! You now have a fully functional task manager with AI suggestions.

---

## Full Setup (Desktop App + Backend)

### Prerequisites
- Python 3.10+
- pip or conda

### Step 1: Install Dependencies

```bash
pip install -r requirements.txt
```

Or using conda:
```bash
conda create -n ai-planner python=3.10
conda activate ai-planner
pip install -r requirements.txt
```

### Step 2: Start the Backend (Optional)

In one terminal:
```bash
cd backend
uvicorn app.main:app --reload --port 8000
```

You should see:
```
INFO:     Uvicorn running on http://127.0.0.1:8000
```

### Step 3: Run the Desktop App

In another terminal:
```bash
python main.py
```

---

## Features Overview

### 1. Task Management
- **Add Task**: Enter description, priority (1-5), and optional due date
- **View Tasks**: See all tasks sorted by priority and due date
- **Delete Task**: Select and delete completed tasks
- **Auto-Save**: All tasks are saved locally in `data/tasks.json`

### 2. AI Suggestions
- **Suggest Next Task**: Uses local AI logic to recommend the most urgent task
- **Get Recommendations**: Connects to backend API for detailed AI recommendations (when running)

### 3. Backend API (Optional)
When the backend is running, you can use:
- `GET /health` - Check server status
- `POST /recommendations` - Get AI recommendations
- `GET /tasks` - Retrieve tasks
- `POST /tasks` - Create new task
- `POST /events` - Log user events
- `GET /analytics/me` - Get analytics

---

## Database Options

### Current Setup (JSON)
- **Location**: `data/tasks.json`
- **Type**: Local file storage
- **Use Case**: Quick development and testing

### Upgrade to Database

For production, replace JSON with a database:

#### Option 1: SQLite (Minimal Setup)
```python
# Install: pip install sqlalchemy
# Update backend/app/main.py to use SQLModel
```

#### Option 2: PostgreSQL (Recommended)
```bash
pip install psycopg2-binary sqlalchemy
```

```python
# In backend/app/main.py
from sqlalchemy import create_engine
DATABASE_URL = "postgresql://user:password@localhost/ai_planner"
engine = create_engine(DATABASE_URL)
```

#### Option 3: Firebase/Firestore (Cloud)
```bash
pip install firebase-admin
```

```python
# In backend/app/main.py
import firebase_admin
from firebase_admin import credentials, firestore

cred = credentials.Certificate("firebase-key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()
```

---

## Environment Variables

Create a `.env` file in the root directory:

```env
# Backend Configuration
BACKEND_URL=http://localhost:8000

# Firebase (if using)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_API_KEY=your-api-key

# Database (if switching from JSON)
DATABASE_URL=postgresql://user:password@localhost/ai_planner
```

Load in your app:
```python
from dotenv import load_dotenv
import os

load_dotenv()
backend_url = os.getenv("BACKEND_URL", "http://localhost:8000")
```

---

## Troubleshooting

### Issue: "ModuleNotFoundError: No module named 'ttkbootstrap'"
**Solution**: Install the package
```bash
pip install ttkbootstrap
```

### Issue: "Cannot connect to backend server"
**Solution**: Make sure the backend is running
```bash
cd backend
uvicorn app.main:app --reload
```

### Issue: Tasks not saving
**Solution**: Ensure `data/` directory exists
```bash
mkdir data
```

### Issue: Port 8000 already in use
**Solution**: Use a different port
```bash
uvicorn app.main:app --reload --port 8001
# Then update API_BASE_URL in gui/main_ui.py
```

---

## Development Workflow

1. **Make changes to GUI**: Edit `gui/main_ui.py` and reload
2. **Update task logic**: Modify `planner/scheduler.py` or `planner/ai_suggester.py`
3. **Update API**: Edit `backend/app/routes.py` and restart server
4. **Add new schemas**: Update `backend/app/schemas.py`

---

## Testing

### Test the Desktop App
```bash
python main.py
# Try creating, viewing, deleting tasks
```

### Test the Backend API
```bash
# In another terminal, with backend running:
curl http://localhost:8000/health

curl -X POST http://localhost:8000/recommendations \
  -H "Content-Type: application/json" \
  -d '{"user_id": "test_user", "limit": 5}'
```

---

## Deployment

### Desktop App (Standalone Executable)
```bash
pip install pyinstaller
pyinstaller --onefile --windowed main.py
# Creates: dist/main.exe (Windows) or dist/main (Mac/Linux)
```

### Backend (Cloud Deployment)

#### Option 1: Heroku
```bash
pip install gunicorn
# Create Procfile:
echo "web: gunicorn -w 4 -k uvicorn.workers.UvicornWorker backend.app.main:app" > Procfile
git push heroku main
```

#### Option 2: Docker
```dockerfile
FROM python:3.10
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD ["uvicorn", "backend.app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

---

## Next Steps

1. ✅ Run the desktop app locally
2. ⚠️ Test with the backend server (optional)
3. 📊 Add more AI features (ML models, prediction, learning)
4. 💾 Migrate to a production database
5. 🔐 Implement Firebase authentication
6. 📱 Create a mobile companion app (Flutter/React Native)
7. ☁️ Deploy to production (AWS, GCP, Azure, etc.)

---

For detailed API documentation, start the backend and visit:
```
http://localhost:8000/docs
```

Happy planning! 🎯
