package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout main;
    int bg = Color.rgb(15, 16, 22);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLogin();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setPadding(15, 12, 15, 12);
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
        main.setPadding(20, 30, 20, 30);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        main.setBackgroundColor(bg);

        scroll.addView(main);
        setContentView(scroll);
    }

    void showLogin() {
        base();

        TextView logo = text("🤖 Viyzo Worker", 32);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        main.addView(logo);

        TextView sub = text(
                "AI Managed Global Work Platform",
                16
        );
        sub.setGravity(Gravity.CENTER);
        sub.setTextColor(Color.LTGRAY);
        main.addView(sub);

        main.addView(text("\n🔐 Login", 26));

        EditText email = new EditText(this);
        email.setHint("Email or Mobile Number");
        email.setTextColor(Color.WHITE);
        email.setHintTextColor(Color.LTGRAY);
        main.addView(email);

        EditText password = new EditText(this);
        password.setHint("Password");
        password.setTextColor(Color.WHITE);
        password.setHintTextColor(Color.LTGRAY);
        password.setInputType(129);
        main.addView(password);

        Button login = button("🔐 Login");
        Button signup = button("📝 Create New Account");
        Button forgot = button("❓ Forgot Password");

        main.addView(login);
        main.addView(signup);
        main.addView(forgot);

        login.setOnClickListener(v -> {

            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(
                        this,
                        "Please enter Email/Mobile and Password",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    this,
                    "✅ Demo Login Successful",
                    Toast.LENGTH_SHORT
            ).show();

            showHome();
        });

        signup.setOnClickListener(v -> showSignup());

        forgot.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Password recovery will be connected later.",
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    void showSignup() {
        base();

        main.addView(text("📝 Create Viyzo Worker Account", 26));

        EditText name = new EditText(this);
        name.setHint("Full Name");
        name.setTextColor(Color.WHITE);
        name.setHintTextColor(Color.LTGRAY);
        main.addView(name);

        EditText email = new EditText(this);
        email.setHint("Email");
        email.setTextColor(Color.WHITE);
        email.setHintTextColor(Color.LTGRAY);
        main.addView(email);

        EditText mobile = new EditText(this);
        mobile.setHint("Mobile Number");
        mobile.setTextColor(Color.WHITE);
        mobile.setHintTextColor(Color.LTGRAY);
        main.addView(mobile);

        EditText password = new EditText(this);
        password.setHint("Create Password");
        password.setTextColor(Color.WHITE);
        password.setHintTextColor(Color.LTGRAY);
        password.setInputType(129);
        main.addView(password);

        Button create = button("✅ Create Account");
        Button back = button("⬅ Back to Login");

        main.addView(create);
        main.addView(back);

        create.setOnClickListener(v -> {

            if (name.getText().toString().trim().isEmpty() ||
                email.getText().toString().trim().isEmpty() ||
                mobile.getText().toString().trim().isEmpty() ||
                password.getText().toString().trim().isEmpty()) {

                Toast.makeText(
                        this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "✅ Demo Account Created Successfully",
                    Toast.LENGTH_LONG
            ).show();

            showLogin();
        });

        back.setOnClickListener(v -> showLogin());
    }

    void showHome() {
        base();

        main.addView(text("🏠 Viyzo Worker Home", 28));

        main.addView(text(
                "\nWelcome, Worker! 👋\n\n" +
                "🔎 Find Work\n" +
                "📋 Manage Jobs\n" +
                "💰 Track Earnings\n" +
                "👤 Manage Profile",
                19
        ));

        Button jobs = button("🔎 Available Jobs");
        Button myJobs = button("📋 My Jobs");
        Button earnings = button("💰 Earnings");
        Button profile = button("👤 Profile");
        Button logout = button("🚪 Logout");

        main.addView(jobs);
        main.addView(myJobs);
        main.addView(earnings);
        main.addView(profile);
        main.addView(logout);

        jobs.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Available Jobs screen coming next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        myJobs.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "My Jobs screen coming next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        earnings.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Earnings screen coming next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        profile.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Profile screen coming next.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        logout.setOnClickListener(v -> showLogin());
    }
}
