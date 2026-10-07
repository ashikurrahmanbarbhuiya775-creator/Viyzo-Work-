package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // =========================
    // VIYZO DESIGN SYSTEM
    // =========================

    private final int BG = Color.rgb(7, 12, 22);
    private final int BG2 = Color.rgb(10, 17, 31);
    private final int CARD = Color.rgb(17, 28, 48);
    private final int CARD2 = Color.rgb(21, 35, 59);

    private final int PRIMARY = Color.rgb(78, 70, 255);
    private final int PRIMARY2 = Color.rgb(105, 55, 235);

    private final int WHITE = Color.WHITE;
    private final int TEXT = Color.rgb(235, 238, 250);
    private final int GRAY = Color.rgb(155, 165, 185);

    private final int GREEN = Color.rgb(38, 210, 125);
    private final int RED = Color.rgb(240, 75, 100);
    private final int GOLD = Color.rgb(245, 185, 60);
    private final int BLUE = Color.rgb(50, 150, 255);

    private LinearLayout root;
    private FrameLayout screenFrame;

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
    // BASIC HELPERS
    // =========================

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private TextView title(String value) {
        TextView t = text(value, 24, WHITE);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(4), 0, dp(10));
        return t;
    }

    private TextView subtitle(String value) {
        TextView t = text(value, 14, GRAY);
        t.setPadding(0, 0, 0, dp(12));
        return t;
    }

    private GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private GradientDrawable strokeBg(
            int fill,
            int stroke,
            int strokeWidth,
            float radius
    ) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radius));
        g.setStroke(dp(strokeWidth), stroke);
        return g;
    }

    private GradientDrawable gradient() {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{PRIMARY, PRIMARY2}
        );
        g.setCornerRadius(dp(22));
        return g;
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void margin(View v, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        v.setLayoutParams(p);
    }

    // =========================
    // MAIN SCREEN CONTAINER
    // =========================

    private void base(boolean bottomNav) {

        root = vertical();
        root.setBackgroundColor(BG);

        if (bottomNav) {

            screenFrame = new FrameLayout(this);

            LinearLayout.LayoutParams frameParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0
                    );

            frameParams.weight = 1;

            root.addView(screenFrame, frameParams);

            root.addView(bottomNavigation());
        } else {
            screenFrame = new FrameLayout(this);

            LinearLayout.LayoutParams frameParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );

            root.addView(screenFrame, frameParams);
        }

        setContentView(root);
    }

    private ScrollView scrollContent() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout content = vertical();
        content.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(35)
        );

        scroll.addView(content);

        screenFrame.addView(
                scroll,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        return scroll;
    }

    private LinearLayout contentOf(ScrollView scroll) {
        return (LinearLayout) scroll.getChildAt(0);
    }

    // =========================
    // VIYZO LOGO
    // =========================

    private TextView logoIcon(int size) {

        TextView logo = text("V", size, WHITE);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(120, 75, 255), Color.rgb(50, 75, 255)}
        );

        g.setShape(GradientDrawable.OVAL);

        logo.setBackground(g);

        return logo;
    }

    private LinearLayout brandHeader() {

        LinearLayout row = horizontal();

        TextView logo = logoIcon(28);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(55), dp(55));

        row.addView(logo, lp);

        LinearLayout names = vertical();

        TextView name = text("VIYZO", 21, WHITE);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView small = text(
                "Global Work Network",
                11,
                GRAY
        );

        names.addView(name);
        names.addView(small);

        LinearLayout.LayoutParams np =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        np.weight = 1;
        np.setMargins(dp(12), 0, 0, 0);

        row.addView(names, np);

        TextView bell = text("🔔", 22, WHITE);
        bell.setGravity(Gravity.CENTER);

        bell.setOnClickListener(v -> showNotifications());

        row.addView(
                bell,
                new LinearLayout.LayoutParams(dp(50), dp(55))
        );

        return row;
    }

    // =========================
    // STRONG BUTTON
    // =========================

    private Button primaryButton(String label) {

        Button b = new Button(this);

        b.setText(label);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);

        b.setBackground(gradient());

        b.setElevation(dp(7));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        p.setMargins(0, dp(8), 0, dp(8));

        b.setLayoutParams(p);

        return b;
    }

    private Button darkButton(String label) {

        Button b = new Button(this);

        b.setText(label);
        b.setTextColor(TEXT);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);

        b.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(42, 66, 100),
                        1,
                        18
                )
        );

        b.setElevation(dp(4));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(55)
                );

        p.setMargins(0, dp(6), 0, dp(6));

        b.setLayoutParams(p);

        return b;
    }

    // =========================
    // BIG ACTION CARD
    // =========================

    private LinearLayout actionCard(
            String icon,
            String heading,
            String description,
            View.OnClickListener listener
    ) {

        LinearLayout card = vertical();

        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(31, 55, 90),
                        1,
                        20
                )
        );

        card.setElevation(dp(5));

        TextView iconText = text(icon, 28, WHITE);
        iconText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams iconLp =
                new LinearLayout.LayoutParams(dp(52), dp(52));

        iconLp.setMargins(0, 0, 0, dp(10));

        card.addView(iconText, iconLp);

        TextView h = text(heading, 17, WHITE);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(h);

        TextView d = text(description, 12, GRAY);
        d.setPadding(0, dp(5), 0, 0);
        card.addView(d);

        card.setOnClickListener(listener);

        return card;
    }

    // =========================
    // INFORMATION CARD
    // =========================

    private LinearLayout infoCard(
            String icon,
            String heading,
            String value
    ) {

        LinearLayout card = horizontal();

        card.setPadding(
                dp(15),
                dp(13),
                dp(15),
                dp(13)
        );

        card.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(28, 51, 83),
                        1,
                        18
                )
        );

        TextView ic = text(icon, 24, WHITE);
        ic.setGravity(Gravity.CENTER);

        card.addView(
                ic,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(45)
                )
        );

        LinearLayout texts = vertical();

        TextView h = text(heading, 12, GRAY);

        TextView v = text(value, 17, WHITE);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        texts.addView(h);
        texts.addView(v);

        LinearLayout.LayoutParams tp =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        tp.weight = 1;
        tp.setMargins(dp(12), 0, 0, 0);

        card.addView(texts, tp);

        return card;
    }

    // =========================
    // BOTTOM NAVIGATION
    // =========================

    private LinearLayout bottomNavigation() {

        LinearLayout nav = horizontal();

        nav.setGravity(Gravity.CENTER);

        nav.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        nav.setBackground(
                strokeBg(
                        Color.rgb(13, 24, 42),
                        Color.rgb(39, 63, 98),
                        1,
                        25
                )
        );

        nav.setElevation(dp(15));

        String[] icons = {"⌂", "💼", "💳", "●"};
        String[] labels = {"Home", "Jobs", "Earnings", "Profile"};

        for (int i = 0; i < 4; i++) {

            final int index = i;

            LinearLayout item = vertical();
            item.setGravity(Gravity.CENTER);

            TextView icon = text(
                    icons[i],
                    22,
                    i == 0 ? WHITE : GRAY
            );

            icon.setGravity(Gravity.CENTER);

            TextView label = text(
                    labels[i],
                    10,
                    i == 0 ? WHITE : GRAY
            );

            label.setGravity(Gravity.CENTER);

            item.addView(icon);
            item.addView(label);

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62)
                    );

            p.weight = 1;

            nav.addView(item, p);

            item.setOnClickListener(v -> {

                if (index == 0) showDashboard();
                if (index == 1) showJobs();
                if (index == 2) showEarnings();
                if (index == 3) showProfile();

            });
        }

        LinearLayout.LayoutParams np =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(82)
                );

        np.setMargins(
                dp(10),
                dp(4),
                dp(10),
                dp(10)
        );

        nav.setLayoutParams(np);

        return nav;
    }

    // =========================
    // HOME / SPLASH
    // =========================

    private void showHome() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        LinearLayout center = vertical();
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView logo = logoIcon(60);

        center.addView(
                logo,
                new LinearLayout.LayoutParams(
                        dp(115),
                        dp(115)
                )
        );

        TextView name = text("VIYZO", 40, WHITE);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);

        margin(name, 0, 18, 0, 4);

        center.addView(name);

        TextView network =
                text("Global Work Network", 15, GRAY);

        network.setGravity(Gravity.CENTER);
        center.addView(network);

        TextView slogan =
                text(
                        "\nWork Smarter\nEarn Better\nTogether",
                        20,
                        TEXT
                );

        slogan.setGravity(Gravity.CENTER);
        slogan.setPadding(0, dp(35), 0, dp(35));

        center.addView(slogan);

        TextView globe =
                text("🌐", 80, WHITE);

        globe.setGravity(Gravity.CENTER);

        center.addView(
                globe,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(130)
                )
        );

        Button start =
                primaryButton("Get Started   →");

        start.setOnClickListener(v -> showLogin());

        center.addView(start);

        Button create =
                darkButton("Create New Account");

        create.setOnClickListener(v -> showCreateAccount());

        center.addView(create);

        c.addView(center);
    }

    // =========================
    // LOGIN
    // =========================

    private EditText input(String hint) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(Color.rgb(115, 130, 155));
        e.setTextColor(WHITE);
        e.setTextSize(14);
        e.setSingleLine(true);
        e.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );

        e.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(40, 65, 100),
                        1,
                        18
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(56)
                );

        p.setMargins(0, dp(7), 0, dp(7));

        e.setLayoutParams(p);

        return e;
    }

    private void showLogin() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        LinearLayout header = brandHeader();
        c.addView(header);

        c.addView(title("Welcome Back"));
        c.addView(subtitle("Login to your Viyzo Worker account"));

        EditText email = input("Email or Phone");

        EditText password = input("Viyzo Password");
        password.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        c.addView(email);
        c.addView(password);

        Button login = primaryButton("🔐  Login");

        login.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Login system will connect to server.",
                    Toast.LENGTH_SHORT
            ).show();

            showDashboard();
        });

        c.addView(login);

        Button create = darkButton("Create New Account");

        create.setOnClickListener(v -> showCreateAccount());

        c.addView(create);
    }

    // =========================
    // CREATE ACCOUNT
    // =========================

    private void showCreateAccount() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(brandHeader());

        c.addView(title("Create Account"));
        c.addView(
                subtitle(
                        "Join Viyzo and start working globally"
                )
        );

        c.addView(input("Full Name"));
        c.addView(input("Email"));
        c.addView(input("Phone Number"));

        EditText password = input("Create Viyzo Password");

        password.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        c.addView(password);

        CheckBox terms = new CheckBox(this);
        terms.setText(
                "I agree to Viyzo Terms and Privacy Policy"
        );
        terms.setTextColor(TEXT);
        terms.setTextSize(13);

        c.addView(terms);

        Button create =
                primaryButton("🚀  Create Account");

        create.setOnClickListener(v -> {

            if (!terms.isChecked()) {

                Toast.makeText(
                        this,
                        "Please accept Terms and Privacy Policy.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showWorkerVerification();
        });

        c.addView(create);

        Button login =
                darkButton("Already have an account? Login");

        login.setOnClickListener(v -> showLogin());

        c.addView(login);
    }

    // =========================
    // DASHBOARD
    // =========================

    private void showDashboard() {

        base(true);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(brandHeader());

        LinearLayout welcome =
                infoCard(
                        "👤",
                        "Good day, Ashikur Rahman",
                        "Basic Worker"
                );

        c.addView(welcome);
        margin(welcome, 0, 15, 0, 15);

        LinearLayout manager =
                actionCard(
                        "🤖",
                        "AI Master Manager",
                        "Finds the best jobs based on your profile.",
                        v -> showJobs()
                );

        c.addView(manager);
        margin(manager, 0, 0, 0, 12);

        TextView quick = text(
                "Quick Access",
                19,
                WHITE
        );

        quick.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        c.addView(quick);

        LinearLayout row1 = horizontal();

        LinearLayout jobs =
                actionCard(
                        "🔎",
                        "Available Jobs",
                        "Explore new work",
                        v -> showJobs()
                );

        LinearLayout myJobs =
                actionCard(
                        "📋",
                        "My Jobs",
                        "Track your work",
                        v -> showMyJobs()
                );

        row1.addView(
                jobs,
                new LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1
                )
        );

        LinearLayout.LayoutParams myP =
                new LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1
                );

        myP.setMargins(dp(8), 0, 0, 0);

        row1.addView(myJobs, myP);

        c.addView(row1);

        LinearLayout row2 = horizontal();

        LinearLayout earning =
                actionCard(
                        "💰",
                        "Earnings",
                        "View balance",
                        v -> showEarnings()
                );

        LinearLayout verification =
                actionCard(
                        "🛡",
                        "Verification",
                        "Complete Worker KYC",
                        v -> showWorkerVerification()
                );

        row2.addView(
                earning,
                new LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1
                )
        );

        LinearLayout.LayoutParams verP =
                new LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1
                );

        verP.setMargins(dp(8), 0, 0, 0);

        row2.addView(verification, verP);

        c.addView(row2);

        TextView access =
                text(
                        "More Options",
                        19,
                        WHITE
                );

        access.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        margin(access, 0, 18, 0, 8);

        c.addView(access);

        Button notifications =
                darkButton("🔔  Notifications");

        notifications.setOnClickListener(
                v -> showNotifications()
        );

        c.addView(notifications);

        Button language =
                darkButton("🌐  Language");

        language.setOnClickListener(
                v -> showLanguage()
        );

        c.addView(language);

        Button settings =
                darkButton("⚙  Settings");

        settings.setOnClickListener(
                v -> showSettings()
        );

        c.addView(settings);
    }

    // =========================
    // JOBS
    // =========================

    private void showJobs() {

        base(true);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Available Jobs"));
        c.addView(
                subtitle(
                        "AI Master Manager selected jobs for you"
                )
        );

        EditText search = input("🔎  Search jobs...");
        c.addView(search);

        c.addView(jobCard(
                "🤖",
                "Product Listing",
                "1000 products • Remote",
                "$500 Budget",
                "8 Workers",
                "3 Days"
        ));

        c.addView(jobCard(
                "📊",
                "Data Entry",
                "500 records • Flexible",
                "$250 Budget",
                "5 Workers",
                "2 Days"
        ));

        c.addView(jobCard(
                "📝",
                "Content Review",
                "500 items • Remote",
                "$300 Budget",
                "6 Workers",
                "3 Days"
        ));
    }

    private LinearLayout jobCard(
            String icon,
            String name,
            String type,
            String budget,
            String workers,
            String deadline
    ) {

        LinearLayout card = vertical();

        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(37, 62, 100),
                        1,
                        20
                )
        );

        card.setElevation(dp(6));

        LinearLayout top = horizontal();

        TextView ic = text(icon, 25, WHITE);
        ic.setGravity(Gravity.CENTER);

        top.addView(
                ic,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                )
        );

        LinearLayout names = vertical();

        TextView n = text(name, 17, WHITE);
        n.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView tp = text(type, 12, GRAY);

        names.addView(n);
        names.addView(tp);

        LinearLayout.LayoutParams np =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        np.weight = 1;
        np.setMargins(dp(10), 0, 0, 0);

        top.addView(names, np);

        TextView global = text("Global", 11, GREEN);
        global.setGravity(Gravity.CENTER);

        top.addView(global);

        card.addView(top);

        TextView info = text(
                budget +
                        "\nWorker Pool: " +
                        workers +
                        "\nDeadline: " +
                        deadline,
                13,
                TEXT
        );

        info.setPadding(0, dp(14), 0, dp(8));

        card.addView(info);

        Button details =
                primaryButton("View Details  →");

        details.setOnClickListener(
                v -> showJobDetails(name, budget)
        );

        card.addView(details);

        margin(card, 0, 0, 0, 14);

        return card;
    }

    // =========================
    // JOB DETAILS
    // =========================

    private void showJobDetails(
            String jobName,
            String budget
    ) {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Job Details"));
        c.addView(
                subtitle(
                        "AI Master Manager recommendation"
                )
        );

        LinearLayout card =
                infoCard(
                        "🤖",
                        jobName,
                        "Global • Remote"
                );

        c.addView(card);

        LinearLayout details = vertical();

        details.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        details.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(37, 62, 100),
                        1,
                        20
                )
        );

        TextView d = text(
                "Company Budget       " + budget +
                        "\n\nWorker Pool            $400.00" +
                        "\n\nViyzo Fee                 $100.00" +
                        "\n\nRecommended Workers   8" +
                        "\n\nWorkload per Worker   125 products" +
                        "\n\nDeadline                   3 Days",
                14,
                TEXT
        );

        details.addView(d);

        margin(details, 0, 15, 0, 15);

        c.addView(details);

        TextView req =
                text(
                        "Requirements",
                        18,
                        WHITE
                );

        req.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        c.addView(req);

        c.addView(infoCard(
                "✓",
                "Internet",
                "Good internet connection"
        ));

        c.addView(infoCard(
                "✓",
                "Skills",
                "Basic mobile/computer skills"
        ));

        c.addView(infoCard(
                "✓",
                "Quality",
                "Accuracy and quality work"
        ));

        Button apply =
                primaryButton("🚀  Apply Now");

        apply.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Job application submitted.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        c.addView(apply);
    }

    // =========================
    // MY JOBS
    // =========================

    private void showMyJobs() {

        base(true);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("My Jobs"));

        c.addView(
                infoCard(
                        "💼",
                        "Product Listing",
                        "In Progress • 12%"
                )
        );

        LinearLayout progress =
                vertical();

        progress.setPadding(
                dp(15),
                dp(15),
                dp(15),
                dp(15)
        );

        progress.setBackground(bg(CARD2, 18));

        progress.addView(
                text(
                        "125 / 1000 products completed",
                        14,
                        TEXT
                )
        );

        progress.addView(
                text(
                        "Deadline: 3 Days",
                        12,
                        GRAY
                )
        );

        c.addView(progress);

        Button open =
                primaryButton("Open Work");

        open.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Work screen coming next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        c.addView(open);
    }

    // =========================
    // EARNINGS
    // =========================

    private void showEarnings() {

        base(true);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Earnings"));

        LinearLayout balance = vertical();

        balance.setPadding(
                dp(20),
                dp(22),
                dp(20),
                dp(22)
        );

        balance.setBackground(gradient());
        balance.setElevation(dp(8));

        TextView b1 =
                text(
                        "Available Balance",
                        13,
                        WHITE
                );

        TextView b2 =
                text(
                        "$0.00",
                        32,
                        WHITE
                );

        b2.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        balance.addView(b1);
        balance.addView(b2);

        Button withdraw =
                primaryButton("Withdraw");

        withdraw.setBackground(
                bg(WHITE, 20)
        );

        withdraw.setTextColor(PRIMARY);

        withdraw.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Withdrawal system will connect to payment provider.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        balance.addView(withdraw);

        c.addView(balance);

        c.addView(
                infoCard(
                        "💵",
                        "Total Earned",
                        "$0.00"
                )
        );

        c.addView(
                infoCard(
                        "⏳",
                        "Pending",
                        "$0.00"
                )
        );

        c.addView(
                infoCard(
                        "📈",
                        "Recent Transactions",
                        "No transactions yet"
                )
        );

        Button methods =
                darkButton("💳  Payment Methods");

        c.addView(methods);

        Button history =
                darkButton("📋  Transaction History");

        c.addView(history);
    }

    // =========================
    // PROFILE
    // =========================

    private void showProfile() {

        base(true);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("My Profile"));

        LinearLayout profile = horizontal();

        profile.setPadding(
                dp(15),
                dp(15),
                dp(15),
                dp(15)
        );

        profile.setBackground(
                strokeBg(
                        CARD,
                        Color.rgb(37, 62, 100),
                        1,
                        20
                )
        );

        TextView avatar = text("👤", 35, WHITE);
        avatar.setGravity(Gravity.CENTER);

        profile.addView(
                avatar,
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(70)
                )
        );

        LinearLayout details = vertical();

        TextView name =
                text(
                        "Ashikur Rahman",
                        18,
                        WHITE
                );

        name.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        details.addView(name);

        details.addView(
                text(
                        "VZ388742",
                        12,
                        GRAY
                )
        );

        TextView status =
                text(
                        "● Basic Worker",
                        12,
                        GREEN
                );

        details.addView(status);

        LinearLayout.LayoutParams dp1 =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        dp1.weight = 1;
        dp1.setMargins(dp(12), 0, 0, 0);

        profile.addView(details, dp1);

        c.addView(profile);

        c.addView(
                infoCard(
                        "🛡",
                        "Verification Status",
                        "Basic Verification • Not completed"
                )
        );

        Button verify =
                darkButton("🛡  Worker Verification");

        verify.setOnClickListener(
                v -> showWorkerVerification()
        );

        c.addView(verify);

        Button settings =
                darkButton("⚙  Account Settings");

        settings.setOnClickListener(
                v -> showSettings()
        );

        c.addView(settings);

        Button help =
                darkButton("❓  Help & Support");

        help.setOnClickListener(
                v -> showHelp()
        );

        c.addView(help);

        Button about =
                darkButton("ⓘ  About Viyzo");

        about.setOnClickListener(
                v -> showAbout()
        );

        c.addView(about);

        Button logout =
                primaryButton("Logout");

        logout.setBackground(bg(RED, 20));

        logout.setOnClickListener(
                v -> showHome()
        );

        c.addView(logout);
    }

    // =========================
    // WORKER VERIFICATION
    // =========================

    private void showWorkerVerification() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Worker Verification"));

        c.addView(
                subtitle(
                        "Complete verification to unlock more jobs and higher-value work."
                )
        );

        c.addView(
                infoCard(
                        "1",
                        "Country",
                        "Select your country"
                )
        );

        c.addView(
                infoCard(
                        "2",
                        "Basic Verification",
                        "Name, DOB, Phone, Email, Selfie"
                )
        );

        c.addView(
                infoCard(
                        "3",
                        "Identity Document",
                        "Optional if available"
                )
        );

        c.addView(
                infoCard(
                        "4",
                        "Consent",
                        "Agree to terms and privacy policy"
                )
        );

        Button start =
                primaryButton("Start Verification");

        start.setOnClickListener(
                v -> showBasicVerification()
        );

        c.addView(start);

        Button noDocument =
                darkButton("I Don't Have a Document");

        noDocument.setOnClickListener(
                v -> showNoDocument()
        );

        c.addView(noDocument);
    }

    // =========================
    // BASIC VERIFICATION
    // =========================

    private void showBasicVerification() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Basic Verification"));

        c.addView(
                subtitle(
                        "Enter your real information for Worker Verification."
                )
        );

        c.addView(input("Full Name"));
        c.addView(input("Date of Birth"));
        c.addView(input("Phone Number"));
        c.addView(input("Email Address"));

        Button otp =
                darkButton("📱 Send Phone OTP");

        otp.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "OTP system will connect to backend.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        c.addView(otp);

        Button selfie =
                darkButton("📷 Take Live Selfie");

        selfie.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "Selfie verification will connect to verification service.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        c.addView(selfie);

        CheckBox consent = new CheckBox(this);

        consent.setText(
                "I confirm that the information is mine and I agree to Viyzo's verification and privacy terms."
        );

        consent.setTextColor(TEXT);
        consent.setTextSize(13);

        c.addView(consent);

        Button continueBtn =
                primaryButton("Continue Verification");

        continueBtn.setOnClickListener(v -> {

            if (!consent.isChecked()) {

                Toast.makeText(
                        this,
                        "Please accept the verification consent.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showDocumentOptions();
        });

        c.addView(continueBtn);
    }

    // =========================
    // DOCUMENT OPTIONS
    // =========================

    private void showDocumentOptions() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Identity Document"));

        c.addView(
                subtitle(
                        "Document requirements can vary by country and verification provider."
                )
        );

        Spinner country = new Spinner(this);

        String[] countries = {
                "Select Country",
                "India",
                "United States",
                "United Kingdom",
                "Bangladesh",
                "UAE",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        countries
                );

        country.setAdapter(adapter);

        c.addView(country);

        c.addView(
                infoCard(
                        "🪪",
                        "Government ID",
                        "Upload an accepted document when required."
                )
        );

        Button upload =
                primaryButton("📷 Upload / Capture Document");

        upload.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "Document upload will connect to secure verification backend.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        c.addView(upload);

        Button skip =
                darkButton("Continue Without Document");

        skip.setOnClickListener(
                v -> showNoDocument()
        );

        c.addView(skip);
    }

    // =========================
    // NO DOCUMENT
    // =========================

    private void showNoDocument() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Basic Worker Account"));

        c.addView(
                infoCard(
                        "✓",
                        "Account Created",
                        "Your account can continue with basic access."
                )
        );

        c.addView(
                infoCard(
                        "🛡",
                        "Basic Verification",
                        "Some jobs may remain available."
                )
        );

        c.addView(
                infoCard(
                        "🔒",
                        "Higher Verification",
                        "Some high-value jobs or payouts may require stronger verification."
                )
        );

        Button done =
                primaryButton("Continue to Dashboard");

        done.setOnClickListener(
                v -> showDashboard()
        );

        c.addView(done);
    }

    // =========================
    // NOTIFICATIONS
    // =========================

    private void showNotifications() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Notifications"));

        c.addView(
                infoCard(
                        "🤖",
                        "AI Master Manager",
                        "A new Product Listing job is available."
                )
        );

        c.addView(
                infoCard(
                        "💰",
                        "Earnings",
                        "Complete jobs to start earning."
                )
        );

        c.addView(
                infoCard(
                        "🛡",
                        "Verification",
                        "Complete Worker Verification for more access."
                )
        );

        Button back =
                darkButton("← Back");

        back.setOnClickListener(
                v -> showDashboard()
        );

        c.addView(back);
    }

    // =========================
    // LANGUAGE
    // =========================

    private void showLanguage() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Language"));

        String[] languages = {
                "🇬🇧  English",
                "🇮🇳  हिन्दी",
                "🇧🇩  বাংলা",
                "🇵🇰  اردو",
                "🇸🇦  العربية",
                "🇹🇷  Türkçe"
        };

        for (String lang : languages) {

            Button b = darkButton(lang);

            b.setOnClickListener(
                    v -> Toast.makeText(
                            this,
                            "Language selected: " + lang,
                            Toast.LENGTH_SHORT
                    ).show()
            );

            c.addView(b);
        }
    }

    // =========================
    // SETTINGS
    // =========================

    private void showSettings() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Settings"));

        Button notifications =
                darkButton("🔔  Notification Settings");

        c.addView(notifications);

        Button language =
                darkButton("🌐  Language");

        language.setOnClickListener(
                v -> showLanguage()
        );

        c.addView(language);

        Button privacy =
                darkButton("🔐  Privacy");

        c.addView(privacy);

        Button payment =
                darkButton("💳  Payment Settings");

        c.addView(payment);

        Button help =
                darkButton("❓  Help & Support");

        help.setOnClickListener(
                v -> showHelp()
        );

        c.addView(help);

        Button about =
                darkButton("ⓘ  About Viyzo");

        about.setOnClickListener(
                v -> showAbout()
        );

        c.addView(about);

        Button logout =
                primaryButton("Logout");

        logout.setBackground(bg(RED, 20));

        logout.setOnClickListener(
                v -> showHome()
        );

        c.addView(logout);
    }

    // =========================
    // HELP
    // =========================

    private void showHelp() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        c.addView(title("Help & Support"));

        c.addView(
                infoCard(
                        "💬",
                        "Worker Support",
                        "Get help with jobs, account and verification."
                )
        );

        c.addView(
                infoCard(
                        "📚",
                        "Help Center",
                        "Learn how Viyzo works."
                )
        );

        Button contact =
                primaryButton("Contact Support");

        contact.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "Support system will connect to backend.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        c.addView(contact);
    }

    // =========================
    // ABOUT
    // =========================

    private void showAbout() {

        base(false);

        ScrollView scroll = scrollContent();
        LinearLayout c = contentOf(scroll);

        LinearLayout center = vertical();
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView logo = logoIcon(35);

        center.addView(
                logo,
                new LinearLayout.LayoutParams(
                        dp(90),
                        dp(90)
                )
        );

        TextView name =
                text("VIYZO", 30, WHITE);

        name.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        name.setGravity(Gravity.CENTER);

        center.addView(name);

        TextView global =
                text(
                        "Global Work Network",
                        13,
                        GRAY
                );

        global.setGravity(Gravity.CENTER);

        center.addView(global);

        c.addView(center);

        c.addView(
                infoCard(
                        "🤖",
                        "AI Master Manager",
                        "Smart job distribution"
                )
        );

        c.addView(
                infoCard(
                        "🌐",
                        "Global Network",
                        "Workers and companies"
                )
        );

        c.addView(
                infoCard(
                        "🛡",
                        "Secure & Transparent",
                        "Designed for global work"
                )
        );

        TextView version =
                text(
                        "Version 1.0.0",
                        12,
                        GRAY
                );

        version.setGravity(Gravity.CENTER);

        c.addView(version);
    }
}
