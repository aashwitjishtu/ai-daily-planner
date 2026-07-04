import json
import math
import random
from dataclasses import dataclass
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Any


@dataclass
class UserProfile:
    user_id: str
    interests: List[str]
    preferred_categories: List[str]
    daily_available_minutes: int


def _sigmoid(x: float) -> float:
    return 1.0 / (1.0 + math.exp(-x))


def load_seed_tasks(seed_path: str) -> List[Dict[str, Any]]:
    with open(seed_path, "r", encoding="utf-8") as f:
        return json.load(f)


def make_user_profiles(num_users: int) -> List[UserProfile]:
    categories = ["focus", "planning", "admin", "learning", "wellbeing", "reflection"]
    interest_pool = [
        "work",
        "focus",
        "writing",
        "productivity",
        "email",
        "study",
        "movement",
        "reflection",
        "planning",
        "break",
    ]
    profiles: List[UserProfile] = []

    for i in range(num_users):
        user_id = f"u{i+1}"
        preferred_categories = random.sample(categories, k=random.randint(2, 4))
        interests = random.sample(interest_pool, k=random.randint(3, 6))
        daily_available_minutes = random.choice([30, 45, 60, 75, 90])
        profiles.append(
            UserProfile(
                user_id=user_id,
                interests=interests,
                preferred_categories=preferred_categories,
                daily_available_minutes=daily_available_minutes,
            )
        )
    return profiles


def item_relevance_score(item: Dict[str, Any], profile: UserProfile) -> float:
    score = 0.0
    if item["category"] in profile.preferred_categories:
        score += 1.5
    # tag overlap
    tag_overlap = len(set(item["tags"]) & set(profile.interests))
    score += 0.35 * tag_overlap

    # time fit
    minutes = item["estimated_minutes"]
    if minutes <= profile.daily_available_minutes:
        score += 0.8
    else:
        # penalize hard when too long
        score -= 0.6 * ((minutes - profile.daily_available_minutes) / 10.0)
    return score


def sample_day_events(
    profile: UserProfile,
    tasks: List[Dict[str, Any]],
    rng: random.Random,
    day_idx: int,
) -> List[Dict[str, Any]]:
    """Simulate: impression(s) -> click/accept -> completion.

    The goal is to produce realistic training signals for CTR and acceptance.
    """
    base_date = datetime(2026, 1, 1) + timedelta(days=day_idx)

    events: List[Dict[str, Any]] = []

    # each day: generate a small candidate list
    candidate_count = rng.randint(4, 7)
    candidates = rng.sample(tasks, k=candidate_count)

    for rank, item in enumerate(candidates, start=1):
        rel = item_relevance_score(item, profile)

        # Position bias (rank 1 tends to get more attention)
        pos_bias = 1.2 - 0.18 * (rank - 1)
        # convert to click probability
        p_click = _sigmoid(rel + pos_bias - 2.0)

        impression_time = base_date + timedelta(minutes=10 * rank)
        clicked = rng.random() < max(0.02, p_click)

        # Acceptance probability depends on click and item suitability
        if clicked:
            rel2 = rel + 0.6
            p_accept = _sigmoid(rel2 - 1.8)
            accepted = rng.random() < max(0.05, p_accept)
        else:
            accepted = False

        # Completion depends on acceptance and estimated minutes
        if accepted:
            # users with more minutes more likely complete
            minutes = item["estimated_minutes"]
            time_factor = min(1.0, profile.daily_available_minutes / max(1, minutes))
            p_complete = _sigmoid(rel + 0.5 * time_factor - 2.0)
            completed = rng.random() < max(0.03, p_complete)
        else:
            completed = False

        # dwell time proxy (minutes): shorter if not clicked
        if clicked:
            dwell = max(1, int(rng.gauss(4 + 0.5 * rel, 1.5)))
        else:
            dwell = max(1, int(rng.gauss(1.5, 0.6)))

        events.append(
            {
                "user_id": profile.user_id,
                "item_id": item["item_id"],
                "day": base_date.date().isoformat(),
                "rank": rank,
                "impression_ts": impression_time.isoformat(),
                "clicked": bool(clicked),
                "accepted": bool(accepted),
                "completed": bool(completed),
                "dwell_minutes": dwell,
                "item_category": item["category"],
                "item_tags": item["tags"],
            }
        )

    return events


def main():
    repo_root = Path(__file__).resolve().parents[1]
    seed_path = repo_root / "scripts" / "seed_tasks.json"
    out_path = repo_root / "scripts" / "synthetic_interactions.jsonl"

    seed_tasks = load_seed_tasks(str(seed_path))
    rng = random.Random(42)

    num_users = 120
    num_days = 45

    profiles = make_user_profiles(num_users)

    with open(out_path, "w", encoding="utf-8") as f:
        for day_idx in range(num_days):
            # vary daily volume
            for profile in profiles:
                day_events = sample_day_events(profile, seed_tasks, rng, day_idx)
                for ev in day_events:
                    f.write(json.dumps(ev, ensure_ascii=False) + "\n")

    print(f"Wrote synthetic interactions to: {out_path}")


if __name__ == "__main__":
    main()

