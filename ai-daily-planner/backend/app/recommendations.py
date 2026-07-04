from typing import List, Dict, Any


# In initial scaffold, return deterministic items.
# Later this will call ML inference artifacts.

def get_recommendations(user_id: str, limit: int = 10, context: Dict[str, Any] | None = None) -> List[Any]:
    seed_items = [
        {
            "item_id": "t1",
            "title": "Deep Work Sprint (45 min)",
            "category": "focus",
            "tags": ["work", "focus", "writing"],
            "estimated_minutes": 45,
            "score": 0.92,
            "reasons": [
                "Matches your focus preferences",
                "High likelihood of acceptance (learned)",
            ],
        },
        {
            "item_id": "t2",
            "title": "Quick Planning (10 min)",
            "category": "planning",
            "tags": ["productivity", "planning", "clarity"],
            "estimated_minutes": 10,
            "score": 0.84,
            "reasons": [
                "Improves daily structure",
                "Often completed after onboarding",
            ],
        },
        {
            "item_id": "t7",
            "title": "Review & Reflect (8 min)",
            "category": "reflection",
            "tags": ["review", "reflection", "progress"],
            "estimated_minutes": 8,
            "score": 0.79,
            "reasons": [
                "Boosts engagement in recent sessions",
                "Short time commitment",
            ],
        },
        {
            "item_id": "t6",
            "title": "Movement Break (20 min)",
            "category": "wellbeing",
            "tags": ["health", "energy", "movement"],
            "estimated_minutes": 20,
            "score": 0.76,
            "reasons": [
                "Supports energy patterns",
                "User tends to accept breaks",
            ],
        },
    ]

    # Simple deterministic shuffle based on user_id hash
    h = sum(ord(c) for c in user_id)
    ordered = seed_items[h % len(seed_items) :] + seed_items[: h % len(seed_items)]
    return ordered[:limit]


