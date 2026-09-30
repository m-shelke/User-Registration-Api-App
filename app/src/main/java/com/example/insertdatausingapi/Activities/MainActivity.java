package com.example.insertdatausingapi.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.insertdatausingapi.R;
import com.example.insertdatausingapi.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String TAG = "MAIN_ACTIVITY";
    private Uri imageUri;
    ActivityMainBinding binding;
    String[] genderList = {"Boy", "Girl"};
    ArrayAdapter<String> arrayAdapter;
    DatePickerDialog.OnDateSetListener dateSetListener;
    String selectedGender, calculateAge;
    String insetDataUrl = "http://10.0.2.2/userapi/insertData.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        binding.skipCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShowDataActivity.class));
            }
        });

        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImgPickDialog();
            }
        });

        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.show_gender, genderList);
        binding.selectGenderACT.setAdapter(arrayAdapter);


        // Fetch current date configurations
        Calendar calendar = Calendar.getInstance();
        final int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Define the Date Set Listener first
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Update Button label to the selected Year
                binding.btSelectBirthDate.setText(String.valueOf(year));

                // Age Calculation logic
                calculateAge = String.valueOf(currentYear - year);
                binding.etAge.setText(calculateAge + " years");
            }
        };

        binding.btSelectBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                        dateSetListener, currentYear, currentMonth, currentDay
                );

                // Apply dynamic limitations (Cannot pick future dates)
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

                // Hide standard layout day & month columns safely
                int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
                if (daySpinnerId != 0) {
                    View daySpinner = datePickerDialog.getDatePicker().findViewById(daySpinnerId);
                    if (daySpinner != null) daySpinner.setVisibility(View.GONE);
                }

                int monthSpinnerId = Resources.getSystem().getIdentifier("month", "id", "android");
                if (monthSpinnerId != 0) {
                    View monthSpinner = datePickerDialog.getDatePicker().findViewById(monthSpinnerId);
                    if (monthSpinner != null) monthSpinner.setVisibility(View.GONE);
                }

                // Window optimization: Clean backgrounds, transparent transitions
                if (datePickerDialog.getWindow() != null) {
                    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    datePickerDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    WindowManager.LayoutParams layoutParams = datePickerDialog.getWindow().getAttributes();
                    layoutParams.gravity = android.view.Gravity.BOTTOM; // Pins it to the bottom
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Spans edge-to-edge
                    layoutParams.y = 40; // Optional spacing offset from navigation bar edge

                    datePickerDialog.getWindow().setAttributes(layoutParams);

                }

                datePickerDialog.show();
            }
        });

        binding.selectGenderACT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = arrayAdapter.getItem(position).toString();

                if (selectedGender.equals("Boy")) {
                    binding.genderTil.setStartIconDrawable(R.drawable.baseline_man_24);
                } else if (selectedGender.equals("Girl")) {
                    binding.genderTil.setStartIconDrawable(R.drawable.baseline_woman_24);
                }


            }
        });

        binding.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = binding.nameEt.getText().toString();

                if (imageUri == null){
                    Toast.makeText(MainActivity.this, "Profile Image Missing", Toast.LENGTH_SHORT).show();
                }
                else if (name.isEmpty()) {
                    binding.nameEt.setError("Name Missing");
                    binding.nameEt.requestFocus();
                } else if (selectedGender.isEmpty()) {
                    binding.selectGenderACT.setError("Select Gender");
                    binding.selectGenderACT.requestFocus();
                } else if (calculateAge.isEmpty()) {
                    binding.etAge.setError("Age Missing");
                    binding.etAge.requestFocus();
                } else {

                    // Convert the image URI to a Base64 string
                    Bitmap bitmap = uriToBitmap(imageUri);
                    String imageString = getStringImage(bitmap);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, insetDataUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (response.toString().equals("Inserted Successfully")){
                                startActivity(new Intent(MainActivity.this, ShowDataActivity.class));
                            }

                            Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG,response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(MainActivity.this, volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, volleyError.toString());
                        }
                    }) {
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            Map<String, String> map = new HashMap<String, String>();
                            map.put("image", imageString);
                            map.put("name", name);
                            map.put("gender", selectedGender);
                            map.put("age", calculateAge);
                            return map;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                    requestQueue.add(stringRequest);
                }
            }
        });

    }

    private void profileImgPickDialog() {

        //init popup menu param#1  is context and Param#2 is the UI View (profileImagePickFab) to above or below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);

        //add menu items to popup menu Param#1 is GroupId,Param#2 is ItemId,Param#3 is OrderID,Param#4 Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        //show popup menu
        popupMenu.show();

        //Handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                //get id of the menu item clicked
                int itemId = menuItem.getItemId();

                if (itemId == 1) {

                    //Camera is clicked we need to check if we have permission of Camera,Storage before launching Camera to capture Image
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU or above. We only need Camera Permission
                        requestCameraPermission.launch(new String[]{android.Manifest.permission.CAMERA});
                    } else {
                        //Device version is below TIRAMISU. We need Camera and Storage Permission
                        requestCameraPermission.launch(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Check if Storage permission is granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        requestsStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            }
        });
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check if image capture or not
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        //Image Captured, we have image in imageUri as asinged in PickImageCamera()
                        Log.e(TAG, "onActivityResult: Image Capture " + imageUri);

                        //set to profileIv
                        binding.profileImg.setImageURI(imageUri);
                    } else {
                        //canceled
                        Toast.makeText(MainActivity.this, "Image Not Capture", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    Log.d(TAG, "onActivityResult: " + result.toString());

                    //Let's check if permission granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        //Camera or Storage or both permission granted, we can now launch camera to capture image
                        Log.d(TAG, "onActivityResult: All Granted e.g Camera, Storage");
                        pickImageCamera();
                    } else {
                        //Camera or Storage or both permission denied, can not camera to capture image
                        Log.d(TAG, "onActivityResult: All or Either one is denied");
                        Toast.makeText(MainActivity.this, "Camera or Storage or both permission denied", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check, if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();

                        //get Uri of image picked
                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked from Gallery " + imageUri);

                        //set to profileIv
                        binding.profileImg.setImageURI(imageUri);

                    } else {
                        //Canceled
                        Toast.makeText(MainActivity.this, "Image Not Picked", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private ActivityResultLauncher<String> requestsStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    //Let's check if permission is granted or not
                    if (isGranted) {
                        //  Storage Permission granted, we can now launch Gallery to pick Image
                        pickImageGallery();
                    } else {
                        //Storage Permission denied, we can't launch  Gallery to picked Image
                        Toast.makeText(MainActivity.this, "Storage Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera() {

        //setup Content Values,MediaStore to capture high quality image using Camera Intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Intent to launch Camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {

        //Intent to launch Image Picker e.g.Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    // 1. Converts Uri to Bitmap
    private Bitmap uriToBitmap(Uri imageUri) {
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 2. Converts Bitmap to Base64 String
    public String getStringImage(Bitmap bmp) {
        if (bmp == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Compress to JPEG format (Quality: 80% to keep data payload smaller)
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}