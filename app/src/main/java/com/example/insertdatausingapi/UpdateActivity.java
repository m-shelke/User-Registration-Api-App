package com.example.insertdatausingapi;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.example.insertdatausingapi.databinding.ActivityUpdateBinding;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity {

    String TAG = "UPDATE_ACTIVITY";
    String id;
    ActivityUpdateBinding binding;
    String[] genderList = {"Boy", "Girl"};
    ArrayAdapter<String> arrayAdapter;
    DatePickerDialog.OnDateSetListener dateSetListener;
    String selectedGender, calculateAge;
    String updateUrl = "http://10.0.2.2/userapi/updateData.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getString("id");
            String name = bundle.getString("name");
            String gender = bundle.getString("gender");
            String age = bundle.getString("age");

            binding.nameEt.setText(name);
            binding.selectGenderACT.setText(gender);
            binding.ageEt.setText(age);
        }

        arrayAdapter = new ArrayAdapter<String>(UpdateActivity.this, R.layout.show_gender, genderList);
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
                binding.ageEt.setText(calculateAge + " years");
            }
        };

        binding.btSelectBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        UpdateActivity.this,
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

        binding.btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = binding.nameEt.getText().toString();

                if (name.isEmpty()) {
                    binding.nameEt.setError("Name Missing");
                } else if (selectedGender.isEmpty()) {
                    binding.selectGenderACT.setError("Select Gender");
                } else if (calculateAge.isEmpty()) {
                    binding.ageEt.setError("Age Missing");
                } else {

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Toast.makeText(UpdateActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(UpdateActivity.this, volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, volleyError.toString());
                        }
                    }) {
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            Map<String, String> map = new HashMap<String, String>();
                            map.put("id",id);
                            map.put("name", name);
                            map.put("gender", selectedGender);
                            map.put("age", calculateAge);
                            return map;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(UpdateActivity.this);
                    requestQueue.add(stringRequest);


                }
            }
        });
    }
}