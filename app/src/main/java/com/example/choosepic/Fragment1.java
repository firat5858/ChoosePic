package com.example.choosepic;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

public class Fragment1 extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback{

    private Button btn;
    private ImageView imageview;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;


    File file;
    String path;
    String imagePath;

    //Camera
    private Bitmap bitmap;
    private File destination = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1, container, false);


        btn = (Button) view.findViewById(R.id.btn);
        imageview = (ImageView) view.findViewById(R.id.iv);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermissions();
                }
                selectImage();
            }
        });

        return view;
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED|| ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)  {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1052);
        }
    }




    private void selectImage() {
        try {
            PackageManager pm = getActivity().getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getActivity().getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Fotoğraf Çek", "Galeriden Seç","İptal"};
                 AlertDialog.Builder builder = new  AlertDialog.Builder(getActivity());
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Fotoğraf Çek")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Galeriden Seç")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("İptal")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(getActivity(), "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                Log.e("Activity", "Pick from Camera::>>> ");
                path = Environment.getExternalStorageDirectory().toString();
                String uniqueId = UUID.randomUUID().toString();
                Log.i("firat" , uniqueId);
                destination = new File(path,uniqueId +  ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 imageview.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == PICK_IMAGE_GALLERY) {
            try {
                Uri selectedImage = data.getData();
                String selectedFilePath =  getPath( getActivity().getApplicationContext( ), selectedImage );
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor =getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);
                imageview.setImageBitmap(BitmapFactory.decodeFile(selectedFilePath));
                Toast.makeText(getActivity(), imagePath, Toast.LENGTH_SHORT).show();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                path = Environment.getExternalStorageDirectory().toString();
                String uniqueId = UUID.randomUUID().toString();
                file = new File(path,uniqueId+".jpg");
                try{
                    OutputStream stream = null;
                    stream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    stream.flush();
                    stream.close();
                }catch (IOException e) // Catch the exception
                {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), path, Toast.LENGTH_SHORT).show();
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

}