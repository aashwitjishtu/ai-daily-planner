# 🚀 Quick Start Guide

## ⚡ Start in 30 Seconds (Desktop App Only)

### Windows
```bash
python main.py
```

Or double-click: `start_gui.bat`

### Mac/Linux
```bash
python main.py
```

Or run: `./start_gui.sh`

---

## 🎯 Run Backend + GUI (Full Setup)

### Terminal 1: Start Backend Server
```bash
# Windows
start_backend.bat

# Mac/Linux
./start_backend.sh
```

You should see:
```
INFO:     Uvicorn running on http://127.0.0.1:8000
```

### Terminal 2: Start Desktop App
```bash
python main.py
```

The GUI will now have full access to AI recommendations!

---

## 📋 What You Can Do

### In the Desktop App

1. **Add Tasks** 
   - Type task description
   - Choose priority (1 = highest urgency)
   - Optional: Set due date (YYYY-MM-DD)
   - Click "Add Task"

2. **View All Tasks**
   - Tasks auto-sort by priority and due date
   - Overdue tasks appear first
   - Select any task to delete

3. **Get AI Suggestions**
   - **"Suggest Next Task"** → Shows the most urgent task to do now
   - **"Get AI Recommendations"** → Fetches detailed suggestions from backend (requires running backend)

4. **Manage Tasks**
   - Select task and delete
   - Tasks persist in `data/tasks.json`

### Via API (When Backend is Running)

```bash
# Check if server is running
curl http://localhost:8000/health

# Get AI recommendations
curl -X POST http://localhost:8000/recommendations \
  -H "Content-Type: application/json" \
  -d '{"user_id": "demo_user", "limit": 5}'

# View interactive API docs
# Open: http://localhost:8000/docs
```

---

## 🗂️ Project Structure (After Setup)

```
ai-daily-planner/
├── main.py                    # Run this for GUI
├── start_gui.bat              # Quick launch (Windows)
├── start_gui.sh               # Quick launch (Mac/Linux)
│
├── planner/                   # Core task logic
│   ├── scheduler.py           # Task management
│   ├── ai_suggester.py        # AI recommendations
│   └── utils.py               # Date validation
│
├── gui/                       # Desktop interface
│   └── main_ui.py             # Tkinter UI
│
├── backend/                   # FastAPI server
│   └── app/
│       ├── main.py            # Server entry
│       ├── routes.py          # API endpoints
│       ├── schemas.py         # Data models
│       └── recommendations.py # AI logic
│
├── data/                      # Task storage
│   └── tasks.json             # Auto-created on first run
│
└── requirements.txt           # Dependencies
```

---

## ✅ Verify Installation

```bash
# Test GUI loads
python -c "import gui.main_ui; print('✓ GUI OK')"

# Test backend loads
cd backend && python -c "from app.main import app; print('✓ Backend OK')"

# Test all dependencies
python -m pip list | grep -E "fastapi|ttkbootstrap|requests"
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| "No module ttkbootstrap" | `pip install ttkbootstrap` |
| Can't connect to backend | Make sure `start_backend.bat` is running first |
| Port 8000 in use | Change port in `start_backend.bat` (e.g., `--port 8001`) |
| Tasks not saving | Ensure `data/` folder exists: `mkdir data` |

---

## 📚 Next Steps

1. ✅ **Run the app** - `python main.py`
2. ✅ **Add 5 tasks** - Practice creating tasks with different priorities
3. ✅ **Test "Suggest Next Task"** - See smart recommendations
4. ✅ **Start backend** - `start_backend.bat` (optional for full features)
5. ✅ **Click "Get AI Recommendations"** - See API integration
6. 📖 **Read SETUP.md** - Learn about database options and deployment
7. 🔧 **Customize** - Edit `gui/main_ui.py` or `planner/` modules

---

## 🎨 Customize the App

### Change Theme
In `gui/main_ui.py`, line 31:
```python
self.style = ttk.Style("flatly")  # Try: "darkly", "superhero", "solar"
```

### Change Backend URL
In `gui/main_ui.py`, line 13:
```python
API_BASE_URL = "http://localhost:8000"  # Change this
```

### Change Task Storage Location
In `planner/scheduler.py`, line 5:
```python
TASKS_FILE = "data/tasks.json"  # Change this
```

---

## 💡 Pro Tips

- **No internet needed** - Works completely offline
- **Local storage** - All tasks saved in `data/tasks.json`
- **Backend optional** - GUI works without backend server
- **Portable** - Package as `.exe` with PyInstaller

---

## 📖 Full Documentation

- **Setup Guide**: Read `SETUP.md` for advanced configuration
- **Architecture**: See `README.md` for detailed overview
- **API Docs**: Start backend and visit `http://localhost:8000/docs`

---

## 🎉 You're All Set!

Enjoy your AI-powered daily planner! 🚀

Questions? Check:
- README.md (overall guide)
- SETUP.md (detailed setup)
- Code comments in `planner/` and `gui/` folders
