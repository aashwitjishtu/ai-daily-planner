# AI Daily Planner - FastAPI REST Backend

This is the Python-based FastAPI backend engine designed to synchronize schedules and execute server-side automated plan generation.

## Features Included
- **CORS Configuration**: Open loopback policies for Android emulator connections (`http://10.0.2.2:8000/`)
- **JWT Cryptographic Authentication**: Secure user credentials, hashing passwords with `bcrypt` and signing with `PyJWT`
- **Mock and Firestore compatibility**: Easily replace in-memory storage with Firebase Admin SDK or SQLAlchemy
- **Automated Gemini API Routing**: Server-side plan architecting

---

## Installation & Setup

1. **Clone or export this backend folder** to your target server space.
2. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```
3. **Configure Environment Variables**:
   Create a `.env` file in this directory or export directly:
   ```env
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY
   JWT_SECRET_KEY=YOUR_JWT_CRYPTOGRAPHIC_SECRET_KEY
   ```

## Running the Server
Launch the server using Uvicorn:
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
Once active, you can access:
- **Interactive Swagger Documentation**: `http://localhost:8000/docs`
- **Service Status Page**: `http://localhost:8000/`

---

## Deploying to Production (Cloud Firestore Integration)
To switch from in-memory mock databases to production Firestore:
1. Generate a Firebase service account private key JSON file in your Firebase Console.
2. Store it as `service-account-key.json` inside this directory.
3. Modify `/backend/database.py` to initialize Firebase Admin SDK:
   ```python
   import firebase_admin
   from firebase_admin import credentials, firestore

   cred = credentials.Certificate("service-account-key.json")
   firebase_admin.initialize_app(cred)
   db = firestore.client()
   ```
4. Save tasks and retrieve records using the `db` client reference instead!
