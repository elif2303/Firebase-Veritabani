package com.example.qrcode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class KullaniciKayit extends AppCompatActivity {

    EditText kullaniciAdiYeni,parolaYeni;
    Button kayitOlustur;
    TextView baslik;
    FirebaseAuth yetki;
    DatabaseReference yol;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kullanici_kayit);


        kullaniciAdiYeni = (EditText)findViewById(R.id.kullaniciAdiYeni);
        parolaYeni = (EditText)findViewById(R.id.parolaYeni);
        kayitOlustur = (Button)findViewById(R.id.kayitOlustur);
        baslik = (TextView)findViewById(R.id.baslik);

        yetki= FirebaseAuth.getInstance();


        kayitOlustur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent (KullaniciKayit.this, GirisEkrani.class));

                pd = new ProgressDialog(KullaniciKayit.this);
                pd.setMessage("Lütfen Bekleyiniz");
                pd.show();

                String str_kullaniciAdiYeni = kullaniciAdiYeni.getText().toString();
                String str_parolaYeni = parolaYeni.getText().toString();

                if(TextUtils.isEmpty(str_kullaniciAdiYeni) || TextUtils.isEmpty(str_parolaYeni)){
                    Toast.makeText(KullaniciKayit.this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_LONG).show();
                }
                else if(str_parolaYeni.length()<6){
                    Toast.makeText(KullaniciKayit.this,"Şifre 6 karakterden küçük olamaz!", Toast.LENGTH_LONG).show();
                }
                else{
                    kaydet(str_kullaniciAdiYeni,str_parolaYeni);
                }



                /*
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users");
                dbRef.push().setValue(
                    new User(
                       kullaniciAdiYeni.getText().toString(),
                       parolaYeni.getText().toString()
                    )
                );

                Toast.makeText(getApplicationContext(),"Kullanıcı Başarıyla Oluşturuldu", Toast.LENGTH_SHORT).show();

                */
            }
        });
    }

    private void kaydet(final String kullaniciAdiYeni, final String parolaYeni){

        yetki.createUserWithEmailAndPassword(kullaniciAdiYeni,parolaYeni)
                .addOnCompleteListener(KullaniciKayit.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {

                            FirebaseUser firebaseKullanici = yetki.getCurrentUser();

                            String kullaniciId = firebaseKullanici.getUid();

                            yol = FirebaseDatabase.getInstance().getReference().child("users").child(kullaniciId);

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("id",kullaniciId);
                            hashMap.put("kullaniciadi",kullaniciAdiYeni.toLowerCase());
                            hashMap.put("parola", parolaYeni);

                            yol.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        pd.dismiss();
                                        Intent intent = new Intent(KullaniciKayit.this, GirisEkrani.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                }
                            });

                        }

                        else {
                            pd.dismiss();
                            Toast.makeText(KullaniciKayit.this, "Bu mail veya şifre ile kayıt başarısız", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

