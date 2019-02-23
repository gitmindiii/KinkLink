package com.kinklink.modules.authentication.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.ImageRotationHelper;
import com.kinklink.helper.Progress;
import com.kinklink.helper.Utils;
import com.kinklink.image.picker.ImagePicker;
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.adapter.ProfileImageAdapter;
import com.kinklink.modules.authentication.listener.ProfileImageListener;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.authentication.model.GetUserDetailModel;
import com.kinklink.modules.authentication.model.ProfileImageModel;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.matches.activity.MainActivity;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;
import com.kinklink.view.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class EditProfilePictureFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private RecyclerView profile_recycler_view;
    private ProfileImageAdapter profileImageAdapter;
    private ArrayList<ProfileImageModel> imagesList;


    private ImageView iv_back, iv_step_one, iv_step_two, iv_step_three, iv_step_one_bullet, iv_step_two_bullet;
    private TextView tv_step_two, tv_basic_info, tv_step_three, tv_other_info,
            tv_profile_pictures;
    private View status_view_1, status_view_2;
    private TextView btn_edit_images;

    //Alert Dialog declaration
    private Dialog dialog;

    // variable to track event time
    private long mLastClickTime = 0;

    private LinearLayout ly_no_network, ly_edit_profile;
    private TextView btn_try_again;
    private Progress progress;

    public EditProfilePictureFragment() {
    }

    public static EditProfilePictureFragment newInstance() {
        Bundle args = new Bundle();
        EditProfilePictureFragment fragment = new EditProfilePictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photos, container, false);
        init(view);

        progress = new Progress(mContext);
        setEditProfilePicFragmentActive();

        imagesList = new ArrayList<>();

        // Get user details api
        getUserDetails();

        // Adapter Listener to pick image or delete image
        adapterForEditProfilePic();

        // Add default add image icon to model
        if (imagesList.size() == 0) {
            ProfileImageModel modal = new ProfileImageModel();
            modal.img = ContextCompat.getDrawable(mContext, R.drawable.upload_image);
            imagesList.add(imagesList.size(), modal);
            profileImageAdapter.notifyDataSetChanged();
        }

        // Click Listeners
        btn_edit_images.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        return view;
    }

    // Adapter Listener to pick image or delete image
    private void adapterForEditProfilePic() {
        profileImageAdapter = new ProfileImageAdapter(imagesList, mContext, new ProfileImageListener() {
            @Override
            public void getPosition(int position, boolean pickImage) {
                if (pickImage) {    // Pick image from camera or gallery
                    if (position + 1 == imagesList.size()) {
                        if (Build.VERSION.SDK_INT >= 23) {

                            if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(
                                        new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                            } else {
                                ImagePicker.pickImage(EditProfilePictureFragment.this);
                            }
                        } else {
                            ImagePicker.pickImage(EditProfilePictureFragment.this);
                        }


                    }
                } else {   // Delete image
                    if (imagesList.size() > 1) {
                        btn_edit_images.setEnabled(false);
                        deleteUserProfileImage(imagesList.get(position).imageId, position);
                    }
                }
            }
        });
        profile_recycler_view.setLayoutManager(new GridLayoutManager(mContext, 3));
        profile_recycler_view.setAdapter(profileImageAdapter);
    }

    // Get User details Api
    private void getUserDetails() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_edit_profile.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            Gson gson = new Gson();
                            GetUserDetailModel getUser = gson.fromJson(String.valueOf(js), GetUserDetailModel.class);

                            for (int i = 0; i < getUser.userDetail.images.size(); i++) {
                                ProfileImageModel imageModal = new ProfileImageModel();
                                imageModal.imageId = getUser.userDetail.images.get(i).userImageId;
                                imageModal.profileUrl = getUser.userDetail.images.get(i).image;
                                imagesList.add(0, imageModal);
                            }

                            profileImageAdapter.notifyDataSetChanged();
                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/userDetail", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    getUserDetails();
                }
            });
        }
    }

    private void init(View view) {
        profile_recycler_view = view.findViewById(R.id.profile_recycler_view);
        iv_back = ((EditProfileActivity) mContext).findViewById(R.id.iv_back);
        iv_step_one = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_one);
        iv_step_one_bullet = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_one_bullet);
        iv_step_two_bullet = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_two_bullet);
        iv_step_two = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_two);
        tv_step_two = ((EditProfileActivity) mContext).findViewById(R.id.tv_step_two);
        tv_other_info = ((EditProfileActivity) mContext).findViewById(R.id.tv_other_info);
        tv_basic_info = ((EditProfileActivity) mContext).findViewById(R.id.tv_basic_info);
        tv_profile_pictures = ((EditProfileActivity) mContext).findViewById(R.id.tv_profile_pictures);
        status_view_1 = ((EditProfileActivity) mContext).findViewById(R.id.status_view_1);
        status_view_2 = ((EditProfileActivity) mContext).findViewById(R.id.status_view_2);
        iv_step_three = ((EditProfileActivity) mContext).findViewById(R.id.iv_step_three);
        tv_step_three = ((EditProfileActivity) mContext).findViewById(R.id.tv_step_three);

        btn_edit_images = view.findViewById(R.id.btn_edit_images);
        TextView btn_upload_images = view.findViewById(R.id.btn_upload_images);
        TextView btn_skip = view.findViewById(R.id.btn_skip);
        btn_edit_images.setVisibility(View.GONE);
        btn_upload_images.setVisibility(View.VISIBLE);
        btn_upload_images.setText(R.string.done);
        btn_skip.setVisibility(View.VISIBLE);
        btn_skip.setOnClickListener(this);
        btn_upload_images.setOnClickListener(this);

        dialog = new Dialog(mContext);

        ly_edit_profile = ((EditProfileActivity) mContext).findViewById(R.id.ly_edit_profile);
        ly_no_network = ((EditProfileActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((EditProfileActivity) mContext).findViewById(R.id.btn_try_again);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Set Upload Photos Fragment Active
    private void setEditProfilePicFragmentActive() {
        iv_back.setVisibility(View.VISIBLE);
        iv_step_one.setVisibility(View.VISIBLE);
        iv_step_one_bullet.setVisibility(View.GONE);
        iv_step_two.setVisibility(View.VISIBLE);
        iv_step_two_bullet.setVisibility(View.GONE);
        tv_step_two.setVisibility(View.GONE);
        iv_step_three.setVisibility(View.VISIBLE);
        tv_step_three.setVisibility(View.GONE);
        Utils.setTypeface(tv_basic_info, mContext, R.font.lato_regular);
        Utils.setTypeface(tv_other_info, mContext, R.font.lato_regular);
        Utils.setTypeface(tv_profile_pictures, mContext, R.font.lato_bold);
        status_view_1.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        status_view_2.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
    }

    @Override
    public void onClick(View view) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.btn_edit_images:   // Upload Images button
                // Write data to user table in firebase
                addUserFirebaseDatabase();
                startActivity(new Intent(mContext, MainActivity.class));

                ((EditProfileActivity) mContext).finish();
                break;

            case R.id.dialog_decline_button:  // Dialog dismiss button
                dialog.dismiss();
                break;

            case R.id.btn_alert:   // Camera Permission denied Alert button
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                break;

            case R.id.iv_back:    // Back
                ((EditProfileActivity) mContext).onBackPressed();
                break;

            case R.id.btn_skip:
                addUserFirebaseDatabase();
                startActivity(new Intent(mContext, MainActivity.class));
                ((EditProfileActivity) mContext).finish();
                break;


            case R.id.btn_upload_images:
                addUserFirebaseDatabase();
                startActivity(new Intent(mContext, MainActivity.class));
                ((EditProfileActivity) mContext).finish();

                break;


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(EditProfilePictureFragment.this);
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(EditProfilePictureFragment.this);
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {    // Image Picker
                Uri imageUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);
                if (imageUri != null) {   // Calling Cropper after picking image
                   /* CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setAspectRatio(4, 3)
                            .setRequestedSize(640, 480, CropImageView.RequestSizeOptions.RESIZE_EXACT)
                            .start(mContext, EditProfilePictureFragment.this);*/
                    try {
                        Bitmap profileImageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
                        btn_edit_images.setEnabled(false);
                        new ImageCopressionTask(imageUri, profileImageBitmap).execute();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {   // Image Cropper
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null) {
                        Bitmap profileImageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), result.getUri());
                        btn_edit_images.setEnabled(false);
                        callImageUploadApi(profileImageBitmap);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    CustomToast.getInstance(mContext).showToast(mContext, mContext.getResources().getString(R.string.alertImageException));
                } catch (OutOfMemoryError error) {
                    CustomToast.getInstance(mContext).showToast(mContext, mContext.getResources().getString(R.string.alertOutOfMemory));
                }

            }


        }
    }

    // Image Upload Api
    private void callImageUploadApi(final Bitmap imageBitmap) {
        if (AppHelper.isConnectingToInternet(mContext)) {


            Map<String, Bitmap> bitmapList = null;
            final Map<String, String> map = new HashMap<>();

            if (imageBitmap == null) {
                map.put("image", "");
            } else {
                bitmapList = new HashMap<>();
                bitmapList.put("image", imageBitmap);
            }

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_edit_profile.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            imagesList.clear();
                            if (imageBitmap != null) {
                                JSONArray jsonArray = jsonObject.getJSONArray("userImage");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    ProfileImageModel profileImageModel = new ProfileImageModel();
                                    profileImageModel.profileBitmap = imageBitmap;
                                    profileImageModel.imageId = jsonArray.getJSONObject(i).getString("userImageId");
                                    profileImageModel.profileUrl = jsonArray.getJSONObject(i).getString("image");

                                    imagesList.add(0, profileImageModel);
                                }

                                ProfileImageModel modal = new ProfileImageModel();
                                modal.img = ContextCompat.getDrawable(mContext, R.drawable.upload_image);
                                imagesList.add(imagesList.size(), modal);
                                profileImageAdapter.notifyDataSetChanged();
                            }
                            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.profile_image_upload_success));
                            btn_edit_images.setEnabled(true);
                        } else {
                            progress.dismiss();
                            btn_edit_images.setEnabled(true);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        btn_edit_images.setEnabled(true);
                        progress.dismiss();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    btn_edit_images.setEnabled(true);
                }
            });
            api.callMultiPartApi("uploadUserImage", map, bitmapList);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callImageUploadApi(imageBitmap);
                }
            });
        }
    }

    // Delete Profile Image Api
    private void deleteUserProfileImage(final String imageId, final int position) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            Map<String, String> map = new HashMap<>();
            map.put("imageId", imageId);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_edit_profile.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            imagesList.remove(position);
                            profileImageAdapter.notifyDataSetChanged();

                            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.profile_image_delete_success));
                            btn_edit_images.setEnabled(true);
                        } else {
                            progress.dismiss();
                            btn_edit_images.setEnabled(true);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        btn_edit_images.setEnabled(true);
                        progress.dismiss();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    btn_edit_images.setEnabled(true);
                }

            });
            api.callApi("removeImage", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_edit_profile.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteUserProfileImage(imageId, position);
                }
            });
        }
    }

    // Write data to user table in firebase
    private void addUserFirebaseDatabase() {
        RegistrationInfo registrationInfo = new Session(mContext).getRegistration();
        String device_token = FirebaseInstanceId.getInstance().getToken();
        String name = registrationInfo.userDetail.full_name;
        String uId = registrationInfo.userDetail.userId;

        String profilePic;
        if (imagesList.get(0).profileUrl != null) {
            profilePic = imagesList.get(0).profileUrl;
        } else {
            profilePic = registrationInfo.userDetail.defaultImg;
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseUserModel userModel = new FirebaseUserModel();
        userModel.firebaseToken = device_token;
        userModel.name = name;
        userModel.profilePic = profilePic;
        userModel.timeStamp = ServerValue.TIMESTAMP;
        userModel.uid = uId;
        userModel.isAdmin = "0";
        userModel.isUserDeleted = 0;
        database.child(Constant.USER_TABLE).child(uId).setValue(userModel).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progress != null) {
            progress.dismiss();
        }
    }


    class ImageCopressionTask extends AsyncTask<Void, Void, Void> {
        Bitmap task_bitmap;
        Uri task_uri;
        Bitmap task_decoded = null;

        ImageCopressionTask(Uri task_uri, Bitmap task_bitmap) {
            this.task_bitmap = task_bitmap;
            this.task_uri = task_uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();

        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int angle = ImageRotationHelper.checkPictureRotatation(task_uri);
                int flipType = ImageRotationHelper.checkIslFlipped(task_uri);
                Bitmap rotate_bitmap = ImageRotationHelper.rotateAndFlippedImage(task_bitmap, angle, flipType);
                if (rotate_bitmap == null) rotate_bitmap = task_bitmap;
                task_decoded = ImageRotationHelper.imageCopressor(rotate_bitmap,mContext);

            } catch (IOException e) {
                e.printStackTrace();

            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (task_decoded != null) callImageUploadApi(task_decoded);
            else if (task_bitmap != null) {
                try {
                    task_decoded = ImageRotationHelper.imageCopressor(task_bitmap,mContext);
                    if (task_decoded != null) callImageUploadApi(task_decoded);
                    else callImageUploadApi(task_bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else progress.dismiss();
        }
    }


}
