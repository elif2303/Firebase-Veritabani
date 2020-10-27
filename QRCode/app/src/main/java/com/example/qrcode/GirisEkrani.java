package com.example.qrcode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GirisEkrani extends AppCompatActivity {

    Button butonGiris,kullaniciOlustur;
    EditText KullaniciAdi, parola;
    FirebaseAuth girisYetkisi;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_ekrani);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        kullaniciOlustur = (Button) findViewById(R.id.kullaniciOlustur);
        butonGiris = (Button) findViewById(R.id.butonGiris);
        KullaniciAdi = (EditText) findViewById(R.id.KullaniciAdi);
        parola = (EditText)findViewById(R.id.parola);

        girisYetkisi = FirebaseAuth.getInstance();


        // **********Kullanıcı birkez giriş yaptıktan sonra tekrar giriş yapmasına gerek yok*******

        FirebaseUser firebaseUser = girisYetkisi.getCurrentUser();

        if(firebaseUser != null){
            Intent intent = new Intent(GirisEkrani.this, MainActivity.class);
            startActivity(intent);
        }
        //*****************************************************************************************

        butonGiris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final ProgressDialog pdGiris = new ProgressDialog(GirisEkrani.this);
                pdGiris.setMessage("Giriş yapılıyor...");
                pdGiris.show();

                String str_KullaniciAdi = KullaniciAdi.getText().toString();
                String str_parola = parola.getText().toString();

                if(TextUtils.isEmpty(str_KullaniciAdi) || TextUtils.isEmpty(str_parola)){

                    Toast.makeText(GirisEkrani.this, "Tüm alanların doldurulması gerekir", Toast.LENGTH_LONG).show();

                }
                else{
                    girisYetkisi.signInWithEmailAndPassword(str_KullaniciAdi,str_parola)
                            .addOnCompleteListener(GirisEkrani.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()){

                                        DatabaseReference yolGiris = FirebaseDatabase.getInstance().getReference().
                                                child("users").child(girisYetkisi.getCurrentUser().getUid());

                                        yolGiris.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                pdGiris.dismiss();

                                                Intent intent = new Intent(GirisEkrani.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                                pdGiris.dismiss();

                                            }
                                        });
                                    }

                                    else{
                                        pdGiris.dismiss();
                                        Toast.makeText(GirisEkrani.this, "Giriş bşarısız", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }

            }
        });


        kullaniciOlustur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(GirisEkrani.this, KullaniciKayit.class));

            }
        });

    }


}



