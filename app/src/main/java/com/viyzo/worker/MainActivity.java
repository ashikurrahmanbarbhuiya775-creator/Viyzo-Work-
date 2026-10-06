package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(16, 16, 20);
    private final int CARD = Color.rgb(28, 28, 36);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.LTGRAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private LinearLayout baseLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 35, 28, 35);
        layout.setBackgroundColor(BG);
        return layout;
    }

    private ScrollView screenContainer(LinearLayout content) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.addView(content);
        return scroll;
    }

    private TextView title(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(WHITE);
        t.setTextSize(28);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 15, 0, 20);
        return t;
    }

    private TextView subtitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(GRAY);
        t.setTextSize(16);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 5, 0, 20);
        return t;
    }

    private Button button(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setOnClickListener(listener);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, 8, 0, 8);
        b.setLayoutParams(p);

        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.GRAY);
        e.setTextColor(WHITE);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(20, 12, 20, 12);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, 6, 0, 6);
        e.setLayoutParams(p);

        return e;
    }

    // ----------------------------------------------------
    // HOME
    // ----------------------------------------------------

    private void showHome() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🤖 Viyzo Worker"));
        layout.addView(subtitle("AI Managed Global Work Platform"));

        layout.addView(button("🔐 Login", v -> showLogin()));

        layout.addView(button("📝 Create Account", v -> showCreateAccount()));

        layout.addView(button("🔎 Available Jobs", v -> showJobs()));

        layout.addView(button("💰 Earnings", v -> showEarnings()));

        layout.addView(button("👤 Profile", v -> showProfile()));

        layout.addView(button("🔔 Notifications", v -> showNotifications()));

        layout.addView(button("⚙️ Settings", v -> showSettings()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // LOGIN
    // ----------------------------------------------------

    private void showLogin() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🔐 Worker Login"));

        EditText email = input("Email or Phone");

        EditText password = input("Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        layout.addView(email);
        layout.addView(password);

        layout.addView(button("Login", v -> {

            Toast.makeText(
                    this,
                    "Login system will connect to backend later",
                    Toast.LENGTH_SHORT
            ).show();

            showDashboard();
        }));

        layout.addView(button("Create New Account", v -> showCreateAccount()));

        layout.addView(button("← Back", v -> showHome()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // CREATE ACCOUNT
    // ----------------------------------------------------

    private void showCreateAccount() {

        LinearLayout layout = baseLayout();

        layout.addView(title("📝 Create Worker Account"));

        EditText name = input("Full Name");
        EditText email = input("Email");
        EditText phone = input("Phone Number");

        EditText password = input("Create Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        layout.addView(name);
        layout.addView(email);
        layout.addView(phone);
        layout.addView(password);

        layout.addView(button("Create Account", v -> {

            Toast.makeText(
                    this,
                    "Account created successfully (demo)",
                    Toast.LENGTH_SHORT
            ).show();

            showDashboard();
        }));

        layout.addView(button("← Back", v -> showHome()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // DASHBOARD
    // ----------------------------------------------------

    private void showDashboard() {

        LinearLayout layout = baseLayout();

        layout.addView(title("📊 Worker Dashboard"));

        layout.addView(subtitle(
                "Welcome to your Viyzo Worker dashboard"
        ));

        layout.addView(button("🔎 Available Jobs", v -> showJobs()));

        layout.addView(button("📋 My Jobs", v -> showMyJobs()));

        layout.addView(button("💰 Earnings", v -> showEarnings()));

        layout.addView(button("🔔 Notifications", v -> showNotifications()));

        layout.addView(button("👤 Profile", v -> showProfile()));

        layout.addView(button("⚙️ Settings", v -> showSettings()));

        layout.addView(button("🚪 Logout", v -> showHome()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // AVAILABLE JOBS
    // ----------------------------------------------------

    private void showJobs() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🔎 Available Jobs"));

        layout.addView(subtitle(
                "Jobs available for workers"
        ));

        layout.addView(button(
                "📦 Product Listing - $50",
                v -> showJobDetails("Product Listing", "$50")
        ));

        layout.addView(button(
                "📝 Data Entry - $30",
                v -> showJobDetails("Data Entry", "$30")
        ));

        layout.addView(button(
                "🌐 Translation - $40",
                v -> showJobDetails("Translation", "$40")
        ));

        layout.addView(button(
                "📱 App Testing - $25",
                v -> showJobDetails("App Testing", "$25")
        ));

        layout.addView(button("← Back", v -> showDashboard()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // JOB DETAILS
    // ----------------------------------------------------

    private void showJobDetails(String jobName, String payment) {

        LinearLayout layout = baseLayout();

        layout.addView(title("📋 Job Details"));

        layout.addView(subtitle(jobName));

        TextView info = new TextView(this);

        info.setText(
                "Job: " + jobName +
                "\n\nWorker Payment: " + payment +
                "\n\nStatus: Available" +
                "\n\nAI Master Manager will assign the final workload."
        );

        info.setTextColor(WHITE);
        info.setTextSize(17);
        info.setPadding(10, 10, 10, 20);

        layout.addView(info);

        layout.addView(button("✅ Accept Job", v -> {

            Toast.makeText(
                    this,
                    "Job accepted successfully (demo)",
                    Toast.LENGTH_SHORT
            ).show();

            showMyJobs();
        }));

        layout.addView(button("← Back to Jobs", v -> showJobs()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // MY JOBS
    // ----------------------------------------------------

    private void showMyJobs() {

        LinearLayout layout = baseLayout();

        layout.addView(title("📋 My Jobs"));

        TextView info = new TextView(this);

        info.setText(
                "Active Jobs\n\n" +
                "📦 Product Listing\n" +
                "Status: In Progress\n" +
                "Payment: $50\n\n" +
                "No other active jobs."
        );

        info.setTextColor(WHITE);
        info.setTextSize(17);
        info.setPadding(10, 15, 10, 20);

        layout.addView(info);

        layout.addView(button("🔎 Find More Jobs", v -> showJobs()));

        layout.addView(button("← Back", v -> showDashboard()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // EARNINGS
    // ----------------------------------------------------

    private void showEarnings() {

        LinearLayout layout = baseLayout();

        layout.addView(title("💰 Earnings"));

        TextView balance = new TextView(this);

        balance.setText(
                "Available Balance\n\n$50.00\n\n" +
                "Total Earned: $50.00\n" +
                "Pending: $0.00"
        );

        balance.setTextColor(WHITE);
        balance.setTextSize(20);
        balance.setGravity(Gravity.CENTER);
        balance.setPadding(0, 10, 0, 25);

        layout.addView(balance);

        layout.addView(button("💵 Withdraw", v -> {

            Toast.makeText(
                    this,
                    "Withdrawal system will connect to backend/payment provider later",
                    Toast.LENGTH_LONG
            ).show();

        }));

        layout.addView(button("📜 Withdrawal History", v -> {

            Toast.makeText(
                    this,
                    "No withdrawal history yet",
                    Toast.LENGTH_SHORT
            ).show();

        }));

        layout.addView(button("← Back", v -> showDashboard()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // PROFILE
    // ----------------------------------------------------

    private void showProfile() {

        LinearLayout layout = baseLayout();

        layout.addView(title("👤 My Profile"));

        layout.addView(subtitle(
                "Worker Account"
        ));

        TextView profile = new TextView(this);

        profile.setText(
                "Name: Worker\n\n" +
                "Email: worker@example.com\n\n" +
                "Account Status: Active\n\n" +
                "KYC Status: Not Submitted"
        );

        profile.setTextColor(WHITE);
        profile.setTextSize(17);
        profile.setPadding(10, 10, 10, 20);

        layout.addView(profile);

        layout.addView(button("🪪 Complete KYC", v -> showKyc()));

        layout.addView(button("🌐 Language", v -> showLanguage()));

        layout.addView(button("⚙️ Settings", v -> showSettings()));

        layout.addView(button("← Back", v -> showDashboard()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // KYC
    // ----------------------------------------------------

    private void showKyc() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🪪 Worker KYC"));

        layout.addView(subtitle(
                "Complete your identity verification"
        ));

        EditText name = input("Full Name");

        EditText dob = input("Date of Birth");

        EditText country = input("Country");

        EditText idType = input(
                "ID Type (Passport / Aadhaar / National ID)"
        );

        EditText idNumber = input("ID Number");

        layout.addView(name);
        layout.addView(dob);
        layout.addView(country);
        layout.addView(idType);
        layout.addView(idNumber);

        layout.addView(button("📤 Submit KYC", v -> {

            Toast.makeText(
                    this,
                    "KYC submitted (demo). Real verification will be connected later.",
                    Toast.LENGTH_LONG
            ).show();

        }));

        layout.addView(button("← Back to Profile", v -> showProfile()));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    private void showNotifications() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🔔 Notifications"));

        TextView n1 = new TextView(this);

        n1.setText(
                "📦 New Job Available\n" +
                "A Product Listing job is available.\n\n" +
                "💰 Payment Update\n" +
                "Your current worker balance is $50.00.\n\n" +
                "🤖 AI Manager\n" +
                "New jobs will be matched automatically."
        );

        n1.setTextColor(WHITE);
        n1.setTextSize(16);
        n1.setPadding(10, 10, 10, 25);

        layout.addView(n1);

        layout.addView(button(
                "✓ Mark All as Read",
                v -> Toast.makeText(
                        this,
                        "Notifications marked as read",
                        Toast.LENGTH_SHORT
                ).show()
        ));

        layout.addView(button(
                "← Back",
                v -> showDashboard()
        ));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // LANGUAGE
    // ----------------------------------------------------

    private void showLanguage() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🌐 Language"));

        layout.addView(subtitle(
                "Choose your preferred language"
        ));

        layout.addView(button(
                "🇬🇧 English",
                v -> languageSelected("English")
        ));

        layout.addView(button(
                "🇮🇳 Hindi",
                v -> languageSelected("Hindi")
        ));

        layout.addView(button(
                "🇮🇳 Bengali",
                v -> languageSelected("Bengali")
        ));

        layout.addView(button(
                "🇮🇳 Assamese",
                v -> languageSelected("Assamese")
        ));

        layout.addView(button(
                "← Back",
                v -> showProfile()
        ));

        setContentView(screenContainer(layout));
    }

    private void languageSelected(String language) {

        Toast.makeText(
                this,
                language + " selected (demo)",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ----------------------------------------------------
    // SETTINGS
    // ----------------------------------------------------

    private void showSettings() {

        LinearLayout layout = baseLayout();

        layout.addView(title("⚙️ Settings"));

        layout.addView(button(
                "🔔 Notification Settings",
                v -> Toast.makeText(
                        this,
                        "Notification settings coming next",
                        Toast.LENGTH_SHORT
                ).show()
        ));

        layout.addView(button(
                "🌐 Language",
                v -> showLanguage()
        ));

        layout.addView(button(
                "🆘 Help & Support",
                v -> showHelp()
        ));

        layout.addView(button(
                "ℹ️ About Viyzo",
                v -> showAbout()
        ));

        layout.addView(button(
                "🚪 Logout",
                v -> showHome()
        ));

        layout.addView(button(
                "← Back",
                v -> showDashboard()
        ));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // HELP & SUPPORT
    // ----------------------------------------------------

    private void showHelp() {

        LinearLayout layout = baseLayout();

        layout.addView(title("🆘 Help & Support"));

        TextView help = new TextView(this);

        help.setText(
                "How can we help?\n\n" +
                "1. How do I accept a job?\n" +
                "Open Available Jobs and select a job.\n\n" +
                "2. How do I receive payment?\n" +
                "Complete your work and follow the payout process.\n\n" +
                "3. How does KYC work?\n" +
                "Open Profile → Complete KYC.\n\n" +
                "4. Need more help?\n" +
                "The real support system will be connected with the backend later."
        );

        help.setTextColor(WHITE);
        help.setTextSize(16);
        help.setPadding(10, 10, 10, 25);

        layout.addView(help);

        layout.addView(button(
                "💬 Contact Support",
                v -> Toast.makeText(
                        this,
                        "Support system coming later",
                        Toast.LENGTH_SHORT
                ).show()
        ));

        layout.addView(button(
                "← Back",
                v -> showSettings()
        ));

        setContentView(screenContainer(layout));
    }

    // ----------------------------------------------------
    // ABOUT
    // ----------------------------------------------------

    private void showAbout() {

        LinearLayout layout = baseLayout();

        layout.addView(title("ℹ️ About Viyzo"));

        TextView about = new TextView(this);

        about.setText(
                "🤖 Viyzo Worker\n\n" +
                "Version: 1.0\n\n" +
                "AI Managed Global Work Platform\n\n" +
                "Viyzo is designed to connect companies " +
                "with workers and manage digital work through " +
                "an AI-powered job management system.\n\n" +
                "Current version is a prototype. " +
                "Backend, real KYC, payment processing, " +
                "and production security will be connected later."
        );

        about.setTextColor(WHITE);
        about.setTextSize(16);
        about.setPadding(10, 10, 10, 25);

        layout.addView(about);

        layout.addView(button(
                "← Back",
                v -> showSettings()
        ));

        setContentView(screenContainer(layout));
    }
}
