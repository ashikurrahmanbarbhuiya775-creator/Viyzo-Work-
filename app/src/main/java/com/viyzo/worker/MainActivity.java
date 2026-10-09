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

    // AI Master voice assistant (on-device speech; real LLM answers need backend).
    private TextToSpeech aiTts;
    private boolean aiTtsReady = false;
    private Locale aiLocale = Locale.getDefault();
    private TextView aiStatusView;
    private TextView aiBubbleView;
    private static final int AI_VOICE_REQUEST = 7401;
    private static final int AI_AUDIO_PERMISSION_REQUEST = 7402;
    private boolean aiWelcomeSpoken = false;
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
        int first = Character.codePointAt(code.toUpperCase(Locale.ROOT), 0) - 65 + 0x1F1E6;
        int second = Character.codePointAt(code.toUpperCase(Locale.ROOT), 1) - 65 + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }

    private String localeDisplayName(Locale locale) {
        String nativeName = locale.getDisplayName(locale);
        if (nativeName == null || nativeName.trim().isEmpty()) nativeName = locale.toLanguageTag();
        String english = locale.getDisplayName(Locale.ENGLISH);
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
        TextView title = heading("🌍 Choose your country and language");
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, dp(6));
        hero.addView(title);
        TextView explain = tv("AI Master will guide you in the language you select. You can change these settings later.", 13, TEXT);
        explain.setGravity(Gravity.CENTER);
        explain.setPadding(dp(8), 0, dp(8), dp(12));
        hero.addView(explain);
        c.addView(hero);

        c.addView(tv("COUNTRY / PAÍS / দেশ / البلد", 13, GRAY));
        String[] iso = Locale.getISOCountries();
        java.util.Arrays.sort(iso, (a, b) -> new Locale("", a).getDisplayCountry(Locale.ENGLISH)
                .compareToIgnoreCase(new Locale("", b).getDisplayCountry(Locale.ENGLISH)));
        java.util.ArrayList<String> countryLabels = new java.util.ArrayList<>();
        int suggested = 0;
        String deviceCountry = Locale.getDefault().getCountry();
        for (int i = 0; i < iso.length; i++) {
            Locale countryLocale = new Locale("", iso[i]);
            String countryName = countryLocale.getDisplayCountry(Locale.ENGLISH);
            countryLabels.add(flagForCountry(iso[i]) + "  " + countryName + "  (" + iso[i] + ")");
            if (iso[i].equalsIgnoreCase(deviceCountry)) suggested = i;
        }
        Spinner countries = new Spinner(this);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryLabels);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countries.setAdapter(countryAdapter);
        countries.setSelection(suggested);
        c.addView(countries, new LinearLayout.LayoutParams(-1, dp(54)));

        TextView languageTitle = tv("LANGUAGE / भाषा / ভাষা / اللغة", 13, GRAY);
        languageTitle.setPadding(0, dp(16), 0, dp(2));
        c.addView(languageTitle);
        java.util.TreeMap<String, Locale> uniqueLocales = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Locale loc : Locale.getAvailableLocales()) {
            String lang = loc.getLanguage();
            if (lang == null || lang.trim().isEmpty() || lang.equals("und")) continue;
            if (loc.getCountry() != null && !loc.getCountry().isEmpty()) {
                uniqueLocales.putIfAbsent(lang + "-" + loc.getCountry(), loc);
            } else {
                uniqueLocales.putIfAbsent(lang, loc);
            }
        }
        java.util.ArrayList<Locale> localeChoices = new java.util.ArrayList<>(uniqueLocales.values());
        localeChoices.sort((a, b) -> localeDisplayName(a).compareToIgnoreCase(localeDisplayName(b)));
        java.util.ArrayList<String> languageLabels = new java.util.ArrayList<>();
        int langSuggested = 0;
        String deviceTag = Locale.getDefault().toLanguageTag();
        for (int i = 0; i < localeChoices.size(); i++) {
            Locale loc = localeChoices.get(i);
            languageLabels.add(localeDisplayName(loc));
            if (loc.toLanguageTag().equalsIgnoreCase(deviceTag)) langSuggested = i;
        }
        Spinner languages = new Spinner(this);
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languageLabels);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languages.setAdapter(languageAdapter);
        languages.setSelection(langSuggested);
        c.addView(languages, new LinearLayout.LayoutParams(-1, dp(54)));

        c.addView(wideInfo("🤖", "AI Master", "I will explain account creation, jobs, verification and earnings step by step."));
        Button continueButton = primary("Continue  →");
        continueButton.setOnClickListener(v -> {
            int ci = countries.getSelectedItemPosition();
            int li = languages.getSelectedItemPosition();
            if (ci < 0 || li < 0 || ci >= iso.length || li >= localeChoices.size()) {
                Toast.makeText(this, "Please select a country and language.", Toast.LENGTH_LONG).show();
                return;
            }
            selectedCountryCode = iso[ci];
            selectedCountryName = new Locale("", selectedCountryCode).getDisplayCountry(Locale.ENGLISH);
            Locale chosen = localeChoices.get(li);
            aiLocale = chosen;
            selectedLanguageTag = chosen.toLanguageTag();
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString("country_code", selectedCountryCode)
                    .putString("country_name", selectedCountryName)
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

    private void setAILanguageByLocale(Locale locale) {
        aiLocale = locale == null ? Locale.getDefault() : locale;
        if (aiTts != null) {
            int result = aiTts.setLanguage(aiLocale);
            aiTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
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
        c.addView(small("Login to your Viyzo Worker account."));

        c.addView(input("Email or Phone"));

        EditText pass = input("Viyzo Password");
        pass.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        c.addView(pass);

        Button login = primary("🔐  Login");
        login.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Demo login: secure backend connection is required for real accounts.",
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
        c.addView(small(
                "Start with basic access. Stronger verification can unlock more work."
        ));

        c.addView(input("Full Name"));
        c.addView(input("Email"));
        c.addView(input("Phone Number"));

        EditText pass = input("Create Viyzo Password");
        pass.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
    // WORKER DASHBOARD
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
        startScreen(true);
        LinearLayout c = content();

        LinearLayout top = row();
        TextView back = tv("‹", 32, WHITE);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showDashboard());
        top.addView(back, new LinearLayout.LayoutParams(dp(42), dp(50)));

        LinearLayout titles = column();
        TextView h = tv("Available Work", 21, WHITE);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titles.addView(h);
        titles.addView(tv(
                "Demo opportunities now • backend will supply real jobs later",
                10, GRAY));

        top.addView(titles, new LinearLayout.LayoutParams(0, dp(50), 1));
        c.addView(top);

        c.addView(input("🔎  Search jobs, skills or categories"));

        LinearLayout filters1 = row();
        Button all = secondary("All");
        Button urgent = secondary("Urgent");
        Button bulk = secondary("Bulk");
        filters1.addView(all, new LinearLayout.LayoutParams(0, dp(48), 1));

        LinearLayout.LayoutParams f2 = new LinearLayout.LayoutParams(0, dp(48), 1);
        f2.setMargins(dp(5), 0, 0, 0);
        filters1.addView(urgent, f2);

        LinearLayout.LayoutParams f3 = new LinearLayout.LayoutParams(0, dp(48), 1);
        f3.setMargins(dp(5), 0, 0, 0);
        filters1.addView(bulk, f3);
        c.addView(filters1);

        LinearLayout filters2 = row();
        Button recurring = secondary("Recurring");
        Button highPay = secondary("High Pay");
        Button global = secondary("Global");
        filters2.addView(recurring, new LinearLayout.LayoutParams(0, dp(48), 1));

        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, dp(48), 1);
        hp.setMargins(dp(5), 0, 0, 0);
        filters2.addView(highPay, hp);

        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(0, dp(48), 1);
        gp.setMargins(dp(5), 0, 0, 0);
        filters2.addView(global, gp);
        c.addView(filters2);

        for (JobData job : DEMO_JOBS) {
            c.addView(jobCard(job));
        }

        Button company = secondary("🏢  Are you a company? Post work");
        company.setOnClickListener(v -> showCompanyPortal());
        c.addView(company);
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

        Button apply = primary("🚀  Apply / Join Work Pool");
        apply.setOnClickListener(v -> Toast.makeText(
                this,
                "Demo application saved. Real assignment requires backend matching.",
                Toast.LENGTH_SHORT).show());
        c.addView(apply);

        Button back = secondary("← Back to Work Feed");
        back.setOnClickListener(v -> showJobs());
        c.addView(back);
    }

    // ============================================================
    // MY WORK / WORK POOL
    // ============================================================
    private void showMyJobs() {
        startScreen(true);
        LinearLayout c = content();

        c.addView(heading("My Work"));
        c.addView(small("Assignments, work pools and progress."));

        LinearLayout active = card();
        active.addView(wideInfo("💼", "Product Listing",
                "Demo Assignment • In Progress"));

        active.addView(tv("125 / 1000 products", 13, TEXT));
        active.addView(tv("Worker payout is based on approved completed work.",
                11, GRAY));
        active.addView(tv("Deadline: 3 Days", 11, GRAY));

        Button open = primary("Open Work");
        open.setOnClickListener(v -> showWorkExecution());
        active.addView(open);
        c.addView(active);

        LinearLayout queue = card();
        queue.addView(tv("AI Work Queue", 16, WHITE));
        queue.addView(tv(
                "1 Active • 2 Ready • 3 Recommended • 0 Waiting",
                12, GRAY));

        Button pool = secondary("📦  Open Work Pool");
        pool.setOnClickListener(v -> showWorkPool());
        queue.addView(pool);
        c.addView(queue);

        c.addView(wideInfo("⚡", "Urgent Queue",
                "Ready when eligible work is available"));
        c.addView(wideInfo("🔁", "Recurring Queue",
                "Repeat work can return automatically from backend"));
    }

    private void showWorkExecution() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Work Execution"));
        c.addView(small("Demo task workspace — production task tools connect to backend."));

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
        startScreen(true);
        LinearLayout c = content();

        c.addView(heading("Earnings"));

        LinearLayout balance = column();
        balance.setPadding(dp(20), dp(20), dp(20), dp(20));
        balance.setBackground(primaryBg());
        balance.setElevation(dp(8));

        balance.addView(tv("Available Balance", 12, WHITE));
        TextView moneyText = tv("₹0.00", 32, WHITE);
        moneyText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        balance.addView(moneyText);

        Button withdraw = primary("Withdraw");
        withdraw.setBackground(solid(WHITE, 20));
        withdraw.setTextColor(PRIMARY);
        withdraw.setOnClickListener(v -> Toast.makeText(
                this,
                "Withdrawal will connect to a supported country/payment provider.",
                Toast.LENGTH_SHORT).show());
        balance.addView(withdraw);

        c.addView(balance);
        setMargins(balance, 0, 0, 0, 12);

        LinearLayout target = card();
        target.addView(tv("🎯 Daily Target Planning", 15, WHITE));
        target.addView(tv(
                "Opportunity target: ₹" + formatNumber(DAILY_EARNING_TARGET_INR) +
                        "/day • up to " + formatNumber(TARGET_HOURS_PER_DAY) +
                        " hours",
                13, TEXT));
        target.addView(tv(
                "At a 50% worker-pool model, about ₹" +
                        formatNumber(DAILY_EARNING_TARGET_INR * 2) +
                        " of total job value would be needed to create ₹" +
                        formatNumber(DAILY_EARNING_TARGET_INR) +
                        " worker-pool value, before task/quality/payment effects.",
                11, GRAY));
        c.addView(target);

        LinearLayout r = row();
        LinearLayout total = statCard("💵", "Total Earned", "₹0.00");
        LinearLayout pending = statCard("⏳", "Pending", "₹0.00");

        r.addView(total, new LinearLayout.LayoutParams(0, dp(125), 1));
        LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(0, dp(125), 1);
        pp.setMargins(dp(8), 0, 0, 0);
        r.addView(pending, pp);
        c.addView(r);

        c.addView(wideInfo("📈", "Transactions",
                "No real transactions yet"));
        c.addView(wideInfo("🧾", "Payout Status",
                "Backend/payment provider required"));
        c.addView(wideInfo("🔒", "Payment Verification",
                "May be required before withdrawals"));

        Button methods = secondary("💳  Payment Methods");
        methods.setOnClickListener(v -> showPaymentSettings());
        c.addView(methods);

        Button history = secondary("📋  Transaction History");
        history.setOnClickListener(v -> Toast.makeText(
                this, "Transaction history will come from secure backend.",
                Toast.LENGTH_SHORT).show());
        c.addView(history);
    }

    private void showPaymentSettings() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Payment Settings"));
        c.addView(small(
                "Payment methods and payout rules should be country/provider specific."
        ));

        c.addView(wideInfo("🌍", "Country",
                "Country-specific payout options"));
        c.addView(wideInfo("🏦", "Bank / Wallet",
                "Connect supported provider later"));
        c.addView(wideInfo("🛡", "Payment Verification",
                "Separate from worker identity verification"));

        c.addView(secondary("＋ Add Payment Method"));
        c.addView(secondary("📋 Payout Requirements"));

        Button back = secondary("← Back to Earnings");
        back.setOnClickListener(v -> showEarnings());
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
        TextView name = tv("Ashikur Rahman", 17, WHITE);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        pd.addView(name);
        pd.addView(tv("Worker ID: VZ388742", 11, GRAY));
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

        c.addView(wideInfo("💵", "Company Spending", "₹0.00 / $0.00"));
        c.addView(wideInfo("🌍", "Global Reach",
                "Countries/languages can be configured"));
        c.addView(wideInfo("🤖", "AI Manager",
                "Ready to recommend worker allocation"));

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
        c.addView(small(
                "This is the intake form. Submission becomes a real company job only after backend onboarding, payment and review."
        ));

        c.addView(input("Company Name"));
        c.addView(input("Job Title / Work Name"));
        c.addView(input("Job Description"));

        c.addView(spinner(new String[]{
                "Select Category",
                "Product Listing",
                "Data Entry",
                "Content Review",
                "Translation",
                "Research",
                "Image / Media Work",
                "Customer Support",
                "AI Data / Annotation",
                "Other"
        }));

        c.addView(spinner(new String[]{
                "Company Size",
                "Small Business",
                "Startup",
                "Medium Business",
                "Enterprise",
                "Agency / Other"
        }));

        c.addView(spinner(new String[]{
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
        }));

        c.addView(spinner(new String[]{
                "Currency",
                "USD", "EUR", "GBP", "INR", "AED", "BDT", "Other"
        }));

        c.addView(input("Company Budget"));
        c.addView(input("Number of Workers Needed"));
        c.addView(input("Workload / Quantity"));
        c.addView(input("Deadline"));

        c.addView(spinner(new String[]{
                "Urgency",
                "Normal",
                "High",
                "Urgent"
        }));

        c.addView(spinner(new String[]{
                "Work Type",
                "Quick Task",
                "Bulk",
                "Project",
                "Recurring"
        }));

        c.addView(input("Required Skills"));
        c.addView(input("Required Language"));

        CheckBox remote = new CheckBox(this);
        remote.setText("Remote / Online Work");
        remote.setChecked(true);
        remote.setTextColor(TEXT);
        c.addView(remote);

        CheckBox agree = new CheckBox(this);
        agree.setText(
                "I confirm that the work, budget and payment information is accurate."
        );
        agree.setTextColor(TEXT);
        c.addView(agree);

        Button preview = secondary("🧠  Preview AI Allocation");
        preview.setOnClickListener(v -> showAllocationPreview());
        c.addView(preview);

        Button post = primary("🌍  Submit Work to Viyzo");
        post.setOnClickListener(v -> {
            if (!agree.isChecked()) {
                Toast.makeText(this,
                        "Please confirm the work information.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this,
                    "Demo intake saved locally. Real posting requires secure backend/payment onboarding.",
                    Toast.LENGTH_LONG).show();
            showCompanyDashboard();
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

    private void showSettings() {
        startScreen(false);
        LinearLayout c = content();

        c.addView(heading("Settings"));

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
        aiTts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = aiTts.setLanguage(aiLocale);
                aiTtsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
                aiTts.setSpeechRate(0.94f);
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
                            if (aiStatusView != null) aiStatusView.setText("Voice unavailable • Tap to read/help");
                        });
                    }
                });
            }
        });
    }

    private void addAIMasterOverlay() {
        if (frame == null) return;
        LinearLayout panel = column();
        panel.setPadding(dp(10), dp(8), dp(10), dp(8));
        panel.setBackground(outlined(Color.rgb(20, 25, 54), PRIMARY, 18));
        panel.setElevation(dp(12));

        LinearLayout top = row();
        TextView icon = tv("🤖", 23, WHITE);
        icon.setGravity(Gravity.CENTER);
        top.addView(icon, new LinearLayout.LayoutParams(dp(35), dp(35)));
        LinearLayout titleColumn = column();
        TextView title = tv("AI MASTER", 12, WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleColumn.addView(title);
        titleColumn.addView(tv("Your Viyzo guide", 9, GRAY));
        top.addView(titleColumn, new LinearLayout.LayoutParams(0, dp(37), 1));
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
        aiBubbleView = tv("Ask me about account, jobs, work steps, verification or earnings.", 11, TEXT);
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
                if (aiBubbleView != null) aiBubbleView.setText("You: " + question);
                String answer = answerAIQuestion(question);
                if (aiBubbleView != null) aiBubbleView.setText("You: " + question + "\n\nAI Master: " + answer);
                speakAI(answer);
            }
        }
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
        if (aiTts == null || !aiTtsReady) {
            if (aiStatusView != null) aiStatusView.setText("Voice language may be missing in Android speech settings.");
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            aiTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "viyzo_ai_" + System.currentTimeMillis());
        } else {
            aiTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
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
