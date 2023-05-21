
package com.project.poupay.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.project.poupay.R;

public class TelaCadastro extends AppCompatActivity {

    Button btn_concluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        btn_concluir = findViewById(R.id.btn_concluir);

        btn_concluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaCadastro.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}