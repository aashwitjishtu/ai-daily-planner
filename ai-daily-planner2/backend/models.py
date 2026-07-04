from pydantic import BaseModel, EmailStr, Field
from typing import List, Optional

class UserRegister(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=6)
    name: str

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str
    email: str
    name: str

class TaskSyncItem(BaseModel):
    id: int
    title: str
    description: Optional[str] = ""
    isCompleted: bool
    isArchived: bool
    dueDate: int
    priority: int
    category: str
    recurrence: str
    subtasksRaw: Optional[str] = ""

class SyncRequest(BaseModel):
    tasks: List[TaskSyncItem]

class SyncResponse(BaseModel):
    status: str
    message: str
