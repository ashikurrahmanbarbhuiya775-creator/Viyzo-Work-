package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(16, 16, 20);
    private final int CARD = Color.rgb(28, 28, 36);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(180, 180, 190);
    private final int GREEN = Color.rgb(60, 200, 120);

    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    // =========================
    // COMMON UI
    // =========================

    private ScrollView createScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 40, 32, 40);
        root.setBackgroundColor(BG);

        scroll.addView(root);
        return scroll;
    }

    private void setScreen() {
        setContentView(createScreen());
    }

    private TextView title(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(WHITE);
        t.setTextSize(27);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 18);
        root.addView(t,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        return t;
    }

    private TextView subtitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(GRAY);
        t.setTextSize(15);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 24);

        root.addView(t,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        return t;
    }

    private TextView section(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(WHITE);
        t.setTextSize(18);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, 20, 0, 10);

        root.addView(t,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        return t;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0, 8, 0, 8);
        b.setLayoutParams(p);

        root.addView(b);
        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(GRAY);
        e.setTextColor(WHITE);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0, 6, 0, 6);
        e.setLayoutParams(p);

        root.addView(e);
        return e;
    }

    private void addSpace(int height) {
        TextView space = new TextView(this);
        root.addView(space,
                new LinearLayout.LayoutParams(
                        1, height));
    }

    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // =========================
    // HOME
    // =========================

    private void showHome() {
        setScreen();

        title("🤖 Viyzo Worker");
        subtitle("AI Managed Global Work Platform");

        Button login = button("🔐 Login");
        login.setOnClickListener(v -> showLogin());

        Button create = button("📝 Create Account");
        create.setOnClickListener(v -> showCreateAccount());

        Button jobs = button("🔎 Available Jobs");
        jobs.setOnClickListener(v -> showJobs());

        Button about = button("ℹ️ About Viyzo");
        about.setOnClickListener(v -> showAbout());
    }

    // =========================
    // LOGIN
    // =========================

    private void showLogin() {
        setScreen();

        title("🔐 Worker Login");
        subtitle("Login to your Viyzo Worker account");

        input("Phone or Email");

        EditText password = input("Viyzo Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button login = button("Login");

        login.setOnClickListener(v -> {
            message("Login demo successful");
            showDashboard();
        });

        Button back = button("← Back");
        back.setOnClickListener(v -> showHome());
    }

    // =========================
    // CREATE ACCOUNT
    // =========================

    private void showCreateAccount() {
        setScreen();

        title("📝 Create Worker Account");
        subtitle("Create your Viyzo Worker account");

        input("Full Name");
        input("Date of Birth");
        input("Country");

        EditText phone = input("Mobile Number");
        phone.setInputType(InputType.TYPE_CLASS_PHONE);

        input("Email");

        EditText password = input("Create Viyzo Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        CheckBox consent = new CheckBox(this);
        consent.setText(
                "I agree to Viyzo's verification and privacy notice.");
        consent.setTextColor(WHITE);
        consent.setTextSize(14);
        root.addView(consent);

        Button create = button("Create Account");

        create.setOnClickListener(v -> {
            if (!consent.isChecked()) {
                message("Please accept the consent");
                return;
            }

            message("Account created");
            showDashboard();
        });

        Button back = button("← Back");
        back.setOnClickListener(v -> showHome());
    }

    // =========================
    // DASHBOARD
    // =========================

    private void showDashboard() {
        setScreen();

        title("🏠 Worker Dashboard");
        subtitle("Welcome to Viyzo Worker");

        Button jobs = button("🔎 Available Jobs");
        jobs.setOnClickListener(v -> showJobs());

        Button myJobs = button("📋 My Jobs");
        myJobs.setOnClickListener(v -> showMyJobs());

        Button earnings = button("💰 Earnings & Withdrawal");
        earnings.setOnClickListener(v -> showEarnings());

        Button profile = button("👤 Profile & Verification");
        profile.setOnClickListener(v -> showProfile());

        Button notifications = button("🔔 Notifications");
        notifications.setOnClickListener(v -> showNotifications());

        Button language = button("🌐 Language");
        language.setOnClickListener(v -> showLanguage());

        Button settings = button("⚙️ Settings");
        settings.setOnClickListener(v -> showSettings());

        Button help = button("❓ Help & Support");
        help.setOnClickListener(v -> showHelp());

        Button logout = button("🚪 Logout");
        logout.setOnClickListener(v -> showHome());
    }

    // =========================
    // JOBS
    // =========================

    private void showJobs() {
        setScreen();

        title("🔎 Available Jobs");
        subtitle("Jobs selected by Viyzo AI Manager");

        section("Product Listing");
        subtitle("1000 products • Remote • Global");

        Button details = button("View Job");
        details.setOnClickListener(v -> showJobDetails());

        section("Data Entry");
        subtitle("500 records • Remote");

        Button data = button("View Job");
        data.setOnClickListener(v ->
                message("Data Entry job details coming next"));

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    private void showJobDetails() {
        setScreen();

        title("📦 Product Listing Job");

        section("Company Budget");
        subtitle("$500.00");

        section("Worker Pool");
        subtitle("$400.00");

        section("Viyzo Fee");
        subtitle("$100.00");

        section("AI Recommended Workers");
        subtitle("8 Workers");

        section("Workload");
        subtitle("125 products per worker");

        Button apply = button("✅ Apply for Job");
        apply.setOnClickListener(v ->
                message("Job application submitted"));

        Button back = button("← Back to Jobs");
        back.setOnClickListener(v -> showJobs());
    }

    // =========================
    // MY JOBS
    // =========================

    private void showMyJobs() {
        setScreen();

        title("📋 My Jobs");

        section("Current Job");
        subtitle("Product Listing • In Progress");

        Button open = button("Open Job");
        open.setOnClickListener(v ->
                message("Job workspace coming next"));

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // EARNINGS
    // =========================

    private void showEarnings() {
        setScreen();

        title("💰 Earnings");

        section("Available Balance");
        subtitle("$0.00");

        section("Total Earned");
        subtitle("$0.00");

        section("Pending");
        subtitle("$0.00");

        Button withdraw = button("💸 Withdraw");
        withdraw.setOnClickListener(v ->
                message("Withdrawal system will be connected later"));

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // PROFILE
    // =========================

    private void showProfile() {
        setScreen();

        title("👤 Worker Profile");
        subtitle("Manage your Viyzo Worker account");

        section("Verification Status");
        subtitle(
                "🟡 Basic Worker\n\n" +
                "Complete Worker Verification to unlock more jobs.");

        Button verification = button("🪪 Worker Verification");
        verification.setOnClickListener(v -> showWorkerVerification());

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // NEW WORKER VERIFICATION
    // =========================

    private void showWorkerVerification() {
        setScreen();

        title("🪪 Worker Verification");

        subtitle(
                "Viyzo uses different verification options depending " +
                "on your country and the type of work/payment.");

        section("Step 1 — Country");

        Spinner countrySpinner = new Spinner(this);

        String[] countries = {
                "Select Country",
                "India",
                "Bangladesh",
                "Pakistan",
                "United States",
                "United Kingdom",
                "United Arab Emirates",
                "Other Country"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        countries);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        countrySpinner.setAdapter(adapter);

        LinearLayout.LayoutParams spinnerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        spinnerParams.setMargins(0, 8, 0, 15);

        root.addView(countrySpinner, spinnerParams);

        section("Step 2 — Basic Verification");

        input("Full Name");
        input("Date of Birth");

        Button phone = button("📱 Verify Phone with OTP");
        phone.setOnClickListener(v ->
                message("OTP verification will be connected with backend"));

        Button email = button("✉️ Verify Email");
        email.setOnClickListener(v ->
                message("Email verification will be connected with backend"));

        Button selfie = button("🤳 Start Live Selfie Verification");
        selfie.setOnClickListener(v ->
                message("Live selfie verification will be connected with verification provider"));

        section("Step 3 — Identity Document");

        subtitle(
                "If you have an accepted identity document, " +
                "choose the document shown for your country.\n\n" +
                "If you do not have a document, you can continue with Basic Worker Verification.");

        Button document = button("🪪 I Have an Identity Document");
        document.setOnClickListener(v -> showDocumentOptions());

        Button noDocument = button("🙋 I Don't Have a Document");
        noDocument.setOnClickListener(v -> showNoDocument());

        section("Consent");

        CheckBox consent = new CheckBox(this);
        consent.setText(
                "I consent to Viyzo processing the information " +
                "needed for worker verification.");
        consent.setTextColor(WHITE);
        consent.setTextSize(14);

        root.addView(consent);

        Button submit = button("✅ Submit Worker Verification");

        submit.setOnClickListener(v -> {
            if (!consent.isChecked()) {
                message("Please accept the verification consent");
                return;
            }

            message("Worker Verification submitted");
            showProfile();
        });

        Button back = button("← Back to Profile");
        back.setOnClickListener(v -> showProfile());
    }

    // =========================
    // DOCUMENT OPTIONS
    // =========================

    private void showDocumentOptions() {
        setScreen();

        title("🪪 Identity Document");

        subtitle(
                "Viyzo will show the available verification " +
                "documents according to your selected country.");

        section("Document Options");

        Button primary = button("📄 Primary Identity Document");
        primary.setOnClickListener(v ->
                message("Document upload will be connected to verification provider"));

        Button alternative1 = button("📄 Alternative Document 1");
        alternative1.setOnClickListener(v ->
                message("Alternative document selected"));

        Button alternative2 = button("📄 Alternative Document 2");
        alternative2.setOnClickListener(v ->
                message("Alternative document selected"));

        section("Important");

        subtitle(
                "The final accepted document list will be controlled " +
                "by Viyzo's country-specific verification rules and " +
                "authorized verification provider.");

        Button back = button("← Back to Verification");
        back.setOnClickListener(v -> showWorkerVerification());
    }

    // =========================
    // NO DOCUMENT PATH
    // =========================

    private void showNoDocument() {
        setScreen();

        title("🙋 Basic Worker Verification");

        subtitle(
                "You can continue without a government identity document " +
                "for the basic account level.");

        section("What You Can Complete");

        subtitle(
                "✓ Name\n" +
                "✓ Date of Birth\n" +
                "✓ Phone verification\n" +
                "✓ Email verification\n" +
                "✓ Live selfie verification\n" +
                "✓ Verification consent");

        section("Your Worker Status");

        subtitle(
                "🟡 Basic Worker\n\n" +
                "You may use the app and access jobs that Viyzo " +
                "allows for Basic Workers.");

        section("Higher Verification");

        subtitle(
                "Some higher-risk, higher-value jobs or withdrawals " +
                "may require stronger identity or payment verification.");

        Button continueButton =
                button("✅ Continue as Basic Worker");

        continueButton.setOnClickListener(v -> {
            message("Basic Worker verification selected");
            showProfile();
        });

        Button back = button("← Back to Verification");
        back.setOnClickListener(v -> showWorkerVerification());
    }

    // =========================
    // NOTIFICATIONS
    // =========================

    private void showNotifications() {
        setScreen();

        title("🔔 Notifications");

        section("Job Notification");
        subtitle("New Product Listing job available.");

        section("Verification");
        subtitle("Complete Worker Verification for more access.");

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // LANGUAGE
    // =========================

    private void showLanguage() {
        setScreen();

        title("🌐 Language");

        Button english = button("English");
        english.setOnClickListener(v ->
                message("English selected"));

        Button hindi = button("हिन्दी");
        hindi.setOnClickListener(v ->
                message("Hindi selected"));

        Button bengali = button("বাংলা");
        bengali.setOnClickListener(v ->
                message("Bengali selected"));

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // SETTINGS
    // =========================

    private void showSettings() {
        setScreen();

        title("⚙️ Settings");

        Button notifications = button("🔔 Notification Settings");
        notifications.setOnClickListener(v ->
                message("Notification settings coming next"));

        Button security = button("🔐 Account Security");
        security.setOnClickListener(v ->
                message("Security settings coming next"));

        Button privacy = button("🛡️ Privacy");
        privacy.setOnClickListener(v ->
                message("Privacy settings coming next"));

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // HELP
    // =========================

    private void showHelp() {
        setScreen();

        title("❓ Help & Support");

        section("Need Help?");
        subtitle(
                "Contact Viyzo Support for account, job, " +
                "verification and payment questions.");

        Button support = button("💬 Contact Support");
        support.setOnClickListener(v ->
                message("Support system coming next"));

        Button back = button("← Dashboard");
        back.setOnClickListener(v -> showDashboard());
    }

    // =========================
    // ABOUT
    // =========================

    private void showAbout() {
        setScreen();

        title("ℹ️ About Viyzo");

        subtitle(
                "Viyzo Worker\n\n" +
                "AI Managed Global Work Platform\n\n" +
                "Viyzo connects companies with workers " +
                "for digital work and manages job distribution.");

        Button back = button("← Home");
        back.setOnClickListener(v -> showHome());
    }
}
