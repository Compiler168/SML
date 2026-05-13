"""
Smart Loan AI - Auth Service
==============================
Authentication and user management logic.
"""

from firebase.operations import FirebaseOperations
from utils.jwt_handler import create_token, hash_password, verify_password
from utils.validators import validate_email, validate_password


USERS_COLLECTION = "users"


class AuthService:
    """Handles user authentication and profile management."""

    @staticmethod
    def signup(name: str, email: str, password: str, phone: str = None) -> dict:
        """Register a new user."""
        # Validate email
        if not validate_email(email):
            return {"error": "Invalid email format"}

        # Validate password
        pw_issues = validate_password(password)
        if pw_issues:
            return {"error": pw_issues[0]}

        # Check if email exists
        existing = FirebaseOperations.query(USERS_COLLECTION, "email", "==", email)
        if existing:
            return {"error": "Email already registered"}

        # Create user with a nested profile for the new Android frontend.
        user_data = {
            "name": name,
            "email": email,
            "password": hash_password(password),
            "phone": phone,
            "role": "user",
            "profile": {
                "monthly_income": 0.0,
                "monthly_expenses": 0.0,
                "credit_score": 650,
                "employment_status": "",
                "employment_years": 0,
                "existing_loans": 0,
                "existing_emi": 0.0,
                "savings_balance": 0.0,
                "dependents": 0,
                "age": 18,
                "property_value": 0.0
            }
        }
        user_id = FirebaseOperations.create(USERS_COLLECTION, user_data)

        # Generate token
        token = create_token(user_id, email, "user")

        return {
            "token": token,
            "user": {
                "id": user_id,
                "name": name,
                "email": email,
                "role": "user",
                "profile": user_data["profile"],
                "created_at": user_data["created_at"]
            }
        }

    @staticmethod
    def login(email: str, password: str) -> dict:
        """Authenticate a user."""
        users = FirebaseOperations.query(USERS_COLLECTION, "email", "==", email)
        if not users:
            return {"error": "Invalid email or password"}

        user = users[0]
        if not verify_password(password, user.get("password", "")):
            return {"error": "Invalid email or password"}

        token = create_token(user["id"], email, user.get("role", "user"))

        return {
            "token": token,
            "user": {
                "id": user["id"],
                "name": user.get("name", ""),
                "email": user["email"],
                "role": user.get("role", "user"),
                "profile": user.get("profile", {
                    "monthly_income": 0.0,
                    "monthly_expenses": 0.0,
                    "credit_score": 650,
                    "employment_status": "",
                    "employment_years": 0,
                    "existing_loans": 0,
                    "existing_emi": 0.0,
                    "savings_balance": 0.0,
                    "dependents": 0,
                    "age": 18,
                    "property_value": 0.0
                }),
                "created_at": user.get("created_at")
            }
        }

    @staticmethod
    def get_profile(user_id: str) -> dict:
        """Get user profile."""
        user = FirebaseOperations.get(USERS_COLLECTION, user_id)
        if not user:
            return {"error": "User not found"}
        user.pop("password", None)
        user["profile"] = user.get("profile", {
            "monthly_income": 0.0,
            "monthly_expenses": 0.0,
            "credit_score": 650,
            "employment_status": "",
            "employment_years": 0,
            "existing_loans": 0,
            "existing_emi": 0.0,
            "savings_balance": 0.0,
            "dependents": 0,
            "age": 18,
            "property_value": 0.0
        })
        return user

    @staticmethod
    def update_profile(user_id: str, data: dict) -> dict:
        """Update user profile."""
        existing = FirebaseOperations.get(USERS_COLLECTION, user_id) or {}
        profile_updates = data.get("profile") if isinstance(data.get("profile"), dict) else None
        update_data = {k: v for k, v in data.items() if v is not None and k != "password" and k != "profile"}
        if profile_updates:
            current_profile = existing.get("profile", {
                "monthly_income": 0.0,
                "monthly_expenses": 0.0,
                "credit_score": 650,
                "employment_status": "",
                "employment_years": 0,
                "existing_loans": 0,
                "existing_emi": 0.0,
                "savings_balance": 0.0,
                "dependents": 0,
                "age": 18,
                "property_value": 0.0
            })
            update_data["profile"] = {**current_profile, **profile_updates}

        FirebaseOperations.update(USERS_COLLECTION, user_id, update_data)
        return AuthService.get_profile(user_id)

    @staticmethod
    def get_all_users(limit: int = 100) -> list:
        """Get all users (admin)."""
        users = FirebaseOperations.get_all(USERS_COLLECTION, limit)
        for u in users:
            u.pop("password", None)
        return users
