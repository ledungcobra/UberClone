package com.example.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtUsername,edtPassword,edtDriverOrPassenger;
    private Button btnsignUp,btnOneTimeLogin;
    private State state = State.SIGNUP;
    private RadioButton driverRadioButton,passengerRadioButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();


        setTitle("Register Or Login");

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverOrPassenger = findViewById(R.id.edtDriverPassenger);

        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        btnsignUp = findViewById(R.id.btnSignUp);
        driverRadioButton = findViewById(R.id.rdbDriver);
        passengerRadioButton = findViewById(R.id.rdbPassenger);

        btnOneTimeLogin.setOnClickListener(this);
        btnsignUp.setOnClickListener(this);

        if(ParseUser.getCurrentUser()!=null){

           transitionToPassengerActivity();
           transitionToDriverListActivity();

        }




    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnSignUp:

                if(state == State.LOGIN){

                    ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user!=null && e == null){

                                //TODO:
                                showToast(user.getUsername()+" was logged in",FancyToast.SUCCESS);


                            }else{

                                showToast(e.getMessage(),FancyToast.ERROR);

                            }
                        }
                    });

                }else if(state == State.SIGNUP){

                    if (driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false) {
                        Toast.makeText(MainActivity.this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (driverRadioButton.isChecked()) {
                        appUser.put("as", "Driver");

                    } else if (passengerRadioButton.isChecked()) {
                        appUser.put("as", "Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                showToast("Signed Up!",FancyToast.SUCCESS );
                                transitionToPassengerActivity();
                                transitionToDriverListActivity();

                            }else{

                                showToast(e.getMessage(),FancyToast.ERROR);

                            }
                        }
                    });
                }

                break;
            case R.id.btnOneTimeLogin:

                if(edtDriverOrPassenger.getText().toString().equals("Driver")||
                        edtDriverOrPassenger.getText().toString().equals("Passenger")){

                    ParseAnonymousUtils.logIn(new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user!=null && e == null){

                                showToast("Login as guest",FancyToast.INFO);
                                user.put("as",edtDriverOrPassenger.getText().toString());
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        transitionToPassengerActivity();
                                        transitionToDriverListActivity();
                                    }
                                });


                            }
                        }
                    });
                }

                break;

        }

    }

    private void showToast(String message, int type) {
        FancyToast.makeText(this,message,FancyToast.LENGTH_SHORT,type,false).show();
    }

    enum State{
        SIGNUP,
        LOGIN

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.logOutItem){

            if(state == State.LOGIN){


                item.setTitle("Login");
                state = State.SIGNUP;
                btnsignUp.setText("SIGN UP");




            }else if(state == State.SIGNUP){

                item.setTitle("Sign up");
                state = State.LOGIN;
                btnsignUp.setText("LOG IN");


            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity(){

        if(ParseUser.getCurrentUser()!=null){

            if(ParseUser.getCurrentUser().get("as").equals("Passenger"));
            startActivity(new Intent(this,PassengerActivity.class));

        }



    }
    private void transitionToDriverListActivity(){

        if(ParseUser.getCurrentUser()!=null && ParseUser.getCurrentUser().get("as").equals("Driver")){

            finish();
            startActivity(new Intent(this,DriverRequestListActivity.class));

        }



    }
}
