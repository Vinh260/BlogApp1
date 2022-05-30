package com.example.blogapp.Adapters;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.blogapp.CommentActivity;
import com.example.blogapp.Constant;
import com.example.blogapp.EditPostActivity;
import com.example.blogapp.HomeActivity;
import com.example.blogapp.Models.Post;
import com.example.blogapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder>{
    private ArrayList<Post> list;
    private Context context;
    private ArrayList<Post> listAll;
private SharedPreferences preferences;
    public PostsAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
        this.listAll = new ArrayList<>(list);
        preferences=context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
    }



    @NonNull
    @Override
    public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post,parent,false);
        return new PostsHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostsHolder holder,@SuppressLint("RecyclerView") int position  ) {
        Post post = list.get(position);

        Picasso.get().load(Constant.URL+"storage/"+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constant.URL+"posts/"+post.getPhoto()).into(holder.imgPost);
        holder.txtName.setText(post.getUser().getUserName());
        holder.txtComments.setText("View all comments"+" "+"("+post.getComments()+")");
        holder.txtLikes.setText(post.getLikes()+"");
        holder.txtDate.setText(post.getDate());
        holder.txtDesc.setText(post.getDesc());
        holder.btnLike.setImageResource(
                post.isSelfLike()?R.drawable.ic_red_favorite:R.drawable.ic_favorite_outline
        );
        holder.btnLike.setOnClickListener(v->{
            holder.btnLike.setImageResource(
                    post.isSelfLike()?R.drawable.ic_favorite_outline:R.drawable.ic_red_favorite
            );

      StringRequest request=new StringRequest(Request.Method.POST,Constant.LIKE_POST,response -> {
          Post mPost=list.get(position);
          try {
              JSONObject object=new JSONObject(response);
              if(object.getBoolean("success")){
                mPost.setSelfLike(!post.isSelfLike());
                mPost.setLikes(mPost.isSelfLike()?post.getLikes()+1:post.getLikes()-1);
                list.set(position,mPost);
                notifyItemChanged(position);
                notifyDataSetChanged();
              }else {
                  holder.btnLike.setImageResource(
                          post.isSelfLike() ? R.drawable.ic_red_favorite : R.drawable.ic_favorite_outline
                  );
              }
          } catch (JSONException e) {
              e.printStackTrace();
          }
      },err-> {
          err.printStackTrace();

      }){
          //add token

          @Override
          public Map<String, String> getHeaders() throws AuthFailureError {
             String token=preferences.getString("token","");
             HashMap<String,String> map = new HashMap<>();
             map.put("Authorization","Bearer"+token);
             return map;
          }

          @Override
          protected Map<String, String> getParams() throws AuthFailureError {
              HashMap<String,String> map = new HashMap<>();
              map.put("id",post.getId()+"");
              return  map;
          }
      };
        RequestQueue queue=Volley.newRequestQueue(context);
        queue.add(request);
        });
        //like action
        if(post.getUser().getId()==preferences.getInt("id",0)){
            holder.btnPostOption.setVisibility(View.VISIBLE);
        }else{
            holder.btnPostOption.setVisibility(View.GONE);
        }
        holder.btnPostOption.setOnClickListener(v->{
            PopupMenu popupMenu=new PopupMenu(context,holder.btnPostOption);
            popupMenu.inflate(R.menu.menu_post_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.item_edit:{
                            Intent i = new Intent(((HomeActivity)context), EditPostActivity.class);
                            i.putExtra("postId",post.getId());
                            i.putExtra("position",position);
                            i.putExtra("text",post.getDesc());
                            context.startActivity(i);
                            return true;
                        }
                        case R.id.item_delete:{
                            deletePost(post.getId(),position);
                            return true;
                        }
                    }
                    return false;
                }
            });
            popupMenu.show();
        });
        // end like action

        //load comment count
        holder.txtComments.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)context), CommentActivity.class);
            i.putExtra("postId",post.getId());
            i.putExtra("postPosition",position);
            context.startActivity(i);
        });

        holder.btnComment.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)context),CommentActivity.class);
            i.putExtra("postId",post.getId());
            i.putExtra("postPosition",position);
            context.startActivity(i);
        });
        //end load comment
    }

    private void deletePost(int postId,int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete post?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest request = new StringRequest(Request.Method.POST,Constant.DELETE_POST,response -> {

                    try {
                        JSONObject object = new JSONObject(response);
                        if(object.getBoolean("success")){
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                            listAll.clear();
                            listAll.addAll(list);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },error -> {

                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = preferences.getString("token","");
                        HashMap<String,String> map = new HashMap<>();
                        map.put("Authorization","Bearer "+token);
                        return map;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> map = new HashMap<>();
                        map.put("id",postId+"");
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.show();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Post> filteredList = new ArrayList<>();
            if (charSequence.toString().isEmpty()){
                filteredList.addAll(listAll);
            }else{
                for (Post post : listAll){
                    if(post.getDesc().toLowerCase().contains(charSequence.toString().toLowerCase())
                            || post.getUser().getUserName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredList.add(post);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            list.addAll((Collection<? extends Post>) filterResults.values);
            notifyDataSetChanged();
        }
    };
    public Filter getFilter() {
        return filter;
    }

    class PostsHolder extends RecyclerView.ViewHolder{

        private TextView txtName,txtDate,txtDesc,txtLikes,txtComments;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        private ImageButton btnPostOption,btnLike,btnComment;

        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            txtLikes = itemView.findViewById(R.id.txtPostLikes);
            txtComments = itemView.findViewById(R.id.txtPostComments);
            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            imgPost = itemView.findViewById(R.id.imgPostPhoto);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            btnLike = itemView.findViewById(R.id.btnPostLike);
            btnComment = itemView.findViewById(R.id.btnPostComment);
            btnPostOption.setVisibility(View.GONE);


        }
    }

}
