package com.example.phoneotpactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    String number;

    EditText edtNumber;
    EditText edtOtp;
    RelativeLayout btnSend;
    TextView txtSend,txtLogin;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String verificationId;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtNumber = findViewById(R.id.edtNumber);
        edtOtp = findViewById(R.id.edtOtp);
        txtSend = findViewById(R.id.txtSend);
        progressBar = findViewById(R.id.progressbar);
        txtLogin=findViewById(R.id.txtLogin);

        btnSend = findViewById(R.id.btnSend);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();





        edtOtp.setEnabled(false);

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtSend.getText().toString().equals("Send OTP") || txtSend.getText().toString().equals("ReSend OTP")) {
                    number = edtNumber.getText().toString();
                    String code = number.substring(0, 3);
                    Log.d("TAGER", "length of number is "+number.length()+code);
                    if (number.length() == 10) {
                        number = "+91" + number;
                        progressBar.setVisibility(View.VISIBLE);
                        disableViews();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS, MainActivity.this, mCallbacks);
                    } else if (number.length() == 13 && code.equals("+91")) {
                        progressBar.setVisibility(View.VISIBLE);
                        disableViews();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS, MainActivity.this, mCallbacks);
                    } else {
                        Toast.makeText(MainActivity.this, "Enter valid Phone Number", Toast.LENGTH_SHORT).show();
                    }
                } else if (txtSend.getText().equals("Verify OTP")) {
                    String otp = edtOtp.getText().toString();
                    if (otp.length() == 6) {
                        verifyVerificationCode(otp);
                    } else {
                        Toast.makeText(MainActivity.this, "Enter valid OTP", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            enableViews();
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                progressBar.setVisibility(View.INVISIBLE);
                edtOtp.setText(code);
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            enableViews();
            Log.d("TAGER", "Verification failed" + e.toString());
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            txtSend.setText("ReSend OTP");
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            txtSend.setText("Verify OTP");
            Toast.makeText(MainActivity.this, "OTP sent to "+number, Toast.LENGTH_SHORT).show();
            edtOtp.setAlpha(1);
            edtOtp.setEnabled(true);
            btnSend.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            verificationId = s;
        }
    };

    public void verifyVerificationCode(String code) {
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(phoneAuthCredential);
    }

    public void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        disableViews();
        txtSend.setText("Please wait..");
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                final String id = firebaseAuth.getCurrentUser().getUid();

                txtSend.setText("Logging In...");
                progressBar.setVisibility(View.VISIBLE);



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                txtSend.setText("Send OTP");
                enableViews();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableViews(){
        edtOtp.setEnabled(false);
        edtNumber.setEnabled(false);
        btnSend.setEnabled(false);
    }

    private void enableViews(){
        edtOtp.setEnabled(true);
        edtNumber.setEnabled(true);
        btnSend.setEnabled(true);
    }

}

