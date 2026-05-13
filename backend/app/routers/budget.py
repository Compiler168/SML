"""
Smart Loan AI - Budget Router
"""

from fastapi import APIRouter, Depends
from models.schemas import BudgetInput, EMICalculation
from services.budget_service import BudgetService
from services.loan_service import LoanService
from firebase.operations import FirebaseOperations
from routers.auth import get_current_user

router = APIRouter()


@router.post("/analyze")
async def analyze_budget(budget: BudgetInput, current_user: dict = Depends(get_current_user)):
    """Analyze budget and get financial health score."""
    result = BudgetService.analyze_budget(budget.model_dump())
    return {"success": True, "data": result}


@router.post("/emi-calculator")
async def calculate_emi(emi: EMICalculation, current_user: dict = Depends(get_current_user)):
    """Calculate EMI and affordability."""
    monthly_income = 0
    result = BudgetService.calculate_emi(
        emi.loan_amount, emi.interest_rate, emi.loan_term, monthly_income
    )
    return {"success": True, "data": result}


@router.get("/dashboard")
async def get_dashboard(current_user: dict = Depends(get_current_user)):
    """Get the user's financial dashboard summary."""
    user_doc = FirebaseOperations.get("users", current_user["user_id"])
    profile = user_doc.get("profile", {}) if user_doc else {}
    history = LoanService.get_history(current_user["user_id"])
    result = BudgetService.build_dashboard(profile, history)
    return {"success": True, "data": result}


@router.post("/health-score")
async def health_score(data: dict, current_user: dict = Depends(get_current_user)):
    """Calculate detailed financial health score."""
    result = BudgetService.analyze_health_score(data)
    return {"success": True, "data": result}


@router.post("/risk-analysis")
async def risk_analysis(data: dict, current_user: dict = Depends(get_current_user)):
    """Analyze financial risk."""
    result = BudgetService.analyze_risk(data)
    return {"success": True, "data": result}


@router.post("/simulate")
async def simulate(data: dict, current_user: dict = Depends(get_current_user)):
    """Simulate financial outcomes and loan scenarios."""
    result = BudgetService.simulate_scenario(data)
    return {"success": True, "data": result}
