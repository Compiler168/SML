package com.smartloanai.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.smartloanai.R;
import com.smartloanai.SmartLoanApp;
import com.smartloanai.ui.auth.LoginActivity;

/**
 * Onboarding activity with 3 intro screens using ViewPager2.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext, btnSkip;
    private LinearLayout dotsLayout;

    private final int[] titles = {
        R.string.onboarding_title_1,
        R.string.onboarding_title_2,
        R.string.onboarding_title_3
    };

    private final int[] descriptions = {
        R.string.onboarding_desc_1,
        R.string.onboarding_desc_2,
        R.string.onboarding_desc_3
    };

    private final int[] icons = {
        android.R.drawable.ic_menu_search,
        android.R.drawable.ic_menu_send,
        android.R.drawable.ic_menu_compass
    };

    private final int[] bgColors = {
        R.color.onboarding_bg_1,
        R.color.onboarding_bg_2,
        R.color.onboarding_bg_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.onboarding_viewpager);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);
        dotsLayout = findViewById(R.id.dots_layout);

        viewPager.setAdapter(new OnboardingAdapter());
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == titles.length - 1) {
                    btnNext.setText(R.string.get_started);
                    btnSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText(R.string.next);
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < titles.length - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                completeOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        SmartLoanApp.getInstance().setOnboardingComplete();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupDots(int currentPage) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < titles.length; i++) {
            TextView dot = new TextView(this);
            dot.setText("●");
            dot.setTextSize(14);
            dot.setTextColor(getColor(i == currentPage ? R.color.dot_active : R.color.dot_inactive));
            dot.setPadding(8, 0, 8, 0);
            dotsLayout.addView(dot);
        }
    }

    /**
     * ViewPager2 adapter for onboarding pages.
     */
    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_page, parent, false);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            holder.title.setText(titles[position]);
            holder.description.setText(descriptions[position]);
            holder.icon.setImageResource(icons[position]);
            holder.itemView.setBackgroundColor(getColor(bgColors[position]));
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        class PageViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, description;

            PageViewHolder(View view) {
                super(view);
                icon = view.findViewById(R.id.onboarding_icon);
                title = view.findViewById(R.id.onboarding_title);
                description = view.findViewById(R.id.onboarding_description);
            }
        }
    }
}
