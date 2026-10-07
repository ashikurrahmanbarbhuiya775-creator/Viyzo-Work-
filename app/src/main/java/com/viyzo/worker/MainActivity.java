package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // =========================
    // VIYZO COLORS
    // =========================

    private final int BG = Color.rgb(10, 11, 18);
    private final int CARD = Color.rgb(24, 25, 35);
    private final int CARD2 = Color.rgb(31, 32, 45);

    private final int PRIMARY = Color.rgb(108, 92, 231);
    private final int PRIMARY_DARK = Color.rgb(78, 65, 180);

    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(175, 178, 192);
    private final int GREEN = Color.rgb(55, 205, 125);
    private final int RED = Color.rgb(235, 80, 95);
    private final int GOLD = Color.rgb(245, 190, 70);

    private LinearLayout root;

    // =========================
    // ACTIVITY
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        showHome();
    }

    // =========================
    // MAIN SCREEN CREATOR
    // =========================

    private void setScreen(boolean bottomNavigation) {

        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 25, 22, bottomNavigation ? 95 : 30);
        root.setBackgroundColor(BG);

        scroll.addView(root);

        frame.addView(
                scroll,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        if (bottomNavigation) {
            addBottomNavigation(frame);
        }

        setContentView(frame);
    }

    // =========================
    // LOGO
    // =========================

    private LinearLayout logoHeader() {

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(4, 5, 4, 20);

        TextView logo = new TextView(this);
        logo.setText("V");
        logo.setTextColor(WHITE);
        logo.setTextSize(27);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setShape(GradientDrawable.OVAL);
        logoBg.setColor(PRIMARY);

        logo.setBackground(logoBg);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(58, 58);

        logoParams.setMargins(0, 0, 14, 0);

        header.addView(logo, logoParams);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText("VIYZO");
        name.setTextColor(WHITE);
        name.setTextSize(23);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView company = new TextView(this);
        company.setText("Global Work Network");
        company.setTextColor(GRAY);
        company.setTextSize(13);

        textBox.addView(name);
        textBox.addView(company);

        header.addView(
                textBox,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        root.addView(header);

        return header;
    }

    // =========================
    // TITLE
    // =========================

    private TextView title(String text) {

        TextView t = new TextView(this);

        t.setText(text);
        t.setTextColor(WHITE);
        t.setTextSize(26);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.LEFT);
        t.setPadding(0, 5, 0, 10);

        root.addView(
                t,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return t;
    }

    // =========================
    // SUBTITLE
    // =========================

    private TextView subtitle(String text) {

        TextView t = new TextView(this);

        t.setText(text);
        t.setTextColor(GRAY);
        t.setTextSize(14);
        t.setLineSpacing(3, 1.0f);
        t.setPadding(0, 0, 0, 15);

        root.addView(
                t,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return t;
    }

    // =========================
    // SECTION TITLE
    // =========================

    private TextView section(String text) {

        TextView t = new TextView(this);

        t.setText(text);
        t.setTextColor(WHITE);
        t.setTextSize(18);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        t.setPadding(3, 18, 0, 10);

        root.addView(t);

        return t;
    }

    // =========================
    // PROFESSIONAL BUTTON
    // =========================

    private Button appButton(String text) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(20, 5, 20, 5);

        GradientDrawable bg = new GradientDrawable();

        bg.setColor(CARD2);
        bg.setCornerRadius(22);

        bg.setStroke(1, Color.rgb(55, 56, 72));

        b.setBackground(bg);

        b.setElevation(4);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        58
                );

        p.setMargins(0, 7, 0, 7);

        root.addView(b, p);

        return b;
    }

    // =========================
    // PRIMARY BUTTON
    // =========================

    private Button primaryButton(String text) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{PRIMARY, PRIMARY_DARK}
        );

        bg.setCornerRadius(22);

        b.setBackground(bg);
        b.setElevation(6);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        58
                );

        p.setMargins(0, 8, 0, 8);

        root.addView(b, p);

        return b;
    }

    // =========================
    // INPUT
    // =========================

    private EditText input(String hint) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(Color.rgb(125, 128, 145));
        e.setTextColor(WHITE);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(18, 0, 18, 0);

        GradientDrawable bg = new GradientDrawable();

        bg.setColor(CARD);
        bg.setCornerRadius(18);
        bg.setStroke(1, Color.rgb(55, 56, 72));

        e.setBackground(bg);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                );

        p.setMargins(0, 6, 0, 6);

        root.addView(e, p);

        return e;
    }

    // =========================
    // INFO CARD
    // =========================

    private void infoCard(String heading, String text) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 17, 20, 17);

        GradientDrawable bg = new GradientDrawable();

        bg.setColor(CARD);
        bg.setCornerRadius(20);
        bg.setStroke(1, Color.rgb(50, 51, 67));

        card.setBackground(bg);
        card.setElevation(3);

        TextView h = new TextView(this);

        h.setText(heading);
        h.setTextColor(WHITE);
        h.setTextSize(16);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView d = new TextView(this);

        d.setText(text);
        d.setTextColor(GRAY);
        d.setTextSize(14);
        d.setPadding(0, 7, 0, 0);

        card.addView(h);
        card.addView(d);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, 7, 0, 7);

        root.addView(card, p);
    }

    // =========================
    // BOTTOM NAVIGATION
    // =========================

    private void addBottomNavigation(FrameLayout frame) {

        LinearLayout nav = new LinearLayout(this);

        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(8, 6, 8, 6);

        GradientDrawable bg = new GradientDrawable();

        bg.setColor(Color.rgb(20, 21, 30));
        bg.setCornerRadius(25);

        bg.setStroke(1, Color.rgb(55, 56, 72));

        nav.setBackground(bg);

        FrameLayout.LayoutParams navParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        72,
                        Gravity.BOTTOM
                );

        navParams.setMargins(10, 0, 10, 10);

        frame.addView(nav, navParams);

        Button home = navButton("⌂\nHome");
        Button jobs = navButton("▣\nJobs");
        Button money = navButton("$\nEarnings");
        Button profile = navButton("●\nProfile");

        nav.addView(home);
        nav.addView(jobs);
        nav.addView(money);
        nav.addView(profile);

        home.setOnClickListener(v -> showDashboard());
        jobs.setOnClickListener(v -> showJobs());
        money.setOnClickListener(v -> showEarnings());
        profile.setOnClickListener(v -> showProfile());
    }

    private Button navButton(String text) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextColor(GRAY);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);

        b.setBackgroundColor(Color.TRANSPARENT);

        b.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        return b;
    }

    // =========================
    // TOAST
    // =========================

    private void message(String text) {

        Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // HOME
    // =========================================================

    private void showHome() {

        setScreen(false);

        logoHeader();

        title("Work smarter with Viyzo");

        subtitle(
                "AI-managed global work platform for workers " +
                "and companies."
        );

        infoCard(
                "🤖 AI Master Manager",
                "Viyzo intelligently distributes jobs " +
                "to suitable workers."
        );

        primaryButton("🔐 Login").setOnClickListener(
                v -> showLogin()
        );

        primaryButton("📝 Create Worker Account").setOnClickListener(
                v -> showCreateAccount()
        );

        appButton("🔎 Explore Available Jobs").setOnClickListener(
                v -> showJobs()
        );

        appButton("ℹ️ About Viyzo").setOnClickListener(
                v -> showAbout()
        );

        subtitle(
                "Secure account • Global work • AI job management"
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void showLogin() {

        setScreen(false);

        logoHeader();

        title("Welcome back");

        subtitle("Login to your Viyzo Worker account.");

        input("Phone or Email");

        EditText password = input("Viyzo Password");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        primaryButton("🔐 Login").setOnClickListener(
                v -> {
                    message("Login successful");
                    showDashboard();
                }
        );

        appButton("Forgot Password?").setOnClickListener(
                v -> message("Password recovery will be connected later")
        );

        appButton("← Back").setOnClickListener(
                v -> showHome()
        );
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    private void showCreateAccount() {

        setScreen(false);

        logoHeader();

        title("Create your account");

        subtitle(
                "Start as a Viyzo Worker. You can complete " +
                "stronger verification later when required."
        );

        input("Full Name");
        input("Date of Birth");
        input("Country");
        input("Mobile Number");
        input("Email");

        EditText password = input("Create Viyzo Password");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        CheckBox consent = new CheckBox(this);

        consent.setText(
                "I agree to Viyzo's verification and privacy notice."
        );

        consent.setTextColor(WHITE);
        consent.setTextSize(13);

        root.addView(consent);

        primaryButton("✅ Create Account").setOnClickListener(
                v -> {

                    if (!consent.isChecked()) {
                        message("Please accept the consent");
                        return;
                    }

                    message("Account created");

                    showDashboard();
                }
        );

        appButton("← Back").setOnClickListener(
                v -> showHome()
        );
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        setScreen(true);

        logoHeader();

        title("Good day, Worker 👋");

        subtitle(
                "Your Viyzo work dashboard"
        );

        infoCard(
                "🟡 Worker Status",
                "Basic Worker • Complete verification for more access"
        );

        section("Quick Actions");

        appButton("🔎 Available Jobs").setOnClickListener(
                v -> showJobs()
        );

        appButton("📋 My Jobs").setOnClickListener(
                v -> showMyJobs()
        );

        appButton("💰 Earnings & Withdrawal").setOnClickListener(
                v -> showEarnings()
        );

        appButton("👤 Profile & Verification").setOnClickListener(
                v -> showProfile()
        );

        section("Viyzo Services");

        appButton("🔔 Notifications").setOnClickListener(
                v -> showNotifications()
        );

        appButton("🌐 Language").setOnClickListener(
                v -> showLanguage()
        );

        appButton("⚙️ Settings").setOnClickListener(
                v -> showSettings()
        );

        appButton("❓ Help & Support").setOnClickListener(
                v -> showHelp()
        );

        appButton("🚪 Logout").setOnClickListener(
                v -> showHome()
        );
    }

    // =========================================================
    // JOBS
    // =========================================================

    private void showJobs() {

        setScreen(true);

        title("Available Jobs");

        subtitle(
                "AI Master Manager has selected these jobs."
        );

        infoCard(
                "📦 Product Listing",
                "1,000 products • Remote • Global\n" +
                "Company Budget: $500\n" +
                "Worker Pool: $400"
        );

        primaryButton("View Product Listing Job")
                .setOnClickListener(
                        v -> showJobDetails()
                );

        infoCard(
                "⌨️ Data Entry",
                "500 records • Remote\n" +
                "Flexible workload"
        );

        appButton("View Data Entry Job")
                .setOnClickListener(
                        v -> message("Data Entry details coming next")
                );

        infoCard(
                "📝 Content Review",
                "Remote • AI-assisted workflow\n" +
                "Quality-based payment"
        );

        appButton("View Content Review")
                .setOnClickListener(
                        v -> message("Content Review details coming next")
                );
    }

    // =========================================================
    // JOB DETAILS
    // =========================================================

    private void showJobDetails() {

        setScreen(true);

        title("Product Listing Job");

        infoCard(
                "💵 Company Budget",
                "$500.00"
        );

        infoCard(
                "👷 Worker Pool",
                "$400.00"
        );

        infoCard(
                "🏢 Viyzo Fee",
                "$100.00"
        );

        infoCard(
                "🤖 AI Recommended Workers",
                "8 Workers"
        );

        infoCard(
                "📦 Workload",
                "125 products per worker"
        );

        infoCard(
                "⏱️ Deadline",
                "3 Days"
        );

        primaryButton("✅ Apply for Job")
                .setOnClickListener(
                        v -> message("Job application submitted")
                );

        appButton("← Back to Jobs")
                .setOnClickListener(
                        v -> showJobs()
                );
    }

    // =========================================================
    // MY JOBS
    // =========================================================

    private void showMyJobs() {

        setScreen(true);

        title("My Jobs");

        infoCard(
                "📦 Product Listing",
                "Status: In Progress\n" +
                "Progress: 0%\n" +
                "125 products assigned"
        );

        primaryButton("Open Work")
                .setOnClickListener(
                        v -> message("Work workspace coming next")
                );

        infoCard(
                "Completed Jobs",
                "No completed jobs yet."
        );
    }

    // =========================================================
    // EARNINGS
    // =========================================================

    private void showEarnings() {

        setScreen(true);

        title("Earnings");

        infoCard(
                "💰 Available Balance",
                "$0.00"
        );

        infoCard(
                "📈 Total Earned",
                "$0.00"
        );

        infoCard(
                "⏳ Pending",
                "$0.00"
        );

        primaryButton("💸 Withdraw")
                .setOnClickListener(
                        v -> message(
                                "Payment verification will be connected later"
                        )
                );

        appButton("Payment Methods")
                .setOnClickListener(
                        v -> message(
                                "Payment methods will be connected later"
                        )
                );

        appButton("Transaction History")
                .setOnClickListener(
                        v -> message(
                                "Transaction history coming next"
                        )
                );
    }

    // =========================================================
    // PROFILE
    // =========================================================

    private void showProfile() {

        setScreen(true);

        title("My Profile");

        infoCard(
                "👤 Worker Account",
                "Basic Worker"
        );

        infoCard(
                "🪪 Verification",
                "🟡 Basic Verification\n" +
                "Document verification not completed"
        );

        primaryButton("🪪 Worker Verification")
                .setOnClickListener(
                        v -> showWorkerVerification()
                );

        appButton("Edit Profile")
                .setOnClickListener(
                        v -> message("Profile editing coming next")
                );

        appButton("Account Security")
                .setOnClickListener(
                        v -> message("Security settings coming next")
                );
    }

    // =========================================================
    // WORKER VERIFICATION
    // =========================================================

    private void showWorkerVerification() {

        setScreen(false);

        logoHeader();

        title("Worker Verification");

        subtitle(
                "Choose the verification path available for you."
        );

        infoCard(
                "🟢 Basic Worker",
                "Phone + Email + Name + DOB + " +
                "Live Selfie + Consent"
        );

        infoCard(
                "🔵 Identity Verified",
                "Additional identity verification " +
                "may be required for certain jobs."
        );

        section("1. Country");

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
                        countries
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        countrySpinner.setAdapter(adapter);

        root.addView(
                countrySpinner,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                )
        );

        section("2. Basic Verification");

        input("Full Name");
        input("Date of Birth");

        appButton("📱 Verify Phone with OTP")
                .setOnClickListener(
                        v -> message(
                                "OTP service will be connected later"
                        )
                );

        appButton("✉️ Verify Email")
                .setOnClickListener(
                        v -> message(
                                "Email verification will be connected later"
                        )
                );

        appButton("🤳 Live Selfie Verification")
                .setOnClickListener(
                        v -> message(
                                "Live selfie provider will be connected later"
                        )
                );

        section("3. Identity Document");

        subtitle(
                "If you have an accepted document, choose document verification. " +
                "If you do not have one, continue as Basic Worker."
        );

        primaryButton("🪪 I Have an Identity Document")
                .setOnClickListener(
                        v -> showDocumentOptions()
                );

        appButton("🙋 I Don't Have a Document")
                .setOnClickListener(
                        v -> showNoDocument()
                );

        section("4. Consent");

        CheckBox consent = new CheckBox(this);

        consent.setText(
                "I consent to Viyzo processing the information " +
                "needed for worker verification."
        );

        consent.setTextColor(WHITE);
        consent.setTextSize(13);

        root.addView(consent);

        primaryButton("✅ Submit Verification")
                .setOnClickListener(
                        v -> {

                            if (!consent.isChecked()) {
                                message(
                                        "Please accept verification consent"
                                );
                                return;
                            }

                            message(
                                    "Worker Verification submitted"
                            );

                            showProfile();
                        }
                );

        appButton("← Back to Profile")
                .setOnClickListener(
                        v -> showProfile()
                );
    }

    // =========================================================
    // DOCUMENT OPTIONS
    // =========================================================

    private void showDocumentOptions() {

        setScreen(false);

        logoHeader();

        title("Identity Document");

        subtitle(
                "The final accepted documents will be controlled " +
                "by Viyzo's country-specific verification rules."
        );

        infoCard(
                "📄 Primary Document",
                "Country-specific identity document"
        );

        primaryButton("Upload Primary Document")
                .setOnClickListener(
                        v -> message(
                                "Document upload provider will be connected later"
                        )
                );

        infoCard(
                "📄 Alternative Document",
                "Another accepted identity document"
        );

        appButton("Upload Alternative Document")
                .setOnClickListener(
                        v -> message(
                                "Alternative document upload coming later"
                        )
                );

        appButton("← Back")
                .setOnClickListener(
                        v -> showWorkerVerification()
                );
    }

    // =========================================================
    // NO DOCUMENT
    // =========================================================

    private void showNoDocument() {

        setScreen(false);

        logoHeader();

        title("Basic Worker Verification");

        subtitle(
                "You can continue without a government identity " +
                "document at the basic account level."
        );

        infoCard(
                "✓ Basic Verification",
                "Name\n" +
                "Date of Birth\n" +
                "Phone verification\n" +
                "Email verification\n" +
                "Live selfie verification\n" +
                "Consent"
        );

        infoCard(
                "🟡 Worker Status",
                "Basic Worker"
        );

        infoCard(
                "🔐 Higher Verification",
                "Some higher-value, higher-risk jobs or " +
                "withdrawals may require stronger verification."
        );

        primaryButton("✅ Continue as Basic Worker")
                .setOnClickListener(
                        v -> {

                            message(
                                    "Basic Worker verification selected"
                            );

                            showProfile();
                        }
                );

        appButton("← Back")
                .setOnClickListener(
                        v -> showWorkerVerification()
                );
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    private void showNotifications() {

        setScreen(true);

        title("Notifications");

        infoCard(
                "🔎 New Job",
                "A new Product Listing job is available."
        );

        infoCard(
                "🪪 Verification",
                "Complete Worker Verification for more access."
        );

        infoCard(
                "💰 Earnings",
                "Your earnings dashboard is ready."
        );
    }

    // =========================================================
    // LANGUAGE
    // =========================================================

    private void showLanguage() {

        setScreen(true);

        title("Language");

        primaryButton("English")
                .setOnClickListener(
                        v -> message("English selected")
                );

        appButton("हिन्दी")
                .setOnClickListener(
                        v -> message("Hindi selected")
                );

        appButton("বাংলা")
                .setOnClickListener(
                        v -> message("Bengali selected")
                );

        appButton("اردو")
                .setOnClickListener(
                        v -> message("Urdu selected")
                );
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void showSettings() {

        setScreen(true);

        title("Settings");

        appButton("🔔 Notification Settings")
                .setOnClickListener(
                        v -> message(
                                "Notification settings coming next"
                        )
                );

        appButton("🔐 Account Security")
                .setOnClickListener(
                        v -> message(
                                "Security settings coming next"
                        )
                );

        appButton("🛡️ Privacy")
                .setOnClickListener(
                        v -> message(
                                "Privacy settings coming next"
                        )
                );

        appButton("🌐 Language")
                .setOnClickListener(
                        v -> showLanguage()
                );

        appButton("💳 Payment Settings")
                .setOnClickListener(
                        v -> message(
                                "Payment settings coming next"
                        )
                );

        appButton("🚪 Logout")
                .setOnClickListener(
                        v -> showHome()
                );
    }

    // =========================================================
    // HELP
    // =========================================================

    private void showHelp() {

        setScreen(true);

        title("Help & Support");

        infoCard(
                "💬 Viyzo Support",
                "Get help with your account, jobs, " +
                "verification and payments."
        );

        primaryButton("💬 Contact Support")
                .setOnClickListener(
                        v -> message(
                                "Support system coming next"
                        )
                );

        appButton("📚 Help Center")
                .setOnClickListener(
                        v -> message(
                                "Help Center coming next"
                        )
                );

        appButton("🚨 Report a Problem")
                .setOnClickListener(
                        v -> message(
                                "Problem reporting coming next"
                        )
                );
    }

    // =========================================================
    // ABOUT
    // =========================================================

    private void showAbout() {

        setScreen(false);

        logoHeader();

        title("About Viyzo");

        infoCard(
                "VIYZO",
                "Global Work Network"
        );

        subtitle(
                "Viyzo is designed as an AI-managed work platform " +
                "connecting companies with workers globally."
        );

        infoCard(
                "🤖 AI Master Manager",
                "Job distribution, worker recommendations, " +
                "workload management and workflow assistance."
        );

        infoCard(
                "👷 Worker Network",
                "Workers can discover suitable digital jobs " +
                "and manage their work."
        );

        infoCard(
                "🏢 Company Network",
                "Companies can submit jobs and budgets " +
                "for managed worker execution."
        );

        infoCard(
                "💰 Platform Model",
                "Example: company budget → worker pool + " +
                "Viyzo platform fee."
        );

        appButton("← Back")
                .setOnClickListener(
                        v -> showHome()
                );
    }
}
