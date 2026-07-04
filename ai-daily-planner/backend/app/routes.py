from fastapi import APIRouter
from typing import List

from app.recommendations import get_recommendations
from app.schemas import (
    AnalyticsOut,
    EventIn,
    EventOut,
    RecommendationRequest,
    RecommendationResponse,
    TaskCreate,
    TaskOut,
)

router = APIRouter()


@router.post("/recommendations", response_model=RecommendationResponse)
def recommendations(req: RecommendationRequest):
    items = get_recommendations(req.user_id, limit=req.limit, context=req.context)
    return RecommendationResponse(user_id=req.user_id, items=items)


@router.post("/events", response_model=EventOut)
def ingest_event(ev: EventIn):
    # Scaffold: later this will log into Firestore.
    return EventOut(ok=True, doc_id=None)


@router.get("/analytics/me", response_model=AnalyticsOut)
def analytics_me(user_id: str):
    # Scaffold: later this will aggregate stored events.
    return AnalyticsOut(
        user_id=user_id,
        session_duration_avg_minutes=25.5,
        ctr=0.45,
        acceptance_rate=0.72,
        completion_rate=0.68,
        satisfaction_score_avg=4.2
    )


# --- Task Management Endpoints ---
# In-memory task storage (replace with database in production)
user_tasks = {}


@router.post("/tasks", response_model=TaskOut)
def create_task(user_id: str, task: TaskCreate):
    """Create a new task for a user."""
    if user_id not in user_tasks:
        user_tasks[user_id] = []
    
    task_dict = task.dict()
    task_dict["id"] = len(user_tasks[user_id]) + 1
    user_tasks[user_id].append(task_dict)
    
    return TaskOut(**task_dict)


@router.get("/tasks", response_model=List[TaskOut])
def get_user_tasks(user_id: str):
    """Get all tasks for a user."""
    return user_tasks.get(user_id, [])


@router.delete("/tasks/{task_id}")
def delete_task(user_id: str, task_id: int):
    """Delete a task."""
    if user_id in user_tasks:
        user_tasks[user_id] = [t for t in user_tasks[user_id] if t["id"] != task_id]
        return {"ok": True, "message": "Task deleted"}
    return {"ok": False, "message": "User not found"}

