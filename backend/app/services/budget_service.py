"""
Smart Loan AI - Budget Analysis Service
=========================================
Budget analysis, EMI calculation, and financial health scoring.
"""

import math
from ml.feature_engineering import compute_financial_health_score


class BudgetService:
    """Budget analysis and EMI calculation service."""

    @staticmethod
    def analyze_budget(data: dict) -> dict:
        """Comprehensive budget analysis."""
        annual_income = data.get('annual_income', 0)
        monthly_expenses = data.get('monthly_expenses', 0)
        existing_debts = data.get('existing_debts', 0)
        loan_amount = data.get('loan_amount', 0)
        loan_term = data.get('loan_term', 12)
        credit_score = data.get('credit_score', 650)

        monthly_income = annual_income / 12
        monthly_savings = monthly_income - monthly_expenses
        savings_rate = monthly_savings / max(monthly_income, 1) * 100

        # Financial health
        health = compute_financial_health_score(
            annual_income, monthly_expenses, existing_debts,
            credit_score, loan_amount, loan_term
        )

        # Expense breakdown
        expense_breakdown = data.get('expense_breakdown', {})
        if not expense_breakdown:
            expense_breakdown = {
                'Housing': monthly_expenses * 0.35,
                'Food': monthly_expenses * 0.20,
                'Transportation': monthly_expenses * 0.15,
                'Utilities': monthly_expenses * 0.10,
                'Entertainment': monthly_expenses * 0.10,
                'Other': monthly_expenses * 0.10
            }

        # Recommendations
        recommendations = []
        if savings_rate < 10:
            recommendations.append({
                'type': 'warning',
                'message': f'Your savings rate ({savings_rate:.1f}%) is very low. Aim for at least 20%.'
            })
        elif savings_rate < 20:
            recommendations.append({
                'type': 'info',
                'message': f'Your savings rate ({savings_rate:.1f}%) could be improved. Target 20-30%.'
            })
        else:
            recommendations.append({
                'type': 'success',
                'message': f'Great savings rate of {savings_rate:.1f}%! Keep it up.'
            })

        dti = existing_debts / max(annual_income, 1)
        if dti > 0.4:
            recommendations.append({
                'type': 'warning',
                'message': f'Your debt-to-income ratio ({dti:.0%}) is high. Focus on debt reduction.'
            })

        if monthly_savings < monthly_income * 0.1:
            recommendations.append({
                'type': 'info',
                'message': 'Consider building a 3-6 month emergency fund.'
            })

        return {
            'health_score': health['health_score'],
            'grade': health['grade'],
            'components': health['components'],
            'metrics': {
                **health['metrics'],
                'monthly_income': round(monthly_income, 2),
                'monthly_savings': round(monthly_savings, 2),
                'savings_rate': round(savings_rate, 1),
                'annual_income': annual_income,
            },
            'expense_breakdown': {k: round(v, 2) for k, v in expense_breakdown.items()},
            'recommendations': recommendations
        }

    @staticmethod
    def calculate_emi(loan_amount: float, interest_rate: float, loan_term: int,
                      monthly_income: float = 0) -> dict:
        """Calculate EMI and affordability."""
        monthly_rate = interest_rate / 100 / 12

        if monthly_rate > 0:
            emi = loan_amount * monthly_rate * math.pow(1 + monthly_rate, loan_term) / \
                  (math.pow(1 + monthly_rate, loan_term) - 1)
        else:
            emi = loan_amount / loan_term

        total_payment = emi * loan_term
        total_interest = total_payment - loan_amount

        emi_to_income = emi / max(monthly_income, 1) if monthly_income > 0 else 0
        affordable = emi_to_income <= 0.3

        return {
            'monthly_emi': round(emi, 2),
            'total_payment': round(total_payment, 2),
            'total_interest': round(total_interest, 2),
            'affordable': affordable,
            'emi_to_income_ratio': round(emi_to_income * 100, 1),
            'amortization_summary': {
                'first_month_interest': round(loan_amount * monthly_rate, 2) if monthly_rate > 0 else 0,
                'first_month_principal': round(emi - loan_amount * monthly_rate, 2) if monthly_rate > 0 else round(emi, 2),
            }
        }

    @staticmethod
    def build_dashboard(profile: dict, history: list) -> dict:
        """Build the financial dashboard payload for the frontend."""
        monthly_income = profile.get('monthly_income', 0.0)
        monthly_expenses = profile.get('monthly_expenses', 0.0)
        existing_emi = profile.get('existing_emi', 0.0)
        credit_score = profile.get('credit_score', 650)
        savings_balance = profile.get('savings_balance', 0.0)

        income_vs_expenses = [
            {'month': f'Month {i}', 'income': round(monthly_income, 2), 'expenses': round(monthly_expenses, 2)}
            for i in range(1, 7)
        ]

        net_worth_base = savings_balance
        financial_growth = []
        for i in range(1, 7):
            net_worth_base += max(monthly_income - monthly_expenses - existing_emi, 0)
            financial_growth.append({
                'month': f'Month {i}',
                'savings': round(monthly_income - monthly_expenses, 2),
                'investments': round(max((monthly_income - monthly_expenses) * 0.2, 0), 2),
                'net_worth': round(net_worth_base, 2)
            })

        dti_ratio = round(existing_emi / max(monthly_income, 1), 4)
        loan_probability = max((1 - dti_ratio) * 100, 10) if history else 0
        health_score = min(100, max(0, 50 + int((credit_score - 650) / 2) - int(dti_ratio * 30)))
        risk_level = 'Low Risk' if credit_score >= 700 else 'Moderate Risk' if credit_score >= 600 else 'High Risk'

        insights = [
            {'type': 'info', 'icon': 'trending_up', 'title': 'Savings Momentum',
             'message': f'Your savings rate is {round(max(monthly_income - monthly_expenses, 0) / max(monthly_income, 1) * 100, 1)}%.'},
            {'type': 'warning', 'icon': 'warning', 'title': 'Debt Coverage',
             'message': f'Your EMI coverage is {round(dti_ratio * 100, 1)}% of monthly income.'},
            {'type': 'success', 'icon': 'heart', 'title': 'Credit Health',
             'message': f'Your credit score is {credit_score}.'}
        ]

        emi_forecast = []
        for i in range(1, 7):
            emi_forecast.append({
                'month': f'Month {i}',
                'emi': round(existing_emi, 2),
                'remaining': round(max(monthly_income - monthly_expenses - existing_emi, 0) * (7 - i), 2)
            })

        recent_activity = []
        for record in history[:5]:
            recent_activity.append({
                'type': record.get('status', 'unknown'),
                'message': f"Loan {record.get('status')} for ${record.get('amount', 0):,.0f}",
                'time': record.get('date', ''),
                'result': f"{round(record.get('probability', 0)*100, 1)}%"
            })

        return {
            'loan_probability': round(min(max(loan_probability / 100, 0), 1) * 100, 1),
            'health_score': health_score,
            'risk_level': risk_level,
            'credit_score': credit_score,
            'monthly_savings': round(max(monthly_income - monthly_expenses, 0), 2),
            'dti_ratio': dti_ratio,
            'insights': insights,
            'income_vs_expenses': income_vs_expenses,
            'financial_growth': financial_growth,
            'risk_radar': [
                {'category': 'Credit', 'value': min(1.0, credit_score / 850)},
                {'category': 'Savings', 'value': min(1.0, max(monthly_income - monthly_expenses, 0) / max(monthly_income, 1))},
                {'category': 'Debt', 'value': max(0.0, 1 - dti_ratio)},
            ],
            'emi_forecast': emi_forecast,
            'recent_activity': recent_activity
        }

    @staticmethod
    def analyze_health_score(data: dict) -> dict:
        """Run a detailed health score evaluation."""
        annual_income = data.get('annual_income', 0.0)
        monthly_expenses = data.get('monthly_expenses', 0.0)
        existing_debts = data.get('existing_debts', 0.0)
        credit_score = data.get('credit_score', 650)
        loan_amount = data.get('loan_amount', 0.0)
        loan_term = data.get('loan_term', 12)

        health = compute_financial_health_score(
            annual_income, monthly_expenses, existing_debts,
            credit_score, loan_amount, loan_term
        )

        overall_score = int(round(health.get('health_score', 0)))
        grade = health.get('grade', 'Stable')
        summary = f"Your financial health is currently {grade} with a score of {overall_score}."

        breakdown = [
            {'category': 'Income Stability', 'score': min(100, int((annual_income / 12000) * 100)),
             'reasoning': ['Consistent income improves eligibility.']},
            {'category': 'Expense Control', 'score': min(100, int((1 - monthly_expenses / max(annual_income / 12, 1)) * 100)),
             'reasoning': ['Lower expenses relative to income improve your score.']},
            {'category': 'Debt Management', 'score': max(0, 100 - int(existing_debts / max(annual_income, 1) * 100)),
             'reasoning': ['Reducing debts will improve your rating.']}
        ]

        roadmap = [
            {'category': 'Credit Score', 'priority': 'high', 'actions': ['Pay bills on time', 'Reduce credit utilization']},
            {'category': 'Savings', 'priority': 'medium', 'actions': ['Build emergency savings', 'Automate monthly transfers']},
            {'category': 'Debt', 'priority': 'high', 'actions': ['Lower outstanding debt', 'Avoid new high-interest loans']}
        ]

        return {
            'overall_score': overall_score,
            'grade': grade,
            'grade_label': grade,
            'summary': summary,
            'breakdown': breakdown,
            'roadmap': roadmap
        }

    @staticmethod
    def analyze_risk(data: dict) -> dict:
        """Analyze financial risk and return a risk profile."""
        credit_score = data.get('credit_score', 650)
        annual_income = data.get('annual_income', 0.0)
        monthly_expenses = data.get('monthly_expenses', 0.0)
        existing_debts = data.get('existing_debts', 0.0)

        dti_ratio = min(1.0, existing_debts / max(annual_income, 1))
        overall_risk = int(round((1 - (credit_score - 300) / 550) * 100 * 0.6 + dti_ratio * 40))
        risk_level = 'Low' if overall_risk <= 40 else 'Moderate' if overall_risk <= 70 else 'High'
        risk_color = '#4CAF50' if risk_level == 'Low' else '#FFC107' if risk_level == 'Moderate' else '#F44336'

        dimensions = [
            {'dimension': 'Credit Score', 'severity': 'Low' if credit_score >= 700 else 'High',
             'value': str(credit_score), 'score': min(100, max(0, int((credit_score - 300) / 5.5))) ,
             'message': 'A strong credit score lowers your risk.'},
            {'dimension': 'Debt Load', 'severity': 'High' if dti_ratio >= 0.4 else 'Moderate',
             'value': f"{round(dti_ratio * 100, 1)}%", 'score': int((1 - dti_ratio) * 100),
             'message': 'High debt relative to income increases risk.'},
            {'dimension': 'Expense Ratio', 'severity': 'High' if monthly_expenses / max(annual_income / 12, 1) >= 0.7 else 'Low',
             'value': f"{round(monthly_expenses / max(annual_income / 12, 1) * 100, 1)}%", 'score': max(0, 100 - int(monthly_expenses / max(annual_income / 12, 1) * 100)),
             'message': 'Keeping expenses under control improves your risk profile.'}
        ]

        return {
            'risk_level': risk_level,
            'overall_risk': max(0, min(100, overall_risk)),
            'risk_color': risk_color,
            'summary': 'Your financial risk profile is based on credit health, debt load, and spending behavior.',
            'dimensions': dimensions
        }

    @staticmethod
    def simulate_scenario(data: dict) -> dict:
        """Simulate financial trajectory for a borrower."""
        monthly_income = data.get('monthly_income', data.get('annual_income', 0.0) / 12)
        monthly_expenses = data.get('monthly_expenses', 0.0)
        current_emi = data.get('current_emi', 0.0)
        loan_amount = data.get('loan_amount', 0.0)
        interest_rate = data.get('interest_rate', 10.0)
        loan_term = data.get('loan_term', 12)
        new_loan_amount = data.get('new_loan_amount', loan_amount)
        new_interest_rate = data.get('new_interest_rate', interest_rate)
        new_loan_term = data.get('new_loan_term', loan_term)

        baseline_net = monthly_income - monthly_expenses - current_emi
        new_emi = BudgetService.calculate_emi(new_loan_amount, new_interest_rate, new_loan_term)['monthly_emi']
        projected_net = monthly_income - monthly_expenses - new_emi

        baseline = {
            'trajectory': [],
            'final_savings': round(max(baseline_net, 0) * 12, 2),
            'monthly_net': round(baseline_net, 2),
            'current_emi': round(current_emi, 2),
            'new_emi': round(new_emi, 2)
        }
        projected = {
            'trajectory': [],
            'final_savings': round(max(projected_net, 0) * 12, 2),
            'monthly_net': round(projected_net, 2),
            'current_emi': round(current_emi, 2),
            'new_emi': round(new_emi, 2)
        }

        chart_data = []
        for month in range(1, 13):
            baseline_save = max(baseline_net, 0) * month
            projected_save = max(projected_net, 0) * month
            baseline['trajectory'].append({
                'month': month,
                'savings': round(baseline_save, 2),
                'net_income': round(monthly_income - monthly_expenses, 2),
                'cumulative_interest': round(month * current_emi * 0.05, 2)
            })
            projected['trajectory'].append({
                'month': month,
                'savings': round(projected_save, 2),
                'net_income': round(monthly_income - monthly_expenses, 2),
                'cumulative_interest': round(month * new_emi * 0.05, 2)
            })
            chart_data.append({
                'month': "M" + str(month),
                'baseline': round(baseline_save, 2),
                'projected': round(projected_save, 2)
            })

        comparison = {
            'savings_difference': round(projected['final_savings'] - baseline['final_savings'], 2),
            'monthly_difference': round(projected_net - baseline_net, 2),
            'emi_difference': round(new_emi - current_emi, 2),
            'projection_months': 12
        }

        recommendations = []
        if projected_net > baseline_net:
            recommendations.append({'type': 'success', 'message': 'Projected savings improve with the new loan scenario.'})
        else:
            recommendations.append({'type': 'warning', 'message': 'Projected cash flow may be tighter under this scenario.'})

        recommendations.append({'type': 'info', 'message': 'Consider reducing variable expenses to improve your monthly surplus.'})

        return {
            'summary': 'This simulation compares your current path against a new loan scenario.',
            'baseline': baseline,
            'projected': projected,
            'comparison': comparison,
            'chart_data': chart_data,
            'recommendations': recommendations
        }
