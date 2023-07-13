package com.estudos.whatsapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.estudos.whatsapp.R;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText inputEmail, inputPassword;
    private Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_login);
        initComponents();
    }

    private void initComponents(){
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isValidUserEntries())return;
                login();
            }
        });
    }

    private Boolean isValidUserEntries(){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if(!TextUtils.isEmpty(email)){
            if(!TextUtils.isEmpty(password)){
                return true;
            }else{
                Toast.makeText(this, "Digite sua senha!", Toast.LENGTH_SHORT).show();
            return false;
            }
        }else{
            Toast.makeText(this, "Digite seu email!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private void login(){
        FirebaseAuth auth = FirebaseUtils.getAuth();
        auth.signInWithEmailAndPassword(
                inputEmail.getText().toString(),
                inputPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String exception = "";
                try{
                    throw  task.getException();
                }catch (FirebaseAuthInvalidCredentialsException e){
                    exception = "Email ou senha não corresponde a um usuário cadastrado";
                }catch (FirebaseAuthInvalidUserException e){
                    exception = "Usuário não cadastrado!";
                }catch (Exception e){
                    exception="Erro ao realizar o login: "+e.getMessage();
                }
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Successo no Login!", Toast.LENGTH_SHORT).show();
                    goToMain();
                }else{
                    Toast.makeText(getApplicationContext(), exception, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void telaCadastro(View view){
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    private void goToMain(){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
   ///    finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseUtils.getAuth().getCurrentUser();
        if(user !=null){
            goToMain();
        }
    }
}