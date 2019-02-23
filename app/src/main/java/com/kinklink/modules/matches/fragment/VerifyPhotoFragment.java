package com.kinklink.modules.matches.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.ImageRotationHelper;
import com.kinklink.helper.Progress;
import com.kinklink.image.picker.ImagePicker;
import com.kinklink.image.picker.ImageRotator;
import com.kinklink.modules.matches.activity.MyProfileActivity;
import com.kinklink.server_task.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class VerifyPhotoFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private TextView btn_try_again;
    private TextView tv_verify_status;
    private TextView tv_reject_reason;
    private TextView btn_submit_photo;
    private ImageView iv_back, iv_verify_status_img, iv_verify_image;
    private Progress progress;
    private LinearLayout ly_verify_photo, ly_no_network, ly_verify_photo_status, ly_reason;
    private RelativeLayout rl_add_verify_photo;
    private Bitmap verifyPhotoBitmap;
    private String is_verify = "", gesture_photo;

    // variable to track event time
    private long mLastClickTime = 0;

    public VerifyPhotoFragment() {
    }

    public static VerifyPhotoFragment newInstance() {
        Bundle args = new Bundle();
        VerifyPhotoFragment fragment = new VerifyPhotoFragment();
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
        View view = inflater.inflate(R.layout.fragment_verify_photo, container, false);
        init(view);

        progress = new Progress(mContext);
        progress.dismiss();
        callGetVerifyStatus();

        iv_back.setOnClickListener(this);
        rl_add_verify_photo.setOnClickListener(this);
        btn_submit_photo.setOnClickListener(this);
        iv_verify_status_img.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        TextView action_bar_heading = ((MyProfileActivity) mContext).findViewById(R.id.action_bar_heading);
        action_bar_heading.setText(getString(R.string.verify_photo));

        iv_back = ((MyProfileActivity) mContext).findViewById(R.id.iv_back);

        ImageView iv_settings = ((MyProfileActivity) mContext).findViewById(R.id.iv_settings);
        iv_settings.setVisibility(View.GONE);

        TextView tv_verify_txt = view.findViewById(R.id.tv_verify_txt);
        String text = "<font color=" + ContextCompat.getColor(mContext, R.color.field_text_color) + ">" + getString(R.string.verify_photo_instruction1) + "</font> <font color=" + ContextCompat.getColor(mContext, R.color.colorPrimary) + ">" + getString(R.string.app_name) + "</font> <font color=" + ContextCompat.getColor(mContext, R.color.field_text_color) + ">" + getString(R.string.verify_photo_instruction2) + "</font>";
        tv_verify_txt.setText(Html.fromHtml(text));

        ly_verify_photo = view.findViewById(R.id.ly_verify_photo);
        ly_verify_photo_status = view.findViewById(R.id.ly_verify_photo_status);
        ly_reason = view.findViewById(R.id.ly_reason);
        ly_no_network = view.findViewById(R.id.ly_no_network);
        btn_try_again = view.findViewById(R.id.btn_try_again);
        tv_verify_status = view.findViewById(R.id.tv_verify_status);
        iv_verify_status_img = view.findViewById(R.id.iv_verify_status_img);
        tv_reject_reason = view.findViewById(R.id.tv_reject_reason);
        iv_verify_image = view.findViewById(R.id.iv_verify_image);
        rl_add_verify_photo = view.findViewById(R.id.rl_add_verify_photo);
        btn_submit_photo = view.findViewById(R.id.btn_submit_photo);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        progress.dismiss();

        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.iv_back:
                ((MyProfileActivity) mContext).onBackPressed();
                break;

            case R.id.rl_add_verify_photo:
                progress.dismiss();
                if (!is_verify.equals("")) {
                    if (is_verify.equals("0")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(
                                        new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                            } else {
                                ImagePicker.pickImage(VerifyPhotoFragment.this);
                            }
                        } else {
                            ImagePicker.pickImage(VerifyPhotoFragment.this);
                        }
                    }
                }
                break;

            case R.id.btn_submit_photo:
                if (verifyPhotoBitmap == null) {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.verify_photo_null));
                } else {
                    try {
                        Bitmap task_decoded = ImageRotationHelper.imageCopressor(verifyPhotoBitmap,mContext);
                        callVerifyPhotoUploadApi(task_decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;

            case R.id.iv_verify_status_img:
                if (!is_verify.equals("")) {
                    if (is_verify.equals("0")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(
                                        new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                            } else {
                                ImagePicker.pickImage(VerifyPhotoFragment.this);
                            }
                        } else {
                            ImagePicker.pickImage(VerifyPhotoFragment.this);
                        }
                    } else if (is_verify.equals("2")) {
                        progress.dismiss();
                        ly_verify_photo_status.setVisibility(View.GONE);
                        ly_reason.setVisibility(View.GONE);
                        is_verify = "0";

                        Glide.with(mContext).load(gesture_photo).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.placeholder_user))).into(iv_verify_image);

                        iv_verify_status_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.add_ico));
                        iv_verify_status_img.setVisibility(View.VISIBLE);
                        btn_submit_photo.setVisibility(View.VISIBLE);
                        rl_add_verify_photo.setEnabled(true);
                    }
                }
                break;
        }
    }

    private void callGetVerifyStatus() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");
                        is_verify = js.getString("is_verify");
                        String reason = js.getString("reason");
                        String verify_image = js.getString("verify_image");
                        gesture_photo = js.getString("gesture_photo");

                        if (status.equals("success")) {
                            progress.dismiss();
                            ly_verify_photo.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            if (isAdded()) {
                                switch (is_verify) {
                                    case "0":
                                        Glide.with(mContext).load(gesture_photo).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.placeholder_user))).into(iv_verify_image);
                                        ly_verify_photo_status.setVisibility(View.GONE);
                                        ly_reason.setVisibility(View.GONE);
                                        iv_verify_status_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.add_ico));
                                        iv_verify_status_img.setVisibility(View.VISIBLE);
                                        btn_submit_photo.setVisibility(View.VISIBLE);
                                        rl_add_verify_photo.setEnabled(true);
                                        break;

                                    case "1":
                                        ly_verify_photo_status.setVisibility(View.VISIBLE);
                                        ly_reason.setVisibility(View.GONE);
                                        tv_verify_status.setText(getString(R.string.being_reviewed));
                                        tv_verify_status.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                                        iv_verify_status_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.review_ico));
                                        iv_verify_status_img.setVisibility(View.VISIBLE);
                                        btn_submit_photo.setVisibility(View.GONE);
                                        rl_add_verify_photo.setEnabled(false);
                                        break;

                                    case "2":
                                        ly_verify_photo_status.setVisibility(View.VISIBLE);
                                        tv_verify_status.setText(getString(R.string.rejected));
                                        tv_verify_status.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                                        iv_verify_status_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.refresh_ico));
                                        iv_verify_status_img.setVisibility(View.VISIBLE);
                                        tv_reject_reason.setText(reason);
                                        ly_reason.setVisibility(View.VISIBLE);
                                        btn_submit_photo.setVisibility(View.GONE);
                                        rl_add_verify_photo.setEnabled(false);
                                        break;

                                    case "3":
                                        ly_verify_photo_status.setVisibility(View.VISIBLE);
                                        tv_verify_status.setText(getString(R.string.verified));
                                        tv_verify_status.setTextColor(ContextCompat.getColor(mContext, R.color.accept_color));
                                        iv_verify_status_img.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.tick_img));
                                        iv_verify_status_img.setVisibility(View.VISIBLE);
                                        btn_submit_photo.setVisibility(View.GONE);
                                        ly_reason.setVisibility(View.GONE);
                                        rl_add_verify_photo.setEnabled(false);
                                        break;
                                }

                                if (!verify_image.equals("")) {
                                    Glide.with(mContext).load(verify_image).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.placeholder_user))).into(iv_verify_image);
                                }
                            }

                        } else {
                            progress.dismiss();
                            ly_verify_photo.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        ly_verify_photo.setVisibility(View.VISIBLE);
                        ly_no_network.setVisibility(View.GONE);
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    ly_verify_photo.setVisibility(View.VISIBLE);
                    ly_no_network.setVisibility(View.GONE);
                }

            });
            api.callApi("getVerifyStatus", Request.Method.GET, null);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            assert view != null;
            TextView btn_try_again = view.findViewById(R.id.btn_try_again);
            ly_verify_photo.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callGetVerifyStatus();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(VerifyPhotoFragment.this);
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(VerifyPhotoFragment.this);
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

                try {
                    Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);

                    int orientation = ImageRotator.getRotation(mContext, imageUri, true);

                    verifyPhotoBitmap = ImageRotator.rotate(tempBitmap, orientation);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError error) {
                    CustomToast.getInstance(mContext).showToast(mContext, mContext.getResources().getString(R.string.alertOutOfMemory));
                }

                Glide.with(mContext).load(verifyPhotoBitmap).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext,R.drawable.placeholder_user))).into(iv_verify_image);
            }
        }
    }

    // Image Upload Api
    private void callVerifyPhotoUploadApi(final Bitmap imageBitmap) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

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

                        if (isAdded()) {
                            if (status.equals("success")) {
                                progress.dismiss();
                                ly_verify_photo.setVisibility(View.VISIBLE);
                                ly_no_network.setVisibility(View.GONE);

                                callGetVerifyStatus();

                            } else {
                                progress.dismiss();
                                CustomToast.getInstance(mContext).showToast(mContext, message);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (isAdded()) {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                        }
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    if (isAdded()) {
                        progress.dismiss();
                    }
                }
            });
            api.callMultiPartApi("uploadVerifyPhoto", map, bitmapList);
        } else {

            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            ly_verify_photo.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callVerifyPhotoUploadApi(imageBitmap);
                }
            });
        }
    }
}
