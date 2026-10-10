package com.viyzo.worker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Viyzo Worker - Android Java starter.
 *
 * Configure SUPABASE_PUBLISHABLE_KEY below with the project's PUBLIC publishable/anon key.
 * Never put a service_role or secret key in an Android app.
 *
 * Backend expectations:
 * - Supabase Auth enabled (email/password)
 * - public.worker_profiles: id uuid PK references auth.users(id), display_name text,
 *   country text, preferred_language text
 * - public.jobs: id uuid PK, title text, description text, category text, country text,
 *   payment_amount numeric, currency text, status text
 * - public.earnings: id uuid PK, worker_id uuid, job_id uuid, amount numeric,
 *   currency text, status text
 * - RLS policies must protect every table. Do not trust client-side payout/job status changes.
 *
 * This is a functional integration scaffold, not a finished production marketplace.
 * Live payments, KYC, company verification, job assignment, moderation and AI chat require
 * server-side endpoints/provider configuration and security review.
 */
public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final String SUPABASE_URL = "https://jxnfsqxyrakkblizziel.supabase.co";
    private static final String SUPABASE_PUBLISHABLE_KEY = "PASTE_YOUR_SUPABASE_PUBLISHABLE_KEY_HERE";

    private static final int C_BG = Color.rgb(16, 16, 20);
    private static final int C_CARD = Color.rgb(28, 28, 36);
    private static final int C_TEXT = Color.rgb(245, 245, 250);
    private static final int C_MUTED = Color.rgb(170, 174, 190);
    private static final int C_ACCENT = Color.rgb(115, 96, 255);
    private static final int VOICE_REQUEST = 7021;
    private static final int MIC_PERMISSION = 7022;
    private static final String PREFS = "viyzo_worker_prefs";

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private LinearLayout root;
    private TextToSpeech tts;
    private boolean ttsReady;
    private Locale voiceLocale = Locale.US;
    private String accessToken = "";
    private String userId = "";
    private String userEmail = "";
    private String displayName = "";
    private String country = "India";
    private String language = "English";
    private TextView statusView;
    private TextView aiAnswerView;
    private EditText aiQuestion;
    private boolean busy;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(C_BG);
        getWindow().setNavigationBarColor(C_BG);
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        accessToken = p.getString("access_token", "");
        userId = p.getString("user_id", "");
        userEmail = p.getString("email", "");
        displayName = p.getString("name", "");
        country = p.getString("country", "India");
        language = p.getString("language", "English");
        tts = new TextToSpeech(this, this);
        if (!accessToken.isEmpty() && !userId.isEmpty()) showDashboard();
        else showWelcome();
    }

    @Override public void onInit(int result) {
        if (result == TextToSpeech.SUCCESS) {
            int status = tts.setLanguage(voiceLocale);
            if (status == TextToSpeech.LANG_MISSING_DATA || status == TextToSpeech.LANG_NOT_SUPPORTED) {
                voiceLocale = Locale.US;
                status = tts.setLanguage(voiceLocale);
            }
            ttsReady = status != TextToSpeech.LANG_MISSING_DATA && status != TextToSpeech.LANG_NOT_SUPPORTED;
        }
    }

    private void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (tts != null && ttsReady) {
            if (Build.VERSION.SDK_INT >= 21) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "viyzo_" + System.currentTimeMillis());
            else tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else toast("Voice data is not available. Install Android Text-to-Speech voice data in phone settings.");
    }

    private void setScreen(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(18));
        root.setBackgroundColor(C_BG);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        setContentView(scroll);
        TextView brand = text("🤖 VIYZO WORKER", 23, C_TEXT, true);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, matchWrap());
        TextView subtitle = text(title, 15, C_MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(4), 0, dp(18));
        root.addView(subtitle, matchWrap());
    }

    private void showWelcome() {
        setScreen("Global work platform");
        cardText("Find work, manage your profile, and get guided by AI Master.");
        addButton("Create Worker Account", () -> showSignup());
        addButton("Login", () -> showLogin());
        addButton("Company Portal", () -> showCompanyPortal());
        addButton("AI Master", () -> showAI());
        addButton("Country & Language", () -> showSettings());
        addStatus("Demo job examples are not paid work. Real account access requires Supabase setup.");
    }

    private void showSignup() {
        setScreen("Create worker account");
        EditText name = field("Full name");
        EditText email = field("Email address");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText phone = field("Phone (optional)");
        EditText pass = field("Create password (at least 6 characters)");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        CheckBox terms = new CheckBox(this);
        terms.setText("I agree to the Terms and Privacy Policy");
        terms.setTextColor(C_TEXT);
        root.addView(terms, matchWrap());
        addButton("Create Account", () -> {
            String n = value(name), e = value(email), pw = value(pass);
            if (n.isEmpty() || e.isEmpty() || pw.length() < 6 || !terms.isChecked()) {
                toast("Enter name, valid email, password (6+ characters), and accept terms.");
                return;
            }
            displayName = n;
            userEmail = e;
            JSONObject body = new JSONObject();
            try {
                body.put("email", e);
                body.put("password", pw);
                JSONObject data = new JSONObject();
                data.put("display_name", n);
                data.put("phone", value(phone));
                data.put("country", country);
                data.put("preferred_language", language);
                body.put("data", data);
            } catch (Exception ignored) {}
            request("POST", "/auth/v1/signup", body, false, (code, response) -> {
                if (code >= 200 && code < 300) {
                    JSONObject j = parse(response);
                    JSONObject user = j.optJSONObject("user");
                    if (user == null) user = j;
                    String id = user.optString("id", "");
                    String token = j.optString("access_token", "");
                    if (!id.isEmpty() && !token.isEmpty()) {
                        userId = id; accessToken = token;
                        saveSession();
                        upsertProfileThenDashboard();
                    } else {
                        toast("Signup submitted. If email confirmation is enabled, confirm your email, then log in.");
                        showLogin();
                    }
                } else toast("Signup failed (" + code + "): " + friendlyError(response));
            });
        });
        addButton("Back", () -> showWelcome());
    }

    private void showLogin() {
        setScreen("Login securely");
        EditText email = field("Email address");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pass = field("Password");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        addButton("Login", () -> {
            String e = value(email), pw = value(pass);
            if (e.isEmpty() || pw.isEmpty()) { toast("Enter email and password."); return; }
            JSONObject body = new JSONObject();
            try { body.put("email", e); body.put("password", pw); } catch (Exception ignored) {}
            request("POST", "/auth/v1/token?grant_type=password", body, false, (code, response) -> {
                if (code >= 200 && code < 300) {
                    JSONObject j = parse(response);
                    JSONObject user = j.optJSONObject("user");
                    if (user == null) user = new JSONObject();
                    accessToken = j.optString("access_token", "");
                    userId = user.optString("id", "");
                    userEmail = e;
                    if (accessToken.isEmpty() || userId.isEmpty()) {
                        toast("Login response incomplete. Check Supabase Auth settings.");
                        return;
                    }
                    saveSession();
                    showDashboard();
                } else toast("Login failed (" + code + "): " + friendlyError(response));
            });
        });
        addButton("Create account", () -> showSignup());
        addButton("Back", () -> showWelcome());
    }

    private void upsertProfileThenDashboard() {
        JSONObject profile = new JSONObject();
        try {
            profile.put("id", userId);
            profile.put("display_name", displayName);
            profile.put("country", country);
            profile.put("preferred_language", language);
        } catch (Exception ignored) {}
        request("POST", "/rest/v1/worker_profiles?on_conflict=id", profile, true, (code, response) -> {
            if (code < 200 || code >= 300) {
                toast("Account created, but profile setup needs checking (" + code + "). Check worker_profiles RLS/table columns.");
            }
            showDashboard();
        }, "resolution=merge-duplicates,return=minimal");
    }

    private void showDashboard() {
        setScreen("Your worker dashboard");
        cardText("Welcome, " + (displayName.isEmpty() ? userEmail : displayName) + "\n" + country + " • " + language);
        addButton("Available Jobs", () -> loadJobs());
        addButton("My Earnings", () -> loadEarnings());
        addButton("AI Master — ask by voice or text", () -> showAI());
        addButton("Worker Profile", () -> showProfile());
        addButton("Company Portal", () -> showCompanyPortal());
        addButton("Country & Language", () -> showSettings());
        addButton("Logout", () -> {
            accessToken = ""; userId = ""; userEmail = ""; displayName = "";
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
            showWelcome();
        });
        addStatus("Live jobs and balances appear only when the Supabase tables and Row Level Security policies are configured.");
    }

    private void loadJobs() {
        if (!requireLogin()) return;
        setScreen("Available jobs");
        addButton("Refresh jobs", () -> loadJobs());
        addButton("Back to dashboard", () -> showDashboard());
        addStatus("Loading open jobs…");
        request("GET", "/rest/v1/jobs?select=id,title,description,category,country,payment_amount,currency,status&status=eq.open&order=created_at.desc&limit=50",
                null, true, (code, response) -> {
                    if (code < 200 || code >= 300) { addStatus("Could not load jobs (" + code + "): " + friendlyError(response)); return; }
                    JSONArray arr;
                    try { arr = new JSONArray(response); } catch (Exception e) { addStatus("Unexpected jobs response."); return; }
                    if (arr.length() == 0) { addStatus("No open jobs are currently listed."); return; }
                    for (int i=0; i<arr.length(); i++) {
                        JSONObject j = arr.optJSONObject(i);
                        if (j == null) continue;
                        String title = j.optString("title", "Untitled job");
                        String desc = j.optString("description", "");
                        String pay = j.optString("currency", "USD") + " " + j.optString("payment_amount", "0");
                        cardText(title + "\n" + desc + "\nPayment: " + pay + "\nStatus: " + j.optString("status", "open")
                                + "\n\nJob listings are read-only here. Acceptance/assignment must be implemented securely on the server.");
                    }
                });
    }

    private void loadEarnings() {
        if (!requireLogin()) return;
        setScreen("My earnings");
        addButton("Back to dashboard", () -> showDashboard());
        addStatus("Loading your earnings…");
        request("GET", "/rest/v1/earnings?select=id,amount,currency,status,job_id&worker_id=eq." + userId + "&order=id.desc&limit=100",
                null, true, (code, response) -> {
                    if (code < 200 || code >= 300) { addStatus("Could not load earnings (" + code + "): " + friendlyError(response)); return; }
                    JSONArray arr;
                    try { arr = new JSONArray(response); } catch (Exception e) { addStatus("Unexpected earnings response."); return; }
                    double total = 0;
                    for (int i=0; i<arr.length(); i++) {
                        JSONObject j = arr.optJSONObject(i);
                        if (j == null) continue;
                        double amount = j.optDouble("amount", 0);
                        total += amount;
                        cardText(j.optString("currency", "") + " " + amount + " • " + j.optString("status", "pending"));
                    }
                    addStatus("Listed earnings total: " + String.format(Locale.US, "%.2f", total)
                            + "\nThis is a database display, not a confirmed withdrawable balance.");
                });
    }

    private void showProfile() {
        setScreen("Worker profile");
        cardText("Email: " + userEmail + "\nName: " + displayName + "\nCountry: " + country + "\nLanguage: " + language);
        addButton("Edit country/language", () -> showSettings());
        addButton("Back", () -> showDashboard());
        addStatus("Profile verification/KYC is not enabled in this starter. Do not collect identity documents until secure storage and review workflows are configured.");
    }

    private void showSettings() {
        setScreen("Country & language");
        EditText c = field("Country (for example, India)");
        c.setText(country);
        EditText l = field("Language (for example, Hindi, English, Bengali)");
        l.setText(language);
        addButton("Save", () -> {
            if (!value(c).trim().isEmpty()) country = value(c).trim();
            if (!value(l).trim().isEmpty()) language = value(l).trim();
            voiceLocale = localeFor(language);
            if (tts != null && ttsReady) {
                int result = tts.setLanguage(voiceLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    voiceLocale = Locale.US; tts.setLanguage(voiceLocale);
                    toast("Selected voice not installed; using available English voice.");
                }
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("country", country)
                    .putString("language", language).apply();
            if (!userId.isEmpty()) {
                JSONObject profile = new JSONObject();
                try { profile.put("id", userId); profile.put("display_name", displayName); profile.put("country", country); profile.put("preferred_language", language); } catch (Exception ignored) {}
                request("POST", "/rest/v1/worker_profiles?on_conflict=id", profile, true,
                        (code, response) -> { if (code >= 200 && code < 300) toast("Saved."); else toast("Saved on phone; profile sync needs RLS/table check."); },
                        "resolution=merge-duplicates,return=minimal");
            } else toast("Country and language saved.");
            showWelcomeOrDashboard();
        });
        addButton("Back", () -> showWelcomeOrDashboard());
    }

    private void showCompanyPortal() {
        setScreen("Company portal");
        cardText("Post real work only after company verification, budget funding, fraud checks and server-side approval are implemented.");
        EditText title = field("Job title");
        EditText description = field("Job description / deliverables");
        EditText category = field("Category");
        EditText budget = field("Budget amount (number)");
        EditText currency = field("Currency (e.g. USD, INR)");
        EditText jobCountry = field("Worker country (or Global)");
        addButton("Submit job request", () -> {
            if (value(title).trim().isEmpty() || value(description).trim().isEmpty() || value(budget).trim().isEmpty()) {
                toast("Enter job title, description and budget."); return;
            }
            toast("Company job posting is not enabled yet. A secure server endpoint must validate company identity, budget funding, and job status before publishing.");
        });
        addButton("Back", () -> showWelcomeOrDashboard());
        addStatus("The form is a planning UI only. It deliberately does not publish jobs or accept payments.");
    }

    private void showAI() {
        setScreen("AI Master");
        cardText("AI Master can speak and listen on this device. Built-in answers are guidance, not a connected generative AI service.");
        aiQuestion = field("Type your question");
        aiAnswerView = text("Ask about accounts, jobs, payments or verification.", 14, C_TEXT, false);
        aiAnswerView.setPadding(dp(12), dp(12), dp(12), dp(12));
        aiAnswerView.setBackground(background(C_CARD, 16));
        root.addView(aiAnswerView, matchWrap());
        addButton("Ask by text", () -> answerQuestion(value(aiQuestion)));
        addButton("🎙 Ask by microphone", () -> startVoiceInput());
        addButton("Speak welcome message", () -> speak("Welcome to Viyzo Worker. I can guide you through the app."));
        addButton("Back", () -> showWelcomeOrDashboard());
        addStatus("For real conversational AI, add a server-side AI endpoint. Never embed a private AI API key in this APK.");
    }

    private void answerQuestion(String q) {
        if (q == null || q.trim().isEmpty()) { toast("Type or speak a question first."); return; }
        String x = q.toLowerCase(Locale.ROOT);
        String answer;
        if (containsAny(x, "login", "signup", "account", "खाता", "अकाउंट", "लॉगिन")) {
            answer = "Create your account with your own email and a strong password, then confirm your email if Supabase asks. Never share OTPs or passwords.";
        } else if (containsAny(x, "job", "work", "काम", "जॉब")) {
            answer = "Open Available Jobs to see open listings from the connected database. Read the deliverables, deadline and payment carefully. Current app does not automatically assign jobs.";
        } else if (containsAny(x, "payment", "earning", "withdraw", "पैसा", "कमाई", "पेमेंट")) {
            answer = "Earnings are shown only if valid records exist. Real withdrawals require a verified payment provider, server-side balance checks and applicable legal compliance. No earnings are guaranteed.";
        } else if (containsAny(x, "kyc", "verify", "verification", "दस्तावेज", "पहचान")) {
            answer = "KYC is not active in this starter. Upload identity documents only after the app provides a clear purpose, secure upload, access controls and a privacy notice.";
        } else if (containsAny(x, "company", "client", "कंपनी")) {
            answer = "Companies must be verified, submit genuine work, fund the budget and pass server-side review before a job can be published.";
        } else {
            answer = "I can guide you about accounts, jobs, earnings, verification and the company portal. This built-in guide is not a full generative AI chat yet.";
        }
        if (aiAnswerView != null) aiAnswerView.setText("You: " + q + "\n\nAI Master: " + answer);
        speak(answer);
    }

    private void startVoiceInput() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION);
            toast("Allow microphone access, then tap the microphone button again.");
            return;
        }
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeFor(language).toLanguageTag());
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask AI Master");
            startActivityForResult(i, VOICE_REQUEST);
        } catch (Exception e) {
            toast("Speech recognition is not available on this phone. You can type instead.");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                if (aiQuestion != null) aiQuestion.setText(results.get(0));
                answerQuestion(results.get(0));
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_PERMISSION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            toast("Microphone allowed. Tap the microphone button again.");
        } else if (requestCode == MIC_PERMISSION) toast("Microphone permission denied; text input still works.");
    }

    private interface ApiCallback { void done(int code, String response); }

    private void request(String method, String path, JSONObject body, boolean auth, ApiCallback callback) {
        request(method, path, body, auth, callback, null);
    }

    private void request(String method, String path, JSONObject body, boolean auth, ApiCallback callback, String prefer) {
        if (SUPABASE_PUBLISHABLE_KEY.startsWith("PASTE_")) {
            toast("First add your Supabase public Publishable/anon key in MainActivity.java.");
            return;
        }
        if (busy) { toast("Please wait for the current request."); return; }
        busy = true;
        io.execute(() -> {
            HttpURLConnection conn = null;
            int code = 0;
            String response = "";
            try {
                URL url = new URL(SUPABASE_URL + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("apikey", SUPABASE_PUBLISHABLE_KEY);
                conn.setRequestProperty("Accept", "application/json");
                if (auth && !accessToken.isEmpty()) conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                else conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_PUBLISHABLE_KEY);
                if (prefer != null) conn.setRequestProperty("Prefer", prefer);
                if (body != null) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                    try (OutputStream out = conn.getOutputStream()) { out.write(bytes); }
                }
                code = conn.getResponseCode();
                InputStream stream = code >= 200 && code < 400 ? conn.getInputStream() : conn.getErrorStream();
                if (stream != null) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line; while ((line = br.readLine()) != null) sb.append(line);
                    }
                    response = sb.toString();
                }
            } catch (Exception e) {
                response = "{\"message\":\"" + safeJson(e.getMessage()) + "\"}";
            } finally {
                if (conn != null) conn.disconnect();
            }
            final int finalCode = code;
            final String finalResponse = response;
            runOnUiThread(() -> {
                busy = false;
                if (finalCode == 0) toast("Network request failed. Check internet and Supabase URL.");
                callback.done(finalCode, finalResponse);
            });
        });
    }

    private void saveSession() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("access_token", accessToken).putString("user_id", userId)
                .putString("email", userEmail).putString("name", displayName)
                .putString("country", country).putString("language", language).apply();
    }

    private boolean requireLogin() {
        if (accessToken.isEmpty() || userId.isEmpty()) {
            toast("Please login first.");
            showLogin();
            return false;
        }
        return true;
    }

    private void showWelcomeOrDashboard() {
        if (!accessToken.isEmpty() && !userId.isEmpty()) showDashboard(); else showWelcome();
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setTextColor(C_TEXT);
        e.setHintTextColor(C_MUTED);
        e.setHint(hint);
        e.setTextSize(15);
        e.setPadding(dp(14), dp(8), dp(14), dp(8));
        e.setBackground(background(C_CARD, 14));
        LinearLayout.LayoutParams p = matchWrap();
        p.setMargins(0, dp(5), 0, dp(5));
        e.setLayoutParams(p);
        root.addView(e);
        return e;
    }

    private void addButton(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(background(C_ACCENT, 14));
        LinearLayout.LayoutParams p = matchWrap();
        p.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(p);
        b.setOnClickListener(v -> action.run());
        root.addView(b);
    }

    private void cardText(String message) {
        TextView t = text(message, 14, C_TEXT, false);
        t.setPadding(dp(14), dp(14), dp(14), dp(14));
        t.setBackground(background(C_CARD, 15));
        LinearLayout.LayoutParams p = matchWrap();
        p.setMargins(0, dp(5), 0, dp(7));
        root.addView(t, p);
    }

    private void addStatus(String message) {
        TextView t = text(message, 12, C_MUTED, false);
        t.setPadding(dp(4), dp(8), dp(4), dp(8));
        root.addView(t, matchWrap());
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable background(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), Color.rgb(48, 48, 62));
        return d;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String value(EditText e) { return e == null || e.getText() == null ? "" : e.getText().toString().trim(); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private JSONObject parse(String s) {
        try { return new JSONObject(s); } catch (Exception e) { return new JSONObject(); }
    }

    private String friendlyError(String s) {
        JSONObject j = parse(s);
        String msg = j.optString("msg", j.optString("message", j.optString("error_description", s)));
        if (msg == null || msg.trim().isEmpty()) return "Unknown server error";
        return msg.length() > 220 ? msg.substring(0, 220) : msg;
    }

    private String safeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private Locale localeFor(String name) {
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (n.contains("hindi") || n.contains("हिंदी")) return new Locale("hi", "IN");
        if (n.contains("bengali") || n.contains("বাংলা")) return new Locale("bn", "IN");
        if (n.contains("urdu") || n.contains("اردو")) return new Locale("ur", "IN");
        if (n.contains("arabic") || n.contains("العربية")) return new Locale("ar");
        if (n.contains("turkish") || n.contains("türk")) return new Locale("tr");
        if (n.contains("spanish") || n.contains("español")) return new Locale("es");
        if (n.contains("french") || n.contains("français")) return Locale.FRENCH;
        if (n.contains("portuguese")) return new Locale("pt");
        if (n.contains("german")) return Locale.GERMAN;
        return Locale.US;
    }

    private boolean containsAny(String value, String... terms) {
        for (String t : terms) if (value.contains(t)) return true;
        return false;
    }

    @Override protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        io.shutdownNow();
        super.onDestroy();
    }
}
