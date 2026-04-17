package com.example.pr_18;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "SQLiteDemo";

    private EditText etFirstName, etLastName, etAge, etRecordId;
    private Button btnAdd, btnRead, btnClear, btnUpdate, btnDelete, btnSort;
    private RadioGroup rgSort;
    private TextView tvResult;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        etRecordId = findViewById(R.id.etRecordId);
        tvResult = findViewById(R.id.tvResult);

        btnAdd = findViewById(R.id.btnAdd);
        btnRead = findViewById(R.id.btnRead);
        btnClear = findViewById(R.id.btnClear);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnSort = findViewById(R.id.btnSort);
        rgSort = findViewById(R.id.rgSort);

        btnAdd.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnSort.setOnClickListener(this);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        Cursor c = db.query("mytable", null, null, null, null, null, null);
        if (c.getCount() == 0) {
            insertTestData();
        }
        c.close();
        dbHelper.close();
    }

    private void insertTestData() {
        ContentValues cv = new ContentValues();
        cv.put("first_name", "Иван");
        cv.put("last_name", "Иванов");
        cv.put("age", 30);
        db.insert("mytable", null, cv);

        cv.put("first_name", "Петр");
        cv.put("last_name", "Петров");
        cv.put("age", 25);
        db.insert("mytable", null, cv);

        cv.put("first_name", "Анна");
        cv.put("last_name", "Сидорова");
        cv.put("age", 35);
        db.insert("mytable", null, cv);

        cv.put("first_name", "Мария");
        cv.put("last_name", "Кузнецова");
        cv.put("age", 28);
        db.insert("mytable", null, cv);

        Log.d(LOG_TAG, "Тестовые данные добавлены");
    }

    @Override
    public void onClick(View v) {
        db = dbHelper.getWritableDatabase();
        try {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String idStr = etRecordId.getText().toString().trim();

            int id = v.getId();
            if (id == R.id.btnAdd) {
                if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }
                int age = Integer.parseInt(ageStr);
                ContentValues cv = new ContentValues();
                cv.put("first_name", firstName);
                cv.put("last_name", lastName);
                cv.put("age", age);
                long rowId = db.insert("mytable", null, cv);
                Log.d(LOG_TAG, "Добавлена запись, ID = " + rowId);
                Toast.makeText(this, "Добавлено, ID = " + rowId, Toast.LENGTH_SHORT).show();
                clearInputFields();
                tvResult.setText("Добавлена запись с ID = " + rowId);
            } else if (id == R.id.btnRead) {
                Log.d(LOG_TAG, "--- Чтение всех записей ---");
                Cursor c = db.query("mytable", null, null, null, null, null, null);
                printCursor(c);
            } else if (id == R.id.btnClear) {
                Log.d(LOG_TAG, "--- Очистка таблицы ---");
                int deleted = db.delete("mytable", null, null);
                Log.d(LOG_TAG, "Удалено записей: " + deleted);
                Toast.makeText(this, "Таблица очищена", Toast.LENGTH_SHORT).show();
                tvResult.setText("Таблица очищена, удалено " + deleted + " записей");
            } else if (id == R.id.btnUpdate) {
                if (idStr.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty()) {
                    Toast.makeText(this, "Введите ID и новые данные", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues updateVals = new ContentValues();
                updateVals.put("first_name", firstName);
                updateVals.put("last_name", lastName);
                updateVals.put("age", Integer.parseInt(ageStr));
                int updCount = db.update("mytable", updateVals, "_id = ?", new String[]{idStr});
                Log.d(LOG_TAG, "Обновлено записей: " + updCount);
                Toast.makeText(this, "Обновлено: " + updCount, Toast.LENGTH_SHORT).show();
                tvResult.setText("Обновлено записей: " + updCount);
            } else if (id == R.id.btnDelete) {
                if (idStr.isEmpty()) {
                    Toast.makeText(this, "Введите ID для удаления", Toast.LENGTH_SHORT).show();
                    return;
                }
                int delCount = db.delete("mytable", "_id = ?", new String[]{idStr});
                Log.d(LOG_TAG, "Удалено записей: " + delCount);
                Toast.makeText(this, "Удалено: " + delCount, Toast.LENGTH_SHORT).show();
                tvResult.setText("Удалено записей: " + delCount);
            } else if (id == R.id.btnSort) {
                String orderBy = "";
                int checkedId = rgSort.getCheckedRadioButtonId();
                if (checkedId == R.id.rbName) {
                    orderBy = "first_name";
                    Log.d(LOG_TAG, "--- Сортировка по имени (first_name) ---");
                } else if (checkedId == R.id.rbAge) {
                    orderBy = "age";
                    Log.d(LOG_TAG, "--- Сортировка по возрасту (age) ---");
                } else {
                    Toast.makeText(this, "Выберите вариант сортировки", Toast.LENGTH_SHORT).show();
                    return;
                }
                Cursor sortCursor = db.query("mytable", null, null, null, null, null, orderBy);
                printCursor(sortCursor);
            }
        } finally {
            dbHelper.close();
        }
    }

    private void printCursor(Cursor c) {
        if (c == null) {
            String msg = "Курсор пуст";
            Log.d(LOG_TAG, msg);
            tvResult.setText(msg);
            return;
        }
        if (c.moveToFirst()) {
            StringBuilder resultBuilder = new StringBuilder();
            do {
                StringBuilder sb = new StringBuilder();
                String[] colNames = c.getColumnNames();
                for (String col : colNames) {
                    int index = c.getColumnIndex(col);
                    String value = c.getString(index);
                    sb.append(col).append(" = ").append(value).append("; ");
                }
                String rowStr = sb.toString();
                Log.d(LOG_TAG, rowStr);
                resultBuilder.append(rowStr).append("\n");
            } while (c.moveToNext());
            tvResult.setText(resultBuilder.toString());
        } else {
            String msg = "Нет записей в таблице";
            Log.d(LOG_TAG, msg);
            tvResult.setText(msg);
        }
        c.close();
    }

    private void clearInputFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etAge.setText("");
        etRecordId.setText("");
    }

    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            String createTable = "CREATE TABLE mytable ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "first_name TEXT, "
                    + "last_name TEXT, "
                    + "age INTEGER);";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS mytable");
            onCreate(db);
        }
    }
}