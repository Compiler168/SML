"""
Smart Loan AI - Loan Prediction Router
"""

from fastapi import APIRouter, HTTPException, Depends
from models.schemas import LoanApplication, LoanPredictionResult
from services.loan_service import LoanService
from routers.auth import get_current_user

router = APIRouter()


@router.post("/predict", response_model=None)
async def predict_loan(application: LoanApplication, current_user: dict = Depends(get_current_user)):
    """Predict loan eligibility based on user input."""
    try:
        result = LoanService.predict(application.model_dump(), current_user["user_id"])
        return {"success": True, "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")


@router.get("/history")
async def get_history(current_user: dict = Depends(get_current_user)):
    """Get prediction history for current user."""
    history = LoanService.get_history(current_user["user_id"])
    return {"success": True, "data": history}


@router.get("/stats")
async def get_stats(current_user: dict = Depends(get_current_user)):
    """Get loan statistics for current user."""
    stats = LoanService.get_stats(current_user["user_id"])
    return {"success": True, "data": stats}


@router.get("/suggestions")
async def get_suggestions(current_user: dict = Depends(get_current_user)):
    """Get improvement suggestions based on latest prediction."""
    history = LoanService.get_history(current_user["user_id"])
    if not history:
        return {"success": True, "data": {"suggestions": [], "message": "No predictions found"}}

    latest = max(history, key=lambda x: x.get('created_at', ''))
    return {"success": True, "data": {"suggestions": latest.get('suggestions', [])}}
