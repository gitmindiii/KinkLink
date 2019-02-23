package com.kinklink.modules.chat.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.modules.authentication.listener.AdapterPositionListener;
import com.kinklink.modules.chat.activity.ChatActivity;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.modules.matches.activity.AdminProfileActivity;
import com.kinklink.modules.matches.activity.MatchProfileActivity;
import com.kinklink.session.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<Chat> chatList;
    private String myUId;
    private String otherImage, myImage;
    private Session session;
    // private InterestIdListener listener;
    private AdapterPositionListener listener;
    private String time = "";

    private Dialog zoomImageDialog;

    // variable to track event time
    private long mLastClickTime = 0;

    public interface OnBottomReachedListener {

        void onBottomReached(int position);
        void onBottomNotReached(int position);

    }

    OnBottomReachedListener onBottomReachedListener;

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){

        this.onBottomReachedListener = onBottomReachedListener;
    }


    /*public ChattingAdapter(Context mContext, ArrayList<Chat> chatList, String myUId) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.myUId = myUId;
        session = new Session(mContext);
        if (session.getRegistration().userDetail.images.size() > 0) {
            myImage = session.getRegistration().userDetail.images.get(0).image;
        } else {
            myImage = "";
        }
    }*/

    public ChattingAdapter(Context mContext, ArrayList<Chat> chatList, String myUId, AdapterPositionListener listener) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.myUId = myUId;
        session = new Session(mContext);
        this.listener = listener;

        if (session.getRegistration().userDetail.images.size() > 0) {
            myImage = session.getRegistration().userDetail.images.get(0).image;
        } else {
            myImage = "";
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_my_chat, parent, false);
                holder = new MyChatViewHolder(view);
                return holder;

            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_other_chat, parent, false);
                holder = new OtherChatViewHolder(view);
                return holder;

            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_my_chat, parent, false);
                holder = new MyChatViewHolder(view);
                return holder;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (myUId.equals(chatList.get(position).uid)) {
            return 0;
        } else {
            return 1;
        }

        /*if (chatList.get(position).isSection) {
            return 2;
        } else {
            if (myUId.equals(chatList.get(position).uid)) {
                return 0;
            } else {
                return 1;
            }
        }*/
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        //  listener.getInterest(chatList.get(position).banner_date);

        if (position == chatList.size() - 1){

            onBottomReachedListener.onBottomReached(position);

        }

        else{
            onBottomReachedListener.onBottomNotReached(position);
        }

        int pos = position - 1;
        int tempPos = (pos == -1) ? pos + 1 : pos;
        Chat chat = chatList.get(position);

        if (myUId.equals(chatList.get(position).uid)) {
            ((MyChatViewHolder) h).myBindData(chat, position, tempPos);
        } else {
            ((OtherChatViewHolder) h).otherBindData(chat, position, tempPos);
        }

        listener.getPosition(position);

    }

    public void getImage(String image) {
        this.otherImage = image;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RelativeLayout rl_my_text_msg,rl_my_msg;
        // My Chat Text Msg
        private TextView tv_my_chat, tv_my_msg_time, tv_my_date_label,txtEncryption;
       // private ImageView iv_my_msg_img;
        private ProgressBar img_progress;
        private RelativeLayout rl_img_view;

        // My Chat Image Msg
        private TextView tv_my_img_time;
        private ImageView iv_my_image_pic, iv_image_msg;   // iv_image_msg is image msg and iv_my_image_pic is my profile image

        MyChatViewHolder(View itemView) {
            super(itemView);
            rl_my_text_msg = itemView.findViewById(R.id.rl_my_text_msg);
            rl_my_msg = itemView.findViewById(R.id.rl_my_msg);
            txtEncryption = itemView.findViewById(R.id.txtEncryption);
            tv_my_chat = itemView.findViewById(R.id.tv_my_chat);
            tv_my_msg_time = itemView.findViewById(R.id.tv_my_msg_time);
           // iv_my_msg_img = itemView.findViewById(R.id.iv_my_msg_img);
            tv_my_date_label = itemView.findViewById(R.id.tv_my_date_label);

            iv_image_msg = itemView.findViewById(R.id.iv_image_msg);

            rl_img_view = itemView.findViewById(R.id.rl_img_view);
            img_progress = itemView.findViewById(R.id.img_progress);

            iv_image_msg.setOnClickListener(this);
        }

        void myBindData(Chat chat, int pos, int tempPos) {
            Log.i("97555565",""+ chat.image);
            Log.i("9755556544","image "+ chat.imageUrl);
           if(chatList.get(pos).image==2){

               txtEncryption.setVisibility(View.VISIBLE);
               txtEncryption.setText(chatList.get(pos).message);
               rl_my_msg.setVisibility(View.GONE);
               Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(new RequestListener<Drawable>() {
                   @Override
                   public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                       img_progress.setVisibility(View.GONE);
                       return false;

                   }

                   @Override
                   public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                       img_progress.setVisibility(View.GONE);

                       return false;
                   }


               }).into(iv_image_msg);
               tv_my_msg_time.setText(time);
              // iv_my_msg_img.setVisibility(View.GONE);
               tv_my_msg_time.setVisibility(View.GONE);
               tv_my_chat.setVisibility(View.GONE);
               rl_img_view.setVisibility(View.GONE);
               if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {

                   tv_my_date_label.setVisibility(View.GONE);
               } else {
                   tv_my_date_label.setVisibility(View.GONE);
               }

               if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                   tv_my_date_label.setText(chat.banner_date);
                   tv_my_date_label.setVisibility(View.GONE);
               }
           }else if (chatList.get(pos).image==1){
               txtEncryption.setVisibility(View.GONE);
               String imguri=chat.imageUrl;
               Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(new RequestListener<Drawable>() {
                   @Override
                   public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                       img_progress.setVisibility(View.GONE);
                       return false;

                   }

                   @Override
                   public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                       img_progress.setVisibility(View.GONE);

                       return false;
                   }
               }).into(iv_image_msg);
              // Glide.with(mContext).load(myImage).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(iv_my_msg_img);

               SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
               try {
                   String time_str = sd.format(new Date((Long) chat.timeStamp));
                   time = time_str.replace("am", "AM").replace("pm", "PM");
               } catch (Exception e) {
                   e.printStackTrace();
               }
               tv_my_msg_time.setVisibility(View.VISIBLE);
               tv_my_msg_time.setText(time);

               tv_my_chat.setVisibility(View.GONE);
               rl_img_view.setVisibility(View.VISIBLE);
           }

           else if (chat.image==0){
               //gone
               txtEncryption.setVisibility(View.GONE);
               rl_my_msg.setVisibility(View.VISIBLE);
               tv_my_chat.setVisibility(View.VISIBLE);
               tv_my_chat.setText(chat.message);
              // iv_my_msg_img.setVisibility(View.VISIBLE);
               rl_img_view.setVisibility(View.GONE);
               SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
               try {
                   String time_str = sd.format(new Date((Long) chat.timeStamp));
                   time = time_str.replace("am", "AM").replace("pm", "PM");
               } catch (Exception e) {
                   e.printStackTrace();
               }


               tv_my_msg_time.setText(time);

               if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                   tv_my_date_label.setText(chat.banner_date);
                   tv_my_date_label.setVisibility(View.VISIBLE);
               } else {
                   tv_my_date_label.setVisibility(View.GONE);
               }

               if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                   tv_my_date_label.setText(chat.banner_date);
                   tv_my_date_label.setVisibility(View.VISIBLE);
               }

              // Glide.with(mContext).load(myImage).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(iv_my_msg_img);

              /* if (chat.image == 1) {
                   //   Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).into(iv_image_msg);

                   Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(new RequestListener<Drawable>() {
                       @Override
                       public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                           img_progress.setVisibility(View.GONE);
                           return false;

                       }

                       @Override
                       public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                           img_progress.setVisibility(View.GONE);

                           return false;
                       }
                   }).into(iv_image_msg);

                   tv_my_msg_time.setText(time);

                   tv_my_chat.setVisibility(View.GONE);
                   rl_img_view.setVisibility(View.VISIBLE);
               } else {
                   tv_my_chat.setText(chat.message);
                   tv_my_chat.setVisibility(View.VISIBLE);
                   rl_img_view.setVisibility(View.GONE);
                   tv_my_msg_time.setText(time);
               }*/
           }
        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.iv_image_msg:
                    openZoomImageDialog(chatList.get(getAdapterPosition()).imageUrl);
                    break;
            }
        }
    }

    public class OtherChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RelativeLayout rl_other_text_msg, rl_other_image_msg, rl_web_view, rl_normal_view;
        // My Chat Text Msg
        private TextView tv_other_chat, tv_other_msg_time, wb_other_msg_time, tv_my_date_label,txtEncryption;
      //  private ImageView iv_other_msg_img;
        private WebView wv_other_chat;

        private RelativeLayout rl_img_view,rl_other_msg;
        private ProgressBar img_progress;

        // My Chat Image Msg
       // private TextView tv_other_img_time;
        private ImageView iv_other_image_pic, iv_image_msg;   // iv_image_msg is image msg and iv_my_image_pic is my profile image

        OtherChatViewHolder(View itemView) {
            super(itemView);
            rl_other_text_msg = itemView.findViewById(R.id.rl_other_text_msg);

            rl_web_view = itemView.findViewById(R.id.rl_web_view);
            rl_normal_view = itemView.findViewById(R.id.rl_normal_view);
            rl_other_msg = itemView.findViewById(R.id.rl_other_msg);
            // wv_other_chat = itemView.findViewById(R.id.wv_other_chat);

            tv_other_chat = itemView.findViewById(R.id.tv_other_chat);
            txtEncryption = itemView.findViewById(R.id.txtEncryption);
            tv_other_msg_time = itemView.findViewById(R.id.tv_other_msg_time);
            wb_other_msg_time = itemView.findViewById(R.id.wb_other_msg_time);
          //  iv_other_msg_img = itemView.findViewById(R.id.iv_other_msg_img);
            tv_my_date_label = itemView.findViewById(R.id.tv_my_date_label);

            iv_image_msg = itemView.findViewById(R.id.iv_image_msg);
            rl_img_view = itemView.findViewById(R.id.rl_img_view);
            img_progress = itemView.findViewById(R.id.img_progress);
            iv_image_msg.setOnClickListener(this);
          //  iv_other_msg_img.setOnClickListener(this);
        }

        void otherBindData(Chat chat, int pos, int tempPos) {
           /* ((ChatActivity) mContext).chat_recycler_view.smoothScrollToPosition(chatList.size() - 1);*/


            if (chatList.get(pos).image==1){
                txtEncryption.setVisibility(View.GONE);
                rl_img_view.setVisibility(View.VISIBLE);
                //tv_other_img_time.setVisibility(View.VISIBLE);
                tv_other_msg_time.setText(time);
                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

                if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                }

                SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
                try {
                    String time_str = sd.format(new Date((Long) chat.timeStamp));
                    time = time_str.replace("am", "AM").replace("pm", "PM");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                tv_other_msg_time.setText(time);



                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

                if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                }

                Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        img_progress.setVisibility(View.GONE);
                        return false;

                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        img_progress.setVisibility(View.GONE);

                        return false;
                    }
                }).into(iv_image_msg);




                //Glide.with(mContext).load(otherImage).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(iv_other_msg_img);

            }else if (chatList.get(pos).image==0){
                txtEncryption.setVisibility(View.GONE);
                rl_img_view.setVisibility(View.GONE);
                tv_other_chat.setVisibility(View.VISIBLE);
                tv_other_chat.setText(chat.message);
              //  tv_other_img_time.setVisibility(View.VISIBLE);
               // tv_other_msg_time.setText(time);
                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

                if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                }

                SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
                try {
                    String time_str = sd.format(new Date((Long) chat.timeStamp));
                    time = time_str.replace("am", "AM").replace("pm", "PM");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tv_other_msg_time.setText(time);

                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

                if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                }

                //Glide.with(mContext).load(otherImage).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(iv_other_msg_img);

            }else if (chatList.get(pos).image==2){
                txtEncryption.setVisibility(View.VISIBLE);
                txtEncryption.setText(chat.message);
                rl_img_view.setVisibility(View.GONE);
                rl_other_msg.setVisibility(View.GONE);
                rl_other_text_msg.setVisibility(View.GONE);
                tv_other_chat.setVisibility(View.GONE);
                tv_my_date_label.setVisibility(View.GONE);
             //   tv_other_img_time.setVisibility(View.GONE);

            }

           /* if (chatList.get(pos).image==1){
                txtEncryption.setVisibility(View.VISIBLE);
                txtEncryption.setText(chatList.get(pos).message);
                tv_other_msg_time.setText(time);
                rl_img_view.setVisibility(View.GONE);
                tv_other_chat.setVisibility(View.GONE);
            }else {
                txtEncryption.setVisibility(View.GONE);
                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

                if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                }
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
            try {
                String time_str = sd.format(new Date((Long) chat.timeStamp));
                time = time_str.replace("am", "AM").replace("pm", "PM");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                tv_my_date_label.setText(chat.banner_date);
                tv_my_date_label.setVisibility(View.VISIBLE);
            } else {
                tv_my_date_label.setVisibility(View.GONE);
            }

            if (((ChatActivity) mContext).isCompleteChatLoad && pos == 0) {
                tv_my_date_label.setText(chat.banner_date);
                tv_my_date_label.setVisibility(View.VISIBLE);
            }

            Glide.with(mContext).load(otherImage).apply(new RequestOptions().placeholder(R.drawable.placeholder_user)).into(iv_other_msg_img);

            if (chat.image == 1) {
              //  Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).into(iv_image_msg);
                Glide.with(mContext).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        img_progress.setVisibility(View.GONE);
                        return false;

                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        img_progress.setVisibility(View.GONE);

                        return false;
                    }
                }).into(iv_image_msg);

                tv_other_msg_time.setText(time);
                rl_img_view.setVisibility(View.VISIBLE);
                tv_other_chat.setVisibility(View.GONE);

            } else {
                rl_img_view.setVisibility(View.GONE);
                tv_other_chat.setVisibility(View.VISIBLE);

                tv_other_chat.setText(chat.message);
                tv_other_msg_time.setText(time);


            }*/

        }

        @Override
        public void onClick(View v) {
            // Preventing multiple clicks, using threshold of 1/2 second
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()) {
                case R.id.iv_image_msg:
                    openZoomImageDialog(chatList.get(getAdapterPosition()).imageUrl);
                    break;

              /*  case R.id.iv_other_msg_img:

                    if (!chatList.get(getAdapterPosition()).uid.equals("1")){
                        ((ChatActivity)mContext).setUnread_count();
                        Intent intent=new Intent(mContext,MatchProfileActivity.class);
                        intent.putExtra("match_user_id",chatList.get(getAdapterPosition()).uid);
                        mContext.startActivity(intent);   
                    }else {
                        Intent intent = new Intent(mContext, AdminProfileActivity.class);
                        intent.putExtra("adminId", chatList.get(getAdapterPosition()).uid);
                        mContext.startActivity(intent);
                    }
                   
                    break;*/
            }
        }
    }

    private void openZoomImageDialog(String image) {
        zoomImageDialog = new Dialog(mContext, R.style.MyAppTheme);
        zoomImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        zoomImageDialog.setContentView(R.layout.dialog_zoom_image_view);

        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(zoomImageDialog.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        zoomImageDialog.getWindow().setAttributes(windowParams);

        ImageView mphoto_view = zoomImageDialog.findViewById(R.id.img_enlarge);
        ImageView iv_back = zoomImageDialog.findViewById(R.id.iv_back);

        Glide.with(mContext).load(image).into(mphoto_view);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageDialog.dismiss();
            }
        });

        zoomImageDialog.getWindow().setGravity(Gravity.CENTER);
        zoomImageDialog.show();
    }
}
