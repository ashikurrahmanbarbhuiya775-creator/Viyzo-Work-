package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(16, 16, 20);
    private final int CARD = Color.rgb(28, 28, 36);
    private final int WHITE = Color.WHITE;
    private final int GRAY = Color.rgb(180, 180, 190);
    private final int GREEN = Color.rgb(40, 200, 120);
    private final int BLUE = Color.rgb(60, 130, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showWelcome();
    }

    private void showWelcome() {
        LinearLayout root = createRoot();

        addTitle(root, "🤖 Viyzo Worker", 28);
        addText(root, "AI Managed Global Work Platform", 17, GRAY);

        addSpace(root, 30);

        Button login = button("🔐 Login", BLUE);
        root.addView(login);
        login.setOnClickListener(v -> showLogin());

        addSpace(root, 12);

        Button signup = button("📝 Create Account", GREEN);
        root.addView(signup);
        signup.setOnClickListener(v -> showSignup());

        addSpace(root, 30);

        addText(
                root,
                "Global work platform for workers.\nFind jobs, complete work and track earnings.",
                15,
                GRAY
        );
    }

    private void showLogin() {
        LinearLayout root = createRoot();

        addTitle(root, "🔐 Login", 28);
        addText(root, "Login to your Viyzo Worker account", 16, GRAY);

        addSpace(root, 25);

        EditText email = input("Email or Mobile");
        root.addView(email);

        addSpace(root, 12);

        EditText password = input("Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        root.addView(password);

        addSpace(root, 20);

        Button login = button("Login", BLUE);
        root.addView(login);

        login.setOnClickListener(v -> {
            String user = email.getText().toString().trim();
            String pass = password.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                        this,
                        "Please enter email/mobile and password",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            showHome(user);
        });

        addSpace(root, 12);

        Button demo = button("⚡ Demo Login", GREEN);
        root.addView(demo);

        demo.setOnClickListener(v -> showHome("demo@viyzo.com"));

        addSpace(root, 20);

        Button back = button("← Back", CARD);
        root.addView(back);
        back.setOnClickListener(v -> showWelcome());
    }

    private void showSignup() {
        LinearLayout root = createRoot();

        addTitle(root, "📝 Create Account", 28);
        addText(root, "Create your Viyzo Worker profile", 16, GRAY);

        addSpace(root, 20);

        EditText name = input("Full Name");
        root.addView(name);

        addSpace(root, 10);

        EditText email = input("Email");
        root.addView(email);

        addSpace(root, 10);

        EditText mobile = input("Mobile Number");
        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
        root.addView(mobile);

        addSpace(root, 10);

        EditText password = input("Create Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        root.addView(password);

        addSpace(root, 20);

        Button create = button("Create Account", GREEN);
        root.addView(create);

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
                    "Account created in demo mode",
                    Toast.LENGTH_SHORT
            ).show();

            showHome(n);
        });

        addSpace(root, 12);

        Button back = button("← Back", CARD);
        root.addView(back);
        back.setOnClickListener(v -> showWelcome());
    }

    private void showHome(String userName) {
        LinearLayout root = createRoot();

        addTitle(root, "🤖 Viyzo Worker", 28);
        addText(root, "Welcome, " + userName, 17, GRAY);

        addSpace(root, 20);

        LinearLayout card = card();

        TextView cardTitle = text(
                "🌍 Global Work Dashboard",
                20,
                WHITE
        );
        cardTitle.setTypeface(null, Typeface.BOLD);
        card.addView(cardTitle);

        addSpace(card, 10);

        card.addView(text(
                "Find available work, manage your jobs and track your earnings.",
                15,
                GRAY
        ));

        root.addView(card);

        addSpace(root, 15);

        Button jobs = button("🔎 Available Jobs", BLUE);
        root.addView(jobs);
        jobs.setOnClickListener(v -> showJobs());

        addSpace(root, 10);

        Button myJobs = button("📋 My Jobs", CARD);
        root.addView(myJobs);
        myJobs.setOnClickListener(v -> showMyJobs());

        addSpace(root, 10);

        Button earnings = button("💰 Earnings", CARD);
        root.addView(earnings);
        earnings.setOnClickListener(v -> showEarnings());

        addSpace(root, 10);

        Button profile = button("👤 Profile", CARD);
        root.addView(profile);
        profile.setOnClickListener(v -> showProfile(userName));

        addSpace(root, 20);

        Button logout = button(
                "🚪 Logout",
                Color.rgb(150, 50, 50)
        );
        root.addView(logout);
        logout.setOnClickListener(v -> showWelcome());
    }

    private void showJobs() {
        LinearLayout root = createRoot();

        addTitle(root, "🔎 Available Jobs", 27);
        addText(
                root,
                "Jobs currently available for workers",
                16,
                GRAY
        );

        addSpace(root, 20);

        LinearLayout job = card();

        TextView title = text(
                "📦 Product Listing",
                21,
                WHITE
        );
        title.setTypeface(null, Typeface.BOLD);
        job.addView(title);

        addSpace(job, 8);

        job.addView(text(
                "Work: Add and organize product information",
                15,
                GRAY
        ));

        job.addView(text(
                "Workload: 125 products",
                15,
                GRAY
        ));

        job.addView(text(
                "Payment: $50.00",
                17,
                GREEN
        ));

        job.addView(text(
                "Deadline: 3 days",
                15,
                GRAY
        ));

        addSpace(job, 12);

        Button accept = button("✅ Accept Job", GREEN);
        job.addView(accept);

        accept.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Job accepted in demo mode",
                        Toast.LENGTH_SHORT
                ).show()
        );

        root.addView(job);

        addSpace(root, 20);

        Button back = button("← Back to Home", CARD);
        root.addView(back);
        back.setOnClickListener(v -> showWelcome());
    }

    private void showMyJobs() {
        LinearLayout root = createRoot();

        addTitle(root, "📋 My Jobs", 27);
        addText(
                root,
                "Your accepted and completed jobs",
                16,
                GRAY
        );

        addSpace(root, 20);

        LinearLayout card = card();

        card.addView(text(
                "📦 Product Listing",
                20,
                WHITE
        ));

        card.addView(text(
                "Status: No active job",
                15,
                GRAY
        ));

        card.addView(text(
                "Your accepted jobs will appear here.",
                15,
                GRAY
        ));

        root.addView(card);

        addSpace(root, 20);

        Button back = button("← Back to Home", CARD);
        root.addView(back);
        back.setOnClickListener(v -> showWelcome());
    }

    private void showEarnings() {
        LinearLayout root = createRoot();

        addTitle(root, "💰 Earnings", 27);
        addText(
                root,
                "Track your worker earnings",
                16,
                GRAY
        );

        addSpace(root, 20);

        LinearLayout card = card();

        TextView amount = text("$0.00", 32, GREEN);
        amount.setTypeface(null, Typeface.BOLD);
        amount.setGravity(Gravity.CENTER);
        card.addView(amount);

        addSpace(card, 8);

        TextView label = text(
                "Available Earnings",
                15,
                GRAY
        );
        label.setGravity(Gravity.CENTER);
        card.addView(label);

        root.addView(card);

        addSpace(root, 20);

        addText(
                root,
                "Minimum payout: $5.00\nReal payment and withdrawal system will be connected through a secure backend.",
                15,
                GRAY
        );

        addSpace(root, 20);

        Button back = button("← Back to Home", CARD);
        root.addView(back);
        back.setOnClickListener(v -> showWelcome());
    }

    private void showProfile(String userName) {
        LinearLayout root = createRoot();

        addTitle(root, "👤 Profile", 27);
        addText(root, "Worker account", 16, GRAY);

        addSpace(root, 20);

        LinearLayout card = card();

        card.addView(text(
                "Name: " + userName,
                18,
                WHITE
        ));

        card.addView(text(
                "Role: Worker",
                16,
                GRAY
        ));

        card.addView(text(
                "KYC Status: Not completed",
                16,
                GRAY
        ));

        card.addView(text(
                "Account Status: Demo",
                16,
                GRAY
        ));

        root.addView(card);

        addSpace(root, 20);

        Button kyc = button("🪪 Start KYC", BLUE);
        root.addView(kyc);

        kyc.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Secure KYC system will be connected later",
                        Toast.LENGTH_SHORT
                ).show()
        );

        addSpace(root, 10);

        Button back = button("← Back to Home", CARD);
        root.addView(back);
        back.setOnClickListener(v -> showWelcome());
    }

    private LinearLayout createRoot() {
        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.TOP);
        layout.setPadding(28, 45, 28, 35);
        layout.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(layout);

        setContentView(scroll);

        return layout;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(0, 6, 0, 6);

        return t;
    }

    private void addTitle(
            LinearLayout layout,
            String value,
            int size
    ) {
        TextView title = text(value, size, WHITE);

        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);
    }

    private void addText(
            LinearLayout layout,
            String value,
            int size,
            int color
    ) {
        TextView t = text(value, size, color);

        t.setGravity(Gravity.CENTER);
        layout.addView(t);
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(Color.rgb(130, 130, 140));
        e.setTextColor(WHITE);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(18, 12, 18, 12);
        e.setBackgroundColor(CARD);

        return e;
    }

    private Button button(
            String value,
            int backgroundColor
    ) {
        Button b = new Button(this);

        b.setText(value);
        b.setTextSize(16);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackgroundColor(backgroundColor);
        b.setMinHeight(55);

        return b;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);

        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(20, 20, 20, 20);
        c.setBackgroundColor(CARD);

        return c;
    }

    private void addSpace(
            LinearLayout layout,
            int height
    ) {
        Space space = new Space(this);

        layout.addView(
                space,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        height
                )
        );
    }
}
