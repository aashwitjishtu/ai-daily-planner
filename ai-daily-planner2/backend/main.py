from fastapi import FastAPI, HTTPException, Header, Depends
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Optional
from models import UserRegister, UserLogin, Token, SyncRequest, SyncResponse
from auth import get_password_hash, verify_password, create_access_token, verify_access_token
from database import db_instance
from gemini_service import GeminiService

app = FastAPI(
    title="AI Daily Planner Backend API",
    description="Full-featured FastAPI REST API for synchronization and automated scheduling suggestions.",
    version="1.0"
)

# Enable CORS for local Android development emulators
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

gemini = GeminiService()

@app.get("/")
def read_root():
    return {
        "status": "Online",
        "service": "AI Daily Planner REST API Engine",
        "features": ["Task synchronization", "JWT Authentication", "Direct Google Gemini API schedule generation"]
    }

@app.post("/api/auth/register", response_model=Token)
def register(user: UserRegister):
    existing = db_instance.get_user(user.email)
    if existing:
        raise HTTPException(status_code=400, detail="Account already registered under this email address.")
    
    hashed_password = get_password_hash(user.password)
    user_record = {
        "email": user.email,
        "password": hashed_password,
        "name": user.name
    }
    db_instance.add_user(user.email, user_record)
    
    # Generate token
    token = create_access_token({"sub": user.email})
    return {
        "access_token": token,
        "token_type": "bearer",
        "email": user.email,
        "name": user.name
    }

@app.post("/api/auth/login", response_model=Token)
def login(user: UserLogin):
    record = db_instance.get_user(user.email)
    if not record or not verify_password(user.password, record["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password.")
    
    token = create_access_token({"sub": user.email})
    return {
        "access_token": token,
        "token_type": "bearer",
        "email": user.email,
        "name": record["name"]
    }

@app.post("/api/tasks/sync", response_model=SyncResponse)
def sync_tasks(request: SyncRequest, authorization: str = Header(...)):
    email = verify_access_token(authorization)
    if not email:
        raise HTTPException(status_code=401, detail="Authorization token expired or invalid.")
    
    raw_tasks = [task.model_dump() for task in request.tasks]
    db_instance.save_tasks(email, raw_tasks)
    return {
        "status": "Success",
        "message": f"Successfully backed up {len(raw_tasks)} scheduling elements securely."
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
