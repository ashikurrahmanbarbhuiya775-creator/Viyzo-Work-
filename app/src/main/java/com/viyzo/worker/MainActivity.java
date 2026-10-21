package com.viyzo.worker;

import android.app.Activity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.util.Base64;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * VIYZO WORKER
 * Single-file Android UI foundation.
 *
 * IMPORTANT:
 * - This APK currently contains local/demo UI and calculation logic.
 * - Real accounts, OTP, selfie/document verification, company payments,
 *   job synchronization, payouts, AI services and secure backend APIs
 *   must be connected on the server side before production launch.
 * - Demo jobs are clearly labeled as examples.
 */
public class MainActivity extends Activity {

    // ============================================================
    // VIYZO BUSINESS CONFIGURATION
    // ============================================================
    // Configurable platform model: 50% worker pool / 50% Viyzo platform.
    private static final double WORKER_SHARE_PERCENT = 50.0;
    private static final double VIYZO_SHARE_PERCENT = 50.0;

    // Earning opportunity target, NOT a guaranteed income promise.
    private static final double DAILY_EARNING_TARGET_INR = 1000.0;
    private static final double TARGET_HOURS_PER_DAY = 10.0;
    private static final double TARGET_INR_PER_HOUR =
            DAILY_EARNING_TARGET_INR / TARGET_HOURS_PER_DAY;

    // ============================================================
    // THEME
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
    private final int ORANGE = Color.rgb(255, 145, 55);

    private LinearLayout root;
    private FrameLayout frame;

    private static final String SUPABASE_URL = "https://jxnfsqxyrakkblizziel.supabase.co";
    private static final String SUPABASE_PUBLISHABLE_KEY = "sb_publishable_skl_ZZCN1w0m6AaKYzBucQ_8AmS6Wvx";
    private static final String SESSION_PREFS = "viyzo_live_session";
    private volatile String accessToken = "";
    private volatile String userId = "";
    private volatile String userEmail = "";
    private volatile String workerName = "";

