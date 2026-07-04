import tkinter as tk
import ttkbootstrap as ttk
from ttkbootstrap.constants import *
from planner.scheduler import Task, Scheduler
from planner.ai_suggester import AISuggester
from planner.utils import validate_date
import datetime
import json
import requests


# Initialize AI Suggester
suggester = AISuggester()

# Backend API URL - Change this to match your backend server
API_BASE_URL = "http://localhost:8000"


class PlannerGUI:
    def __init__(self, root):
        """
        Initialize the main window and widgets.
        """
        self.root = root
        self.style = ttk.Style("flatly")
        self.root.title("AI Daily Planner")
        self.root.geometry("900x600")
        self.root.resizable(True, True)

        # Current user
        self.user_id = "demo_user"

        # Create Scheduler instance for local task management
        self.scheduler = Scheduler()

        self.create_widgets()
        self.refresh_task_list()

    def create_widgets(self):
        """
        Create all GUI widgets.
        """
        # --- Header Frame ---
        header_frame = ttk.Frame(self.root, padding=10)
        header_frame.pack(fill=X)
        
        ttk.Label(header_frame, text="📋 AI Daily Planner", font=("Segoe UI", 16, "bold")).pack(side=LEFT)
        ttk.Label(header_frame, text=f"User: {self.user_id}", font=("Segoe UI", 10), foreground="gray").pack(side=RIGHT)

        # --- Add Task Frame ---
        add_frame = ttk.Frame(self.root, padding=10)
        add_frame.pack(fill=X)

        ttk.Label(add_frame, text="Task Description:", font=("Segoe UI", 10)).pack(side=LEFT, padx=(0, 5))
        self.task_entry = ttk.Entry(add_frame, width=40)
        self.task_entry.pack(side=LEFT, padx=(0, 10))

        ttk.Label(add_frame, text="Priority:", font=("Segoe UI", 10)).pack(side=LEFT, padx=(0, 5))
        self.priority_var = ttk.StringVar(value="2")
        self.priority_combo = ttk.Combobox(
            add_frame, 
            textvariable=self.priority_var, 
            width=3,
            values=["1", "2", "3", "4", "5"], 
            state="readonly"
        )
        self.priority_combo.pack(side=LEFT, padx=(0, 10))

        ttk.Label(add_frame, text="Due Date (YYYY-MM-DD):", font=("Segoe UI", 10)).pack(side=LEFT, padx=(0, 5))
        self.due_entry = ttk.Entry(add_frame, width=15)
        self.due_entry.pack(side=LEFT, padx=(0, 10))

        # --- Action Buttons Frame ---
        btn_frame = ttk.Frame(self.root, padding=10)
        btn_frame.pack(fill=X)

        self.add_btn = ttk.Button(btn_frame, text="Add Task", bootstyle=SUCCESS, command=self.add_task)
        self.add_btn.pack(side=LEFT, padx=(0, 10))

        self.delete_btn = ttk.Button(btn_frame, text="Delete Selected Task", bootstyle=DANGER, command=self.delete_task)
        self.delete_btn.pack(side=LEFT, padx=(0, 10))

        self.suggest_btn = ttk.Button(btn_frame, text="Suggest Next Task", bootstyle=INFO, command=self.show_suggestion)
        self.suggest_btn.pack(side=LEFT, padx=(0, 10))

        self.get_recommendations_btn = ttk.Button(
            btn_frame, 
            text="Get AI Recommendations", 
            bootstyle=WARNING, 
            command=self.get_recommendations
        )
        self.get_recommendations_btn.pack(side=LEFT)

        # --- Task List Frame ---
        list_frame = ttk.Frame(self.root, padding=10)
        list_frame.pack(fill=BOTH, expand=True)

        ttk.Label(list_frame, text="Your Tasks:", font=("Segoe UI", 11, "bold")).pack(anchor="w")

        self.task_listbox = ttk.Treeview(
            list_frame, 
            columns=("Description", "Priority", "Due Date"),
            show="headings", 
            selectmode="browse",
            height=12
        )
        self.task_listbox.heading("Description", text="Description")
        self.task_listbox.heading("Priority", text="Priority")
        self.task_listbox.heading("Due Date", text="Due Date")
        
        self.task_listbox.column("Description", width=500, anchor="w")
        self.task_listbox.column("Priority", width=80, anchor="center")
        self.task_listbox.column("Due Date", width=150, anchor="center")
        
        self.task_listbox.pack(side=LEFT, fill=BOTH, expand=True)

        scrollbar = ttk.Scrollbar(list_frame, orient="vertical", command=self.task_listbox.yview)
        scrollbar.pack(side=LEFT, fill=Y)
        self.task_listbox.configure(yscrollcommand=scrollbar.set)

        # --- Suggestion Label ---
        self.suggestion_label = ttk.Label(
            self.root, 
            text="", 
            font=("Segoe UI", 11, "italic"), 
            foreground="#0a84ff"
        )
        self.suggestion_label.pack(pady=(5, 10))

        # --- Recommendations Label ---
        self.recommendations_label = ttk.Label(
            self.root,
            text="",
            font=("Segoe UI", 10),
            foreground="#00aa00"
        )
        self.recommendations_label.pack(pady=(0, 10))

    def add_task(self):
        """
        Add a new task with input validation.
        """
        description = self.task_entry.get().strip()
        priority_str = self.priority_var.get()
        due_date_str = self.due_entry.get().strip()

        if not description:
            self.show_error("Please enter a task description.")
            return

        try:
            priority = int(priority_str)
            if priority not in [1, 2, 3, 4, 5]:
                raise ValueError()
        except ValueError:
            self.show_error("Priority must be 1-5.")
            return

        if due_date_str:
            if not validate_date(due_date_str):
                self.show_error("Due date must be in YYYY-MM-DD format.")
                return
        else:
            due_date_str = None

        task = Task(description, due_date_str, priority)
        self.scheduler.add_task(task)

        # Clear inputs
        self.task_entry.delete(0, 'end')
        self.due_entry.delete(0, 'end')
        self.priority_var.set("2")

        self.refresh_task_list()
        self.suggestion_label.config(text="")

    def refresh_task_list(self):
        """
        Refresh the task list display with sorted tasks.
        """
        for item in self.task_listbox.get_children():
            self.task_listbox.delete(item)

        tasks = self.scheduler.get_tasks_sorted()

        for idx, task in enumerate(tasks):
            due = task.due_date if task.due_date else "None"
            self.task_listbox.insert("", "end", iid=str(idx), values=(task.title, task.priority, due))

    def delete_task(self):
        """
        Delete the selected task.
        """
        selected = self.task_listbox.selection()
        if not selected:
            self.show_error("Please select a task to delete.")
            return

        idx = int(selected[0])
        if 0 <= idx < len(self.scheduler.tasks):
            self.scheduler.delete_task(idx)
            self.refresh_task_list()
            self.suggestion_label.config(text="")
        else:
            self.show_error("Invalid task selection.")

    def show_suggestion(self):
        """
        Show the AI-suggested next task.
        """
        suggested = suggester.suggest_schedule(self.scheduler.tasks)
        if suggested:
            task = suggested[0]
            due = task.due_date if task.due_date else "None"
            text = f"🎯 Next task to do: {task.title} (Priority: {task.priority}, Due: {due})"
        else:
            text = "No tasks available to suggest."

        self.suggestion_label.config(text=text)

    def get_recommendations(self):
        """
        Fetch AI recommendations from the backend API.
        """
        try:
            response = requests.post(
                f"{API_BASE_URL}/recommendations",
                json={"user_id": self.user_id, "limit": 5},
                timeout=5
            )
            response.raise_for_status()
            data = response.json()
            
            if data.get("items"):
                items = data["items"]
                rec_text = "💡 Recommendations: " + " | ".join(
                    [f"{item['title']} ({item['score']:.1%})" for item in items[:3]]
                )
            else:
                rec_text = "No recommendations available."
                
            self.recommendations_label.config(text=rec_text)
        except requests.exceptions.ConnectionError:
            self.show_error("Cannot connect to backend server.\nMake sure the FastAPI server is running on http://localhost:8000")
        except Exception as e:
            self.show_error(f"Error fetching recommendations: {str(e)}")

    def show_error(self, message):
        """
        Show an error message window.
        """
        error_win = ttk.Toplevel(self.root)
        error_win.title("Error")
        error_win.geometry("400x120")
        error_win.resizable(False, False)
        
        ttk.Label(error_win, text=message, foreground="red", font=("Segoe UI", 10)).pack(pady=20)
        ttk.Button(error_win, text="OK", command=error_win.destroy).pack()


def main():
    root = ttk.Window(themename="flatly")
    app = PlannerGUI(root)
    root.mainloop()


if __name__ == "__main__":
    main()
