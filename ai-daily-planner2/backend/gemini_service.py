import os
import google.generativeai as genai

class GeminiService:
    def __init__(self):
        # Read API key from environment
        self.api_key = os.getenv("GEMINI_API_KEY", "")
        if self.api_key:
            genai.configure(api_key=self.api_key)
            self.model = genai.GenerativeModel("gemini-3.5-flash")
        else:
            self.model = None

    def generate_plan(self, tasks_summary: str, habits_summary: str, current_mood: str) -> str:
        if not self.model:
            return "Please configure the server-side GEMINI_API_KEY to generate advanced daily routines."

        prompt = f"""
        You are a personalized AI scheduling coach for 'AI Daily Planner'.
        Generate a highly structured hourly daily schedule based on this user context:
        - Current Mood: {current_mood}
        - User Tasks to schedule: {tasks_summary}
        - Target Habits to maintain: {habits_summary}

        Ensure the schedule suggests perfect focus windows, regular wellness breaks, and leverages their current emotional state to keep them energized. Write the response in a motivating, professional markdown layout.
        """
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            return f"Error executing server-side schedule parsing: {str(e)}"
