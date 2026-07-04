from pydantic import BaseModel
from typing import List, Optional, Dict, Any


class RecommendationRequest(BaseModel):
    user_id: str
    limit: int = 10
    context: Optional[Dict[str, Any]] = None


class RecommendationItem(BaseModel):
    item_id: str
    title: str
    category: str
    tags: List[str]
    estimated_minutes: int
    score: float
    reasons: List[str]  # explainable UX


class RecommendationResponse(BaseModel):
    user_id: str
    items: List[RecommendationItem]


class EventIn(BaseModel):
    user_id: str
    item_id: str
    event_type: str  # impression/click/accept/complete
    rank: Optional[int] = None
    ts: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None


class EventOut(BaseModel):
    ok: bool
    doc_id: Optional[str] = None


class AnalyticsOut(BaseModel):
    user_id: str
    session_duration_avg_minutes: Optional[float] = None
    ctr: Optional[float] = None
    acceptance_rate: Optional[float] = None
    completion_rate: Optional[float] = None
    satisfaction_score_avg: Optional[float] = None


# --- Task Management Schemas ---
class TaskCreate(BaseModel):
    title: str
    due_date: Optional[str] = None
    priority: int = 3


class TaskOut(BaseModel):
    id: int
    title: str
    due_date: Optional[str] = None
    priority: int

