package com.example.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {


    Button scanbtn;
    TextView result;
    Button btn,galeriyeKaydet,kaydet;
    CircleImageView imageView;
    String currentPhotoPath;

    FirebaseDatabase db;
    DatabaseReference myRef;
    Uri filePath;
    String mCurrentPhotoPath;
    private StorageReference mStorageRef;
    private FirebaseStorage firebaseStorage;
    private String deviceIdentifier;

    public static final int REQUEST_CODE = 100;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE1 = 105;
    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mStorageRef = FirebaseStorage.getInstance().getReference();

        scanbtn = (Button) findViewById(R.id.scanbtn);
        result = (TextView) findViewById(R.id.result);
        btn = (Button) findViewById(R.id.button);
        galeriyeKaydet = (Button) findViewById(R.id.galeriyeKaydet);
        kaydet = (Button)findViewById(R.id.kaydet);
        imageView = (CircleImageView) findViewById(R.id.imageView);


        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();

            }
        });

        galeriyeKaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE1);
            }
        });

        kaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("bilgi");
                dbRef.push().setValue(

                        new Bilgi(
                            result.getText().toString()
                        )
                );
            }
        });
    }

    private void askCameraPermission() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else{
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else{
                Toast.makeText(this, "Kamera kullanımı için izin verilmedi", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode;
                barcode = data.getParcelableExtra("barcode");

                result.post(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(barcode.displayValue);
                    }
                });
            }
        }

        if(requestCode ==  CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(f));
                Log.d("tag","Absolute of Image is" + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                uploadImageToFirebase(f.getName(), contentUri);
            }
        }

        if(requestCode ==  GALLERY_REQUEST_CODE1){
            if(resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_" + getFileExt(contentUri);
                Log.d("tag", "OnActivityResult: Gallery Image Uri:   " + imageFileName);
                imageView.setImageURI(contentUri);

                uploadImageToFirebase(imageFileName, contentUri);
            }
        }


    }

    private void uploadImageToFirebase(String name, Uri contentUri) {

        final StorageReference image = mStorageRef.child("pictures/" +  name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Yükleme Başarılı URl " + uri.toString());
                    }
                });
                Toast.makeText(MainActivity.this, "Yükleme Başarılı", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Yükleme Başarısız", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                // ...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

}


