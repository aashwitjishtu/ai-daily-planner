#!/bin/bash
# AI Daily Planner Backend Server

echo "Starting AI Daily Planner Backend Server..."
echo ""
echo "Server will be available at: http://localhost:8000"
echo "API Documentation: http://localhost:8000/docs"
echo ""

cd backend
uvicorn app.main:app --reload --port 8000
