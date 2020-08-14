/**
 * @author Banjo Mofesola Paul
 * Chief Developer, Planet NEST
 * mofesolapaul@live.com
 * Thursday, April 28, 2016
 */

package org.planetnest.aedcapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "db.s3db";
    private static String DB_PATH = Environment.getDataDirectory() + "/data/org.planetnest.aedcapp/databases/";
    private final Gson gson;
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    /**********
     * DB SETUP
     ***********/
    public DatabaseHelper(Context paramContext) {
        super(paramContext, DB_NAME, null, 1);
        this.myContext = paramContext;
        this.gson = new Gson();
    }

    private boolean checkDataBase() {
        File file = new File(DB_PATH + DB_NAME);
        return file.exists();
        /*SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null;*/
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void close() {
        try {
            if (this.myDataBase != null) {
                this.myDataBase.close();
            }
            super.close();
            return;
        } finally {
        }
    }

    public void createDataBase() {
        if (checkDataBase()) return;
        getWritableDatabase();

        try {
            copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDataBase(boolean anew) {
        this.myContext.deleteDatabase(DB_PATH + DB_NAME);
        createDataBase();
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
    }

    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
    }

    public void openDataBase() {
        this.myDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /*********************************/

    @JavascriptInterface
    public void addStat(String user_id, String date, String amt, String kwh, String pin) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", user_id);
        cv.put("date", date);
        cv.put("amt", amt);
        cv.put("kwh", kwh);
        cv.put("pin", pin);
        myDataBase.insert("statistics", null, cv);
    }

    @JavascriptInterface
    public String getPredictionTokens() {
        Cursor data = this.myDataBase.rawQuery("SELECT * FROM `statistics` WHERE (`user_id` = ?) ORDER BY `_id` DESC LIMIT 4",
                new String[] {getSettings("logged_id")});
        if (data.getCount() < 1) return "";

        ArrayList tokens = new ArrayList();
        while (data.moveToNext()) {
            HashMap dataMap = new HashMap();
            dataMap.put("date", data.getString(data.getColumnIndex("date")));
            dataMap.put("amt", data.getString(data.getColumnIndex("amt")));
            dataMap.put("kwh", data.getString(data.getColumnIndex("kwh")));
            dataMap.put("pin", data.getString(data.getColumnIndex("pin")));
            tokens.add(dataMap);
        }
        return this.gson.toJson(tokens);
    }

    @JavascriptInterface
    public String getSettings(String label) {
        Cursor data = this.myDataBase.rawQuery("SELECT `value` FROM `settings` WHERE `label` = ?", new String[]{label});
        if (data.getCount() < 1) return "";

        data.moveToNext();
        return data.getString(data.getColumnIndex("value"));
    }

    public String getStatistics() {
        Cursor data = this.myDataBase.rawQuery("SELECT * FROM `statistics` WHERE (`user_id` = ?) ORDER BY `_id` DESC",
                new String[] {getSettings("logged_id")});
        if (data.getCount() < 1) return "";

        ArrayList stat = new ArrayList();
        while (data.moveToNext()) {
            HashMap dataMap = new HashMap();
            dataMap.put("date", data.getString(data.getColumnIndex("date")));
            dataMap.put("amt", data.getString(data.getColumnIndex("amt")));
            dataMap.put("kwh", data.getString(data.getColumnIndex("kwh")));
            dataMap.put("pin", data.getString(data.getColumnIndex("pin")));
            stat.add(dataMap);
        }
        return this.gson.toJson(stat);
    }

    @JavascriptInterface
    public void setSettings(String label, String value) {
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        this.myDataBase.update("settings", cv, "`label` = ?", new String[]{label});
    }

    @JavascriptInterface
    public void signup(String uname, String email, String pswd) {
        ContentValues cv = new ContentValues();
        cv.put("uname", uname);
        cv.put("email", email);
        cv.put("pswd", pswd);
        myDataBase.insert("user", null, cv);
        login(email, pswd);
    }

    @JavascriptInterface
    public boolean userExists(String email) {
        Cursor data = myDataBase.rawQuery("SELECT '1' FROM `user` WHERE `email` = ?", new String[]{email});
        return data.getCount() > 0;
    }

    @JavascriptInterface
    public boolean login(String email, String pswd) {
        Cursor data = myDataBase.rawQuery("SELECT * FROM `user` WHERE `email` = ? AND `pswd` = ?", new String[]{email, pswd});
        if (data.getCount() < 1) return false;

        data.moveToNext();
        setSettings("logged_uname", data.getString(data.getColumnIndex("uname")));
        setSettings("logged_email", email);
        setSettings("logged_id", data.getString(data.getColumnIndex("_id")));
        return true;
    }

    @JavascriptInterface
    public boolean userLogged() {
        return !getSettings("logged_email").equals("");
    }

    @JavascriptInterface
    public String getUserData() {
        Cursor data = myDataBase.rawQuery("SELECT * FROM `user` WHERE `email` = ?", new String[]{getSettings("logged_email")});
        if (data.getCount() < 1) return "";

        data.moveToNext();

        HashMap dataMap = new HashMap();
        for (int i = 0; i < data.getColumnCount(); i++)
            dataMap.put(data.getColumnName(i), data.getString(i));

        return this.gson.toJson(dataMap);
    }

    @JavascriptInterface
    public void logout() {
        setSettings("logged_uname", "");
        setSettings("logged_email", "");
        setSettings("logged_id", "");
    }

    @JavascriptInterface
    public String topup(String payload) {
        HashMap<String, String> data = gson.fromJson(payload, HashMap.class);

        ContentValues cv = new ContentValues();
        cv.put("card_num", data.get("card_number"));
        cv.put("serial", data.get("serial"));
        myDataBase.update("user", cv, "`email` = ?", new String[]{getSettings("logged_email")});

        String pin = generatePIN();
        addStat(
                getSettings("logged_id"),
                DateFormat.format("dd-MMM-yyyy", new Date(System.currentTimeMillis())).toString(),
                data.get("amt"),
                data.get("kwh"),
                pin
        );

        return pin;
    }

    private String generatePIN() {
        String str = "";
        Random rand = new Random();
        while (str.length() < 20) {
            str = str + String.valueOf(rand.nextInt(10));
        }
        return str;
    }

    @JavascriptInterface
    public String get_statistics_payload() {
        String json = getStatistics();

        ArrayList<Stat> data = (ArrayList) gson.fromJson(json, new TypeToken<ArrayList<Stat>>() {}.getType());
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        Iterator it = data.iterator();

        HashMap<String, StatGroup> statgroups = new HashMap<>();
        Stat stat;
        while (it.hasNext()) {
            stat = (Stat) it.next();
            try {
                String str = (String)DateFormat.format("MMMM yyyy", df.parse(stat.date));
                if (!statgroups.containsKey(str)) statgroups.put(str, new StatGroup(str));
                ((StatGroup) statgroups.get(str)).stats.add(stat);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Set<String> keys = statgroups.keySet();
        it = keys.iterator();
        ArrayList<StatGroup> result = new ArrayList<>();
        while (it.hasNext()) {
            String s = it.next().toString();
            result.add( statgroups.get(s) );
        }

        json = gson.toJson(result);
        return json;
    }

    public class Stat {
        public final double amt;
        public final String date;
        public final double kwh;
        public final String pin;

        public Stat(String date, double amt, double kwh, String pin) {
            this.date = date;
            this.amt = amt;
            this.kwh = kwh;
            this.pin = pin;
        }
    }

    public class StatGroup {
        public final String name;
        public final List<Stat> stats;

        public StatGroup(String name) {
            this.name = name;
            this.stats = new ArrayList();
        }
    }

    @JavascriptInterface
    public void showForecast() {
        String json = getPredictionTokens();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        ArrayList<Stat> data = (ArrayList) gson.fromJson(json, new TypeToken<ArrayList<Stat>>() {}.getType());
        Iterator it = data.iterator();

        if (data.size() <= 1) ((MainActivity) myContext).scriptInterface.swal("No data", "Forecast cannot be computed now, until you make some top-ups", "warn");
        else {
            Stat stat;
            int i = 0;

            Calendar curdate = Calendar.getInstance();
            Calendar firstdate = Calendar.getInstance();
            Double curKwh = 0.0;
            Double kwhs = 0.0;
            Double kwhD = 0.0;

            while (it.hasNext()) {
                stat = (Stat) it.next();
                try {
                    Date dt = df.parse(stat.date);
                    if (i == 0) {
                        curdate.setTime(dt);
                        curdate.set(Calendar.HOUR_OF_DAY, 0);
                        curdate.set(Calendar.MINUTE, 0);
                        curdate.set(Calendar.SECOND, 0);
                        curdate.set(Calendar.MILLISECOND, 0);
                        Log.w("MOFESOLA", stat.date);

                        curKwh = stat.kwh;
                    } else kwhs += stat.kwh;

                    if (i == data.size() - 1) {
                        firstdate.setTime(dt);
                        firstdate.set(Calendar.HOUR_OF_DAY, 0);
                        firstdate.set(Calendar.MINUTE, 0);
                        firstdate.set(Calendar.SECOND, 0);
                        firstdate.set(Calendar.MILLISECOND, 0);
                        Log.w("MOFESOLA", stat.date);
                    }
                    i++;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Long daysBtw = 0L;
            while (firstdate.before(curdate)) {
                firstdate.add(Calendar.DAY_OF_MONTH, 1);
                daysBtw++;
            }
            kwhD = kwhs / daysBtw;

            int f = Double.valueOf(curKwh / kwhD).intValue();
            Date exp = new Date( curdate.getTimeInMillis() + ((Long)(f * 24L * 60L * 60L * 1000L)) );
            df.applyPattern("MMMM dd yyyy");
            String expiry = df.format(exp);

            DecimalFormat dfmt = new DecimalFormat("##.00");
            ((MainActivity) myContext).scriptInterface.swal(
                    "Forecast",
                    "Estimated due date: " + expiry + "\n" + "You use an average of " + dfmt.format(kwhD) + "kwh per day",
                    "success");
        }
    }
}