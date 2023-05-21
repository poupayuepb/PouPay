package com.project.poupay.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.project.poupay.R;

public class TelaLogin extends AppCompatActivity {

    TextView txt_cadastrarse;
    Button btn_entrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_login);

        txt_cadastrarse = findViewById(R.id.txt_cadastrarse);
        btn_entrar = findViewById(R.id.btn_entrar);

        txt_cadastrarse.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(TelaLogin.this, TelaCadastro.class);
                 startActivity(intent);
             }
         });

        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaLogin.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}