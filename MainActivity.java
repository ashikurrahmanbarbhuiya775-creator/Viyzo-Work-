package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout main;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        showHome();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setPadding(10, 10, 10, 10);
        return t;
    }

    Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(16);
        return b;
    }

    void base() {
        main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(25, 25, 25, 25);
        main.setBackgroundColor(Color.rgb(16, 16, 20));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(main);
        setContentView(scroll);
    }

    void showHome() {
        base();

        TextView title = text("🤖 Viyzo Worker", 30);
        title.setGravity(Gravity.CENTER);
        main.addView(title);

        TextView sub = text("AI Managed Global Work Platform", 16);
        sub.setTextColor(Color.LTGRAY);
        sub.setGravity(Gravity.CENTER);
        main.addView(sub);

        main.addView(text("💰 Earnings: $0.00", 20));

        Button jobs = button("🔎 Available Jobs");
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

        main.addView(text("🔎 Available Jobs", 28));

        main.addView(text(
            "📦 Product Listing\n\n" +
            "Company: Demo Company\n" +
            "Budget: $500\n" +
            "Worker Pool: $400\n" +
            "Workers Needed: 8\n" +
            "Payment: $50 / worker\n" +
            "Workload: 125 products\n" +
            "Deadline: 3 days\n\n" +
            "Task: Add product information, images and descriptions.",
            17
        ));

        Button details = button("📄 Job Details");
        Button accept = button("✅ Accept Job");
        Button back = button("⬅ Back");

        main.addView(details);
        main.addView(accept);
        main.addView(back);

        details.setOnClickListener(v -> showJobDetails());

        accept.setOnClickListener(v -> {
            Toast.makeText(this,
                "✅ Job accepted successfully!",
                Toast.LENGTH_LONG).show();
            showMyJobs();
        });

        back.setOnClickListener(v -> showHome());
    }

    void showJobDetails() {
        base();

        main.addView(text("📄 Job Details", 28));

        main.addView(text(
            "Product Listing Job\n\n" +
            "💵 Worker Payment: $50\n" +
            "📦 Workload: 125 products\n" +
            "⏰ Deadline: 3 days\n" +
            "⭐ Required Quality: High\n\n" +
            "Complete the assigned product listings according to the company's instructions.",
            17
        ));

        Button accept = button("✅ Accept This Job");
        Button back = button("⬅ Back to Jobs");

        main.addView(accept);
        main.addView(back);

        accept.setOnClickListener(v -> showMyJobs());
        back.setOnClickListener(v -> showJobs());
    }

    void showMyJobs() {
        base();

        main.addView(text("📋 My Jobs", 28));

        main.addView(text(
            "Active Job\n\n" +
            "📦 Product Listing\n" +
            "Payment: $50\n" +
            "Progress: 0%\n" +
            "Deadline: 3 days\n\n" +
            "Status: 🟡 In Progress",
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

        main.addView(text("📤 Submit Work", 28));

        main.addView(text(
            "Product Listing Job\n\n" +
            "When your work is completed, submit it for company review.",
            17
        ));

        EditText work = new EditText(this);
        work.setHint("Enter your submission details...");
        work.setTextColor(Color.WHITE);
        work.setHintTextColor(Color.LTGRAY);

        main.addView(work);

        Button submit = button("🚀 Submit for Review");
        Button back = button("⬅ Back");

        main.addView(submit);
        main.addView(back);

        submit.setOnClickListener(v -> {
            Toast.makeText(this,
                "✅ Work submitted for review!",
                Toast.LENGTH_LONG).show();
            showMyJobs();
        });

        back.setOnClickListener(v -> showMyJobs());
    }

    void showEarnings() {
        base();

        main.addView(text("💰 Earnings", 28));

        main.addView(text(
            "Available Balance\n\n" +
            "$0.00\n\n" +
            "Pending Earnings\n" +
            "$50.00\n\n" +
            "Total Earned\n" +
            "$50.00",
            20
        ));

        Button withdraw = button("💸 Withdraw");
        Button back = button("⬅ Home");

        main.addView(withdraw);
        main.addView(back);

        withdraw.setOnClickListener(v ->
            Toast.makeText(this,
                "Withdrawal system will be connected later.",
                Toast.LENGTH_LONG).show()
        );

        back.setOnClickListener(v -> showHome());
    }

    void showProfile() {
        base();

        main.addView(text("👤 Worker Profile", 28));

        main.addView(text(
            "Name: Viyzo Worker\n\n" +
            "⭐ Rating: New Worker\n" +
            "🛠 Skills: Product Listing\n" +
            "🌍 Country: India\n" +
            "📊 Jobs Completed: 0\n" +
            "💰 Total Earnings: $0.00",
            18
        ));

        Button back = button("⬅ Home");
        main.addView(back);

        back.setOnClickListener(v -> showHome());
    }
}
