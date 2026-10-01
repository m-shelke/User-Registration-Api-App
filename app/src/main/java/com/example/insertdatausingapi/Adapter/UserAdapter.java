package com.example.insertdatausingapi.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.insertdatausingapi.Activities.UpdateActivity;
import com.example.insertdatausingapi.R;
import com.example.insertdatausingapi.Models.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    String TAG = "USER_ADAPTER";
    Context context;
    ArrayList<UserModel> userModelArrayList;
    String deleteDataUrl = "http://10.0.2.2/userapi/deleteData.php";

    public UserAdapter(Context context, ArrayList<UserModel> userModelArrayList) {
        this.context = context;
        this.userModelArrayList = userModelArrayList;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_data_item,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String imgUrl = "http:/10.0.2.2/userapi/"+userModelArrayList.get(position).getProfileImage();
        Glide.with(context)
                .load(imgUrl)
                .into(holder.profileIv);

        Log.e(TAG, imgUrl );

        holder.nameTv.setText(userModelArrayList.get(position).getName());
        holder.genderTv.setText(userModelArrayList.get(position).getGender());
        holder.ageTv.setText(userModelArrayList.get(position).getAge()+" years old");

        holder.item_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                CharSequence[] items = {"Edit Data","Delete Data"};
                builder.setTitle(userModelArrayList.get(position).getName());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which){
                            case 0:
                                Intent intent = new Intent(context, UpdateActivity.class);

                                Bundle bundle = new Bundle();
                                bundle.putString("profileImg",imgUrl);
                                bundle.putString("id",userModelArrayList.get(position).getId());
                                bundle.putString("name",userModelArrayList.get(position).getName());
                                bundle.putString("gender",userModelArrayList.get(position).getGender());
                                bundle.putString("age",userModelArrayList.get(position).getAge());
                                intent.putExtras(bundle);
                                context.startActivity(intent);
                                break;
                            case 1:
                                deleteData(userModelArrayList.get(holder.getAdapterPosition()).getId());
                                break;
                        }
                    }

                    private void deleteData(String id) {

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteDataUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (response.equalsIgnoreCase("Data Deleted")){
                                    userModelArrayList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                                }

                                Toast.makeText(context,response.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Toast.makeText(context, volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, volleyError.toString() );
                            }
                        }){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String,String> map = new HashMap<String,String>();
                                map.put("id",id);
                                return map;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(stringRequest);
                    }
                });

                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userModelArrayList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView profileIv;
        TextView nameTv,genderTv,ageTv;
        LinearLayout item_linearLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.item_userImage);
            item_linearLayout = itemView.findViewById(R.id.item_linearLayout);
            nameTv = itemView.findViewById(R.id.item_userName);
            genderTv = itemView.findViewById(R.id.item_userGender);
            ageTv = itemView.findViewById(R.id.item_userAge);
        }
    }
}
