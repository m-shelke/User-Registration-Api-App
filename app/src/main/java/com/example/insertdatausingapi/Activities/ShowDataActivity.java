package com.example.insertdatausingapi.Activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.insertdatausingapi.Adapter.UserAdapter;
import com.example.insertdatausingapi.Models.UserModel;
import com.example.insertdatausingapi.R;
import com.example.insertdatausingapi.databinding.ActivityShowDataBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowDataActivity extends AppCompatActivity {

    String TAG = "SHOW_DATA_ACTIVITY";
    ArrayList<UserModel> userModelArrayList = new ArrayList<>();
    UserAdapter userAdapter;
    UserModel userModel;
    String fetchDataUrl = "http://10.0.2.2/userapi/fetchData.php";


    ActivityShowDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityShowDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        userAdapter = new UserAdapter(ShowDataActivity.this, userModelArrayList);
        binding.showDataRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.showDataRecyclerview.setAdapter(userAdapter);

        getData();
    }

    private void getData() {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchDataUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                userModelArrayList.clear();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    if (success.equals("1")) {

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                            String id = jsonObject1.getString("id");
                            String name = jsonObject1.getString("name");
                            String gender = jsonObject1.getString("gender");
                            String age = jsonObject1.getString("age");
                            String imgUrl = jsonObject1.getString("profileImage");

                            String imgUrl2 = "http:/10.0.2.2/userapi/imageMedia/" + imgUrl;
                            Log.e(TAG, imgUrl2);

                            userModel = new UserModel(id, name, gender, age, imgUrl);
                            userModelArrayList.add(userModel);
                            userAdapter.notifyDataSetChanged();

                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Log.e(TAG, volleyError.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(ShowDataActivity.this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }
}