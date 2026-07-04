import os
from typing import Dict, Any, List, Optional

# Simple in-memory or database simulation to make the FastAPI backend completely runnable out-of-the-box
# Users can easily plug in Firebase Firestore Admin SDK credentials or SQLAlchemy as documented in README.md.

class LocalMockDB:
    def __init__(self):
        self.users: Dict[str, Dict[str, Any]] = {}
        self.tasks: Dict[str, List[Dict[str, Any]]] = {}

    def add_user(self, email: str, user_data: Dict[str, Any]):
        self.users[email] = user_data

    def get_user(self, email: str) -> Optional[Dict[str, Any]]:
        return self.users.get(email)

    def save_tasks(self, email: str, tasks: List[Dict[str, Any]]):
        self.tasks[email] = tasks

    def get_tasks(self, email: str) -> List[Dict[str, Any]]:
        return self.tasks.get(email, [])

db_instance = LocalMockDB()
