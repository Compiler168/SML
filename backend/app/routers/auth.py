"""
Smart Loan AI - Auth Router
"""

from fastapi import APIRouter, HTTPException, Depends, Header
from models.schemas import UserCreate, UserLogin, UserUpdate, TokenResponse, APIResponse
from services.auth_service import AuthService
from utils.jwt_handler import verify_token
from typing import Optional

router = APIRouter()


def get_current_user(authorization: Optional[str] = Header(None)) -> dict:
    """Dependency: Extract and verify JWT token from header."""
    if not authorization:
        raise HTTPException(status_code=401, detail="Authorization header required")
    try:
        token = authorization.replace("Bearer ", "")
        return verify_token(token)
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@router.post("/signup", response_model=None)
async def signup(user: UserCreate):
    """Register a new user account."""
    result = AuthService.signup(user.name, user.email, user.password, user.phone)
    if "error" in result:
        raise HTTPException(status_code=400, detail=result["error"])
    return result


@router.post("/login", response_model=None)
async def login(user: UserLogin):
    """Authenticate and receive JWT token."""
    result = AuthService.login(user.email, user.password)
    if "error" in result:
        raise HTTPException(status_code=401, detail=result["error"])
    return result


@router.get("/profile")
async def get_profile(current_user: dict = Depends(get_current_user)):
    """Get current user profile."""
    result = AuthService.get_profile(current_user["user_id"])
    if "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.put("/profile")
async def update_profile(update: UserUpdate, current_user: dict = Depends(get_current_user)):
    """Update user profile."""
    result = AuthService.update_profile(
        current_user["user_id"],
        update.model_dump(exclude_none=True)
    )
    return result
