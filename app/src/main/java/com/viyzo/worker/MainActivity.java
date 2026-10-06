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

        signup.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Create Account coming next",
                        Toast.LENGTH_SHORT
                ).show()
        );
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

            Toast.makeText(
                    this,
                    "Login successful (Demo)",
                    Toast.LENGTH_SHORT
            ).show();
        });

        back.setOnClickListener(v -> showHome());
    }
}
