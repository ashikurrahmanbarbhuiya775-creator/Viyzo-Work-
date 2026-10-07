package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

    // ============================================================
    // VIYZO WORKER — MODERN APP UI + GLOBAL COMPANY JOB SYSTEM
    // UI-only foundation: real accounts, OTP, KYC, payments and
    // global job syncing will connect to a secure backend later.
    // ============================================================

    private final int BG = Color.rgb(7, 12, 22);
    private final int CARD = Color.rgb(15, 27, 47);
    private final int CARD2 = Color.rgb(20, 35, 59);
    private final int BORDER = Color.rgb(39, 65, 103);
    private final int PRIMARY = Color.rgb(78, 70, 255);
    private final int PRIMARY2 = Color.rgb(113, 54, 235);
    private final int WHITE = Color.WHITE;
    private final int TEXT = Color.rgb(235, 238, 250);
    private final int GRAY = Color.rgb(155, 165, 185);
    private final int GREEN = Color.rgb(40, 210, 125);
    private final int RED = Color.rgb(240, 75, 100);
    private final int GOLD = Color.rgb(245, 185, 60);
    private final int BLUE = Color.rgb(65, 150, 255);

    private LinearLayout root;
    private FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showHome();
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView tv(String s, float size, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private TextView heading(String s) {
        TextView t = tv(s, 24, WHITE);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(3), 0, dp(8));
        return t;
    }

    private TextView small(String s) {
        TextView t = tv(s, 13, GRAY);
        t.setPadding(0, 0, 0, dp(8));
        return t;
    }

    private GradientDrawable solid(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private GradientDrawable outlined(int fill, int stroke, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radius));
        g.setStroke(dp(1), stroke);
        return g;
    }

    private GradientDrawable primaryBg() {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{PRIMARY, PRIMARY2}
        );
        g.setCornerRadius(dp(20));
        return g;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void setMargins(View v, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        v.setLayoutParams(p);
    }

    private void startScreen(boolean bottomNav) {
        root = column();
        root.setBackgroundColor(BG);

        frame = new FrameLayout(this);

        if (bottomNav) {
            LinearLayout.LayoutParams fp =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0
                    );
            fp.weight = 1;
            root.addView(frame, fp);
            root.addView(bottomNav());
        } else {
            root.addView(frame, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
        }

        setContentView(root);
    }

    private LinearLayout content() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout c = column();
        c.setPadding(dp(18), dp(16), dp(18), dp(35));
        scroll.addView(c);

        frame.addView(scroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        return c;
    }

    private TextView logo(int size) {
        TextView l = tv("V", size, WHITE);
        l.setGravity(Gravity.CENTER);
        l.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(125, 75, 255), Color.rgb(55, 75, 255)}
        );
        g.setShape(GradientDrawable.OVAL);
        l.setBackground(g);
        return l;
    }

    private LinearLayout brandHeader() {
        LinearLayout h = row();

        h.addView(logo(27), new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout names = column();
        TextView n = tv("VIYZO", 20, WHITE);
        n.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        names.addView(n);
        names.addView(tv("Global Work Network", 10, GRAY));

        LinearLayout.LayoutParams np =
                new LinearLayout.LayoutParams(0, dp(54), 1);
        np.setMargins(dp(11), 0, 0, 0);
        h.addView(names, np);

        TextView bell = tv("🔔", 21, WHITE);
        bell.setGravity(Gravity.CENTER);
        bell.setBackground(outlined(CARD, BORDER, 18));
        bell.setOnClickListener(v -> showNotifications());
        h.addView(bell, new LinearLayout.LayoutParams(dp(52), dp(52)));

        return h;
    }

    private Button primary(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(WHITE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(primaryBg());
        b.setElevation(dp(7));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        p.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(p);
        return b;
    }

    private Button secondary(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(TEXT);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(outlined(CARD, BORDER, 18));
        b.setElevation(dp(3));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(53));
        p.setMargins(0, dp(5), 0, dp(5));
        b.setLayoutParams(p);
        return b;
    }

    private LinearLayout card() {
        LinearLayout c = column();
        c.setPadding(dp(15), dp(15), dp(15), dp(15));
        c.setBackground(outlined(CARD, BORDER, 20));
        c.setElevation(dp(4));
        return c;
    }

    private LinearLayout statCard(String icon, String title, String value) {
        LinearLayout c = card();

        TextView ic = tv(icon, 24, WHITE);
        ic.setGravity(Gravity.CENTER);
        c.addView(ic, new LinearLayout.LayoutParams(dp(45), dp(40)));

        TextView t = tv(title, 11, GRAY);
        t.setPadding(0, dp(5), 0, 0);
        c.addView(t);

        TextView v = tv(value, 17, WHITE);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        c.addView(v);

        return c;
    }

    private LinearLayout menuCard(String icon, String title, String desc,
                                  View.OnClickListener listener) {
        LinearLayout c = card();

        TextView ic = tv(icon, 25, WHITE);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(outlined(CARD2, BORDER, 17));
        c.addView(ic, new LinearLayout.LayoutParams(dp(50), dp(50)));

        TextView t = tv(title, 15, WHITE);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(9), 0, 0);
        c.addView(t);

        TextView d = tv(desc, 11, GRAY);
        d.setPadding(0, dp(4), 0, 0);
        c.addView(d);

        c.setOnClickListener(listener);
        return c;
    }

    private LinearLayout wideInfo(String icon, String title, String value) {
        LinearLayout c = row();
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackground(outlined(CARD, BORDER, 18));
        c.setElevation(dp(3));

        TextView ic = tv(icon, 23, WHITE);
        ic.setGravity(Gravity.CENTER);
        c.addView(ic, new LinearLayout.LayoutParams(dp(45), dp(45)));

        LinearLayout tx = column();
        tx.addView(tv(title, 11, GRAY));
        TextView v = tv(value, 15, WHITE);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tx.addView(v);

        LinearLayout.LayoutParams tp =
                new LinearLayout.LayoutParams(0, dp(45), 1);
        tp.setMargins(dp(10), 0, 0, 0);
        c.addView(tx, tp);

        return c;
    }

    private LinearLayout bottomNav() {
        LinearLayout nav = row();
        nav.setPadding(dp(8), dp(7), dp(8), dp(7));
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(outlined(Color.rgb(11, 22, 39), BORDER, 25));
        nav.setElevation(dp(14));

        String[] icons = {"⌂", "💼", "💳", "●"};
        String[] names = {"Home", "Jobs", "Earnings", "Profile"};

        for (int i = 0; i < 4; i++) {
            final int index = i;

            LinearLayout item = column();
            item.setGravity(Gravity.CENTER);

            TextView ic = tv(icons[i], 21, i == 0 ? WHITE : GRAY);
            ic.setGravity(Gravity.CENTER);

            TextView nm = tv(names[i], 9, i == 0 ? WHITE : GRAY);
            nm.setGravity(Gravity.CENTER);

            item.addView(ic, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(32)));
            item.addView(nm);

            if (i == 0) {
                item.setBackground(solid(PRIMARY, 17));
            }

            LinearLayout.LayoutParams ip =
                    new LinearLayout.LayoutParams(0, dp(62), 1);
            ip.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(item, ip);

            item.setOnClickListener(v -> {
                if (index == 0) showDashboard();
                else if (index == 1) showJobs();
                else if (index == 2) showEarnings();
                else showProfile();
            });
        }

        LinearLayout.LayoutParams np =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(78));
        np.setMargins(dp(9), dp(4), dp(9), dp(10));
        nav.setLayoutParams(np);

        return nav;
    }

    // ============================================================
    // HOME
    // ============================================================

    private void showHome() {
        startScreen(false);
        LinearLayout c = content();

        LinearLayout center = column();
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        center.addView(logo(58), new LinearLayout.LayoutParams(dp(115), dp(115)));

        TextView name = tv("VIYZO", 39, WHITE);
        name.setGravity(Gravity.CENTER);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        center.addView(name);

        TextView sub = tv("Global Work Network", 14, GRAY);
        sub.setGravity(Gravity.CENTER);
        center.addView(sub);

        TextView slogan = tv("\nWork Smarter\nEarn Better\nTogether", 19, TEXT);
        slogan.setGravity(Gravity.CENTER);
        slogan.setPadding(0, dp(30), 0, dp(24));
        center.addView(slogan);

        TextView globe = tv("🌐", 74, WHITE);
        globe.setGravity(Gravity.CENTER);
        center.addView(globe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(115)));

        Button start = primary("Get Started   →");
        start.setOnClickListener(v -> showLogin());
        center.addView(start);

        Button company = secondary("🏢  I'm a Company / Post Work");
        company.setOnClickListener(v -> showCompanyPortal());
        center.addView(company);

        Button create = secondary("Create New Worker Account");
        create.setOnClickListener(v -> showCreateAccount());
        center.addView(create);

        c.addView(center);
    }

    // ============================================================
    // LOGIN / ACCOUNT
    // ============================================================

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(110, 125, 150));
        e.setTextColor(WHITE);
        e.setTextSize(14);
        e.setSingleLine(true);
        e.setPadding(dp(15), 0, dp(15), 0);
        e.setBackground(outlined(CARD, BORDER, 17));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        p.setMargins(0, dp(6), 0, dp(6));
        e.setLayoutParams(p);
        return e;
    }

    private void showLogin() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(brandHeader());
        c.addView(heading("Welcome Back"));
        c.addView(small("Login to your Viyzo Worker account."));

        c.addView(input("Email or Phone"));

        EditText pass = input("Viyzo Password");
        pass.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        c.addView(pass);

        Button login = primary("🔐  Login");
        login.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Login will connect to secure backend.",
                    Toast.LENGTH_SHORT).show();
            showDashboard();
        });
        c.addView(login);

        Button company = secondary("🏢  Company Login");
        company.setOnClickListener(v -> showCompanyPortal());
        c.addView(company);

        Button create = secondary("Create New Worker Account");
        create.setOnClickListener(v -> showCreateAccount());
        c.addView(create);
    }

    private void showCreateAccount() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(brandHeader());
        c.addView(heading("Create Worker Account"));
        c.addView(small("Start with basic access. Stronger verification can unlock more work."));

        c.addView(input("Full Name"));
        c.addView(input("Email"));
        c.addView(input("Phone Number"));

        EditText pass = input("Create Viyzo Password");
        pass.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        c.addView(pass);

        CheckBox terms = new CheckBox(this);
        terms.setText("I agree to Viyzo Terms and Privacy Policy.");
        terms.setTextColor(TEXT);
        terms.setTextSize(12);
        c.addView(terms);

        Button create = primary("🚀  Create Account");
        create.setOnClickListener(v -> {
            if (!terms.isChecked()) {
                Toast.makeText(this,
                        "Please accept the Terms and Privacy Policy.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            showWorkerVerification();
        });
        c.addView(create);

        Button back = secondary("← Back to Login");
        back.setOnClickListener(v -> showLogin());
        c.addView(back);
    }

    // ============================================================
    // WORKER DASHBOARD — MIXED APP-STYLE GRID
    // ============================================================

    private void showDashboard() {
        startScreen(true);
        LinearLayout c = content();

        c.addView(brandHeader());

        LinearLayout welcome = card();
        LinearLayout wr = row();

        TextView avatar = tv("👤", 31, WHITE);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(outlined(CARD2, BORDER, 35));
        wr.addView(avatar, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout wn = column();
        TextView w1 = tv("Good day, Ashikur Rahman 👋", 15, WHITE);
        w1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        wn.addView(w1);
        wn.addView(tv("Worker ID: VZ388742", 11, GRAY));
        TextView verified = tv("● Basic Worker", 11, GREEN);
        wn.addView(verified);

        LinearLayout.LayoutParams wnp =
                new LinearLayout.LayoutParams(0, dp(58), 1);
        wnp.setMargins(dp(11), 0, 0, 0);
        wr.addView(wn, wnp);
        welcome.addView(wr);
        c.addView(welcome);
        setMargins(welcome, 0, 4, 0, 13);

        LinearLayout manager = card();
        TextView mt = tv("🤖  AI Master Manager       ›", 17, WHITE);
        mt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        manager.addView(mt);
        manager.addView(tv(
                "Finds suitable jobs from global company work.",
                11, GRAY));
        manager.setBackground(primaryBg());
        manager.setOnClickListener(v -> showJobs());
        c.addView(manager);
        setMargins(manager, 0, 0, 0, 13);

        // Uneven / separate app-style grid instead of one long list.
        LinearLayout r1 = row();

        LinearLayout jobs = menuCard(
                "🔎", "Available Jobs", "Explore global work",
                v -> showJobs());

        LinearLayout myJobs = menuCard(
                "📋", "My Jobs", "Track active work",
                v -> showMyJobs());

        r1.addView(jobs, new LinearLayout.LayoutParams(0, dp(145), 1));
        LinearLayout.LayoutParams p12 =
                new LinearLayout.LayoutParams(0, dp(145), 1);
        p12.setMargins(dp(8), dp(15), 0, 0);
        r1.addView(myJobs, p12);
        c.addView(r1);

        LinearLayout r2 = row();

        LinearLayout earnings = menuCard(
                "💰", "Earnings", "Balance & payout",
                v -> showEarnings());

        LinearLayout verify = menuCard(
                "🛡", "Verification", "Worker verification",
                v -> showWorkerVerification());

        r2.addView(earnings, new LinearLayout.LayoutParams(0, dp(145), 1));
        LinearLayout.LayoutParams p22 =
                new LinearLayout.LayoutParams(0, dp(145), 1);
        p22.setMargins(dp(8), dp(0), 0, 0);
        r2.addView(verify, p22);
        c.addView(r2);

        TextView q = tv("Quick Access", 18, WHITE);
        q.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        q.setPadding(0, dp(17), 0, dp(6));
        c.addView(q);

        LinearLayout r3 = row();

        LinearLayout notif = menuCard(
                "🔔", "Alerts", "New updates",
                v -> showNotifications());

        LinearLayout language = menuCard(
                "🌐", "Language", "Change language",
                v -> showLanguage());

        r3.addView(notif, new LinearLayout.LayoutParams(0, dp(125), 1));
        LinearLayout.LayoutParams p32 =
                new LinearLayout.LayoutParams(0, dp(125), 1);
        p32.setMargins(dp(8), dp(8), 0, 0);
        r3.addView(language, p32);
        c.addView(r3);

        LinearLayout settings = card();
        settings.setOrientation(LinearLayout.HORIZONTAL);
        TextView si = tv("⚙", 24, WHITE);
        settings.addView(si, new LinearLayout.LayoutParams(dp(42), dp(45)));
        TextView st = tv("Settings & Account", 15, WHITE);
        st.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        settings.addView(st);
        TextView arrow = tv("›", 28, GRAY);
        arrow.setGravity(Gravity.CENTER);
        settings.addView(arrow, new LinearLayout.LayoutParams(dp(40), dp(45)));
        settings.setOnClickListener(v -> showSettings());
        c.addView(settings);
        setMargins(settings, 0, 10, 0, 0);
    }

    // ============================================================
    // JOBS
    // ============================================================

    private void showJobs() {
        startScreen(true);
        LinearLayout c = content();

        LinearLayout top = row();
        TextView back = tv("‹", 32, WHITE);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showDashboard());
        top.addView(back, new LinearLayout.LayoutParams(dp(42), dp(50)));

        LinearLayout titles = column();
        TextView h = tv("Available Jobs", 21, WHITE);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titles.addView(h);
        titles.addView(tv("Global jobs matched by AI Master Manager", 10, GRAY));

        top.addView(titles, new LinearLayout.LayoutParams(0, dp(50), 1));
        c.addView(top);

        c.addView(input("🔎  Search jobs, skills or categories"));

        LinearLayout filter = row();

        Button all = secondary("All");
        Button remote = secondary("Remote");
        Button country = secondary("Country");

        filter.addView(all, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, dp(48), 1);
        rp.setMargins(dp(5), 0, 0, 0);
        filter.addView(remote, rp);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(48), 1);
        cp.setMargins(dp(5), 0, 0, 0);
        filter.addView(country, cp);
        c.addView(filter);

        c.addView(jobCard(
                "🤖", "Product Listing",
                "1000 products • Remote • Global",
                "$500", "8 Workers", "3 Days"));

        c.addView(jobCard(
                "📊", "Data Entry",
                "500 records • Flexible • Global",
                "$250", "5 Workers", "2 Days"));

        c.addView(jobCard(
                "📝", "Content Review",
                "500 items • Remote • Global",
                "$300", "6 Workers", "3 Days"));

        Button company = secondary("🏢  Are you a company? Post work");
        company.setOnClickListener(v -> showCompanyPortal());
        c.addView(company);
    }

    private LinearLayout jobCard(String icon, String name, String type,
                                 String budget, String workers, String deadline) {
        LinearLayout c = card();

        LinearLayout top = row();
        TextView ic = tv(icon, 24, WHITE);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(outlined(CARD2, BORDER, 16));
        top.addView(ic, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout names = column();
        TextView n = tv(name, 16, WHITE);
        n.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        names.addView(n);
        names.addView(tv(type, 10, GRAY));

        top.addView(names, new LinearLayout.LayoutParams(0, dp(48), 1));

        TextView gl = tv("Global", 10, GREEN);
        gl.setGravity(Gravity.CENTER);
        top.addView(gl, new LinearLayout.LayoutParams(dp(52), dp(30)));

        c.addView(top);

        TextView info = tv(
                "Company Budget   " + budget +
                "\nWorker Pool       " + (budget.equals("$500") ? "$400" : "$200") +
                "\nWorkers              " + workers +
                "\nDeadline              " + deadline,
                12, TEXT);
        info.setPadding(0, dp(12), 0, dp(7));
        c.addView(info);

        Button details = primary("View Details  →");
        details.setOnClickListener(v -> showJobDetails(name, budget));
        c.addView(details);

        setMargins(c, 0, 9, 0, 4);
        return c;
    }

    private void showJobDetails(String name, String budget) {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Job Details"));
        c.addView(small("Global work opportunity"));

        c.addView(wideInfo("🤖", name, "Remote • Global"));

        LinearLayout d = card();
        d.addView(tv(
                "Company Budget       " + budget +
                "\n\nWorker Pool            $400.00" +
                "\n\nViyzo Fee                 $100.00" +
                "\n\nRecommended Workers   8" +
                "\n\nWorkload per Worker   125 products" +
                "\n\nDeadline                   3 Days",
                13, TEXT));
        c.addView(d);
        setMargins(d, 0, 12, 0, 12);

        TextView req = tv("Requirements", 18, WHITE);
        req.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        c.addView(req);

        c.addView(wideInfo("✓", "Internet", "Good connection"));
        c.addView(wideInfo("✓", "Skills", "Basic mobile/computer skills"));
        c.addView(wideInfo("✓", "Quality", "Accuracy and quality"));

        Button apply = primary("🚀  Apply Now");
        apply.setOnClickListener(v -> Toast.makeText(
                this, "Application saved. Backend connection comes next.",
                Toast.LENGTH_SHORT).show());
        c.addView(apply);
    }

    // ============================================================
    // MY JOBS / EARNINGS / PROFILE
    // ============================================================

    private void showMyJobs() {
        startScreen(true);
        LinearLayout c = content();

        c.addView(heading("My Jobs"));
        c.addView(small("Work currently assigned to your account."));

        LinearLayout active = card();
        active.addView(wideInfo("💼", "Product Listing", "In Progress • 12%"));

        active.addView(tv("125 / 1000 products", 13, TEXT));
        active.addView(tv("Deadline: 3 Days", 11, GRAY));

        Button open = primary("Open Work");
        open.setOnClickListener(v -> Toast.makeText(
                this, "Work execution screen will connect to job backend.",
                Toast.LENGTH_SHORT).show());
        active.addView(open);

        c.addView(active);
    }

    private void showEarnings() {
        startScreen(true);
        LinearLayout c = content();

        c.addView(heading("Earnings"));

        LinearLayout balance = column();
        balance.setPadding(dp(20), dp(20), dp(20), dp(20));
        balance.setBackground(primaryBg());
        balance.setElevation(dp(8));

        balance.addView(tv("Available Balance", 12, WHITE));
        TextView money = tv("$0.00", 32, WHITE);
        money.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        balance.addView(money);

        Button withdraw = primary("Withdraw");
        withdraw.setBackground(solid(WHITE, 20));
        withdraw.setTextColor(PRIMARY);
        withdraw.setOnClickListener(v -> Toast.makeText(
                this, "Withdrawal will connect to a country/payment provider.",
                Toast.LENGTH_SHORT).show());
        balance.addView(withdraw);

        c.addView(balance);
        setMargins(balance, 0, 0, 0, 12);

        LinearLayout r = row();
        LinearLayout total = statCard("💵", "Total Earned", "$0.00");
        LinearLayout pending = statCard("⏳", "Pending", "$0.00");

        r.addView(total, new LinearLayout.LayoutParams(0, dp(125), 1));
        LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(0, dp(125), 1);
        pp.setMargins(dp(8), 0, 0, 0);
        r.addView(pending, pp);
        c.addView(r);

        c.addView(wideInfo("📈", "Transactions", "No transactions yet"));
        c.addView(secondary("💳  Payment Methods"));
        c.addView(secondary("📋  Transaction History"));
    }

    private void showProfile() {
        startScreen(true);
        LinearLayout c = content();

        c.addView(heading("My Profile"));

        LinearLayout profile = card();
        LinearLayout pr = row();

        TextView av = tv("👤", 34, WHITE);
        av.setGravity(Gravity.CENTER);
        av.setBackground(outlined(CARD2, BORDER, 35));
        pr.addView(av, new LinearLayout.LayoutParams(dp(68), dp(68)));

        LinearLayout pd = column();
        TextView name = tv("Ashikur Rahman", 17, WHITE);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        pd.addView(name);
        pd.addView(tv("Worker ID: VZ388742", 11, GRAY));
        pd.addView(tv("● Basic Worker", 11, GREEN));

        pr.addView(pd, new LinearLayout.LayoutParams(0, dp(68), 1));
        profile.addView(pr);
        c.addView(profile);

        c.addView(wideInfo("🛡", "Verification Status",
                "Basic Verification • Not completed"));

        Button verify = secondary("🛡  Worker Verification");
        verify.setOnClickListener(v -> showWorkerVerification());
        c.addView(verify);

        Button settings = secondary("⚙  Account Settings");
        settings.setOnClickListener(v -> showSettings());
        c.addView(settings);

        Button help = secondary("❓  Help & Support");
        help.setOnClickListener(v -> showHelp());
        c.addView(help);

        Button about = secondary("ⓘ  About Viyzo");
        about.setOnClickListener(v -> showAbout());
        c.addView(about);

        Button logout = primary("Logout");
        logout.setBackground(solid(RED, 20));
        logout.setOnClickListener(v -> showHome());
        c.addView(logout);
    }

    // ============================================================
    // WORKER VERIFICATION
    // ============================================================

    private void showWorkerVerification() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Worker Verification"));
        c.addView(small("Complete verification to unlock more work and higher-value jobs."));

        c.addView(wideInfo("1", "Country", "Select your country"));
        c.addView(wideInfo("2", "Basic Verification",
                "Name, DOB, phone, email and selfie"));
        c.addView(wideInfo("3", "Identity Document",
                "Country/provider requirements may vary"));
        c.addView(wideInfo("4", "Consent",
                "Verification and privacy consent"));

        Button start = primary("Start Verification");
        start.setOnClickListener(v -> showBasicVerification());
        c.addView(start);

        Button noDoc = secondary("I Don't Have a Document");
        noDoc.setOnClickListener(v -> showNoDocument());
        c.addView(noDoc);
    }

    private void showBasicVerification() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Basic Verification"));
        c.addView(small("Only provide information needed for verification."));

        c.addView(input("Full Name"));
        c.addView(input("Date of Birth"));
        c.addView(input("Phone Number"));
        c.addView(input("Email Address"));

        Button otp = secondary("📱  Send Phone OTP");
        otp.setOnClickListener(v -> Toast.makeText(
                this, "OTP will connect to secure backend.",
                Toast.LENGTH_SHORT).show());
        c.addView(otp);

        Button selfie = secondary("📷  Take Live Selfie");
        selfie.setOnClickListener(v -> Toast.makeText(
                this, "Selfie verification will connect to verification service.",
                Toast.LENGTH_SHORT).show());
        c.addView(selfie);

        CheckBox consent = new CheckBox(this);
        consent.setText("I confirm this information is mine and agree to verification/privacy terms.");
        consent.setTextColor(TEXT);
        consent.setTextSize(12);
        c.addView(consent);

        Button next = primary("Continue Verification");
        next.setOnClickListener(v -> {
            if (!consent.isChecked()) {
                Toast.makeText(this,
                        "Please accept verification consent.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            showDocumentOptions();
        });
        c.addView(next);
    }

    private void showDocumentOptions() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Identity Document"));
        c.addView(small("Accepted documents depend on country and verification provider."));

        Spinner country = new Spinner(this);
        String[] countries = {
                "Select Country", "India", "United States",
                "United Kingdom", "Bangladesh", "UAE", "Other"
        };
        country.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                countries));
        c.addView(country);

        c.addView(wideInfo("🪪", "Government ID",
                "Use an accepted document when required."));

        Button upload = primary("📷  Upload / Capture Document");
        upload.setOnClickListener(v -> Toast.makeText(
                this, "Secure document upload will connect to backend.",
                Toast.LENGTH_SHORT).show());
        c.addView(upload);

        Button skip = secondary("Continue Without Document");
        skip.setOnClickListener(v -> showNoDocument());
        c.addView(skip);
    }

    private void showNoDocument() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Basic Worker Account"));
        c.addView(small("You can start with basic access where permitted."));

        c.addView(wideInfo("✓", "Account Created",
                "Basic account access"));
        c.addView(wideInfo("🛡", "Basic Verification",
                "Some jobs can remain available"));
        c.addView(wideInfo("🔒", "Higher Verification",
                "Some high-value jobs or payouts may require stronger verification"));

        Button done = primary("Continue to Dashboard");
        done.setOnClickListener(v -> showDashboard());
        c.addView(done);
    }

    // ============================================================
    // COMPANY PORTAL — GLOBAL JOB POSTING FOUNDATION
    // ============================================================

    private void showCompanyPortal() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(brandHeader());
        c.addView(heading("Company Work Portal"));
        c.addView(small(
                "Small or large companies can create work for workers anywhere in the world."
        ));

        LinearLayout hero = card();
        hero.setBackground(primaryBg());
        hero.addView(tv("🌍  Global Company Jobs", 19, WHITE));
        hero.addView(tv(
                "Post remote work, choose a country/currency, set workload and budget, and let the AI Manager recommend workers.",
                12, WHITE));
        c.addView(hero);
        setMargins(hero, 0, 0, 0, 13);

        Button post = primary("＋  Post New Work");
        post.setOnClickListener(v -> showPostWork());
        c.addView(post);

        Button companyLogin = secondary("🏢  Company Login / Dashboard");
        companyLogin.setOnClickListener(v -> showCompanyDashboard());
        c.addView(companyLogin);

        Button back = secondary("← Back to Worker App");
        back.setOnClickListener(v -> showHome());
        c.addView(back);
    }

    private void showCompanyDashboard() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Company Dashboard"));
        c.addView(small("Manage global work and view job status."));

        LinearLayout r1 = row();
        r1.addView(statCard("📋", "Open Jobs", "0"),
                new LinearLayout.LayoutParams(0, dp(120), 1));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, dp(120), 1);
        p.setMargins(dp(8), 0, 0, 0);
        r1.addView(statCard("👥", "Workers", "0"), p);
        c.addView(r1);

        c.addView(wideInfo("💵", "Company Spending", "$0.00"));
        c.addView(wideInfo("🌍", "Global Reach", "Countries can be configured"));

        Button post = primary("＋  Post New Work");
        post.setOnClickListener(v -> showPostWork());
        c.addView(post);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showCompanyPortal());
        c.addView(back);
    }

    private void showPostWork() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Post New Work"));
        c.addView(small("This form is designed for global companies of any size."));

        c.addView(input("Company Name"));
        c.addView(input("Job Title / Work Name"));
        c.addView(input("Job Description"));

        Spinner category = new Spinner(this);
        String[] categories = {
                "Select Category",
                "Product Listing",
                "Data Entry",
                "Content Review",
                "Translation",
                "Research",
                "Image / Media Work",
                "Customer Support",
                "Other"
        };
        category.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories));
        c.addView(category);

        Spinner companySize = new Spinner(this);
        String[] sizes = {
                "Company Size",
                "Small Business",
                "Startup",
                "Medium Business",
                "Enterprise",
                "Agency / Other"
        };
        companySize.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sizes));
        c.addView(companySize);

        Spinner country = new Spinner(this);
        String[] countries = {
                "Worker Location",
                "Worldwide",
                "India",
                "United States",
                "United Kingdom",
                "Europe",
                "Middle East",
                "Asia",
                "Africa",
                "Other / Multiple Countries"
        };
        country.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                countries));
        c.addView(country);

        Spinner currency = new Spinner(this);
        String[] currencies = {
                "Currency",
                "USD", "EUR", "GBP", "INR", "AED", "BDT", "Other"
        };
        currency.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                currencies));
        c.addView(currency);

        c.addView(input("Company Budget"));
        c.addView(input("Number of Workers Needed"));
        c.addView(input("Workload / Quantity"));
        c.addView(input("Deadline"));

        CheckBox remote = new CheckBox(this);
        remote.setText("Remote / Online Work");
        remote.setChecked(true);
        remote.setTextColor(TEXT);
        c.addView(remote);

        CheckBox agree = new CheckBox(this);
        agree.setText("I confirm that this work and payment information is accurate.");
        agree.setTextColor(TEXT);
        c.addView(agree);

        Button post = primary("🌍  Submit Work to Viyzo");
        post.setOnClickListener(v -> {
            if (!agree.isChecked()) {
                Toast.makeText(this,
                        "Please confirm the work information.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this,
                    "Work saved locally. Secure global job backend will receive it when connected.",
                    Toast.LENGTH_LONG).show();

            showCompanyDashboard();
        });
        c.addView(post);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showCompanyPortal());
        c.addView(back);
    }

    // ============================================================
    // SETTINGS / NOTIFICATIONS / LANGUAGE / HELP / ABOUT
    // ============================================================

    private void showNotifications() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Notifications"));

        c.addView(wideInfo("🤖", "AI Master Manager",
                "A new Product Listing job is available."));
        c.addView(wideInfo("💰", "Earnings",
                "Complete jobs to start earning."));
        c.addView(wideInfo("🛡", "Verification",
                "Complete Worker Verification for more access."));

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showDashboard());
        c.addView(back);
    }

    private void showLanguage() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Language"));

        String[] languages = {
                "🇬🇧  English", "🇮🇳  हिन्दी", "🇧🇩  বাংলা",
                "🇵🇰  اردو", "🇸🇦  العربية", "🇹🇷  Türkçe"
        };

        for (String lang : languages) {
            Button b = secondary(lang);
            b.setOnClickListener(v -> Toast.makeText(
                    this, "Selected: " + lang, Toast.LENGTH_SHORT).show());
            c.addView(b);
        }
    }

    private void showSettings() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Settings"));

        Button notifications = secondary("🔔  Notification Settings");
        c.addView(notifications);

        Button language = secondary("🌐  Language");
        language.setOnClickListener(v -> showLanguage());
        c.addView(language);

        c.addView(secondary("🔐  Privacy"));
        c.addView(secondary("💳  Payment Settings"));

        Button help = secondary("❓  Help & Support");
        help.setOnClickListener(v -> showHelp());
        c.addView(help);

        Button about = secondary("ⓘ  About Viyzo");
        about.setOnClickListener(v -> showAbout());
        c.addView(about);

        Button logout = primary("Logout");
        logout.setBackground(solid(RED, 20));
        logout.setOnClickListener(v -> showHome());
        c.addView(logout);
    }

    private void showHelp() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Help & Support"));
        c.addView(wideInfo("💬", "Worker Support",
                "Help with jobs, accounts and verification."));
        c.addView(wideInfo("🏢", "Company Support",
                "Help with job posting and company accounts."));
        c.addView(wideInfo("📚", "Help Center",
                "Learn how Viyzo works globally."));

        Button contact = primary("Contact Support");
        contact.setOnClickListener(v -> Toast.makeText(
                this, "Support will connect to backend.", Toast.LENGTH_SHORT).show());
        c.addView(contact);
    }

    private void showAbout() {
        startScreen(false);
        LinearLayout c = content();

        LinearLayout center = column();
        center.setGravity(Gravity.CENTER_HORIZONTAL);
        center.addView(logo(35), new LinearLayout.LayoutParams(dp(90), dp(90)));

        TextView name = tv("VIYZO", 30, WHITE);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        center.addView(name);

        TextView network = tv("Global Work Network", 12, GRAY);
        network.setGravity(Gravity.CENTER);
        center.addView(network);

        c.addView(center);

        c.addView(wideInfo("🤖", "AI Master Manager",
                "Smart job distribution"));
        c.addView(wideInfo("🌍", "Global Companies",
                "Small and large companies can post work"));
        c.addView(wideInfo("👥", "Global Workers",
                "Workers can discover suitable jobs"));
        c.addView(wideInfo("🛡", "Verification",
                "Verification levels can be applied to access and payouts"));

        TextView v = tv("Version 1.0.0", 11, GRAY);
        v.setGravity(Gravity.CENTER);
        c.addView(v);
    }
}
