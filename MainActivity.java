package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout main;
    int bg = Color.rgb(15, 16, 22);
    int card = Color.rgb(28, 30, 40);
    int white = Color.WHITE;
    int gray = Color.LTGRAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(white);
        t.setTextSize(size);
        t.setPadding(18, 14, 18, 14);
        return t;
    }

    TextView title(String value) {
        TextView t = text(value, 28);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(16);
        b.setAllCaps(false);
        return b;
    }

    void base() {
        ScrollView scroll = new ScrollView(this);

        main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(18, 18, 18, 30);
        main.setBackgroundColor(bg);

        scroll.addView(main);
        setContentView(scroll);
    }

    void showHome() {
        base();

        main.addView(title("🤖 Viyzo Worker"));

        TextView sub = text(
                "AI Managed Global Work Platform",
                16
        );
        sub.setTextColor(gray);
        sub.setGravity(Gravity.CENTER);
        main.addView(sub);

        main.addView(text(
                "\n💰 Available Earnings\n$0.00\n\n" +
                "📋 Active Jobs\n1\n\n" +
                "⭐ Worker Rating\nNew Worker",
                19
        ));

        main.addView(text(
                "🔥 Recommended for You",
                22
        ));

        main.addView(text(
                "📦 Product Listing\n" +
                "💵 Payment: $50\n" +
                "📦 Workload: 125 products\n" +
                "⏰ Deadline: 3 days",
                17
        ));

        Button jobs = button("🔎 View Available Jobs");
        Button myJobs = button("📋 My Jobs");
        Button earnings = button("💰 Earnings");
        Button profile = button("👤 Worker Profile");

        main.addView(jobs);
        main.addView(myJobs);
        main.addView(earnings);
        main.addView(profile);

        jobs.setOnClickListener(v -> showJobs());
        myJobs.setOnClickListener(v -> showMyJobs());
        earnings.setOnClickListener(v -> showEarnings());
        profile.setOnClickListener(v -> showProfile());
    }

    void showJobs() {
        base();

        main.addView(title("🔎 Available Jobs"));

        main.addView(text(
                "📦 PRODUCT LISTING\n\n" +
                "Company: Demo Company\n\n" +
                "💵 Worker Payment: $50\n" +
                "📦 Workload: 125 products\n" +
                "👥 Workers Needed: 8\n" +
                "⏰ Deadline: 3 days\n" +
                "⭐ Required Quality: High\n\n" +
                "Task:\n" +
                "Add product information, images and descriptions.",
                17
        ));

        Button details = button("📄 Job Details");
        Button accept = button("✅ Accept Job");
        Button back = button("⬅ Home");

        main.addView(details);
        main.addView(accept);
        main.addView(back);

        details.setOnClickListener(v -> showJobDetails());

        accept.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "✅ Job accepted successfully!",
                    Toast.LENGTH_LONG
            ).show();

            showMyJobs();
        });

        back.setOnClickListener(v -> showHome());
    }

    void showJobDetails() {
        base();

        main.addView(title("📄 Job Details"));

        main.addView(text(
                "Product Listing Job\n\n" +
                "Company: Demo Company\n\n" +
                "💵 Payment: $50\n" +
                "📦 Assigned Work: 125 products\n" +
                "⏰ Deadline: 3 days\n" +
                "⭐ Quality: High\n\n" +
                "You must complete the assigned product listings " +
                "according to the company's instructions.\n\n" +
                "After submission, the company will review the work.",
                17
        ));

        Button accept = button("✅ Accept This Job");
        Button back = button("⬅ Back");

        main.addView(accept);
        main.addView(back);

        accept.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "✅ Job accepted!",
                    Toast.LENGTH_LONG
            ).show();

            showMyJobs();
        });

        back.setOnClickListener(v -> showJobs());
    }

    void showMyJobs() {
        base();

        main.addView(title("📋 My Jobs"));

        main.addView(text(
                "🟡 ACTIVE JOB\n\n" +
                "📦 Product Listing\n" +
                "💵 Payment: $50\n" +
                "📊 Progress: 0%\n" +
                "⏰ Deadline: 3 days\n\n" +
                "Status: In Progress",
                18
        ));

        Button submit = button("📤 Submit Work");
        Button back = button("⬅ Home");

        main.addView(submit);
        main.addView(back);

        submit.setOnClickListener(v -> showSubmit());
        back.setOnClickListener(v -> showHome());
    }

    void showSubmit() {
        base();

        main.addView(title("📤 Submit Work"));

        main.addView(text(
                "Product Listing Job\n\n" +
                "Complete your assigned work and submit the details below.\n",
                17
        ));

        EditText work = new EditText(this);
        work.setHint("Enter your submission details...");
        work.setTextColor(white);
        work.setHintTextColor(gray);
        work.setMinLines(5);
        work.setGravity(Gravity.TOP);
        main.addView(work);

        Button submit = button("🚀 Submit for Review");
        Button back = button("⬅ Back");

        main.addView(submit);
        main.addView(back);

        submit.setOnClickListener(v -> {
            if (work.getText().toString().trim().isEmpty()) {
                Toast.makeText(
                        this,
                        "Please enter submission details.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    this,
                    "✅ Work submitted for review!",
                    Toast.LENGTH_LONG
            ).show();

            showMyJobs();
        });

        back.setOnClickListener(v -> showMyJobs());
    }

    void showEarnings() {
        base();

        main.addView(title("💰 Earnings"));

        main.addView(text(
                "Available Balance\n\n" +
                "$0.00\n\n" +
                "Pending Earnings\n\n" +
                "$50.00\n\n" +
                "Total Earned\n\n" +
                "$50.00",
                21
        ));

        Button withdraw = button("💸 Withdraw");
        Button back = button("⬅ Home");

        main.addView(withdraw);
        main.addView(back);

        withdraw.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Withdrawal system will be connected with the real payment backend later.",
                        Toast.LENGTH_LONG
                ).show()
        );

        back.setOnClickListener(v -> showHome());
    }

    void showProfile() {
        base();

        main.addView(title("👤 Worker Profile"));

        main.addView(text(
                "Name: Viyzo Worker\n\n" +
                "🌍 Country: India\n" +
                "⭐ Rating: New Worker\n" +
                "🛠 Skills: Product Listing\n" +
                "📊 Jobs Completed: 0\n" +
                "💰 Total Earnings: $0.00\n" +
                "✅ Verification: Demo",
                18
        ));

        Button back = button("⬅ Home");
        main.addView(back);

        back.setOnClickListener(v -> showHome());
    }
}
