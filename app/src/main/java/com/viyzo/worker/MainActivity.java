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
    private int RED = Color.rgb(150, 50, 50);

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
        welcome.setPadding(0, 15, 0, 25);

        Button jobs = button("🔎 Available Jobs", BLUE);
        Button myJobs = button("📋 My Jobs", CARD);
        Button earnings = button("💰 Earnings", CARD);
        Button profile = button("👤 Profile", CARD);
        Button logout = button("🚪 Logout", RED);

        root.addView(title);
        root.addView(welcome);
        root.addView(jobs);
        root.addView(myJobs);
        root.addView(earnings);
        root.addView(profile);
        root.addView(logout);

        setContentView(root);

        jobs.setOnClickListener(v -> showJobs());

        myJobs.setOnClickListener(v -> showMyJobs());

        earnings.setOnClickListener(v -> showEarnings());

        profile.setOnClickListener(v -> showProfile(user));

        logout.setOnClickListener(v -> showHome());
    }

    private void showJobs() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
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

        Button accept = button("✅ Accept Job", GREEN);
        Button back = button("← Back", CARD);

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

    private void showMyJobs() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("📋 My Jobs");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView info = new TextView(this);
        info.setText(
                "No active jobs\n\n" +
                "Accepted jobs will appear here."
        );
        info.setTextColor(GRAY);
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);
        info.setPadding(20, 40, 20, 40);

        Button back = button("← Back", CARD);

        root.addView(title);
        root.addView(info);
        root.addView(back);

        setContentView(root);

        back.setOnClickListener(v -> showHome());
    }

    private void showEarnings() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("💰 Earnings");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView amount = new TextView(this);
        amount.setText("$0.00");
        amount.setTextColor(GREEN);
        amount.setTextSize(36);
        amount.setGravity(Gravity.CENTER);
        amount.setPadding(0, 30, 0, 10);

        TextView available = new TextView(this);
        available.setText("Available Earnings");
        available.setTextColor(GRAY);
        available.setTextSize(16);
        available.setGravity(Gravity.CENTER);

        Button withdraw = button("💸 Withdraw", GREEN);
        Button history = button("📜 Withdrawal History", CARD);
        Button back = button("← Back", CARD);

        root.addView(title);
        root.addView(amount);
        root.addView(available);
        root.addView(withdraw);
        root.addView(history);
        root.addView(back);

        setContentView(root);

        withdraw.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Minimum withdrawal: $5.00\nSecure payout system coming later",
                        Toast.LENGTH_LONG
                ).show()
        );

        history.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "No withdrawal history",
                        Toast.LENGTH_SHORT
                ).show()
        );

        back.setOnClickListener(v -> showHome());
    }

    private void showProfile(String user) {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("👤 Profile");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView profile = new TextView(this);
        profile.setText(
                "Name: " + user + "\n\n" +
                "Role: Worker\n\n" +
                "KYC Status: Not Completed\n\n" +
                "Account Status: Demo"
        );
        profile.setTextColor(WHITE);
        profile.setTextSize(18);
        profile.setPadding(20, 30, 20, 30);
        profile.setBackgroundColor(CARD);

        Button kyc = button("🪪 Start KYC", BLUE);
        Button back = button("← Back", CARD);

        root.addView(title);
        root.addView(profile);
        root.addView(kyc);
        root.addView(back);

        setContentView(root);

        kyc.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Secure KYC system will be connected later",
                        Toast.LENGTH_SHORT
                ).show()
        );

        back.setOnClickListener(v -> showHome());
    }

    private Button button(String text, int color) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(18);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackgroundColor(color);

        return b;
    }
}
