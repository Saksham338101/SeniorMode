package com.appremtest.Activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.appremtest.R;

import java.util.ArrayList;

/**
 * Activity shown as a small overlay to pick a contact (simplified).
 * Requests READ_CONTACTS permission if needed. Falls back to opening Contacts app.
 */
public class ContactOverlayActivity extends AppCompatActivity {

    private static final int REQ_READ_CONTACTS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overlay_contact);

        Button btnClose = findViewById(R.id.btn_contact_close);
        btnClose.setOnClickListener(v -> finish());
        // Ensure the fullscreen overlay is removed so this activity is visible
        stopService(new Intent(this, com.appremtest.Services.OverlayService.class));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    private void loadContacts() {
        LinearLayout list = findViewById(R.id.contact_list);
        list.removeAllViews();

        ContentResolver cr = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = cr.query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor == null) return;

        ArrayList<String[]> items = new ArrayList<>();
        while (cursor.moveToNext() && items.size() < 20) {
            String name = cursor.getString(0);
            String number = cursor.getString(1);
            items.add(new String[]{name, number});
        }
        cursor.close();

        for (String[] row : items) {
            Button b = new Button(this);
            b.setText(row[0] + "\n" + row[1]);
            b.setAllCaps(false);
            b.setOnClickListener(v -> {
                // Stop overlay so the dialer is visible and user can interact
                stopService(new Intent(this, com.appremtest.Services.OverlayService.class));
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + row[1]));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                // close this overlay activity so user returns to the phone app
                finish();
            });
            list.addView(b);
        }

        if (items.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No contacts available.");
            list.addView(tv);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                // fallback: open system contacts app
                Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}
