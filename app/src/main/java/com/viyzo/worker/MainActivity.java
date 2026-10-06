package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private int BG = Color.rgb(16, 16, 20);
    private int WHITE = Color.WHITE;
    private int GRAY = Color.LTGRAY;
    private int BLUE = Color.rgb(60, 130, 255);
    private int GREEN = Color.rgb(40, 200, 120);
    private int CARD = Color.rgb(35, 35, 45);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private void showHome() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 30, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("🤖 Viyzo Worker");
        title.setTextColor(WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("AI Managed Global Work Platform");
        subtitle.setTextColor(GRAY);
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 15, 0, 30);

        Button login = new Button(this);
        login.setText("🔐 Login");
        login.setTextSize(18);
        login.setTextColor(WHITE);
        login.setBackgroundColor(BLUE);

        Button signup = new Button(this);
        signup.setText("📝 Create Account");
        signup.setTextSize(18);
        signup.setTextColor(WHITE);
        signup.setBackgroundColor(GREEN);

        root.addView(title);
        root.addView(subtitle);
        root.addView(login);
        root.addView(signup);

        setContentView(root);

        login.setOnClickListener(v -> showLogin());

        signup.setOnClickListener(v -> showSignup());
    }

    private void showLogin() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 30, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("🔐 Login");
        title.setTextColor(WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        EditText email = new EditText(this);
        email.setHint("Email or Mobile");
        email.setHintTextColor(GRAY);
        email.setTextColor(WHITE);
        email.setTextSize(17);
        email.setSingleLine(true);

        EditText password = new EditText(this);
        password.setHint("Password");
        password.setHintTextColor(GRAY);
        password.setTextColor(WHITE);
        password.setTextSize(17);
        password.setSingleLine(true);

        Button login = new Button(this);
        login.setText("Login");
        login.setTextSize(18);
        login.setTextColor(WHITE);
        login.setBackgroundColor(BLUE);

        Button back = new Button(this);
        back.setText("← Back");
        back.setTextSize(17);
        back.setTextColor(WHITE);
        back.setBackgroundColor(Color.DKGRAY);

        root.addView(title);
        root.addView(email);
        root.addView(password);
        root.addView(login);
        root.addView(back);

        setContentView(root);

        login.setOnClickListener(v -> {

            String user = email.getText().toString().trim();
            String pass = password.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please enter Email/Mobile and Password",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showDashboard(user);
        });

        back.setOnClickListener(v -> showHome());
    }

    private void showSignup() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 30, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("📝 Create Account");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        EditText name = new EditText(this);
        name.setHint("Full Name");
        name.setHintTextColor(GRAY);
        name.setTextColor(WHITE);
        name.setTextSize(17);
        name.setSingleLine(true);

        EditText email = new EditText(this);
        email.setHint("Email");
        email.setHintTextColor(GRAY);
        email.setTextColor(WHITE);
        email.setTextSize(17);
        email.setSingleLine(true);

        EditText mobile = new EditText(this);
        mobile.setHint("Mobile Number");
        mobile.setHintTextColor(GRAY);
        mobile.setTextColor(WHITE);
        mobile.setTextSize(17);
        mobile.setSingleLine(true);

        EditText password = new EditText(this);
        password.setHint("Create Password");
        password.setHintTextColor(GRAY);
        password.setTextColor(WHITE);
        password.setTextSize(17);
        password.setSingleLine(true);

        Button create = new Button(this);
        create.setText("Create Account");
        create.setTextSize(18);
        create.setTextColor(WHITE);
        create.setBackgroundColor(GREEN);

        Button back = new Button(this);
        back.setText("← Back");
        back.setTextSize(17);
        back.setTextColor(WHITE);
        back.setBackgroundColor(Color.DKGRAY);

        root.addView(title);
        root.addView(name);
        root.addView(email);
        root.addView(mobile);
        root.addView(password);
        root.addView(create);
        root.addView(back);

        setContentView(root);

        create.setOnClickListener(v -> {

            String n = name.getText().toString().trim();
            String e = email.getText().toString().trim();
            String m = mobile.getText().toString().trim();
            String p = password.getText().toString();

            if (n.isEmpty() || e.isEmpty() || m.isEmpty() || p.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Account created in Demo Mode",
                    Toast.LENGTH_SHORT
            ).show();

            showDashboard(n);
        });

        back.setOnClickListener(v -> showHome());
    }

    private void showDashboard(String user) {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.TOP);
        root.setPadding(30, 50, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("🤖 Viyzo Worker");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView welcome = new TextView(this);
        welcome.setText("Welcome, " + user);
        welcome.setTextColor(GRAY);
        welcome.setTextSize(17);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, 15, 0, 30);

        TextView dashboard = new TextView(this);
        dashboard.setText("🌍 Worker Dashboard");
        dashboard.setTextColor(WHITE);
        dashboard.setTextSize(22);
        dashboard.setGravity(Gravity.CENTER);
        dashboard.setPadding(0, 10, 0, 20);

        Button jobs = new Button(this);
        jobs.setText("🔎 Available Jobs");
        jobs.setTextSize(18);
        jobs.setTextColor(WHITE);
        jobs.setBackgroundColor(BLUE);

        Button myJobs = new Button(this);
        myJobs.setText("📋 My Jobs");
        myJobs.setTextSize(18);
        myJobs.setTextColor(WHITE);
        myJobs.setBackgroundColor(CARD);

        Button earnings = new Button(this);
        earnings.setText("💰 Earnings");
        earnings.setTextSize(18);
        earnings.setTextColor(WHITE);
        earnings.setBackgroundColor(CARD);

        Button logout = new Button(this);
        logout.setText("🚪 Logout");
        logout.setTextSize(18);
        logout.setTextColor(WHITE);
        logout.setBackgroundColor(Color.rgb(150, 50, 50));

        root.addView(title);
        root.addView(welcome);
        root.addView(dashboard);
        root.addView(jobs);
        root.addView(myJobs);
        root.addView(earnings);
        root.addView(logout);

        setContentView(root);

        jobs.setOnClickListener(v -> showJobs());

        myJobs.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "No active jobs yet",
                        Toast.LENGTH_SHORT
                ).show()
        );

        earnings.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Current Earnings: $0.00",
                        Toast.LENGTH_SHORT
                ).show()
        );

        logout.setOnClickListener(v -> showHome());
    }

    private void showJobs() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.TOP);
        root.setPadding(30, 50, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("🔎 Available Jobs");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView job = new TextView(this);
        job.setText(
                "📦 Product Listing\n\n" +
                "Workload: 125 products\n" +
                "Payment: $50.00\n" +
                "Deadline: 3 days"
        );
        job.setTextColor(WHITE);
        job.setTextSize(18);
        job.setPadding(20, 30, 20, 30);
        job.setBackgroundColor(CARD);

        Button accept = new Button(this);
        accept.setText("✅ Accept Job");
        accept.setTextSize(18);
        accept.setTextColor(WHITE);
        accept.setBackgroundColor(GREEN);

        Button back = new Button(this);
        back.setText("← Back");
        back.setTextSize(17);
        back.setTextColor(WHITE);
        back.setBackgroundColor(Color.DKGRAY);

        root.addView(title);
        root.addView(job);
        root.addView(accept);
        root.addView(back);

        setContentView(root);

        accept.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Job accepted in Demo Mode",
                        Toast.LENGTH_SHORT
                ).show()
        );

        back.setOnClickListener(v -> showHome());
    }
}
