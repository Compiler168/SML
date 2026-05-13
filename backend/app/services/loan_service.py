"""
Smart Loan AI - Loan Prediction Service
=========================================
Handles ML model loading and loan eligibility prediction.
"""

import pickle
import json
import os
import numpy as np

from firebase.operations import FirebaseOperations

PREDICTIONS_COLLECTION = "predictions"
_model = None
_preprocessor = None
_feature_names = None


class LoanService:
    """Loan eligibility prediction service."""

    @staticmethod
    def load_model():
        """Load the trained ML model and preprocessor."""
        global _model, _preprocessor, _feature_names
        base = os.path.dirname(__file__)

        model_path = os.path.join(base, '..', 'ml', 'trained_model.pkl')
        prep_path = os.path.join(base, '..', 'ml', 'preprocessor.pkl')
        feat_path = os.path.join(base, '..', 'ml', 'feature_names.json')

        if os.path.exists(model_path):
            with open(model_path, 'rb') as f:
                _model = pickle.load(f)

        if os.path.exists(prep_path):
            with open(prep_path, 'rb') as f:
                _preprocessor = pickle.load(f)

        if os.path.exists(feat_path):
            with open(feat_path, 'r') as f:
                _feature_names = json.load(f)

    @staticmethod
    def predict(data: dict, user_id: str = None) -> dict:
        """Run loan eligibility prediction."""
        from ml.feature_engineering import (
            prepare_single_prediction, compute_risk_category,
            compute_financial_health_score, generate_improvement_suggestions
        )

        # Prepare features
        df = prepare_single_prediction(data)

        if _model is not None and _preprocessor is not None:
            # Use trained model
            from ml.preprocessing import NUMERIC_FEATURES, CATEGORICAL_FEATURES, ENGINEERED_FEATURES
            feature_cols = NUMERIC_FEATURES + CATEGORICAL_FEATURES + ENGINEERED_FEATURES
            X = _preprocessor.transform(df[feature_cols])
            probability = float(_model.predict_proba(X)[0][1])
            approved = probability >= 0.5
        else:
            # Fallback: rule-based prediction
            probability = LoanService._rule_based_predict(data)
            approved = probability >= 0.5

        # Risk assessment
        risk_level = compute_risk_category(data.get('credit_score', 650))
        risk_colors = {
            'Very Low Risk': '#4CAF50', 'Low Risk': '#8BC34A',
            'Moderate Risk': '#FFC107', 'High Risk': '#FF9800',
            'Very High Risk': '#F44336'
        }

        # Financial health
        health = compute_financial_health_score(
            data.get('annual_income', 0), data.get('monthly_expenses', 0),
            data.get('existing_debts', 0), data.get('credit_score', 650),
            data.get('loan_amount', 0), data.get('loan_term', 12)
        )

        monthly_income = data.get('annual_income', 0) / 12
        monthly_expenses = data.get('monthly_expenses', 0)
        requested_emi = LoanService._calculate_emi(
            data.get('loan_amount', 0), data.get('interest_rate', 10.0), data.get('loan_term', 12)
        )
        dti_ratio = round(data.get('existing_debts', 0) / max(data.get('annual_income', 1), 1), 4)
        savings_ratio = round(max(monthly_income - monthly_expenses, 0) / max(monthly_income, 1), 4)

        suggestions = generate_improvement_suggestions(
            data.get('credit_score', 650), data.get('annual_income', 0),
            data.get('monthly_expenses', 0), data.get('existing_debts', 0),
            data.get('loan_amount', 0), data.get('loan_term', 12),
            data.get('employment_status', ''), data.get('education', '')
        )

        top_factors = {
            'credit_score': round(max(0.0, min(1.0, (850 - data.get('credit_score', 650)) / 550)), 4),
            'dti_ratio': round(min(1.0, dti_ratio * 2), 4),
            'loan_amount': round(min(1.0, data.get('loan_amount', 0) / max(data.get('annual_income', 1), 1)), 4),
            'savings_ratio': round(1.0 - min(1.0, savings_ratio), 4)
        }

        risk_reasons = []
        if data.get('credit_score', 650) < 650:
            risk_reasons.append({
                'factor': 'Credit Score',
                'severity': 'High',
                'message': 'Your credit score is below the preferred range.',
                'suggestion': 'Build credit by paying bills on time and reducing outstanding debt.'
            })
        if dti_ratio > 0.4:
            risk_reasons.append({
                'factor': 'Debt-to-Income',
                'severity': 'Moderate',
                'message': 'Your debt-to-income ratio is higher than recommended.',
                'suggestion': 'Pay down existing debt and avoid taking new loans.'
            })
        if monthly_expenses > monthly_income * 0.8:
            risk_reasons.append({
                'factor': 'Expenses',
                'severity': 'Moderate',
                'message': 'Your monthly expenses are consuming most of your income.',
                'suggestion': 'Reduce discretionary expenses and increase savings.'
            })
        if not risk_reasons:
            risk_reasons.append({
                'factor': 'Financial Stability',
                'severity': 'Low',
                'message': 'Your profile looks stable, but keep monitoring your cash flow.',
                'suggestion': 'Maintain your current habits and build emergency savings.'
            })

        result = {
            'ensemble': {
                'probability': round(probability, 4),
                'approved': approved,
                'confidence': 'High' if probability >= 0.75 else 'Medium' if probability >= 0.5 else 'Low',
                'confidence_score': round(probability, 4)
            },
            'models': {
                'ensemble_model': {
                    'probability': round(probability, 4),
                    'approved': approved
                }
            },
            'risk_reasons': risk_reasons,
            'top_factors': top_factors,
            'derived_metrics': {
                'requested_emi': round(requested_emi, 2),
                'dti_ratio': dti_ratio,
                'savings_ratio': savings_ratio
            }
        }

        if user_id:
            pred_data = {**data, **result, 'user_id': user_id}
            pred_id = FirebaseOperations.create(PREDICTIONS_COLLECTION, pred_data)
            result['prediction_id'] = pred_id

        return result

    @staticmethod
    def _calculate_emi(amount: float, interest_rate: float, term: int) -> float:
        """Calculate requested EMI for a loan amount."""
        monthly_rate = interest_rate / 100 / 12
        if monthly_rate > 0 and term > 0:
            return amount * monthly_rate * (1 + monthly_rate) ** term / ((1 + monthly_rate) ** term - 1)
        if term > 0:
            return amount / term
        return 0.0

    @staticmethod
    def _rule_based_predict(data: dict) -> float:
        """Fallback rule-based prediction when model is not loaded."""
        score = 0.0
        cs = data.get('credit_score', 600)
        if cs >= 750: score += 0.30
        elif cs >= 700: score += 0.22
        elif cs >= 650: score += 0.15
        elif cs >= 600: score += 0.08

        inc = data.get('annual_income', 0)
        if inc >= 100000: score += 0.20
        elif inc >= 60000: score += 0.15
        elif inc >= 40000: score += 0.10
        elif inc >= 25000: score += 0.05

        dti = data.get('existing_debts', 0) / max(inc, 1)
        if dti < 0.2: score += 0.20
        elif dti < 0.35: score += 0.15
        elif dti < 0.5: score += 0.08

        emp = data.get('employment_status', '')
        emp_scores = {'Employed': 0.15, 'Self-Employed': 0.12, 'Retired': 0.08, 'Part-Time': 0.05, 'Unemployed': 0.0}
        score += emp_scores.get(emp, 0)

        edu = data.get('education', '')
        edu_scores = {'PhD': 0.10, 'Master': 0.08, 'Bachelor': 0.06, 'Associate': 0.04, 'High School': 0.02}
        score += edu_scores.get(edu, 0)

        lti = data.get('loan_amount', 0) / max(inc, 1)
        if lti < 1: score += 0.05
        elif lti > 3: score -= 0.10

        return min(max(score, 0.0), 1.0)

    @staticmethod
    def get_history(user_id: str) -> list:
        """Get prediction history for a user."""
        history = FirebaseOperations.query(PREDICTIONS_COLLECTION, "user_id", "==", user_id)
        return [LoanService._format_prediction_record(p) for p in sorted(history, key=lambda x: x.get('created_at', ''), reverse=True)]

    @staticmethod
    def get_stats(user_id: str) -> dict:
        """Compute loan statistics for a user."""
        history = LoanService.get_history(user_id)
        total = len(history)
        approved = sum(1 for item in history if item.get('status') == 'approved')
        latest = history[0] if history else None
        return {
            'total_predictions': total,
            'approved_count': approved,
            'rejection_count': total - approved,
            'approval_rate': f"{round((approved / total * 100) if total else 0, 1)}%",
            'latest_probability': latest.get('probability') if latest else 0.0
        }

    @staticmethod
    def get_all_predictions(limit: int = 100) -> list:
        """Get all predictions (admin)."""
        return FirebaseOperations.get_all(PREDICTIONS_COLLECTION, limit)

    @staticmethod
    def _format_prediction_record(prediction: dict) -> dict:
        """Format a prediction record for frontend consumption."""
        return {
            'id': prediction.get('id'),
            'date': prediction.get('created_at', ''),
            'amount': prediction.get('loan_amount', 0.0),
            'status': 'approved' if prediction.get('approved') else 'rejected',
            'probability': round(prediction.get('probability', 0.0), 4)
        }