    private JSONObject apiRequest(String path, String method, JSONObject body, boolean authenticated, String prefer) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(SUPABASE_URL + path).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("apikey", SUPABASE_PUBLISHABLE_KEY);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        if (authenticated && !accessToken.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        if (prefer != null && !prefer.isEmpty()) connection.setRequestProperty("Prefer", prefer);
        if (body != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) { os.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
        }
        int code = connection.getResponseCode();
        InputStream stream = code >= 200 && code < 400 ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder text = new StringBuilder();
        if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) text.append(line);
        }
        connection.disconnect();
        String raw = text.toString().trim();
        JSONObject result = raw.startsWith("{") ? new JSONObject(raw) : new JSONObject().put("_raw", raw);
        if (code < 200 || code >= 300) { String message = result.optString("msg", result.optString("message", result.optString("error_description", result.optString("error", "Request failed")))); throw new Exception(message + " (HTTP " + code + ")"); }
        return result;
    }

    private JSONArray apiArray(String path) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(SUPABASE_URL + path).openConnection();
        connection.setRequestMethod("GET"); connection.setConnectTimeout(15000); connection.setReadTimeout(20000);
        connection.setRequestProperty("apikey", SUPABASE_PUBLISHABLE_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Accept", "application/json");
        int code = connection.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder text = new StringBuilder();
        if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) text.append(line);
        }
        connection.disconnect();
        if (code < 200 || code >= 300) throw new Exception("Could not load Viyzo data (HTTP " + code + ").");
        return new JSONArray(text.toString());
    }

    private static final String SESSION_KEY_ALIAS = "viyzo_worker_session_v1";
    private static final String ENCRYPTED_VALUE_PREFIX = "enc:";

    // Encrypt session values with an AES key held by Android Keystore.
    // Tokens are never written as plaintext to SharedPreferences.
    private SecretKey getOrCreateSessionKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        java.security.Key existing = keyStore.getKey(SESSION_KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(SESSION_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    private String encryptSessionValue(String value) throws Exception {
        if (value == null || value.isEmpty()) return "";
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSessionKey());
        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return ENCRYPTED_VALUE_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    private String decryptSessionValue(String stored) throws Exception {
        if (stored == null || stored.isEmpty()) return "";
        // Legacy plaintext values are migrated to encrypted storage on restore.
        if (!stored.startsWith(ENCRYPTED_VALUE_PREFIX)) return stored;
        byte[] combined = Base64.decode(stored.substring(ENCRYPTED_VALUE_PREFIX.length()), Base64.NO_WRAP);
        if (combined.length <= 12) throw new Exception("Stored session value is invalid");
        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[combined.length - iv.length];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSessionKey(), new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private void writeEncryptedSession() throws Exception {
        getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit()
                .putString("access_token", encryptSessionValue(accessToken))
                .putString("user_id", encryptSessionValue(userId))
                .putString("email", encryptSessionValue(userEmail))
                .putString("worker_name", encryptSessionValue(workerName))
                .apply();
    }

    private void saveSession(JSONObject auth) throws Exception {
        JSONObject user = auth.optJSONObject("user");
        if (user == null || auth.optString("access_token", "").isEmpty()) throw new Exception("Account needs email confirmation before login.");
        accessToken = auth.optString("access_token");
        userId = user.optString("id", "");
        userEmail = user.optString("email", "");
        JSONObject metadata = user.optJSONObject("user_metadata");
        workerName = metadata == null ? "" : metadata.optString("full_name", "");
        writeEncryptedSession();
    }

    private void restoreLiveSession() {
        SharedPreferences p = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        try {
            String storedToken = p.getString("access_token", "");
            String storedId = p.getString("user_id", "");
            String storedEmail = p.getString("email", "");
            String storedName = p.getString("worker_name", "");
            accessToken = decryptSessionValue(storedToken);
            userId = decryptSessionValue(storedId);
            userEmail = decryptSessionValue(storedEmail);
            workerName = decryptSessionValue(storedName);
            if (!accessToken.isEmpty() && (!storedToken.startsWith(ENCRYPTED_VALUE_PREFIX)
                    || !storedId.startsWith(ENCRYPTED_VALUE_PREFIX)
                    || !storedEmail.startsWith(ENCRYPTED_VALUE_PREFIX)
                    || !storedName.startsWith(ENCRYPTED_VALUE_PREFIX))) {
                writeEncryptedSession();
            }
        } catch (Exception sessionReadError) {
            // If the keystore key is unavailable or ciphertext is invalid, fail closed
            // and require sign-in again instead of keeping a broken session.
            clearLiveSession();
        }
    }

    private void clearLiveSession() {
        accessToken = ""; userId = ""; userEmail = ""; workerName = "";
        getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().clear().apply();
    }

    // AI Master voice assistant (on-device speech; real LLM answers need backend).
    private TextToSpeech aiTts;
    private boolean aiTtsReady = false;
    private Locale aiLocale = Locale.getDefault();
    private TextView aiStatusView;
    private TextView aiBubbleView;
    private boolean aiPanelCollapsed = false;
    private static final int AI_VOICE_REQUEST = 7401;
    private static final int AI_AUDIO_PERMISSION_REQUEST = 7402;
    private boolean aiWelcomeSpoken = false;
    private String pendingAISpeech = "";
    private boolean aiTtsInitializing = true;
    private String selectedCountryCode = "";
    private String selectedCountryName = "";
    private String selectedLanguageTag = "";
    private static final String PREFS = "viyzo_global_preferences";

    // ============================================================
    // DEMO JOB MODEL
    // ============================================================
    private static class JobData {
        String icon;
        String title;
        String category;
        String company;
        String budget;
        String quantity;
        String workers;
        String deadline;
        String urgency;
        String workType;
        String skill;
        String language;

        JobData(String icon, String title, String category, String company,
                String budget, String quantity, String workers,
                String deadline, String urgency, String workType,
                String skill, String language) {
            this.icon = icon;
            this.title = title;
            this.category = category;
            this.company = company;
            this.budget = budget;
            this.quantity = quantity;
            this.workers = workers;
            this.deadline = deadline;
            this.urgency = urgency;
            this.workType = workType;
            this.skill = skill;
            this.language = language;
        }
    }

    private final JobData[] DEMO_JOBS = new JobData[]{
            new JobData("🤖", "Product Listing", "E-commerce",
                    "Demo Global Company", "$500", "1000 products", "8",
                    "3 Days", "Normal", "Bulk", "Basic mobile/computer", "English"),
            new JobData("⚡", "Urgent Data Cleanup", "Data",
                    "Demo Business", "$300", "600 records", "6",
                    "24 Hours", "Urgent", "Quick Task", "Data entry", "English"),
            new JobData("📦", "Bulk Catalog Review", "E-commerce",
                    "Demo E-commerce Company", "$1000", "5000 items", "10",
                    "5 Days", "High", "Bulk", "Catalog review", "English/Hindi"),
            new JobData("🔁", "Recurring Content Check", "Content",
                    "Demo Agency", "$750", "1500 items/month", "5",
                    "Recurring", "Recurring", "Recurring", "Content review", "English"),
            new JobData("🌍", "Translation Task", "Language",
                    "Demo Global Client", "$400", "200 pages", "4",
                    "4 Days", "Normal", "Project", "Translation", "Multiple"),
            new JobData("📊", "Research & Data Entry", "Research",
                    "Demo Startup", "$650", "1200 records", "7",
                    "4 Days", "Normal", "Project", "Research/Data", "English")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(3, 8, 28));
        getWindow().setNavigationBarColor(Color.rgb(3, 8, 28));
        showBrandSplash();
        new Handler().postDelayed(this::continueStartup, 1400);
    }

    private void showBrandSplash() {
        android.widget.RelativeLayout splash = new android.widget.RelativeLayout(this);
        splash.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(2, 8, 32), Color.rgb(4, 24, 75), Color.rgb(1, 5, 20)}));
        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setGravity(Gravity.CENTER);
        brand.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView emblem = tv("V", 112, Color.rgb(0, 239, 255));
        emblem.setGravity(Gravity.CENTER);
        emblem.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        GradientDrawable emblemBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(0, 239, 255), Color.rgb(0, 91, 255), Color.rgb(23, 40, 150)});
        emblemBg.setShape(GradientDrawable.OVAL);
        emblemBg.setStroke(dp(3), Color.rgb(48, 175, 255));
        emblem.setBackground(emblemBg);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(dp(190), dp(190));
        ep.gravity = Gravity.CENTER;
        ep.bottomMargin = dp(28);
        brand.addView(emblem, ep);

        TextView name = tv("VIYZO", 42, Color.WHITE);
        name.setGravity(Gravity.CENTER);
        name.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        brand.addView(name, new LinearLayout.LayoutParams(-1, -2));

        TextView worker = tv("W O R K E R", 22, Color.rgb(0, 239, 255));
        worker.setGravity(Gravity.CENTER);
        worker.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-1, -2);
        wp.topMargin = dp(4);
        brand.addView(worker, wp);

        TextView tagline = tv("WORK WITH TRUST. GROW WITHOUT BORDERS.", 11, Color.rgb(190, 218, 255));
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = dp(24);
        brand.addView(tagline, tp);

        android.widget.RelativeLayout.LayoutParams bp = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        bp.addRule(android.widget.RelativeLayout.CENTER_IN_PARENT);
        splash.addView(brand, bp);
        setContentView(splash);
    }

    private void continueStartup() {
        if (isFinishing() || isDestroyed()) return;
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        selectedCountryCode = prefs.getString("country_code", "");
        selectedCountryName = prefs.getString("country_name", "");
        selectedLanguageTag = prefs.getString("language_tag", "");
        if (!selectedLanguageTag.isEmpty()) {
            aiLocale = Locale.forLanguageTag(selectedLanguageTag);
        }
        initAIMasterVoice();
        restoreLiveSession();
        if (selectedCountryCode.isEmpty() || selectedLanguageTag.isEmpty()) {
            showCountryLanguageSetup();
        } else {
            showHome();
            new Handler().postDelayed(() -> {
                if (!aiWelcomeSpoken) {
                    aiWelcomeSpoken = true;
                    speakAI(aiWelcomeMessage());
                }
            }, 1100);
        }
    }

    // ============================================================
    // BASIC UI HELPERS
    // ============================================================
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
                        LinearLayout.LayoutParams.WRAP_CONTENT);
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
                            LinearLayout.LayoutParams.MATCH_PARENT, 0);
            fp.weight = 1;
            root.addView(frame, fp);
            root.addView(bottomNav());
        } else {
            root.addView(frame, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }

        setContentView(root);
    }

    private LinearLayout content() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout c = column();
        c.setPadding(dp(18), dp(16), dp(18), dp(150));
        scroll.addView(c);

        frame.addView(scroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        addAIMasterOverlay();

        return c;
    }

    private TextView logo(int size) {
        TextView l = tv("V", size, WHITE);
        l.setGravity(Gravity.CENTER);
        l.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(125, 75, 255), Color.rgb(55, 75, 255)});
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
        attachButtonVoiceHelp(b);
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
        attachButtonVoiceHelp(b);
        return b;
    }

    // Spoken hints run before the existing click action; returning false preserves that action.
    private void attachButtonVoiceHelp(Button button) {
        button.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                String label = String.valueOf(button.getText()).trim();
                if (!label.isEmpty()) speakAI(buttonHelpText(label));
            }
            return false;
        });
    }

    private String buttonHelpText(String label) {
        String en = "You selected " + label + ". Read the information on this screen. If you need help, tap the AI Master panel or microphone and ask your question.";
        String hi = "आपने " + label + " चुना है। स्क्रीन पर दी गई जानकारी पढ़ें। मदद चाहिए तो AI Master पैनल या माइक दबाकर सवाल पूछें।";
        String bn = "আপনি " + label + " বেছে নিয়েছেন। স্ক্রিনের তথ্য পড়ুন। সাহায্যের জন্য AI Master প্যানেল বা মাইকে চাপ দিয়ে প্রশ্ন করুন।";
        String ur = "آپ نے " + label + " منتخب کیا ہے۔ اسکرین کی معلومات پڑھیں۔ مدد کے لیے AI Master پینل یا مائیک دباکر سوال پوچھیں۔";
        String ar = "لقد اخترت " + label + ". اقرأ المعلومات على الشاشة. للمساعدة اضغط على لوحة AI Master أو الميكروفون واسأل سؤالك.";
        String tr = label + " seçeneğini seçtiniz. Ekrandaki bilgileri okuyun. Yardım için AI Master paneline veya mikrofona dokunup sorun.";
        String lang = aiLocale == null ? "en" : aiLocale.getLanguage();
        if ("hi".equals(lang)) return hi;
        if ("bn".equals(lang)) return bn;
        if ("ur".equals(lang)) return ur;
        if ("ar".equals(lang)) return ar;
        if ("tr".equals(lang)) return tr;
        return en;
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

        c.addView(tv(title, 11, GRAY));

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

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                values));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        p.setMargins(0, dp(5), 0, dp(5));
        s.setLayoutParams(p);
        return s;
    }

    // ============================================================
    // BOTTOM NAVIGATION
    // ============================================================
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

            if (i == 0) item.setBackground(solid(PRIMARY, 17));

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
    // FIRST-RUN GLOBAL COUNTRY + LANGUAGE SELECTOR
    // Uses Android's ISO country list and available locale list.
    // Voice availability depends on installed Android speech data.
    // ============================================================
    private String flagForCountry(String code) {
        if (code == null || code.length() != 2) return "🌐";
        String upper = code.toUpperCase(Locale.ROOT);
        int first = upper.charAt(0) - 'A' + 0x1F1E6;
        int second = upper.charAt(1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }

    private String localeDisplayName(Locale locale) {
        String nativeName = locale.getDisplayLanguage(locale);
        if (nativeName == null || nativeName.trim().isEmpty()) nativeName = locale.getLanguage();
        String english = locale.getDisplayLanguage(Locale.ENGLISH);
        if (english != null && !english.equalsIgnoreCase(nativeName)) return nativeName + " — " + english;
        return nativeName;
    }

    private void showCountryLanguageSetup() {
        startScreen(false);
        LinearLayout c = content();
        c.setPadding(dp(20), dp(22), dp(20), dp(26));

        LinearLayout hero = column();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(12), dp(8), dp(12), dp(18));
        hero.addView(logo(38), new LinearLayout.LayoutParams(dp(76), dp(76)));
        TextView brand = tv("VIYZO", 30, WHITE);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);
        hero.addView(brand);
        TextView subtitle = tv("Global Work Network", 13, GRAY);
        subtitle.setGravity(Gravity.CENTER);
        hero.addView(subtitle);
        TextView title = heading("🌍 Choose your country first");
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, dp(6));
        hero.addView(title);
        TextView explain = tv("First select your country. Then Viyzo will show languages used in that country, with English always included.", 13, TEXT);
        explain.setGravity(Gravity.CENTER);
        explain.setPadding(dp(8), 0, dp(8), dp(12));
        hero.addView(explain);
        c.addView(hero);

        c.addView(tv("1. SELECT COUNTRY / देश चुनें", 13, GRAY));
        String[] iso = Locale.getISOCountries();
        java.util.Arrays.sort(iso, (a, b) -> new Locale("", a).getDisplayCountry(Locale.ENGLISH)
                .compareToIgnoreCase(new Locale("", b).getDisplayCountry(Locale.ENGLISH)));
        java.util.ArrayList<String> countryCodes = new java.util.ArrayList<>();
        java.util.ArrayList<String> countryLabels = new java.util.ArrayList<>();
        int suggestedCountry = 0;
        String deviceCountry = Locale.getDefault().getCountry();
        for (String countryCode : iso) {
            String countryName = new Locale("", countryCode).getDisplayCountry(Locale.ENGLISH);
            if (countryName == null || countryName.trim().isEmpty()) continue;
            countryCodes.add(countryCode);
            countryLabels.add(flagForCountry(countryCode) + "  " + countryName);
            if (countryCode.equalsIgnoreCase(deviceCountry)) suggestedCountry = countryCodes.size() - 1;
        }
        // Restore the worker's previously saved country when reopening setup.
        android.content.SharedPreferences savedPrefs = getSharedPreferences("ViyzoPrefs", MODE_PRIVATE);
        String savedCountryCode = savedPrefs.getString("country_code", "");
        if (!savedCountryCode.isEmpty()) {
            for (int i = 0; i < countryCodes.size(); i++) {
                if (countryCodes.get(i).equalsIgnoreCase(savedCountryCode)) {
                    suggestedCountry = i;
                    break;
                }
            }
        }
        Spinner countrySpinner = new Spinner(this);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryLabels);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);
        countrySpinner.setSelection(suggestedCountry);
        c.addView(countrySpinner, new LinearLayout.LayoutParams(-1, dp(56)));

        c.addView(tv("2. SELECT STATE / REGION", 13, GRAY));
        Spinner regionSpinner = new Spinner(this);
        c.addView(regionSpinner, new LinearLayout.LayoutParams(-1, dp(56)));
        final java.util.ArrayList<String>[] regionChoices = new java.util.ArrayList[]{new java.util.ArrayList<String>()};

        c.addView(tv("3. SELECT LANGUAGE / भाषा चुनें", 13, GRAY));
        Spinner languageSpinner = new Spinner(this);
        c.addView(languageSpinner, new LinearLayout.LayoutParams(-1, dp(56)));
        TextView helper = tv("Only languages associated with the selected country are listed. English is always available as a choice. Regional coverage can vary by language-data source.", 12, GRAY);
        helper.setPadding(0, dp(7), 0, dp(12));
        c.addView(helper);
        c.addView(wideInfo("🤖", "AI Master", "The selected language will be used for AI guidance when Android speech support is available."));

        final java.util.ArrayList<Locale>[] languageChoices = new java.util.ArrayList[]{new java.util.ArrayList<Locale>()};
        final boolean[] firstCountryCallback = {true};
        countrySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= countryCodes.size()) return;
                String code = countryCodes.get(position);
                regionChoices[0] = regionsForCountry(code);
                ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, regionChoices[0]);
                regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                regionSpinner.setAdapter(regionAdapter);
                String savedRegion = getSharedPreferences(PREFS, MODE_PRIVATE).getString("region_name", "");
                if (code.equalsIgnoreCase(getSharedPreferences(PREFS, MODE_PRIVATE).getString("country_code", ""))
                        && !savedRegion.isEmpty()) {
                    int ri = regionChoices[0].indexOf(savedRegion);
                    if (ri >= 0) regionSpinner.setSelection(ri);
                }
                languageChoices[0] = languagesForCountry(code);
                java.util.ArrayList<String> labels = new java.util.ArrayList<>();
                int selectedLanguage = 0;
                String currentLanguage = Locale.getDefault().getLanguage();
                String primaryLanguage = defaultLanguageForCountry(code).getLanguage();
                for (int i = 0; i < languageChoices[0].size(); i++) {
                    Locale loc = languageChoices[0].get(i);
                    labels.add(localeDisplayName(loc));
                    if (loc.getLanguage().equalsIgnoreCase(primaryLanguage)) selectedLanguage = i;
                    if (loc.getLanguage().equalsIgnoreCase(currentLanguage)) selectedLanguage = i;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                languageSpinner.setAdapter(adapter);
                if (!firstCountryCallback[0]) {
                    String savedCountry = getSharedPreferences(PREFS, MODE_PRIVATE).getString("country_code", "");
                    String savedTag = getSharedPreferences(PREFS, MODE_PRIVATE).getString("language_tag", "");
                    if (code.equalsIgnoreCase(savedCountry) && !savedTag.isEmpty()) {
                        for (int i = 0; i < languageChoices[0].size(); i++) {
                            if (languageChoices[0].get(i).getLanguage().equalsIgnoreCase(Locale.forLanguageTag(savedTag).getLanguage())) {
                                selectedLanguage = i; break;
                            }
                        }
                    }
                }
                firstCountryCallback[0] = false;
                languageSpinner.setSelection(selectedLanguage);
            }
        });

        Button continueButton = primary("Continue  →");
        continueButton.setOnClickListener(v -> {
            int countryIndex = countrySpinner.getSelectedItemPosition();
            int languageIndex = languageSpinner.getSelectedItemPosition();
            if (countryIndex < 0 || countryIndex >= countryCodes.size()
                    || languageIndex < 0 || languageIndex >= languageChoices[0].size()) {
                Toast.makeText(this, "Please select your country and language.", Toast.LENGTH_LONG).show();
                return;
            }
            selectedCountryCode = countryCodes.get(countryIndex);
            selectedCountryName = new Locale("", selectedCountryCode).getDisplayCountry(Locale.ENGLISH);
            String selectedRegion = regionSpinner.getSelectedItem() == null ? "Not specified" : String.valueOf(regionSpinner.getSelectedItem());
            Locale chosen = languageChoices[0].get(languageIndex);
            aiLocale = chosen;
            selectedLanguageTag = chosen.toLanguageTag();
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString("country_code", selectedCountryCode)
                    .putString("country_name", selectedCountryName)
                    .putString("region_name", selectedRegion)
                    .putString("language_tag", selectedLanguageTag)
                    .apply();
            setAILanguageByLocale(chosen);
            showHome();
            aiWelcomeSpoken = true;
            speakAI(aiText("Welcome to Viyzo. Your country and language are saved. I will guide you step by step.",
                    "Viyzo में आपका स्वागत है। आपका देश और भाषा सेव हो गए हैं। मैं आपको हर कदम पर समझाऊँगा।",
                    "Viyzo-তে স্বাগতম। আপনার দেশ ও ভাষা সংরক্ষণ করা হয়েছে। আমি প্রতিটি ধাপে সাহায্য করব।",
                    "Viyzo میں خوش آمدید۔ آپ کا ملک اور زبان محفوظ ہوگئے ہیں۔ میں ہر قدم پر رہنمائی کروں گا۔",
                    "مرحباً بك في Viyzo. تم حفظ بلدك ولغتك. سأرشدك خطوة بخطوة.",
                    "Viyzo'ya hoş geldiniz. Ülkeniz ve diliniz kaydedildi. Size adım adım rehberlik edeceğim."));
        });
        c.addView(continueButton);
        Button changeLater = secondary("I need to change country/language later");
        changeLater.setOnClickListener(v -> Toast.makeText(this, "You can change this from Settings → Country & Language.", Toast.LENGTH_LONG).show());
        c.addView(changeLater);
    }

    // Country-aware region selector. India includes every state and union territory;
    // other federations include major regions where practical and allow a general option.
    private java.util.ArrayList<String> regionsForCountry(String code) {
        java.util.ArrayList<String> regions = new java.util.ArrayList<>();
        regions.add("All regions / Not specified");
        switch (code.toUpperCase(Locale.ROOT)) {
            case "IN":
                regions.addAll(java.util.Arrays.asList(
                    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
                    "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"));
                break;
            case "US": regions.addAll(java.util.Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming", "District of Columbia", "Puerto Rico")); break;
            case "CA": regions.addAll(java.util.Arrays.asList("Alberta", "British Columbia", "Manitoba", "New Brunswick", "Newfoundland and Labrador", "Northwest Territories", "Nova Scotia", "Nunavut", "Ontario", "Prince Edward Island", "Quebec", "Saskatchewan", "Yukon")); break;
            case "AU": regions.addAll(java.util.Arrays.asList("New South Wales", "Queensland", "South Australia", "Tasmania", "Victoria", "Western Australia", "Australian Capital Territory", "Northern Territory")); break;
            case "PK": regions.addAll(java.util.Arrays.asList("Punjab", "Sindh", "Khyber Pakhtunkhwa", "Balochistan", "Islamabad Capital Territory", "Gilgit-Baltistan", "Azad Jammu and Kashmir")); break;
            case "BD": regions.addAll(java.util.Arrays.asList("Barisal", "Chattogram", "Dhaka", "Khulna", "Mymensingh", "Rajshahi", "Rangpur", "Sylhet")); break;
            case "GB": regions.addAll(java.util.Arrays.asList("England", "Scotland", "Wales", "Northern Ireland")); break;
            case "BR": regions.addAll(java.util.Arrays.asList("Acre", "Alagoas", "Amapá", "Amazonas", "Bahia", "Ceará", "Distrito Federal", "Espírito Santo", "Goiás", "Maranhão", "Mato Grosso", "Mato Grosso do Sul", "Minas Gerais", "Pará", "Paraíba", "Paraná", "Pernambuco", "Piauí", "Rio de Janeiro", "Rio Grande do Norte", "Rio Grande do Sul", "Rondônia", "Roraima", "Santa Catarina", "São Paulo", "Sergipe", "Tocantins")); break;
            case "MX": regions.addAll(java.util.Arrays.asList("Aguascalientes", "Baja California", "Baja California Sur", "Campeche", "Chiapas", "Chihuahua", "Ciudad de México", "Coahuila", "Colima", "Durango", "Guanajuato", "Guerrero", "Hidalgo", "Jalisco", "México", "Michoacán", "Morelos", "Nayarit", "Nuevo León", "Oaxaca", "Puebla", "Querétaro", "Quintana Roo", "San Luis Potosí", "Sinaloa", "Sonora", "Tabasco", "Tamaulipas", "Tlaxcala", "Veracruz", "Yucatán", "Zacatecas")); break;
            case "DE": regions.addAll(java.util.Arrays.asList("Baden-Württemberg", "Bavaria", "Berlin", "Brandenburg", "Bremen", "Hamburg", "Hesse", "Lower Saxony", "Mecklenburg-Vorpommern", "North Rhine-Westphalia", "Rhineland-Palatinate", "Saarland", "Saxony", "Saxony-Anhalt", "Schleswig-Holstein", "Thuringia")); break;
            case "CH": regions.addAll(java.util.Arrays.asList("Aargau", "Appenzell Ausserrhoden", "Appenzell Innerrhoden", "Basel-Landschaft", "Basel-Stadt", "Bern", "Fribourg", "Geneva", "Glarus", "Grisons", "Jura", "Lucerne", "Neuchâtel", "Nidwalden", "Obwalden", "Schaffhausen", "Schwyz", "Solothurn", "St. Gallen", "Thurgau", "Ticino", "Uri", "Valais", "Vaud", "Zug", "Zurich")); break;
            case "MY": regions.addAll(java.util.Arrays.asList("Johor", "Kedah", "Kelantan", "Kuala Lumpur", "Labuan", "Melaka", "Negeri Sembilan", "Pahang", "Penang", "Perak", "Perlis", "Putrajaya", "Sabah", "Sarawak", "Selangor", "Terengganu")); break;
            case "NP": regions.addAll(java.util.Arrays.asList("Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim")); break;
            case "ZA": regions.addAll(java.util.Arrays.asList("Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo", "Mpumalanga", "Northern Cape", "North West", "Western Cape")); break;
            default: regions.add("Other / local region"); break;
        }
        return regions;
    }

    // Country-filtered language choices. English is deliberately included for every country.
    // The device's CLDR/Java locale data supplements the curated official/widely-used languages.
    private java.util.ArrayList<Locale> languagesForCountry(String countryCode) {
        java.util.LinkedHashMap<String, Locale> found = new java.util.LinkedHashMap<>();
        addLanguage(found, "en", countryCode);
        String[] curated = curatedLanguagesForCountry(countryCode);
        for (String tag : curated) {
            Locale loc = Locale.forLanguageTag(tag);
            if (!loc.getLanguage().isEmpty()) addLanguage(found, loc.getLanguage(), countryCode);
        }
        for (Locale loc : Locale.getAvailableLocales()) {
            if (countryCode.equalsIgnoreCase(loc.getCountry()) && !loc.getLanguage().isEmpty()) {
                addLanguage(found, loc.getLanguage(), countryCode);
            }
        }
        Locale primary = defaultLanguageForCountry(countryCode);
        addLanguage(found, primary.getLanguage(), countryCode);
        java.util.ArrayList<Locale> result = new java.util.ArrayList<>(found.values());
        result.sort((a, b) -> {
            if (a.getLanguage().equals("en")) return b.getLanguage().equals("en") ? 0 : -1;
            if (b.getLanguage().equals("en")) return 1;
            return localeDisplayName(a).compareToIgnoreCase(localeDisplayName(b));
        });
        return result;
    }

    private void addLanguage(java.util.LinkedHashMap<String, Locale> found, String languageCode, String countryCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) return;
        String normalized = languageCode.toLowerCase(Locale.ROOT);
        Locale loc = new Locale(normalized, countryCode);
        found.put(normalized, loc);
    }

    private String[] curatedLanguagesForCountry(String code) {
        switch (code.toUpperCase(Locale.ROOT)) {
            case "IN": return new String[]{"as","bn","brx","doi","gu","hi","kn","ks","kok","mai","ml","mni","mr","ne","or","pa","sa","sat","sd","ta","te","ur"};
            case "US": return new String[]{"es","zh","tl","vi","ar","fr","ko","de","ru","pt","ja","hi"};
            case "GB": return new String[]{"cy","gd","ga","pl","ur","bn","pa","gu","ar"};
            case "CA": return new String[]{"fr","iu","cr","oj","zh","pa"};
            case "AU": return new String[]{"en","zh","ar","vi","it","el","yue"};
            case "NZ": return new String[]{"mi","sm","to","zh","hi"};
            case "BD": return new String[]{"bn","ccp","mni"};
            case "PK": return new String[]{"ur","pa","sd","ps","bal","skr","brh"};
            case "NP": return new String[]{"ne","mai","bho","new","dty","sat"};
            case "LK": return new String[]{"si","ta"};
            case "CN": return new String[]{"zh","ug","bo","ii","mn"};
            case "TW": return new String[]{"zh","nan","hak"};
            case "HK": return new String[]{"zh","yue","en"};
            case "MO": return new String[]{"zh","yue","pt"};
            case "JP": return new String[]{"ja","ryu","ain"};
            case "KR": case "KP": return new String[]{"ko"};
            case "SG": return new String[]{"zh","ms","ta"};
            case "MY": return new String[]{"ms","zh","ta","iban","bjn"};
            case "ID": return new String[]{"id","jv","su","min","ace","ban","bug","mak"};
            case "PH": return new String[]{"fil","ceb","ilo","hil","war","pam","bcl","pag","mrw"};
            case "TH": return new String[]{"th","lo","km","ms"};
            case "VN": return new String[]{"vi","km","zh","fr"};
            case "MM": return new String[]{"my","shn","kar","kac","mnw","hak"};
            case "KH": return new String[]{"km","fr"};
            case "LA": return new String[]{"lo","hmn","kdt"};
            case "IR": return new String[]{"fa","az","ku","bal","lrc"};
            case "AF": return new String[]{"ps","fa","uz","tk"};
            case "IQ": return new String[]{"ar","ku","ckb","hy"};
            case "IL": return new String[]{"he","ar","ru","yi"};
            case "SA": case "AE": case "QA": case "KW": case "BH": case "OM": case "YE": return new String[]{"ar","ur","fa","hi","ml","bn","tl"};
            case "EG": case "JO": case "LB": case "SY": case "PS": return new String[]{"ar","fr","hy","ku"};
            case "MA": case "DZ": case "TN": return new String[]{"ar","zgh","tzm","fr"};
            case "ET": return new String[]{"am","om","ti","so","sid","wal"};
            case "KE": return new String[]{"sw","en","ki","luo","kln","kam"};
            case "TZ": return new String[]{"sw","en","suk","gog","cgg","mas"};
            case "UG": return new String[]{"en","sw","lg","nyn","ach","teo"};
            case "ZA": return new String[]{"af","zu","xh","st","tn","ts","ss","ve","nr","nso"};
            case "NG": return new String[]{"ha","yo","ig","pcm","ff","ibo"};
            case "GH": return new String[]{"ak","ee","gaa","dag","ha","tw"};
            case "CM": return new String[]{"fr","en","bas","dua","ewo","ful"};
            case "SN": return new String[]{"fr","wo","ff","sr"};
            case "RW": return new String[]{"rw","fr","sw"};
            case "BI": return new String[]{"rn","fr","sw"};
            case "CD": return new String[]{"fr","ln","sw","kg","lua"};
            case "MG": return new String[]{"mg","fr"};
            case "BR": return new String[]{"pt","gn","yrl"};
            case "MX": return new String[]{"es","nah","yua","oto","zap","mix"};
            case "AR": case "CO": case "PE": case "CL": case "VE": case "EC": case "BO": case "PY": case "UY": case "CU": case "DO": case "GT": case "HN": case "SV": case "NI": case "CR": case "PA": return new String[]{"es","qu","gn","ay","ht"};
            case "ES": return new String[]{"es","ca","gl","eu","oc"};
            case "FR": return new String[]{"fr","br","oc","co","eu","ca","gsw"};
            case "BE": return new String[]{"nl","fr","de"};
            case "CH": return new String[]{"de","fr","it","rm"};
            case "LU": return new String[]{"lb","fr","de"};
            case "DE": case "AT": return new String[]{"de","dsb","hsb","fy"};
            case "IT": return new String[]{"it","de","fr","sc","scn","vec","fur","lld"};
            case "NL": return new String[]{"nl","fy","pap"};
            case "IE": return new String[]{"ga","en"};
            case "FI": return new String[]{"fi","sv","se"};
            case "SE": return new String[]{"sv","se","fi","yi"};
            case "NO": return new String[]{"no","nb","nn","se"};
            case "DK": return new String[]{"da","fo","kl"};
            case "PL": return new String[]{"pl","cs","uk"};
            case "CZ": return new String[]{"cs","sk"};
            case "SK": return new String[]{"sk","hu","rom"};
            case "RO": return new String[]{"ro","hu","rom"};
            case "HU": return new String[]{"hu","rom","hr","sk"};
            case "GR": case "CY": return new String[]{"el","tr"};
            case "UA": return new String[]{"uk","ru","crh"};
            case "RU": return new String[]{"ru","tt","ba","cv","ce","sah","os","av","udm"};
            case "GE": return new String[]{"ka","az","hy","ab"};
            case "AM": return new String[]{"hy","ru"};
            case "AZ": return new String[]{"az","ru","hy"};
            case "KZ": return new String[]{"kk","ru"};
            case "UZ": return new String[]{"uz","ru","kaa"};
            case "KG": return new String[]{"ky","ru","uz"};
            case "TJ": return new String[]{"tg","ru","uz"};
            case "TM": return new String[]{"tk","ru","uz"};
            case "TR": return new String[]{"tr","ku","zza","ar"};
            case "PT": return new String[]{"pt","mwl"};
            case "IS": return new String[]{"is"};
            case "MT": return new String[]{"mt","it"};
            case "RS": return new String[]{"sr","hu","bs","rom"};
            case "BA": return new String[]{"bs","hr","sr"};
            case "HR": return new String[]{"hr","sr","it"};
            case "ME": return new String[]{"sr","cnr","bs","sq"};
            case "AL": case "XK": return new String[]{"sq","sr"};
            case "MK": return new String[]{"mk","sq","tr"};
            case "BG": return new String[]{"bg","tr","rom"};
            case "MD": return new String[]{"ro","uk","ru","gag"};
            case "BY": return new String[]{"be","ru"};
            case "LT": return new String[]{"lt","pl","ru"};
            case "LV": return new String[]{"lv","ru","lt"};
            case "EE": return new String[]{"et","ru"};
            case "FJ": return new String[]{"fj","hi","hif"};
            case "PG": return new String[]{"tpi","ho","meu"};
            case "VU": return new String[]{"bi","fr"};
            case "WS": return new String[]{"sm"};
            case "TO": return new String[]{"to"};
            case "SB": return new String[]{"en","tpi"};
            default: return new String[]{};
        }
    }

    private Locale defaultLanguageForCountry(String countryCode) {
        if (countryCode == null) return Locale.ENGLISH;
        switch (countryCode.toUpperCase(Locale.ROOT)) {
            case "IN": return new Locale("hi", "IN");
            case "BD": return new Locale("bn", "BD");
            case "PK": return new Locale("ur", "PK");
            case "US": case "GB": case "AU": case "NZ": case "IE": case "NG": case "GH":
                return new Locale("en", countryCode);
            case "SA": case "AE": case "EG": case "IQ": case "JO": case "MA": case "DZ":
                return new Locale("ar", countryCode);
            case "FR": return new Locale("fr", "FR");
            case "ES": case "MX": case "AR": case "CO": case "PE": case "CL":
                return new Locale("es", countryCode);
            case "BR": case "PT": return new Locale("pt", countryCode);
            case "TR": return new Locale("tr", "TR");
            case "CN": case "TW": return new Locale("zh", countryCode);
            case "JP": return new Locale("ja", "JP");
            case "KR": return new Locale("ko", "KR");
            case "RU": return new Locale("ru", "RU");
            case "DE": case "AT": return new Locale("de", countryCode);
            case "IT": return new Locale("it", "IT");
            case "ID": return new Locale("id", "ID");
            case "TH": return new Locale("th", "TH");
            case "VN": return new Locale("vi", "VN");
            case "MY": return new Locale("ms", "MY");
            case "PH": return new Locale("fil", "PH");
            case "IR": return new Locale("fa", "IR");
            case "NP": return new Locale("ne", "NP");
            case "LK": return new Locale("si", "LK");
            case "MM": return new Locale("my", "MM");
            case "KH": return new Locale("km", "KH");
            case "LA": return new Locale("lo", "LA");
            case "ET": return new Locale("am", "ET");
            case "KE": case "TZ": return new Locale("sw", countryCode);
            default:
                for (Locale loc : Locale.getAvailableLocales()) {
                    if (countryCode.equalsIgnoreCase(loc.getCountry()) && !loc.getLanguage().isEmpty()) return loc;
                }
                return Locale.ENGLISH;
        }
    }

    private void setAILanguageByLocale(Locale locale) {
        aiLocale = locale == null ? Locale.getDefault() : locale;
        if (aiTts != null && !aiTtsInitializing) {
            int result = aiTts.setLanguage(aiLocale);
            aiTtsReady = result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (!aiTtsReady && aiStatusView != null) {
                aiStatusView.setText("Selected language text is available, but voice data may need to be installed.");
            }
        }
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
        TextView countryLanguage = tv(flagForCountry(selectedCountryCode) + "  " +
                (selectedCountryName.isEmpty() ? "Choose country" : selectedCountryName) + "  •  " +
                (selectedLanguageTag.isEmpty() ? Locale.getDefault().getDisplayLanguage() :
                        Locale.forLanguageTag(selectedLanguageTag).getDisplayName(Locale.forLanguageTag(selectedLanguageTag))), 11, GRAY);
        countryLanguage.setGravity(Gravity.CENTER);
        center.addView(countryLanguage);
        Button changeLocale = secondary("🌐  Change Country / Language");
        changeLocale.setOnClickListener(v -> showCountryLanguageSetup());
        center.addView(changeLocale);

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
    private void showLogin() {
        startScreen(false);
        LinearLayout c = content();
        c.addView(brandHeader());
        c.addView(heading("Welcome Back"));
        c.addView(small("Sign in to your real Viyzo account. Your password is sent only to Supabase Auth over HTTPS."));

        EditText email = input("Email address");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (!userEmail.isEmpty()) email.setText(userEmail);
        c.addView(email);

        EditText pass = input("Viyzo Password");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        c.addView(pass);

        TextView status = small("");
        c.addView(status);
        Button login = primary("🔐  Login securely");
        login.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = pass.getText().toString();
            if (e.isEmpty() || p.isEmpty()) {
                status.setText("Enter your email and Viyzo password.");
                return;
            }
            login.setEnabled(false);
            status.setText("Connecting securely to Viyzo…");
            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject().put("email", e).put("password", p);
                    JSONObject auth = apiRequest("/auth/v1/token?grant_type=password", "POST", body, false, null);
                    saveSession(auth);
                    runOnUiThread(() -> {
                        login.setEnabled(true);
                        Toast.makeText(this, "Signed in to Viyzo.", Toast.LENGTH_SHORT).show();
                        showDashboard();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        login.setEnabled(true);
                        String message = ex.getMessage() == null ? "Please try again." : ex.getMessage();
                        if (message.toLowerCase(Locale.ROOT).contains("email not confirmed")) message = "Email not confirmed. Tap “Resend Confirmation Email” above, then open the link sent to your inbox. Check Spam/Junk too.";
                        status.setText("Login failed: " + message);
                    });
                }
            }).start();
        });
        c.addView(login);

        Button resend = secondary("✉️  Resend Confirmation Email");
        resend.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            if (e.isEmpty()) {
                status.setText("Enter your email address first, then tap Resend Confirmation Email.");
                return;
            }
            resend.setEnabled(false);
            status.setText("Requesting a new confirmation email…");
            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject().put("email", e).put("type", "signup");
                    apiRequest("/auth/v1/resend", "POST", body, false, null);
                    runOnUiThread(() -> {
                        resend.setEnabled(true);
                        status.setText("If this account needs confirmation, Supabase has accepted the request. Check Inbox and Spam/Junk.");
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        resend.setEnabled(true);
                        status.setText("Could not resend confirmation: " + (ex.getMessage() == null ? "Please try again later." : ex.getMessage()));
                    });
                }
            }).start();
        });
        c.addView(resend);

        Button company = secondary("🏢  Company Login");
        company.setOnClickListener(v -> showCompanyPortal());
        c.addView(company);

        Button create = secondary("Create New Worker Account");
        create.setOnClickListener(v -> showCreateAccount());
        c.addView(create);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showHome());
        c.addView(back);
    }

    private void showCreateAccount() {
        startScreen(false);
        LinearLayout c = content();
        c.addView(brandHeader());
        c.addView(heading("Create Worker Account"));
        c.addView(small("Create a real account. Use an email address you control. Never share your OTP or password with anyone."));

        EditText name = input("Full name");
        c.addView(name);
        EditText email = input("Email address");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        c.addView(email);
        EditText phone = input("Phone number (required, include country code)");
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        c.addView(phone);
        EditText pass = input("Create Viyzo Password (8+ characters)");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        c.addView(pass);

        CheckBox terms = new CheckBox(this);
        terms.setText("I agree to Viyzo Terms and Privacy Policy.");
        terms.setTextColor(TEXT);
        terms.setTextSize(12);
        c.addView(terms);
        TextView status = small("");
        c.addView(status);

        Button create = primary("🚀  Create Account");
        create.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            String e = email.getText().toString().trim();
            String phoneValue = phone.getText().toString().trim();
            String p = pass.getText().toString();
            String digitsOnlyPhone = phoneValue.replaceAll("[^0-9]", "");
            if (n.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches() || p.length() < 8) {
                status.setText("Enter your name, a valid email address, and a password of at least 8 characters.");
                return;
            }
            if (digitsOnlyPhone.length() < 8 || digitsOnlyPhone.length() > 15) {
                status.setText("Enter a valid phone number with country code (8–15 digits).");
                return;
            }
            if (!terms.isChecked()) {
                status.setText("Please accept the Terms and Privacy Policy.");
                return;
            }
            create.setEnabled(false);
            status.setText("Creating your account securely…");
            new Thread(() -> {
                try {
                    JSONObject metadata = new JSONObject().put("full_name", n).put("phone", phoneValue).put("account_type", "worker");
                    JSONObject body = new JSONObject().put("email", e).put("password", p).put("data", metadata);
                    JSONObject auth = apiRequest("/auth/v1/signup", "POST", body, false, null);
                    String token = auth.optString("access_token", "");
                    if (!token.isEmpty()) {
                        saveSession(auth);
                        runOnUiThread(() -> {
                            create.setEnabled(true);
                            Toast.makeText(this, "Viyzo account created.", Toast.LENGTH_LONG).show();
                            showDashboard();
                        });
                    } else {
                        runOnUiThread(() -> {
                            create.setEnabled(true);
                            status.setText("Account request accepted. Check your email for confirmation, then sign in.");
                        });
                    }
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        create.setEnabled(true);
                        status.setText("Could not create account: " + (ex.getMessage() == null ? "Please try again." : ex.getMessage()));
                    });
                }
            }).start();
        });
        c.addView(create);

        Button back = secondary("← Back to Login");
        back.setOnClickListener(v -> showLogin());
        c.addView(back);
    }

    // ============================================================
    // WORKER DASHBOARD
    // ============================================================
    private void showDashboard() {
        startScreen(true);
        LinearLayout c = content();

        c.addView(brandHeader());

        // Clearly disclose demo-only behavior so workers do not mistake sample UI for live services.
        LinearLayout demoNotice = card();
        demoNotice.setBackground(outlined(Color.rgb(54, 42, 18), GOLD, 16));
        TextView demoTitle = tv("⚠  DEMO PREVIEW — NOT LIVE", 14, GOLD);
        demoTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        demoNotice.addView(demoTitle);
        demoNotice.addView(tv(
                "Live login and Available Work are connected to Viyzo. Identity verification and real bank payouts are not active yet. Apply only to jobs listed in Available Work; sample cards elsewhere are not real jobs or guaranteed earnings.",
                11, TEXT));
        c.addView(demoNotice);
        setMargins(demoNotice, 0, 4, 0, 13);

        LinearLayout welcome = card();
        LinearLayout wr = row();

        TextView avatar = tv("👤", 31, WHITE);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(outlined(CARD2, BORDER, 35));
        wr.addView(avatar, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout wn = column();
        TextView w1 = tv("Good day, " + (workerName.isEmpty() ? (userEmail.isEmpty() ? "Worker" : userEmail) : workerName) + " 👋", 15, WHITE);
        w1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        wn.addView(w1);
        wn.addView(tv("Account ID: " + (userId.isEmpty() ? "Not signed in" : userId.substring(0, Math.min(8, userId.length()))), 11, GRAY));
        wn.addView(tv("● Basic Worker", 11, GREEN));

        LinearLayout.LayoutParams wnp =
                new LinearLayout.LayoutParams(0, dp(58), 1);
        wnp.setMargins(dp(11), 0, 0, 0);
        wr.addView(wn, wnp);

        welcome.addView(wr);
        c.addView(welcome);
        setMargins(welcome, 0, 4, 0, 13);

        // Earning opportunity target.
        LinearLayout target = card();
        target.setBackground(primaryBg());
        target.addView(tv("🎯  Daily Earning Opportunity", 16, WHITE));
        TextView targetValue = tv("₹" +
                formatNumber(DAILY_EARNING_TARGET_INR) +
                " target opportunity", 25, WHITE);
        targetValue.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        target.addView(targetValue);
        target.addView(tv(
                "Planning guide: up to " +
                        formatNumber(TARGET_HOURS_PER_DAY) +
                        " hours • ~₹" +
                        formatNumber(TARGET_INR_PER_HOUR) +
                        "/hour target",
                11, WHITE));
        target.addView(tv(
                "Not guaranteed income — actual earnings depend on real work supply, rate, quality and availability.",
                10, WHITE));
        c.addView(target);
        setMargins(target, 0, 0, 0, 13);

        LinearLayout manager = card();
        manager.setBackground(primaryBg());
        TextView mt = tv("🤖  AI Master Manager       ›", 17, WHITE);
        mt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        manager.addView(mt);
        manager.addView(tv(
                "Matches skills, language, workload, deadline, verification and availability.",
                11, WHITE));
        manager.setOnClickListener(v -> showAIMasterManager());
        c.addView(manager);
        setMargins(manager, 0, 0, 0, 13);

        LinearLayout supervisor = card();
        supervisor.setBackground(outlined(CARD2, BORDER, 18));
        TextView supervisorTitle = tv("🧭  Supervisor Console       ›", 17, WHITE);
        supervisorTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        supervisor.addView(supervisorTitle);
        supervisor.addView(tv(
                "Review task progress, worker verification, payout screens and build/release checks.",
                11, GRAY));
        supervisor.setOnClickListener(v -> showSupervisorConsole());
        c.addView(supervisor);
        setMargins(supervisor, 0, 0, 0, 13);

        LinearLayout r1 = row();
        LinearLayout jobs = menuCard(
                "🔎", "Available Jobs", "Explore global work",
                v -> showJobs());
        LinearLayout myJobs = menuCard(
                "📋", "My Work", "Active assignments",
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
        p22.setMargins(dp(8), 0, 0, 0);
        r2.addView(verify, p22);
        c.addView(r2);

        TextView q = tv("Work Sources", 18, WHITE);
        q.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        q.setPadding(0, dp(17), 0, dp(6));
        c.addView(q);

        LinearLayout r3 = row();
        r3.addView(menuCard("⚡", "Urgent", "Fast deadlines",
                v -> showJobs()), new LinearLayout.LayoutParams(0, dp(125), 1));

        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(0, dp(125), 1);
        rp.setMargins(dp(8), 0, 0, 0);
        r3.addView(menuCard("📦", "Bulk Work", "Large batches",
                v -> showJobs()), rp);
        c.addView(r3);

        LinearLayout r4 = row();
        r4.addView(menuCard("🔁", "Recurring", "Repeat work",
                v -> showJobs()), new LinearLayout.LayoutParams(0, dp(125), 1));

        LinearLayout.LayoutParams r4p =
                new LinearLayout.LayoutParams(0, dp(125), 1);
        r4p.setMargins(dp(8), dp(8), 0, 0);
        r4.addView(menuCard("🌍", "Global", "Worldwide work",
                v -> showJobs()), r4p);
        c.addView(r4);

        LinearLayout settings = card();
        settings.setOrientation(LinearLayout.HORIZONTAL);
        settings.addView(tv("⚙", 24, WHITE),
                new LinearLayout.LayoutParams(dp(42), dp(45)));

        TextView st = tv("Settings & Account", 15, WHITE);
        st.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        settings.addView(st, new LinearLayout.LayoutParams(0, dp(45), 1));

        TextView arrow = tv("›", 28, GRAY);
        arrow.setGravity(Gravity.CENTER);
        settings.addView(arrow, new LinearLayout.LayoutParams(dp(40), dp(45)));

        settings.setOnClickListener(v -> showSettings());
        c.addView(settings);
        setMargins(settings, 0, 10, 0, 0);
    }

    // ============================================================
    // SUPERVISOR CONSOLE
    // ============================================================
    private void showSupervisorConsole() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("🧭 Supervisor Console"));
        c.addView(small("A quick operational checklist for the current app build. Live company-wide monitoring requires the backend to be connected."));

        LinearLayout status = card();
        status.setBackground(primaryBg());
        status.addView(tv("🟡  Work supply: Demo mode", 16, WHITE));
        status.addView(tv("Real company jobs and worker activity are not live until a secure server is connected.", 11, WHITE));
        c.addView(status);
        setMargins(status, 0, 0, 0, 10);

        c.addView(wideInfo("📋", "Task supervision", "Open My Work to review the assignments currently shown in this app."));
        Button work = secondary("📋  Review My Work");
        work.setOnClickListener(v -> showMyJobs());
        c.addView(work);

        c.addView(wideInfo("🛡", "Worker verification", "Review the verification steps available in this build."));
        Button verify = secondary("🛡  Open Verification");
        verify.setOnClickListener(v -> showWorkerVerification());
        c.addView(verify);

        c.addView(wideInfo("💰", "Payout review", "Open Earnings to inspect the app's displayed balance and payout information."));
        Button earnings = secondary("💰  Open Earnings");
        earnings.setOnClickListener(v -> showEarnings());
        c.addView(earnings);

        c.addView(wideInfo("🏗", "APK build status", "Build status must be confirmed in GitHub Actions. This screen does not claim that a new APK has passed."));
        Button build = primary("🔎  Check GitHub Actions");
        build.setOnClickListener(v -> {
            try {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions")));
            } catch (Exception ignored) {
                Toast.makeText(this, "Open GitHub Actions in your browser to check the latest APK build.", Toast.LENGTH_LONG).show();
            }
        });
        c.addView(build);

        Button jobs = secondary("🔎  Review Available Jobs");
        jobs.setOnClickListener(v -> showJobs());
        c.addView(jobs);

        Button back = secondary("← Back to Dashboard");
        back.setOnClickListener(v -> showDashboard());
        c.addView(back);
    }

    // ============================================================
    // AI MASTER MANAGER
    // ============================================================
    private void showAIMasterManager() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("🤖 AI Master Manager"));
        c.addView(small(
                "Decision layer for matching available company work with suitable workers."
        ));

        LinearLayout hero = card();
        hero.setBackground(primaryBg());
        hero.addView(tv("Global Work Matching Engine", 19, WHITE));
        hero.addView(tv(
                "The production version will use secure backend data to score workers and jobs in real time.",
                11, WHITE));
        c.addView(hero);

        c.addView(wideInfo("🎯", "Worker Earning Target",
                "₹" + formatNumber(DAILY_EARNING_TARGET_INR) +
                        " opportunity target/day"));
        c.addView(wideInfo("💰", "Worker Pool",
                formatNumber(WORKER_SHARE_PERCENT) + "% of configured job budget"));
        c.addView(wideInfo("🏢", "Viyzo Platform",
                formatNumber(VIYZO_SHARE_PERCENT) +
                        "% for platform costs and remaining margin"));
        c.addView(wideInfo("⏱", "Target Planning",
                "Up to " + formatNumber(TARGET_HOURS_PER_DAY) +
                        " hours/day"));

        TextView factors = tv("Matching Factors", 18, WHITE);
        factors.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        factors.setPadding(0, dp(15), 0, dp(7));
        c.addView(factors);

        c.addView(wideInfo("🧠", "Skills",
                "Task category and required skill"));
        c.addView(wideInfo("🌐", "Language / Country",
                "Language, eligibility and location rules"));
        c.addView(wideInfo("🕐", "Availability",
                "Current worker workload and capacity"));
        c.addView(wideInfo("⭐", "Quality",
                "Quality history and task performance"));
        c.addView(wideInfo("🛡", "Verification",
                "Verification level required by the job"));
        c.addView(wideInfo("📅", "Deadline",
                "Urgency and time remaining"));
        c.addView(wideInfo("📦", "Workload",
                "Quantity divided across suitable workers"));

        Button feed = primary("🔎  Open AI-Matched Work");
        feed.setOnClickListener(v -> showJobs());
        c.addView(feed);

        Button back = secondary("← Back to Dashboard");
        back.setOnClickListener(v -> showDashboard());
        c.addView(back);
    }

    // ============================================================
    // JOBS
    // ============================================================
    private void showJobs() {
        if (accessToken.isEmpty() || userId.isEmpty()) { showLogin(); return; }
        startScreen(true);
        LinearLayout c = content();
        c.addView(heading("Available Work"));
        c.addView(small("Live funded work orders from Viyzo. Demo opportunities are not mixed into this list."));
        TextView status = small("Loading available jobs…");
        c.addView(status);
        Button refresh = secondary("↻ Refresh jobs");
        c.addView(refresh);
        refresh.setOnClickListener(v -> showJobs());
        new Thread(() -> {
            try {
                JSONArray jobs = apiArray("/rest/v1/jobs?select=id,title,description,category,payment_amount,currency,task_limit,created_at&status=eq.open&order=created_at.desc&limit=50");
                runOnUiThread(() -> {
                    status.setText(jobs.length() == 0 ? "No live funded jobs are available right now. Check again later." : jobs.length() + " live job(s) available.");
                    for (int i = 0; i < jobs.length(); i++) {
                        JSONObject job = jobs.optJSONObject(i);
                        if (job != null) c.addView(liveJobCard(job));
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> status.setText("Could not load live jobs: " + (ex.getMessage() == null ? "Please try again." : ex.getMessage())));
            }
        }).start();
        Button myWork = secondary("📋 My Work");
        myWork.setOnClickListener(v -> showMyJobs());
        c.addView(myWork);
    }

    private LinearLayout liveJobCard(JSONObject job) {
        LinearLayout c = card();
        String title = job.optString("title", "Viyzo work order");
        String category = job.optString("category", "General");
        double amount = job.optDouble("payment_amount", 0);
        int slots = job.optInt("task_limit", 1);
        c.addView(heading(title));
        c.addView(small(category + " • " + job.optString("currency", "INR")));
        c.addView(tv("Worker payment per approved task: " + formatNumber(amount) + " " + job.optString("currency", "INR"), 15, GREEN));
        c.addView(tv("Task slots: " + slots, 12, TEXT));
        String description = job.optString("description", "");
        if (!description.isEmpty()) c.addView(tv(description, 12, GRAY));
        Button details = primary("View Job / Apply");
        details.setOnClickListener(v -> showLiveJobDetails(job));
        c.addView(details);
        setMargins(c, 0, 8, 0, 8);
        return c;
    }

    private void showLiveJobDetails(JSONObject job) {
        startScreen(false);
        LinearLayout c = content();
        String jobId = job.optString("id", "");
        String title = job.optString("title", "Viyzo work order");
        double amount = job.optDouble("payment_amount", 0);
        c.addView(heading(title));
        c.addView(small("Live Viyzo work order • " + job.optString("currency", "INR")));
        c.addView(wideInfo("💰", "Worker payment per approved task", formatNumber(amount) + " " + job.optString("currency", "INR")));
        c.addView(wideInfo("📦", "Task slots", String.valueOf(job.optInt("task_limit", 1))));
        c.addView(tv(job.optString("description", "No additional description provided."), 13, TEXT));
        c.addView(small("Payment is pending until the work is completed and reviewed. Applying does not guarantee selection or income."));
        TextView status = small("");
        c.addView(status);
        Button apply = primary("🚀 Apply for this Work");
        apply.setOnClickListener(v -> {
            if (accessToken.isEmpty() || userId.isEmpty()) { showLogin(); return; }
            apply.setEnabled(false);
            status.setText("Submitting your application securely…");
            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject().put("job_id", jobId).put("worker_id", userId)
                            .put("message", "Applied from Viyzo Worker Android app");
                    apiRequest("/rest/v1/job_applications", "POST", body, true, "return=minimal");
                    runOnUiThread(() -> {
                        status.setText("Application submitted successfully. Open My Work to track its status.");
                        Toast.makeText(this, "Application submitted.", Toast.LENGTH_SHORT).show();
                        apply.setText("Application Submitted");
                        apply.setEnabled(false);
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        apply.setEnabled(true);
                        String error = ex.getMessage() == null ? "Please try again." : ex.getMessage();
                        String lower = error.toLowerCase(Locale.ROOT);
                        if (lower.contains("23505") || lower.contains("duplicate key") || lower.contains("unique constraint")) {
                            status.setText("You have already applied for this work. Open My Work to check your application status.");
                        } else if (lower.contains("row-level security") || lower.contains("42501") || lower.contains("permission denied") || lower.contains("not allowed")) {
                            status.setText("Your application could not be accepted. This work may not be funded or you may not have permission to apply. Refresh Available Work and try again.");
                        } else if (lower.contains("job_not_funded") || lower.contains("not funded") || lower.contains("funding")) {
                            status.setText("This work is not ready for applications yet because its worker payment reserve is not confirmed. Please choose another funded job.");
                        } else if (lower.contains("job_not_open") || lower.contains("409")) {
                            status.setText("This work is no longer accepting applications. Refresh Available Work to see current jobs.");
                        } else if (lower.contains("401") || lower.contains("unauthorized") || lower.contains("jwt")) {
                            status.setText("Your login session may have expired. Please sign in again, then retry your application.");
                        } else if (lower.contains("network") || lower.contains("timeout") || lower.contains("unable to resolve host")) {
                            status.setText("Network connection problem. Check your internet and try again.");
                        } else {
                            status.setText("Application could not be submitted right now. Please refresh Available Work and try again. Details: " + error);
                        }
                    });
                }
            }).start();
        });
        c.addView(apply);
        Button back = secondary("← Back to Available Work");
        back.setOnClickListener(v -> showJobs());
        c.addView(back);
    }

    private LinearLayout jobCard(JobData job) {
        LinearLayout c = card();

        LinearLayout top = row();

        TextView ic = tv(job.icon, 24, WHITE);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(outlined(CARD2, BORDER, 16));
        top.addView(ic, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout names = column();
        TextView n = tv(job.title, 16, WHITE);
        n.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        names.addView(n);
        names.addView(tv(job.category + " • " + job.workType, 10, GRAY));
        top.addView(names, new LinearLayout.LayoutParams(0, dp(48), 1));

        TextView badge = tv(job.urgency, 9,
                job.urgency.equals("Urgent") ? ORANGE : GREEN);
        badge.setGravity(Gravity.CENTER);
        top.addView(badge, new LinearLayout.LayoutParams(dp(65), dp(30)));

        c.addView(top);

        double budget = parseMoney(job.budget);
        double workerPool = workerPool(budget);

        TextView info = tv(
                "Demo / Example job\n" +
                        "Company        " + job.company +
                        "\nBudget           " + job.budget +
                        "\nWorker Pool    " + money(workerPool) +
                        "\nWorkers          " + job.workers +
                        "\nWorkload         " + job.quantity +
                        "\nDeadline          " + job.deadline,
                12, TEXT);
        info.setPadding(0, dp(12), 0, dp(7));
        c.addView(info);

        Button details = primary("View Details  →");
        details.setOnClickListener(v -> showJobDetails(job));
        c.addView(details);

        setMargins(c, 0, 9, 0, 4);
        return c;
    }

    private void showJobDetails(JobData job) {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Job Details"));
        c.addView(small("AI-matched global work opportunity"));

        c.addView(wideInfo(job.icon, job.title,
                job.workType + " • " + job.category));

        double budget = parseMoney(job.budget);
        double workerPool = workerPool(budget);
        double platformShare = viyzoShare(budget);

        LinearLayout d = card();
        d.addView(tv(
                "Company Budget       " + job.budget +
                        "\n\nWorker Pool            " + money(workerPool) +
                        "\n\nViyzo Platform Share " + money(platformShare) +
                        "\n\nWorkers Needed       " + job.workers +
                        "\n\nWorkload                " + job.quantity +
                        "\n\nDeadline                 " + job.deadline +
                        "\n\nUrgency                   " + job.urgency +
                        "\n\nRequired Skill         " + job.skill +
                        "\n\nLanguage                " + job.language,
                13, TEXT));
        c.addView(d);
        setMargins(d, 0, 12, 0, 12);

        LinearLayout note = card();
        note.addView(tv("Worker view", 15, WHITE));
        note.addView(tv(
                "Workers should see the task payout/earning information relevant to their assignment. Internal platform cost accounting should not be used to mislead users.",
                11, GRAY));
        c.addView(note);

        TextView req = tv("Requirements", 18, WHITE);
        req.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        req.setPadding(0, dp(15), 0, dp(7));
        c.addView(req);

        c.addView(wideInfo("✓", "Skill",
                job.skill));
        c.addView(wideInfo("✓", "Language",
                job.language));
        c.addView(wideInfo("✓", "Quality",
                "Accuracy and quality"));
        c.addView(wideInfo("✓", "Verification",
                "Job-specific verification may apply"));

        TextView exampleOnly = tv(
                "EXAMPLE ONLY — This is a sample opportunity, not a verified company job. No application or earning is created here. Open Available Work to apply to live, funded jobs.",
                12, GOLD);
        exampleOnly.setPadding(dp(12), dp(10), dp(12), dp(10));
        c.addView(exampleOnly);

        Button apply = secondary("Open Live Available Work");
        apply.setOnClickListener(v -> showJobs());
        c.addView(apply);

        Button back = secondary("← Back to Work Feed");
        back.setOnClickListener(v -> showJobs());
        c.addView(back);
    }

    // ============================================================
    // MY WORK / WORK POOL
    // ============================================================
    private String friendlyApplicationStatus(String status) {
        if (status == null || status.trim().isEmpty()) return "Awaiting update";
        switch (status.toLowerCase(Locale.ROOT)) {
            case "submitted": return "Submitted — waiting for company review";
            case "pending": return "Pending review";
            case "accepted": return "Accepted";
            case "approved": return "Approved";
            case "rejected": return "Not accepted";
            case "cancelled": return "Cancelled";
            case "completed": return "Completed";
            default: return status.replace("_", " ");
        }
    }
    private void showMyJobs() {
        if (accessToken.isEmpty() || userId.isEmpty()) { showLogin(); return; }
        startScreen(true);
        LinearLayout c = content();
        c.addView(heading("My Work"));
        c.addView(small("Your live applications from the Viyzo database."));
        TextView status = small("Loading applications…");
        c.addView(status);
        Button refresh = secondary("Refresh My Work");
        refresh.setOnClickListener(v -> showMyJobs());
        c.addView(refresh);
        new Thread(() -> {
            try {
                JSONArray apps = apiArray("/rest/v1/job_applications?select=id,job_id,status,created_at,jobs(title,payment_amount,currency,status)&worker_id=eq." + userId + "&order=created_at.desc&limit=100");
                JSONArray submissions = new JSONArray();
                String submissionLoadWarning = "";
                try {
                    JSONObject response = apiRequest("/functions/v1/worker-task", "GET", null, true, null);
                    submissions = response.optJSONArray("submissions");
                    if (submissions == null) submissions = new JSONArray();
                } catch (Exception submissionError) {
                    submissionLoadWarning = " • Review status could not be refreshed";
                }
                final JSONArray finalSubmissions = submissions;
                final String finalSubmissionWarning = submissionLoadWarning;
                runOnUiThread(() -> {
                    status.setText((apps.length() == 0 ? "No applications yet." : "Applications: " + apps.length()) + finalSubmissionWarning);
                    for (int i=0;i<apps.length();i++) {
                        JSONObject app=apps.optJSONObject(i);
                        if(app==null) continue;
                        JSONObject job=app.optJSONObject("jobs");
                        String appId = app.optString("id","");
                        JSONObject submission = null;
                        for (int si=0;si<finalSubmissions.length();si++) {
                            JSONObject candidate=finalSubmissions.optJSONObject(si);
                            if(candidate!=null && appId.equals(candidate.optString("application_id",""))) {
                                submission=candidate;
                                break;
                            }
                        }
                        LinearLayout item=card();
                        item.addView(heading(job==null?"Viyzo work order":job.optString("title","Viyzo work order")));
                        item.addView(small("Application status: "+friendlyApplicationStatus(app.optString("status","submitted"))));
                        if(job!=null) item.addView(tv("Worker payment per approved task: ₹"+formatNumber(job.optDouble("payment_amount",0)),13,GREEN));
                        if (submission != null) {
                            String reviewStatus = submission.optString("status","submitted");
                            item.addView(small("Work review: " + reviewStatus));
                            String reviewNote = submission.optString("review_note","");
                            if (!reviewNote.isEmpty()) item.addView(small("Supervisor note: " + reviewNote));
                            if ("submitted".equals(reviewStatus)) {
                                item.addView(small("Your work is waiting for supervisor review. No earnings are approved by this status alone."));
                            } else if ("approved".equals(reviewStatus)) {
                                item.addView(small("Work approved. Check Earnings for a separately recorded earning; approval does not confirm a bank payout."));
                            } else if ("rejected".equals(reviewStatus)) {
                                item.addView(small("Work was not approved. Review the supervisor note before taking further action."));
                            }
                        } else if(job!=null && "open".equals(job.optString("status",""))) {
                            Button complete=primary("Submit Completed Work");
                            String jobTitle=job.optString("title","Viyzo work order");
                            complete.setOnClickListener(v -> showCompletionSubmission(appId,jobTitle));
                            item.addView(complete);
                        }
                        item.addView(small("Applied: "+app.optString("created_at","")));
                        setMargins(item,0,8,0,8);
                        c.addView(item);
                    }
                });
            } catch(Exception ex) {
                runOnUiThread(() -> status.setText("Could not load applications: "+ex.getMessage()));
            }
        }).start();
    }

    private void showCompletionSubmission(String applicationId, String title) {
        startScreen(false);
        LinearLayout c=content();
        c.addView(heading("Submit Completed Work"));
        c.addView(small(title));
        EditText note=input("Describe completed work (5+ characters)");
        note.setSingleLine(false); note.setMinLines(3); note.setGravity(Gravity.TOP);
        c.addView(note);
        EditText proof=input("Proof URL (optional HTTPS link)");
        c.addView(proof);
        TextView status=small(""); c.addView(status);
        Button submit=primary("Send for Review");
        submit.setOnClickListener(v -> {
            String summary=note.getText().toString().trim(), link=proof.getText().toString().trim();
            if(summary.length()<5){status.setText("Please add a completion summary.");return;}
            if(!link.isEmpty()&&!link.startsWith("https://")){status.setText("Proof URL must use HTTPS.");return;}
            submit.setEnabled(false);
            new Thread(() -> {
                try {
                    JSONObject body=new JSONObject().put("action","submit_completion").put("application_id",applicationId).put("completion_note",summary);
                    if(!link.isEmpty()) body.put("proof_url",link);
                    apiRequest("/functions/v1/worker-task","POST",body,true,null);
                    runOnUiThread(() -> status.setText("Submitted for supervisor review. This is not a payout confirmation."));
                } catch(Exception ex) {
                    runOnUiThread(() -> { submit.setEnabled(true); status.setText("Submission failed: "+ex.getMessage()); });
                }
            }).start();
        });
        c.addView(submit);
        Button back=secondary("Back to My Work"); back.setOnClickListener(v -> showMyJobs()); c.addView(back);
    }

    private void showWorkExecution() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Work Execution"));
        c.addView(small("DEMO ONLY — this workspace does not submit real work or create earnings. For real funded jobs, use Available Work → My Work → Submit Completed Work for supervisor review."));

        c.addView(wideInfo("📦", "Task Batch",
                "Product Listing • Batch #VZ-DEMO-001"));
        c.addView(wideInfo("📊", "Progress",
                "125 / 1000 completed"));
        c.addView(wideInfo("💰", "Worker Earning",
                "Shown after approved task calculation"));

        c.addView(input("Task result / item ID"));

        CheckBox done = new CheckBox(this);
        done.setText("I confirm this task is complete and accurate.");
        done.setTextColor(TEXT);
        c.addView(done);

        Button submit = primary("✓  Submit Completed Work");
        submit.setOnClickListener(v -> {
            if (!done.isChecked()) {
                Toast.makeText(this,
                        "Please confirm the task is complete.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this,
                    "Demo submission recorded. Quality review will be backend-controlled.",
                    Toast.LENGTH_SHORT).show();
        });
        c.addView(submit);

        Button back = secondary("← Back to My Work");
        back.setOnClickListener(v -> showMyJobs());
        c.addView(back);
    }

    private void showWorkPool() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Work Pool"));
        c.addView(small(
                "Batches are divided among eligible workers by the matching engine."
        ));

        c.addView(wideInfo("📦", "Bulk Pool",
                "Large batches can be divided into smaller assignments"));
        c.addView(wideInfo("⚡", "Urgent Pool",
                "Priority queue for short deadlines"));
        c.addView(wideInfo("🔁", "Recurring Pool",
                "Repeat jobs can create future assignments"));
        c.addView(wideInfo("🌍", "Global Pool",
                "Country/language eligibility can be configured"));

        Button jobs = primary("🔎  Find Eligible Work");
        jobs.setOnClickListener(v -> showJobs());
        c.addView(jobs);
    }

    // ============================================================
    // EARNINGS
    // ============================================================
    private void showEarnings() {
        if (accessToken.isEmpty() || userId.isEmpty()) { showLogin(); return; }
        startScreen(true);
        LinearLayout c = content();
        c.addView(heading("Earnings"));
        c.addView(small("Live records from your account. Pending earnings are not withdrawable and no bank transfer is initiated here."));
        LinearLayout balance = card();
        balance.addView(tv("Approved / paid work records",14,GRAY));
        TextView totalText=tv("₹0.00",30,WHITE);
        totalText.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        balance.addView(totalText);
        TextView pendingText=tv("Pending: ₹0.00",14,GOLD);
        balance.addView(pendingText);
        c.addView(balance);
        setMargins(balance,0,0,0,12);
        TextView status=small("Loading earnings…");
        c.addView(status);
        Button refresh=secondary("Refresh Earnings");
        refresh.setOnClickListener(v -> showEarnings());
        c.addView(refresh);
        new Thread(() -> {
            try {
                JSONArray earnings=apiArray("/rest/v1/earnings?select=id,amount,currency,status,created_at,job_id&worker_id=eq."+userId+"&order=created_at.desc&limit=100");
                JSONArray payouts=apiArray("/rest/v1/worker_payouts?select=id,amount,currency,status,reference,created_at&worker_id=eq."+userId+"&order=created_at.desc&limit=100");
                double total=0,pending=0,paid=0,approved=0;
                for(int i=0;i<earnings.length();i++){
                    JSONObject e=earnings.optJSONObject(i); if(e==null) continue;
                    double amount=e.optDouble("amount",0); String st=e.optString("status","");
                    if("pending".equals(st)) pending+=amount;
                    if("approved".equals(st)) approved+=amount;
                    if("paid".equals(st)) paid+=amount;
                }
                total=approved+paid;
                final double fTotal=total,fPending=pending,fPaid=paid,fApproved=approved;
                runOnUiThread(() -> {
                    totalText.setText("₹"+formatNumber(fTotal));
                    pendingText.setText("Pending: ₹"+formatNumber(fPending));
                    status.setText("Earning records: "+earnings.length()+" • Payout records: "+payouts.length());
                    LinearLayout summary=card();
                    summary.addView(wideInfo("✓","Approved earnings","₹"+formatNumber(fApproved)));
                    summary.addView(wideInfo("🏦","Earnings marked paid (record status)","₹"+formatNumber(fPaid)));
                    c.addView(summary);
                    c.addView(heading("Earning History"));
                    if(earnings.length()==0) c.addView(small("No real earnings recorded yet."));
                    for(int i=0;i<earnings.length();i++){
                        JSONObject e=earnings.optJSONObject(i); if(e==null) continue;
                        LinearLayout item=card();
                        item.addView(tv("₹"+formatNumber(e.optDouble("amount",0))+" "+e.optString("currency","INR"),17,WHITE));
                        item.addView(small("Status: "+e.optString("status","pending")+" • "+e.optString("created_at","")));
                        c.addView(item); setMargins(item,0,6,0,6);
                    }
                    c.addView(heading("Payout History"));
                    if(payouts.length()==0) c.addView(small("No payout records. Bank payments are not connected yet."));
                    for(int i=0;i<payouts.length();i++){
                        JSONObject p=payouts.optJSONObject(i); if(p==null) continue;
                        LinearLayout item=card();
                        item.addView(tv(p.optString("currency","INR")+" "+formatNumber(p.optDouble("amount",0)),16,WHITE));
                        item.addView(small("Payout status: "+p.optString("status","pending")+" • "+p.optString("created_at","")));
                        c.addView(item); setMargins(item,0,6,0,6);
                    }
                });
            } catch(Exception ex) {
                runOnUiThread(() -> status.setText("Could not load earnings right now. Check your connection and try again. If this continues, contact support with the time of the error."));
            }
        }).start();
        Button methods=secondary("Payment Methods / Requirements");
        methods.setOnClickListener(v -> showPaymentSettings());
        c.addView(methods);
    }

    private void showPaymentSettings() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Payment Settings"));
        c.addView(small("Honest status for your account. This screen does not collect bank details or claim a transfer has been set up."));

        c.addView(wideInfo("🌍", "Payout country",
                getSharedPreferences("ViyzoPrefs", MODE_PRIVATE).getString("country_code", "IN")));
        c.addView(wideInfo("🏦", "Bank / payout account",
                "Not connected. No bank transfer can be initiated from this app yet."));
        c.addView(wideInfo("🛡", "Verification",
                "Payout eligibility must be checked separately from worker login and job approval."));
        c.addView(wideInfo("💳", "Payment provider",
                "Worker payout provider and merchant payout configuration are not confirmed as live."));
        c.addView(wideInfo("🔒", "Safety",
                "Do not enter bank passwords, card PINs, OTPs, or full account credentials in this screen."));

        Button earnings = primary("View Live Earnings & Payout Records");
        earnings.setOnClickListener(v -> showEarnings());
        c.addView(earnings);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showProfile());
        c.addView(back);
    }

    // ============================================================
    // PROFILE
    // ============================================================
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
        TextView name = tv(workerName.isEmpty() ? (userEmail.isEmpty() ? "Viyzo Worker" : userEmail) : workerName, 17, WHITE);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        pd.addView(name);
        pd.addView(tv("Account ID: " + (userId.isEmpty() ? "Not signed in" : userId.substring(0, Math.min(8, userId.length()))), 11, GRAY));
        pd.addView(tv("● Basic Worker", 11, GREEN));

        pr.addView(pd, new LinearLayout.LayoutParams(0, dp(68), 1));
        profile.addView(pr);
        c.addView(profile);

        c.addView(wideInfo("🛡", "Verification Status",
                "Basic Worker • Higher levels available"));

        Button verify = secondary("🛡  Worker Verification");
        verify.setOnClickListener(v -> showWorkerVerification());
        c.addView(verify);

        Button earnings = secondary("💰  Earnings & Payout");
        earnings.setOnClickListener(v -> showEarnings());
        c.addView(earnings);

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
        logout.setOnClickListener(v -> { clearLiveSession(); showHome(); });
        c.addView(logout);
    }

    // ============================================================
    // WORKER VERIFICATION
    // ============================================================
    private void showWorkerVerification() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Worker Verification"));
        c.addView(small(
                "Verification level can control access to higher-value or restricted work."
        ));

        c.addView(wideInfo("1", "Country",
                "Country and provider rules"));
        c.addView(wideInfo("2", "Basic Verification",
                "Name, DOB, phone, email and consent"));
        c.addView(wideInfo("3", "Identity Verification",
                "Document requirements can vary by country/provider"));
        c.addView(wideInfo("4", "Payout Verification",
                "Payment provider requirements may apply"));

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

        c.addView(spinner(new String[]{
                "Select Country", "India", "Bangladesh",
                "United States", "United Kingdom", "UAE", "Other"
        }));

        c.addView(input("Full Name"));
        c.addView(input("Date of Birth"));
        c.addView(input("Phone Number"));
        c.addView(input("Email Address"));

        Button otp = secondary("📱  Send Phone OTP");
        otp.setOnClickListener(v -> Toast.makeText(
                this, "OTP service will connect to secure backend.",
                Toast.LENGTH_SHORT).show());
        c.addView(otp);

        Button selfie = secondary("📷  Take Live Selfie");
        selfie.setOnClickListener(v -> Toast.makeText(
                this, "Selfie verification will connect to an approved verification service.",
                Toast.LENGTH_SHORT).show());
        c.addView(selfie);

        CheckBox consent = new CheckBox(this);
        consent.setText(
                "I confirm this information is mine and agree to verification/privacy terms."
        );
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
        c.addView(small(
                "Accepted documents depend on country and verification provider."
        ));

        c.addView(spinner(new String[]{
                "Select Country", "India", "United States",
                "United Kingdom", "Bangladesh", "UAE", "Other"
        }));

        c.addView(wideInfo("🪪", "Government ID",
                "Use an accepted document only when required."));
        c.addView(wideInfo("🔐", "Secure Upload",
                "Production upload should use encrypted backend storage."));

        Button upload = primary("📷  Upload / Capture Document");
        upload.setOnClickListener(v -> Toast.makeText(
                this,
                "Secure document upload will connect to backend/provider.",
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
        c.addView(small(
                "Basic access may be available where permitted; stronger verification can be required later."
        ));

        c.addView(wideInfo("✓", "Account Created",
                "Basic account access"));
        c.addView(wideInfo("🛡", "Basic Verification",
                "Some jobs can remain available"));
        c.addView(wideInfo("🔒", "Higher Verification",
                "Some high-value or restricted jobs may require more verification"));
        c.addView(wideInfo("💳", "Payout Verification",
                "Payment provider rules may apply before withdrawal"));

        Button done = primary("Continue to Dashboard");
        done.setOnClickListener(v -> showDashboard());
        c.addView(done);
    }

    // ============================================================
    // COMPANY PORTAL
    // ============================================================
    private void showCompanyPortal() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(brandHeader());
        c.addView(heading("Company Work Portal"));
        c.addView(small(
                "Company onboarding and real job supply will be powered by a secure backend."
        ));

        LinearLayout hero = card();
        hero.setBackground(primaryBg());
        hero.addView(tv("🌍  Global Company Jobs", 19, WHITE));
        hero.addView(tv(
                "Post remote work, choose country/currency, set workload and budget, then let the matching engine recommend workers.",
                12, WHITE));
        c.addView(hero);
        setMargins(hero, 0, 0, 0, 13);

        Button post = primary("＋  Post New Work");
        post.setOnClickListener(v -> showPostWork());
        c.addView(post);

        Button companyLogin = secondary("🏢  Company Login / Dashboard");
        companyLogin.setOnClickListener(v -> showCompanyDashboard());
        c.addView(companyLogin);

        Button model = secondary("💰  Pricing / Work Budget Model");
        model.setOnClickListener(v -> showPricingModel());
        c.addView(model);

        Button back = secondary("← Back to Worker App");
        back.setOnClickListener(v -> showHome());
        c.addView(back);
    }

    private void showPricingModel() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Viyzo Work Budget Model"));
        c.addView(small(
                "Configurable internal platform model. Final customer pricing, taxes, payment fees and local legal requirements must be handled by the production backend."
        ));

        c.addView(wideInfo("👥", "Worker Pool",
                formatNumber(WORKER_SHARE_PERCENT) + "%"));
        c.addView(wideInfo("🏢", "Viyzo Platform Share",
                formatNumber(VIYZO_SHARE_PERCENT) + "%"));
        c.addView(wideInfo("🖥", "Platform Costs",
                "Backend/server, payments, verification, support, security, refunds/disputes"));
        c.addView(wideInfo("📈", "Remaining Margin",
                "Calculated only after actual platform expenses"));

        LinearLayout example = card();
        example.addView(tv("Example: ₹2,000 total job value", 16, WHITE));
        example.addView(tv(
                "Worker pool: ₹1,000\n" +
                        "Viyzo platform share: ₹1,000\n" +
                        "Viyzo share is not automatically pure profit.",
                13, TEXT));
        c.addView(example);

        Button back = secondary("← Back to Company Portal");
        back.setOnClickListener(v -> showCompanyPortal());
        c.addView(back);
    }

    private void showCompanyDashboard() {
        if (accessToken == null || accessToken.isEmpty() || userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Sign in first to open the company dashboard.", Toast.LENGTH_LONG).show();
            showLogin();
            return;
        }
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Company Dashboard"));
        c.addView(small("Live company records from Viyzo. Only your own jobs and status are shown."));

        TextView profileStatus = small("Company verification: checking…");
        TextView jobsStatus = small("Your jobs: loading…");
        TextView spendingStatus = small("Company-funded budget: loading…");
        c.addView(profileStatus);
        c.addView(jobsStatus);
        c.addView(spendingStatus);

        Button post = primary("＋  Post New Work");
        post.setOnClickListener(v -> showPostWork());
        c.addView(post);

        Button refresh = secondary("↻  Refresh Company Status");
        refresh.setOnClickListener(v -> showCompanyDashboard());
        c.addView(refresh);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showCompanyPortal());
        c.addView(back);

        new Thread(() -> {
            String profileMessage;
            String jobsMessage;
            String spendingMessage;
            try {
                JSONArray profiles = apiArray("/rest/v1/company_profiles?select=id,legal_name,verification_status&id=eq." + userId + "&limit=1");
                if (profiles.length() == 0) {
                    profileMessage = "Company profile: not submitted yet. Use Post New Work to submit it for review.";
                } else {
                    JSONObject profile = profiles.getJSONObject(0);
                    profileMessage = "Company: " + profile.optString("legal_name", "Company")
                            + " • Verification: " + profile.optString("verification_status", "pending");
                }
                JSONArray jobs = apiArray("/rest/v1/jobs?select=id,title,status,payment_amount,task_limit,currency,created_at&company_id=eq." + userId + "&order=created_at.desc&limit=1000");
                int openCount = 0;
                int totalCount = jobs.length();
                double totalBudget = 0.0;
                for (int i = 0; i < jobs.length(); i++) {
                    JSONObject job = jobs.getJSONObject(i);
                    if ("open".equalsIgnoreCase(job.optString("status", ""))) openCount++;
                    totalBudget += job.optDouble("payment_amount", 0.0) * job.optInt("task_limit", 1) * 2.0;
                }
                jobsMessage = "Your jobs: " + totalCount + " total • " + openCount + " currently open to workers";
                spendingMessage = "Recorded job budget: ₹" + String.format(Locale.ROOT, "%.2f", totalBudget)
                        + " (includes worker pool + platform share; not a payment receipt)";
                final JSONArray companyJobs = jobs;
                runOnUiThread(() -> {
                    c.addView(heading("Your Job History"));
                    if (companyJobs.length() == 0) {
                        c.addView(small("No company jobs saved yet. Submit a company profile first; verified companies can then create jobs."));
                    } else {
                        for (int i = 0; i < companyJobs.length(); i++) {
                            JSONObject job = companyJobs.optJSONObject(i);
                            if (job == null) continue;
                            LinearLayout item = card();
                            item.addView(tv(job.optString("title", "Untitled job"), 16, WHITE));
                            item.addView(small("Status: " + job.optString("status", "unknown")));
                            double taskPay = job.optDouble("payment_amount", 0.0);
                            int taskLimit = job.optInt("task_limit", 1);
                            item.addView(small("Worker payment per task: " + job.optString("currency", "INR") + " " 
                                    + String.format(Locale.ROOT, "%.2f", taskPay) + " • Work slots: " + taskLimit));
                            item.addView(small("Estimated total funding: " + job.optString("currency", "INR") + " "
                                    + String.format(Locale.ROOT, "%.2f", taskPay * taskLimit * 2.0)
                                    + " • This is not proof of payment."));
                            c.addView(item);
                            setMargins(item, 0, 6, 0, 6);
                        }
                    }
                });
            } catch (Exception ex) {
                profileMessage = "Company status unavailable: " + (ex.getMessage() == null ? "Please refresh." : ex.getMessage());
                jobsMessage = "Your jobs could not be loaded. Check sign-in and access permissions.";
                spendingMessage = "Budget total unavailable until company records load.";
            }
            final String pMessage = profileMessage;
            final String jMessage = jobsMessage;
            final String sMessage = spendingMessage;
            runOnUiThread(() -> {
                profileStatus.setText(pMessage);
                jobsStatus.setText(jMessage);
                spendingStatus.setText(sMessage);
            });
        }).start();
    }

    private void showPostWork() {
        startScreen(false);
        LinearLayout c = content();
        c.addView(heading("Post New Work"));
        c.addView(small("Real company jobs are saved securely. New company profiles require verification before a job can be funded or shown to workers."));

        EditText companyName = input("Company / Business Legal Name");
        EditText titleField = input("Job Title / Work Name");
        EditText descriptionField = input("Job Description");
        c.addView(companyName);
        c.addView(titleField);
        c.addView(descriptionField);

        Spinner categoryField = spinner(new String[]{"Select Category","Product Listing","Data Entry","Content Review","Translation","Research","Image / Media Work","Customer Support","AI Data / Annotation","Other"});
        c.addView(categoryField);
        Spinner locationField = spinner(new String[]{"Worker Location","Worldwide","India","United States","United Kingdom","Europe","Middle East","Asia","Africa","Other / Multiple Countries"});
        c.addView(locationField);
        Spinner currencyField = spinner(new String[]{"Currency","INR"});
        c.addView(currencyField);

        EditText budgetField = input("Total Company Budget (INR)");
        EditText workersField = input("Number of Work Slots (1–1000000)");
        c.addView(budgetField);
        c.addView(workersField);
        EditText deadlineField = input("Deadline (optional, describe date/time)");
        EditText skillsField = input("Required Skills (optional)");
        EditText languageField = input("Required Language (optional)");
        c.addView(deadlineField);
        c.addView(skillsField);
        c.addView(languageField);

        CheckBox remote = new CheckBox(this);
        remote.setText("Remote / Online Work");
        remote.setChecked(true);
        remote.setTextColor(TEXT);
        c.addView(remote);
        CheckBox agree = new CheckBox(this);
        agree.setText("I confirm the work is lawful, genuine, and the budget is accurate.");
        agree.setTextColor(TEXT);
        c.addView(agree);

        Button preview = secondary("🧠 Preview Budget Split");
        preview.setOnClickListener(v -> showAllocationPreview());
        c.addView(preview);

        Button post = primary("🌍 Save Company Profile / Submit Job");
        post.setOnClickListener(v -> {
            if (accessToken == null || accessToken.isEmpty() || userId == null || userId.isEmpty()) {
                Toast.makeText(this, "Please sign in to your Viyzo account first.", Toast.LENGTH_LONG).show();
                showLogin();
                return;
            }
            String company = companyName.getText().toString().trim();
            String title = titleField.getText().toString().trim();
            String description = descriptionField.getText().toString().trim();
            String budgetText = budgetField.getText().toString().trim().replace(",", "");
            String workersText = workersField.getText().toString().trim().replace(",", "");
            if (company.isEmpty() || title.isEmpty() || description.isEmpty() || budgetText.isEmpty() || workersText.isEmpty()) {
                Toast.makeText(this, "Enter company name, job title, description, total budget and work slots.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!agree.isChecked()) {
                Toast.makeText(this, "Please confirm the work and budget are genuine.", Toast.LENGTH_LONG).show();
                return;
            }
            final double totalBudget;
            final int slots;
            try {
                totalBudget = Double.parseDouble(budgetText);
                slots = Integer.parseInt(workersText);
                if (!Double.isFinite(totalBudget) || totalBudget < 100 || totalBudget > 1000000 || slots < 1 || slots > 1000000) {
                    throw new NumberFormatException();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "For this INR payment flow, budget must be ₹100–₹10,00,000 and work slots 1–1,000,000.", Toast.LENGTH_LONG).show();
                return;
            }
            final String selectedCategory = categoryField.getSelectedItemPosition() == 0 ? "Other" : String.valueOf(categoryField.getSelectedItem());
            final String selectedLocation = locationField.getSelectedItemPosition() == 0 ? "India" : String.valueOf(locationField.getSelectedItem());
            final String selectedTitle = title;
            final String selectedDescription = description
                    + "\\nSkills: " + (skillsField.getText().toString().trim().isEmpty() ? "Not specified" : skillsField.getText().toString().trim())
                    + "\\nLanguage: " + (languageField.getText().toString().trim().isEmpty() ? "Not specified" : languageField.getText().toString().trim())
                    + "\\nDeadline: " + (deadlineField.getText().toString().trim().isEmpty() ? "Not specified" : deadlineField.getText().toString().trim())
                    + "\\nRemote: " + (remote.isChecked() ? "Yes" : "No");
            post.setEnabled(false);
            post.setText("Saving securely…");
            new Thread(() -> {
                String outcome;
                try {
                    JSONArray profiles = apiArray("/rest/v1/company_profiles?select=id,legal_name,verification_status&id=eq." + userId + "&limit=1");
                    JSONObject profile = profiles.length() > 0 ? profiles.getJSONObject(0) : null;
                    if (profile == null) {
                        JSONObject newProfile = new JSONObject();
                        newProfile.put("id", userId);
                        newProfile.put("legal_name", company);
                        newProfile.put("display_name", company);
                        newProfile.put("country", "IN");
                        newProfile.put("contact_email", userEmail);
                        newProfile.put("verification_status", "pending");
                        apiRequest("/rest/v1/company_profiles", "POST", newProfile, true, "return=minimal");
                        outcome = "Company profile submitted for review. Job posting and payments will unlock after company verification. No job or payment was created yet.";
                    } else if (!"verified".equalsIgnoreCase(profile.optString("verification_status", "pending"))) {
                        outcome = "Company profile status: " + profile.optString("verification_status", "pending") + ". Company verification is required before a real job can be posted or funded.";
                    } else {
                        double perTask = totalBudget / (slots * 2.0);
                        if (perTask <= 0 || !Double.isFinite(perTask)) throw new Exception("Budget is too small for the selected number of work slots.");
                        perTask = Math.round(perTask * 100.0) / 100.0;
                        if (perTask < 0.01) {
                            throw new Exception("The budget is too small for this many work slots. Increase the budget or reduce the number of slots so each slot has at least ₹0.01 for the worker and ₹0.01 for Viyzo.");
                        }
                        JSONObject job = new JSONObject();
                        job.put("company_id", userId);
                        job.put("title", selectedTitle);
                        job.put("description", selectedDescription);
                        job.put("category", selectedCategory);
                        job.put("country", selectedLocation);
                        job.put("payment_amount", perTask);
                        job.put("currency", "INR");
                        job.put("task_limit", slots);
                        job.put("status", "pending_funding");
                        apiRequest("/rest/v1/jobs", "POST", job, true, "return=minimal");
                        outcome = "Job saved securely as pending funding. It will not appear to workers until the verified company funds it and backend review allows it.";
                    }
                } catch (Exception ex) {
                    outcome = "Could not save company/job: " + ex.getMessage();
                }
                final String message = outcome;
                runOnUiThread(() -> {
                    post.setEnabled(true);
                    post.setText("🌍 Save Company Profile / Submit Job");
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    showCompanyDashboard();
                });
            }).start();
        });
        c.addView(post);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showCompanyPortal());
        c.addView(back);
    }

    private void showAllocationPreview() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("AI Allocation Preview"));
        c.addView(small(
                "Example calculation using the configured 50/50 model."
        ));

        double sample = 2000.0;
        double worker = workerPool(sample);
        double platform = viyzoShare(sample);

        c.addView(wideInfo("💼", "Example Total Job Value",
                "₹" + formatNumber(sample)));
        c.addView(wideInfo("👥", "Worker Pool",
                "₹" + formatNumber(worker)));
        c.addView(wideInfo("🏢", "Viyzo Platform Share",
                "₹" + formatNumber(platform)));

        int workers = 10;
        c.addView(wideInfo("👥", "Example Workers",
                String.valueOf(workers)));
        c.addView(wideInfo("💰", "Example Pool/Worker",
                "₹" + formatNumber(worker / workers)));

        c.addView(wideInfo("🤖", "Manager Factors",
                "Skills • Language • Quality • Availability • Deadline • Verification"));

        Button back = secondary("← Back to Work Posting");
        back.setOnClickListener(v -> showPostWork());
        c.addView(back);
    }

    // ============================================================
    // NOTIFICATIONS / LANGUAGE / SETTINGS / HELP / ABOUT
    // ============================================================
    private void showNotifications() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Notifications"));

        c.addView(wideInfo("⚡", "Urgent Work",
                "Eligible urgent work can appear here"));
        c.addView(wideInfo("🤖", "AI Match",
                "A new AI-matched work opportunity may be available"));
        c.addView(wideInfo("📦", "Bulk Pool",
                "Large batches can be split into assignments"));
        c.addView(wideInfo("🔁", "Recurring",
                "Repeat work can create future assignments"));
        c.addView(wideInfo("🛡", "Verification",
                "Complete required verification for eligible work"));
        c.addView(wideInfo("💰", "Earnings",
                "Approved work can move to pending/available balance"));

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showDashboard());
        c.addView(back);
    }

    private void showLanguage() {
        showCountryLanguageSetup();
    }

    // Live diagnostics: check Auth reachability without reading private user data.
    private void showSystemHealth() {
        startScreen(false);
        LinearLayout c = content();
        c.addView(heading("System Health"));
        c.addView(small("Checks the Viyzo authentication service and your current sign-in session. This test does not change account data."));

        LinearLayout authCard = card();
        authCard.addView(tv("Authentication service", 15, WHITE));
        TextView authStatus = tv("Not checked yet", 13, GOLD);
        authCard.addView(authStatus);
        c.addView(authCard);
        setMargins(authCard, 0, 0, 0, 10);

        LinearLayout sessionCard = card();
        sessionCard.addView(tv("Current session", 15, WHITE));
        TextView sessionStatus = tv(accessToken.isEmpty() ? "Signed out" : "Session token present — validation pending", 13, accessToken.isEmpty() ? GRAY : GOLD);
        sessionCard.addView(sessionStatus);
        c.addView(sessionCard);
        setMargins(sessionCard, 0, 0, 0, 10);

        TextView details = small("Run a check to get the latest result.");
        c.addView(details);

        Button check = primary("Check Connection Now");
        check.setOnClickListener(v -> {
            check.setEnabled(false);
            authStatus.setText("Checking…");
            authStatus.setTextColor(GOLD);
            details.setText("Contacting authentication service…");
            new Thread(() -> {
                String serviceResult;
                String sessionResult;
                String detailResult;
                boolean serviceOk = false;
                boolean sessionOk = false;
                try {
                    apiRequest("/auth/v1/health", "GET", null, false, "");
                    serviceOk = true;
                    serviceResult = "Reachable";
                    detailResult = "Authentication endpoint responded successfully.";
                } catch (Exception ex) {
                    serviceResult = "Could not confirm";
                    detailResult = "Service check failed: " + ex.getMessage();
                }
                if (!accessToken.isEmpty()) {
                    try {
                        apiRequest("/auth/v1/user", "GET", null, true, "");
                        sessionOk = true;
                        sessionResult = "Signed in • session validated";
                    } catch (Exception ex) {
                        sessionResult = "Session invalid or expired — sign in again";
                        if (serviceOk) detailResult = "Authentication service is reachable, but this session could not be validated.";
                    }
                } else {
                    sessionResult = "Signed out • sign in to validate an account";
                }
                final boolean fServiceOk = serviceOk;
                final boolean fSessionOk = sessionOk;
                final String fService = serviceResult;
                final String fSession = sessionResult;
                final String fDetail = detailResult;
                runOnUiThread(() -> {
                    authStatus.setText(fService);
                    authStatus.setTextColor(fServiceOk ? GREEN : RED);
                    sessionStatus.setText(fSession);
                    sessionStatus.setTextColor(accessToken.isEmpty() ? GRAY : (fSessionOk ? GREEN : RED));
                    details.setText(fDetail);
                    check.setEnabled(true);
                });
            }).start();
        });
        c.addView(check);

        Button back = secondary("← Back to Settings");
        back.setOnClickListener(v -> showSettings());
        c.addView(back);
    }

    private void showSettings() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Settings"));

        Button health = secondary("🩺  System Health & Connection");
        health.setOnClickListener(v -> showSystemHealth());
        c.addView(health);

        Button notifications = secondary("🔔  Notification Settings");
        notifications.setOnClickListener(v -> showNotifications());
        c.addView(notifications);

        Button language = secondary("🌐  Language");
        language.setOnClickListener(v -> showLanguage());
        c.addView(language);

        Button privacy = secondary("🔐  Privacy & Data");
        privacy.setOnClickListener(v -> showPrivacy());
        c.addView(privacy);

        Button payment = secondary("💳  Payment Settings");
        payment.setOnClickListener(v -> showPaymentSettings());
        c.addView(payment);

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

    private void showPrivacy() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Privacy & Data"));
        c.addView(small(
                "Production privacy controls should be enforced by secure backend policies and applicable law."
        ));

        c.addView(wideInfo("🔒", "Data Minimization",
                "Collect only data needed for the stated purpose"));
        c.addView(wideInfo("🛡", "Security",
                "Encrypt sensitive data and restrict access"));
        c.addView(wideInfo("📷", "Selfie / ID",
                "Use approved verification providers where required"));
        c.addView(wideInfo("🗑", "Data Retention",
                "Retention/deletion rules must be defined"));
        c.addView(wideInfo("📄", "Consent",
                "Clear consent and privacy notices are required"));

        Button back = secondary("← Back to Settings");
        back.setOnClickListener(v -> showSettings());
        c.addView(back);
    }

    private void showHelp() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Help & Support"));

        c.addView(wideInfo("💬", "Worker Support",
                "Jobs, accounts, verification and payouts"));
        c.addView(wideInfo("🏢", "Company Support",
                "Job posting, company accounts and payments"));
        c.addView(wideInfo("📚", "Help Center",
                "Learn how Viyzo work flows are designed"));
        c.addView(wideInfo("⚖", "Disputes",
                "Production system needs review and dispute processes"));

        Button contact = primary("Contact Support");
        contact.setOnClickListener(v -> Toast.makeText(
                this,
                "Support contact system will connect to backend.",
                Toast.LENGTH_SHORT).show());
        c.addView(contact);

        Button back = secondary("← Back");
        back.setOnClickListener(v -> showDashboard());
        c.addView(back);
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
                "Matching and work distribution foundation"));
        c.addView(wideInfo("🌍", "Global Companies",
                "Company work intake foundation"));
        c.addView(wideInfo("👥", "Global Workers",
                "Worker discovery and assignment foundation"));
        c.addView(wideInfo("📦", "Work Supply",
                "Urgent • Bulk • Quick • Project • Recurring"));
        c.addView(wideInfo("🛡", "Verification",
                "Verification levels can control access and payouts"));
        c.addView(wideInfo("💳", "Payments",
                "Country/provider backend required"));
        c.addView(wideInfo("⚙", "Business Model",
                "Configurable 50% worker pool / 50% Viyzo platform"));

        TextView v = tv("Version 2.0 UI foundation", 11, GRAY);
        v.setGravity(Gravity.CENTER);
        c.addView(v);
    }

    // ============================================================
    // CALCULATIONS
    // ============================================================
    // ============================================================
    // AI MASTER VOICE EXPERIENCE
    // ============================================================
    private void initAIMasterVoice() {
        aiTtsInitializing = true;
        try {
            aiTts = new TextToSpeech(getApplicationContext(), status -> runOnUiThread(() -> {
                aiTtsInitializing = false;
                if (status != TextToSpeech.SUCCESS || aiTts == null) {
                    aiTtsReady = false;
                    if (aiStatusView != null) aiStatusView.setText("Text-to-speech unavailable. Install/enable an Android TTS engine.");
                    Toast.makeText(MainActivity.this, "AI voice engine unavailable. Enable Speech Services by Google or another TTS engine in Android settings.", Toast.LENGTH_LONG).show();
                    return;
                }

                int result = aiTts.setLanguage(aiLocale == null ? Locale.getDefault() : aiLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Locale deviceLocale = Locale.getDefault();
                    result = aiTts.setLanguage(deviceLocale);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        result = aiTts.setLanguage(Locale.US);
                        if (aiStatusView != null) aiStatusView.setText("Selected language voice unavailable; trying English voice.");
                    }
                }
                aiTtsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
                aiTts.setSpeechRate(0.90f);
                aiTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String utteranceId) {
                        runOnUiThread(() -> {
                            if (aiStatusView != null) aiStatusView.setText("🔊 AI Master is speaking…");
                        });
                    }
                    @Override public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            if (aiStatusView != null) aiStatusView.setText("🟢 AI Master ready • Tap mic to ask");
                        });
                    }
                    @Override public void onError(String utteranceId) {
                        runOnUiThread(() -> {
                            if (aiStatusView != null) aiStatusView.setText("Voice playback failed. Check phone media volume and TTS voice data.");
                        });
                    }
                });

                if (aiTtsReady && !pendingAISpeech.isEmpty()) {
                    String queued = pendingAISpeech;
                    pendingAISpeech = "";
                    speakAI(queued);
                } else if (!aiTtsReady && aiStatusView != null) {
                    aiStatusView.setText("No supported voice installed. Install voice data in Android Text-to-Speech settings.");
                }
            }));
        } catch (Exception e) {
            aiTtsInitializing = false;
            aiTtsReady = false;
            if (aiStatusView != null) aiStatusView.setText("Could not start Android text-to-speech.");
        }
    }

    private void addAIMasterOverlay() {
        if (frame == null) return;
        LinearLayout panel = column();
        panel.setPadding(dp(10), dp(8), dp(10), dp(8));
        panel.setBackground(outlined(Color.rgb(20, 25, 54), PRIMARY, 18));
        panel.setElevation(dp(12));
        panel.setOnClickListener(v -> showAIMasterVoiceScreen());

        LinearLayout top = row();
        TextView icon = tv("🤖", 23, WHITE);
        icon.setGravity(Gravity.CENTER);
        top.addView(icon, new LinearLayout.LayoutParams(dp(35), dp(35)));
        LinearLayout titleColumn = column();
        TextView title = tv("AI MASTER", 12, WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleColumn.addView(title);
        titleColumn.addView(tv("Tap panel for step-by-step voice help", 9, GRAY));
        top.addView(titleColumn, new LinearLayout.LayoutParams(0, dp(37), 1));
        Button collapse = new Button(this);
        collapse.setText("−");
        collapse.setTextSize(18);
        collapse.setAllCaps(false);
        collapse.setTextColor(WHITE);
        collapse.setPadding(0, 0, 0, 0);
        collapse.setBackground(outlined(CARD2, BORDER, 14));
        collapse.setContentDescription("Collapse or expand AI Master panel");
        collapse.setOnClickListener(v -> {
            aiPanelCollapsed = !aiPanelCollapsed;
            if (aiStatusView != null) aiStatusView.setVisibility(aiPanelCollapsed ? View.GONE : View.VISIBLE);
            if (aiBubbleView != null) aiBubbleView.setVisibility(aiPanelCollapsed ? View.GONE : View.VISIBLE);
            collapse.setText(aiPanelCollapsed ? "+" : "−");
            panel.setPadding(dp(10), dp(6), dp(10), dp(6));
        });
        top.addView(collapse, new LinearLayout.LayoutParams(dp(38), dp(43)));
        Button mic = new Button(this);
        mic.setText("🎙️");
        mic.setTextSize(17);
        mic.setAllCaps(false);
        mic.setTextColor(WHITE);
        mic.setPadding(0, 0, 0, 0);
        mic.setBackground(primaryBg());
        mic.setOnClickListener(v -> startAIVoiceInput());
        top.addView(mic, new LinearLayout.LayoutParams(dp(48), dp(43)));
        panel.addView(top);

        aiStatusView = tv("🟢 AI Master ready • Tap mic to ask", 10, GRAY);
        aiStatusView.setPadding(dp(4), dp(4), dp(4), 0);
        panel.addView(aiStatusView);
        aiBubbleView = tv("Tap any main action for spoken guidance, or ask about accounts, jobs, work steps, verification, payments or earnings.", 11, TEXT);
        aiBubbleView.setMaxLines(3);
        aiBubbleView.setPadding(dp(4), dp(3), dp(4), 0);
        panel.addView(aiBubbleView);

        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        fp.setMargins(dp(12), 0, dp(12), dp(10));
        frame.addView(panel, fp);
    }

    private void startAIVoiceInput() {
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AI_AUDIO_PERMISSION_REQUEST);
            if (aiStatusView != null) aiStatusView.setText("Allow microphone permission, then tap mic again.");
            return;
        }
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, aiLocale.toLanguageTag());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, aiText(
                    "Ask AI Master your question", "AI Master से अपना सवाल पूछें",
                    "AI Master-কে আপনার প্রশ্ন বলুন", "AI Master سے سوال پوچھیں",
                    "AI Master'a sorunuzu söyleyin", "AI Master'a sorunuzu söyleyin"));
            startActivityForResult(intent, AI_VOICE_REQUEST);
            if (aiStatusView != null) aiStatusView.setText("🎙️ Listening… speak now");
        } catch (Exception e) {
            Toast.makeText(this, "Voice input is not available on this device. You can still use the app.", Toast.LENGTH_LONG).show();
            if (aiStatusView != null) aiStatusView.setText("Voice input unavailable on this device.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AI_VOICE_REQUEST && resultCode == RESULT_OK && data != null) {
            java.util.ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String question = results.get(0);
                if (aiBubbleView != null) aiBubbleView.setText("You: " + question + "\\n\\nAI Master: Connecting securely…");
                if (aiStatusView != null) aiStatusView.setText("🤖 AI Master is preparing your answer…");
                askAIMasterBackend(question);
            }
        }
    }


    // Calls the secured server-side AI endpoint; local help remains as a fallback.
    private void askAIMasterBackend(String question) {
        final String token = accessToken;
        if (token == null || token.trim().isEmpty()) {
            String answer = answerAIQuestion(question);
            if (aiBubbleView != null) aiBubbleView.setText("You: " + question + "\n\nAI Master: " + answer);
            speakAI(answer);
            return;
        }
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(SUPABASE_URL + "/functions/v1/ai-master").openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(25000);
                connection.setRequestProperty("apikey", SUPABASE_PUBLISHABLE_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true);
                JSONObject payload = new JSONObject();
                payload.put("question", question);
                payload.put("language", aiLocale == null ? "en" : aiLocale.toLanguageTag());
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }
                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder raw = new StringBuilder();
                if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) raw.append(line);
                }
                JSONObject result = raw.length() > 0 ? new JSONObject(raw.toString()) : new JSONObject();
                String answer = code >= 200 && code < 300 && result.optBoolean("ok")
                        ? result.optString("answer", "").trim() : "";
                boolean liveAnswer = !answer.isEmpty();
                if (!liveAnswer) answer = answerAIQuestion(question);
                final String finalAnswer = answer;
                final String status = liveAnswer ? "🟢 AI Master • Secure AI response" :
                        (code == 503 ? "Built-in help active • AI provider setup pending" : "Built-in help active • AI service unavailable");
                runOnUiThread(() -> {
                    if (aiBubbleView != null) aiBubbleView.setText("You: " + question + "\n\nAI Master: " + finalAnswer);
                    if (aiStatusView != null) aiStatusView.setText(status);
                    speakAI(finalAnswer);
                });
            } catch (Exception e) {
                String answer = answerAIQuestion(question);
                runOnUiThread(() -> {
                    if (aiBubbleView != null) aiBubbleView.setText("You: " + question + "\n\nAI Master: " + answer);
                    if (aiStatusView != null) aiStatusView.setText("Built-in help active • Check connection");
                    speakAI(answer);
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, "Viyzo-AI-Master").start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AI_AUDIO_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAIVoiceInput();
            } else {
                Toast.makeText(this, "Microphone permission is needed for voice questions. You can still read the AI guide.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void speakAI(String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (aiBubbleView != null) aiBubbleView.setText(text);
        // Keep speech until the asynchronous Android TTS engine has finished starting.
        if (aiTts == null || aiTtsInitializing) {
            pendingAISpeech = text;
            if (aiStatusView != null) aiStatusView.setText("AI Master is preparing voice…");
            return;
        }
        if (!aiTtsReady) {
            pendingAISpeech = text;
            if (aiStatusView != null) aiStatusView.setText("Voice unavailable. Install/enable a TTS engine and voice data in Android settings.");
            Toast.makeText(this, "AI voice is not ready. Please enable Speech Services by Google and install voice data in Android settings.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            aiTts.setAudioAttributes(new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            int result;
            if (Build.VERSION.SDK_INT >= 21) {
                result = aiTts.speak(text, TextToSpeech.QUEUE_FLUSH, null,
                        "viyzo_ai_" + System.currentTimeMillis());
            } else {
                result = aiTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
            if (result == TextToSpeech.ERROR && aiStatusView != null) {
                aiStatusView.setText("Could not play voice. Check media volume and Android TTS settings.");
            }
        } catch (Exception e) {
            if (aiStatusView != null) aiStatusView.setText("Voice playback error. Check Android TTS settings.");
        }
    }

    private void showAIMasterVoiceScreen() {
        startScreen(false);
        LinearLayout c = content();
        c.setPadding(dp(20), dp(24), dp(20), dp(150));
        TextView back = tv("← Back", 15, TEXT);
        back.setPadding(0, dp(4), 0, dp(18));
        back.setOnClickListener(v -> showHome());
        c.addView(back);
        LinearLayout hero = column();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(12), dp(20), dp(12), dp(20));
        TextView bot = tv("🤖", 66, WHITE);
        bot.setGravity(Gravity.CENTER);
        hero.addView(bot, new LinearLayout.LayoutParams(-1, dp(100)));
        TextView title = tv("AI MASTER", 27, WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        hero.addView(title);
        TextView subtitle = tv("Your voice guide for every step", 14, GRAY);
        subtitle.setGravity(Gravity.CENTER);
        hero.addView(subtitle);
        c.addView(hero);
        TextView status = tv("Tap the microphone and ask your question", 14, TEXT);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(8), dp(10), dp(8), dp(10));
        c.addView(status);
        Button mic = primary("🎙  TALK TO AI MASTER");
        mic.setOnClickListener(v -> startAIVoiceInput());
        c.addView(mic);
        Button replay = secondary("🔊  Speak the step-by-step welcome");
        replay.setOnClickListener(v -> speakAI(aiWelcomeMessage()));
        c.addView(replay);
        c.addView(heading("What do you need help with?"));
        Button account = secondary("Create account / Login");
        account.setOnClickListener(v -> { String a = aiText("To start, choose Get Started and fill in your own details.", "शुरू करने के लिए Get Started दबाएँ और अपनी जानकारी भरें।", "শুরু করতে Get Started চাপুন এবং নিজের তথ্য দিন।", "شروع کرنے کے لیے Get Started دبائیں اور اپنی معلومات درج کریں۔", "Başlamak için Get Started'a dokunun ve bilgilerinizi girin.", "ابدأ بالضغط على Get Started وأدخل معلوماتك."); speakAI(a); });
        c.addView(account);
        Button jobs = secondary("Find and complete work");
        jobs.setOnClickListener(v -> { speakAI(aiText("Open Jobs and read the task details before accepting.", "Jobs खोलें और काम स्वीकार करने से पहले उसकी जानकारी पढ़ें।", "Jobs খুলুন এবং কাজ গ্রহণের আগে বিবরণ পড়ুন।", "Jobs کھولیں اور کام قبول کرنے سے پہلے تفصیل پڑھیں۔", "Jobs bölümünü açın ve kabul etmeden önce görev ayrıntılarını okuyun.", "افتح Jobs واقرأ التفاصيل قبل قبول المهمة.")); showJobs(); });
        c.addView(jobs);
        Button verification = secondary("Verification / KYC help");
        verification.setOnClickListener(v -> { speakAI(aiText("Open Profile and then Worker Verification.", "Profile खोलें, फिर Worker Verification चुनें।", "Profile খুলুন, তারপর Worker Verification বেছে নিন।", "Profile کھولیں، پھر Worker Verification منتخب کریں۔", "Profile bölümünü açıp Worker Verification'ı seçin.", "افتح Profile ثم اختر Worker Verification.")); showWorkerVerification(); });
        c.addView(verification);
        Button earnings = secondary("Earnings / Payments help");
        earnings.setOnClickListener(v -> { speakAI(aiText("Open Earnings to check your balance and payout information.", "बैलेंस और पेमेंट जानकारी के लिए Earnings खोलें।", "ব্যালেন্স ও পেমেন্টের তথ্য দেখতে Earnings খুলুন।", "بیلنس اور ادائیگی کی معلومات کے لیے Earnings کھولیں۔", "Bakiye ve ödeme bilgileri için Earnings bölümünü açın.", "افتح Earnings لمعرفة الرصيد ومعلومات الدفع.")); showEarnings(); });
        c.addView(earnings);
        if (aiBubbleView != null) aiBubbleView.setText("AI Master is ready. Tap the microphone to ask by voice.");
    }

    private void setAILanguage(String label) {
        String l = label.toLowerCase(Locale.ROOT);
        if (l.contains("हिन्दी")) aiLocale = new Locale("hi", "IN");
        else if (l.contains("বাংলা")) aiLocale = new Locale("bn", "BD");
        else if (l.contains("اردو")) aiLocale = new Locale("ur", "PK");
        else if (l.contains("العربية")) aiLocale = new Locale("ar");
        else if (l.contains("türkçe")) aiLocale = new Locale("tr", "TR");
        else aiLocale = new Locale("en", "US");
        if (aiTts != null) {
            int result = aiTts.setLanguage(aiLocale);
            aiTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
        }
    }

    private String aiText(String en, String hi, String bn, String ur, String ar, String tr) {
        String lang = aiLocale.getLanguage();
        if ("hi".equals(lang)) return hi;
        if ("bn".equals(lang)) return bn;
        if ("ur".equals(lang)) return ur;
        if ("ar".equals(lang)) return ar;
        if ("tr".equals(lang)) return tr;
        return en;
    }

    private String aiWelcomeMessage() {
        return aiText(
                "Welcome to Viyzo Worker. I am AI Master. First create your account, choose your country and language, complete the verification steps available to you, then open Jobs and read each task carefully before accepting it. Demo jobs shown in this version are examples, not confirmed live company work. Real work availability will appear when the live server is connected.",
                "Viyzo Worker में आपका स्वागत है। मैं AI Master हूँ। पहले अपना अकाउंट बनाइए, देश और भाषा चुनिए, उपलब्ध वेरिफिकेशन पूरा कीजिए, फिर Jobs खोलकर काम की जानकारी पढ़कर ही काम स्वीकार कीजिए। अभी दिखने वाले डेमो जॉब उदाहरण हैं, पक्के लाइव कंपनी जॉब नहीं। असली काम की उपलब्धता लाइव सर्वर जुड़ने पर दिखेगी।",
                "Viyzo Worker-এ স্বাগতম। আমি AI Master। প্রথমে অ্যাকাউন্ট তৈরি করুন, দেশ ও ভাষা বেছে নিন, উপলব্ধ যাচাইকরণ সম্পন্ন করুন, তারপর Jobs খুলে কাজের বিবরণ পড়ে কাজ গ্রহণ করুন। এখনকার ডেমো কাজগুলো উদাহরণ, নিশ্চিত লাইভ কোম্পানির কাজ নয়। লাইভ সার্ভার যুক্ত হলে প্রকৃত কাজের তথ্য দেখানো যাবে।",
                "Viyzo Worker میں خوش آمدید۔ میں AI Master ہوں۔ پہلے اکاؤنٹ بنائیں، ملک اور زبان منتخب کریں، دستیاب تصدیق مکمل کریں، پھر Jobs کھول کر کام کی تفصیل پڑھ کر ہی کام قبول کریں۔ ابھی دکھائے گئے ڈیمو کام مثالیں ہیں، تصدیق شدہ لائیو کمپنی کے کام نہیں۔ اصل دستیابی لائیو سرور جڑنے پر دکھائی جائے گی۔",
                "مرحباً بك في Viyzo Worker. أنا AI Master. أنشئ حسابك أولاً، واختر بلدك ولغتك، وأكمل خطوات التحقق المتاحة، ثم افتح الوظائف واقرأ التفاصيل قبل قبول أي مهمة. الوظائف التجريبية الحالية أمثلة وليست وظائف حقيقية مؤكدة. ستظهر الوظائف الفعلية بعد ربط الخادم المباشر.",
                "Viyzo Worker'a hoş geldiniz. Ben AI Master. Önce hesap oluşturun, ülkenizi ve dilinizi seçin, mevcut doğrulama adımlarını tamamlayın, ardından Jobs bölümünü açıp ayrıntıları okuyarak işi kabul edin. Şu anki demo işler örnektir, doğrulanmış canlı işler değildir. Gerçek işler canlı sunucu bağlandığında gösterilir.");
    }

    private String answerAIQuestion(String q) {
        String x = q == null ? "" : q.toLowerCase(Locale.ROOT);
        boolean account = hasAny(x, "account", "register", "sign up", "login", "अकाउंट", "खाता", "रजिस्टर", "একাউন্ট", "اکاؤنٹ", "حساب");
        boolean job = hasAny(x, "job", "work", "काम", "जॉब", "কাজ", "کام", "وظيفة", "iş");
        boolean money = hasAny(x, "earning", "money", "payment", "withdraw", "पैसा", "कमाई", "पेमेंट", "টাকা", "پیسے", "مال", "ödeme");
        boolean kyc = hasAny(x, "kyc", "verify", "verification", "document", "selfie", "पहचान", "वेरिफ", "दस्तावेज", "যাচাই", "تصدیق", "doğrula");
        boolean company = hasAny(x, "company", "client", "business", "कंपनी", "क्लाइंट", "কোম্পানি", "کمپنی", "شركة", "şirket");
        if (account) return aiText("Open Create Account, enter only your own details, choose your country and language, set a Viyzo password, and submit. Never share your email-provider password or OTP with anyone. This demo does not yet create a secure server account.", "Create Account खोलें, अपनी सही जानकारी भरें, देश और भाषा चुनें, Viyzo पासवर्ड बनाएँ और सबमिट करें। अपना ईमेल पासवर्ड या OTP किसी को न दें। इस डेमो में अभी सुरक्षित सर्वर अकाउंट जुड़ा नहीं है।", "Create Account খুলুন, নিজের তথ্য দিন, দেশ ও ভাষা নির্বাচন করুন, Viyzo পাসওয়ার্ড সেট করে জমা দিন। ইমেইলের পাসওয়ার্ড বা OTP কাউকে দেবেন না। এই ডেমোতে নিরাপদ সার্ভার অ্যাকাউন্ট এখনও যুক্ত নয়।", "Create Account کھولیں، اپنی معلومات درج کریں، ملک اور زبان منتخب کریں، Viyzo پاس ورڈ بنائیں اور جمع کریں۔ اپنا ای میل پاس ورڈ یا OTP کسی کو نہ دیں۔ اس ڈیمو میں محفوظ سرور اکاؤنٹ ابھی منسلک نہیں۔", "Create Account bölümünü açın, kendi bilgilerinizi girin, ülke ve dili seçin ve Viyzo şifresi oluşturun. E-posta şifrenizi veya OTP'nizi kimseyle paylaşmayın. Bu demoda güvenli sunucu hesabı henüz bağlı değil.", "Create Account bölümünü açın, kendi bilgilerinizi girin, ülke ve dili seçin ve Viyzo şifresi oluşturun. E-posta şifrenizi veya OTP'nizi kimseyle paylaşmayın. Bu demoda güvenli sunucu hesabı henüz bağlı değil.");
        if (kyc) return aiText("Open Profile or Worker Verification. Complete only the steps offered for your country. Document requirements and payout checks must be configured safely for each country; do not upload sensitive documents unless the app shows a clear purpose and secure process.", "Profile या Worker Verification खोलें। अपने देश के लिए जो चरण दिखें वही पूरा करें। दस्तावेज और भुगतान की जाँच देश के अनुसार सुरक्षित तरीके से सेट होनी चाहिए। साफ कारण और सुरक्षित प्रक्रिया के बिना संवेदनशील दस्तावेज अपलोड न करें।", "Profile বা Worker Verification খুলুন। আপনার দেশের জন্য যে ধাপ দেখানো হয় তা সম্পন্ন করুন। স্পষ্ট কারণ ও নিরাপদ ব্যবস্থা ছাড়া সংবেদনশীল নথি আপলোড করবেন না।", "Profile یا Worker Verification کھولیں۔ اپنے ملک کے لیے دکھائے گئے مراحل مکمل کریں۔ واضح وجہ اور محفوظ طریقے کے بغیر حساس دستاویزات اپ لوڈ نہ کریں۔", "Profile veya Worker Verification bölümünü açın. Ülkeniz için gösterilen adımları tamamlayın. Açık amaç ve güvenli süreç olmadan hassas belgeleri yüklemeyin.", "Profile veya Worker Verification bölümünü açın. Ülkeniz için gösterilen adımları tamamlayın. Açık amaç ve güvenli süreç olmadan hassas belgeleri yüklemeyin.");
        if (money) return aiText("Open Earnings to review the demo balance and payout information. The configured business model allocates 50 percent to the worker pool and 50 percent to Viyzo's platform share before expenses. Earnings are not guaranteed; actual payouts require approved work, real company funds, and a connected payment backend.", "Earnings खोलकर डेमो बैलेंस और पेमेंट जानकारी देखें। मौजूदा मॉडल में 50% वर्कर पूल और 50% Viyzo प्लेटफॉर्म शेयर है; Viyzo के हिस्से से खर्च भी निकलेंगे। कमाई की गारंटी नहीं है। असली पेमेंट के लिए स्वीकृत काम, कंपनी के वास्तविक पैसे और पेमेंट बैकएंड चाहिए।", "Earnings খুলে ডেমো ব্যালেন্স ও পেমেন্ট তথ্য দেখুন। বর্তমান মডেলে ৫০% কর্মী পুল এবং ৫০% Viyzo প্ল্যাটফর্মের অংশ; খরচও এখান থেকে হবে। আয়ের নিশ্চয়তা নেই। প্রকৃত পেমেন্টের জন্য অনুমোদিত কাজ, কোম্পানির অর্থ ও পেমেন্ট ব্যাকএন্ড দরকার।", "Earnings کھول کر ڈیمو بیلنس دیکھیں۔ موجودہ ماڈل میں 50% ورکر پول اور 50% Viyzo پلیٹ فارم کا حصہ ہے، جس سے اخراجات بھی ادا ہوں گے۔ آمدنی کی ضمانت نہیں۔ حقیقی ادائیگی کے لیے منظور شدہ کام، کمپنی کے فنڈز اور پیمنٹ بیک اینڈ ضروری ہے۔", "Earnings bölümünden demo bakiyeyi inceleyin. Mevcut modelde %50 çalışan havuzuna, %50 Viyzo platform payına ayrılır; platform payı masrafları da karşılar. Kazanç garanti değildir; gerçek ödeme için onaylı iş, şirket fonu ve ödeme altyapısı gerekir.", "Earnings bölümünden demo bakiyeyi inceleyin. Mevcut modelde %50 çalışan havuzuna, %50 Viyzo platform payına ayrılır; platform payı masrafları da karşılar. Kazanç garanti değildir; gerçek ödeme için onaylı iş, şirket fonu ve ödeme altyapısı gerekir.");
        if (company) return aiText("Companies need to onboard, post genuine work with a clear budget, deadline, skills and quality rules, then fund the job. The Company Portal here is a UI foundation; it does not yet send a live job to workers without the backend.", "कंपनी को पहले जुड़ना होगा, असली काम का बजट, डेडलाइन, स्किल और गुणवत्ता नियम देने होंगे, फिर काम के लिए फंड करना होगा। Company Portal अभी UI फाउंडेशन है; बैकएंड के बिना लाइव जॉब वर्कर तक नहीं जाता।", "কোম্পানিকে যুক্ত হয়ে প্রকৃত কাজ, বাজেট, সময়সীমা, দক্ষতা ও মানের নিয়ম দিতে হবে এবং অর্থ জমা করতে হবে। ব্যাকএন্ড ছাড়া এই Company Portal থেকে লাইভ কাজ পাঠানো হয় না।", "کمپنی کو شامل ہو کر حقیقی کام، بجٹ، آخری تاریخ اور معیار بتانا ہوگا اور رقم فراہم کرنی ہوگی۔ بیک اینڈ کے بغیر یہ Company Portal لائیو کام نہیں بھیجتا۔", "Şirketlerin sisteme katılması, gerçek işi, bütçeyi, teslim tarihini ve kalite kurallarını belirtmesi ve işi finanse etmesi gerekir. Arka uç olmadan bu Company Portal canlı iş göndermez.", "Şirketlerin sisteme katılması, gerçek işi, bütçeyi, teslim tarihini ve kalite kurallarını belirtmesi ve işi finanse etmesi gerekir. Arka uç olmadan bu Company Portal canlı iş göndermez.");
        if (job) return aiText("Open Jobs, choose a task, check the company, requirements, workload, deadline and payment details, then accept only if you can complete it. The current list is demo data. It cannot tell you the true live amount of work until the job backend is connected.", "Jobs खोलें, काम चुनें, कंपनी, जरूरी स्किल, मात्रा, डेडलाइन और पेमेंट पढ़ें; तभी स्वीकार करें जब पूरा कर सकें। अभी की सूची डेमो डेटा है। लाइव बैकएंड जुड़ने तक असली उपलब्ध काम की संख्या नहीं बता सकती।", "Jobs খুলুন, কাজ বেছে নিয়ে কোম্পানি, দক্ষতা, পরিমাণ, সময়সীমা ও পেমেন্ট দেখুন। সম্পন্ন করতে পারবেন তবেই গ্রহণ করুন। বর্তমান তালিকা ডেমো ডেটা; লাইভ ব্যাকএন্ড ছাড়া প্রকৃত কাজের সংখ্যা জানা যাবে না।", "Jobs کھولیں، کام منتخب کریں، کمپنی، مہارت، مقدار، آخری تاریخ اور ادائیگی پڑھیں؛ صرف تب قبول کریں جب مکمل کر سکیں۔ موجودہ فہرست ڈیمو ہے۔ لائیو بیک اینڈ کے بغیر حقیقی دستیاب کام کی تعداد معلوم نہیں ہو سکتی۔", "Jobs bölümünü açın; şirketi, becerileri, miktarı, teslim tarihini ve ödemeyi kontrol edin. Yalnızca tamamlayabileceğiniz işi kabul edin. Şu anki liste demo verisidir; canlı arka uç olmadan gerçek iş sayısı bilinemez.", "Jobs bölümünü açın; şirketi, becerileri, miktarı, teslim tarihini ve ödemeyi kontrol edin. Yalnızca tamamlayabileceğiniz işi kabul edin. Şu anki liste demo verisidir; canlı arka uç olmadan gerçek iş sayısı bilinemez.");
        return aiText("I can guide you through account creation, jobs, verification, earnings and the Company Portal. Try asking: How do I create an account? Is this live work? How do I get paid? What is verification? This built-in guide uses local rules; full conversational AI needs a secure AI backend.", "मैं अकाउंट, जॉब, वेरिफिकेशन, कमाई और Company Portal में मदद कर सकता हूँ। पूछें: अकाउंट कैसे बनाऊँ? क्या यह लाइव काम है? पेमेंट कैसे मिलेगा? वेरिफिकेशन क्या है? यह स्थानीय नियमों वाला गाइड है; पूरी बातचीत वाला AI जोड़ने के लिए सुरक्षित AI बैकएंड चाहिए।", "আমি অ্যাকাউন্ট, কাজ, যাচাই, আয় ও Company Portal সম্পর্কে সাহায্য করতে পারি। জিজ্ঞাসা করুন: অ্যাকাউন্ট কীভাবে খুলব? এটি কি লাইভ কাজ? পেমেন্ট কীভাবে পাব? সম্পূর্ণ AI-এর জন্য নিরাপদ ব্যাকএন্ড দরকার।", "میں اکاؤنٹ، کام، تصدیق، آمدنی اور Company Portal میں رہنمائی کر سکتا ہوں۔ پوچھیں: اکاؤنٹ کیسے بناؤں؟ کیا یہ لائیو کام ہے؟ ادائیگی کیسے ہوگی؟ مکمل AI کے لیے محفوظ بیک اینڈ ضروری ہے۔", "Hesap, işler, doğrulama, kazanç ve Company Portal konusunda yardımcı olabilirim. Şunu sorun: Hesap nasıl oluşturulur? Bu canlı iş mi? Ödeme nasıl alınır? Tam sohbet yapay zekâsı için güvenli bir arka uç gerekir.", "Hesap, işler, doğrulama, kazanç ve Company Portal konusunda yardımcı olabilirim. Şunu sorun: Hesap nasıl oluşturulur? Bu canlı iş mi? Ödeme nasıl alınır? Tam sohbet yapay zekâsı için güvenli bir arka uç gerekir.");
    }

    private boolean hasAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }

    @Override
    protected void onDestroy() {
        if (aiTts != null) {
            aiTts.stop();
            aiTts.shutdown();
            aiTts = null;
        }
        super.onDestroy();
    }

    private double parseMoney(String text) {
        if (text == null) return 0.0;

        String clean = text
                .replace("$", "")
                .replace("₹", "")
                .replace(",", "")
                .trim();

        try {
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double workerPool(double total) {
        return total * WORKER_SHARE_PERCENT / 100.0;
    }

    private double viyzoShare(double total) {
        return total * VIYZO_SHARE_PERCENT / 100.0;
    }

    private String money(double value) {
        return String.format(Locale.US, "$%.2f", value);
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
