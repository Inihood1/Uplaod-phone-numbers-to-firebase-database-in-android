package com.inihood.facebookdownloader.phonenumberupload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference userDb;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userDb = FirebaseDatabase.getInstance().getReference().child("users");
        mProgressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            getContacts();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, "Read contacts permission is required to function app correctly", Toast.LENGTH_LONG).show();
                }else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            1);                }

            }
        }
    }

    private void getContacts() {
        mProgressDialog.setTitle("starting upload...");
        mProgressDialog.show();
        Cursor contacts = getContentResolver().
                query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME.toString(),
                        ContactsContract.CommonDataKinds.Phone.NUMBER.toString()},
                        null,
                        null,
                        null);

        HashMap map = new HashMap();
        if (contacts != null){
            while (contacts.moveToNext()){
                map.put(contacts.getString(contacts.
                        getColumnIndex((ContactsContract.CommonDataKinds.Phone.NUMBER))),
                        contacts.getString(contacts.
                                getColumnIndex((ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))));
            }
            contacts.close();
        }
        userDb.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                mProgressDialog.hide();
                Toast.makeText(MainActivity.this, "Successfully uploaded ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.hide();
                Toast.makeText(MainActivity.this, "Something went wrong " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startUpload(View view) {
        checkForPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null){
            signin();
        }else {
            Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
        }
    }

    private void signin() {
        mProgressDialog.setTitle("Signing in...");
        mProgressDialog.show();
        mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this,
                        "Signed In successfully", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this,
                        "Error signing in " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
