package com.estudos.whatsapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.estudos.whatsapp.R;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private TextInputEditText inputName, inputEmail, inputPassword;
    private Button btnSign;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);
        initComponents();
    }

    private void initComponents(){
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSign = findViewById(R.id.btnSign);
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn(){
        if(!isValidNewUser())return;
        FirebaseAuth auth = FirebaseUtils.getAuth();
        auth.createUserWithEmailAndPassword(
                inputEmail.getText().toString(),
                inputPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String exception = "";
                try{
                    throw task.getException();
                }catch (FirebaseAuthWeakPasswordException e){
                    exception = "Digite uma senha mais forte";
                }catch (FirebaseAuthInvalidCredentialsException e){
                    exception = "Digite um email válido";
                }catch (FirebaseAuthUserCollisionException e){
                    exception ="Já existe um cadastro com esses dados!";
                }catch (Exception e){
                    exception = "Ocorreu um erro no cadastro: "+e.getMessage();
                }
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Cadastro efetuado com Sucesso", Toast.LENGTH_SHORT).show();
                    UserModel userModel = new UserModel();
                    userModel.setEmail(inputEmail.getText().toString());
                    userModel.setName(inputName.getText().toString());
                    userModel.setPassword(inputPassword.getText().toString());
                    userModel.saveUser();
                    UserUtils.updateDisplayUserName(inputName.getText().toString());
                    finish();
                }else{
                    Toast.makeText(getApplicationContext() ,exception, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private Boolean isValidNewUser(){
        String name = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if(!TextUtils.isEmpty(name)){
            if(!TextUtils.isEmpty(email)){
                if(!TextUtils.isEmpty(password)){
                    return  true;
                }else{
                    Toast.makeText(this, "É necessário preencher o campo senha!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(this, "É necessário preencher o campo email!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(this, "É necessário preencher o campo nome!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}