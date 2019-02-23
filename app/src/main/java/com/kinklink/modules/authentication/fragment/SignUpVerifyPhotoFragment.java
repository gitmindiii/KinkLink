package com.kinklink.modules.authentication.fragment;

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
import android.support.v7.widget.CardView;
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
import com.kinklink.helper.Progress;
import com.kinklink.image.picker.ImagePicker;
import com.kinklink.image.picker.ImageRotator;
import com.kinklink.modules.authentication.activity.RegistrationActivity;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class SignUpVerifyPhotoFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private Progress progress;
    private Session session;

    private ImageView iv_back, iv_step_one, iv_step_two, iv_step_three, iv_step_one_bullet, iv_step_two_bullet, iv_step_three_bullet, iv_step_four_bullet,
            iv_verify_photo;
    private TextView tv_registration_heading;
    private TextView tv_step_two;
    private TextView tv_step_three;
    private TextView tv_step_four;
    private TextView btn_try_again;
    private TextView btn_submit_photo;
    private TextView btn_skip_verify_photo;
    private View status_view_1, status_view_2, status_view_3;

    private RelativeLayout rl_upload_verify_photo;
    private Bitmap verifyPhotoBitmap;
    private CardView cv_registration;
    private LinearLayout ly_no_network;
    private String gesture_photo;

    // variable to track event time
    private long mLastClickTime = 0;


    public SignUpVerifyPhotoFragment() {
    }

    public static SignUpVerifyPhotoFragment newInstance() {
        Bundle args = new Bundle();
        SignUpVerifyPhotoFragment fragment = new SignUpVerifyPhotoFragment();
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
        View view = inflater.inflate(R.layout.fragment_sign_up_verify_photo, container, false);
        init(view);

        progress = new Progress(mContext);

        session = new Session(mContext);
        session.setScreen("SignUpVerifyPhotoFragment");

        // Set Create Account Fragment Active
        setSignUpVerifyPhotoFragmentActive();

        rl_upload_verify_photo.setOnClickListener(this);
        btn_submit_photo.setOnClickListener(this);
        btn_skip_verify_photo.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        iv_back = ((RegistrationActivity) mContext).findViewById(R.id.iv_back);
        tv_registration_heading = ((RegistrationActivity) mContext).findViewById(R.id.tv_registration_heading);

        // Step 1
        iv_step_one = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_one);
        iv_step_one_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_one_bullet);

        // Step 2
        iv_step_two = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_two);
        iv_step_two_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_two_bullet);
        tv_step_two = ((RegistrationActivity) mContext).findViewById(R.id.tv_step_two);

        // Step 3
        iv_step_three = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_three);
        iv_step_three_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_three_bullet);
        tv_step_three = ((RegistrationActivity) mContext).findViewById(R.id.tv_step_three);

        // Step 4
        iv_step_four_bullet = ((RegistrationActivity) mContext).findViewById(R.id.iv_step_four_bullet);
        tv_step_four = ((RegistrationActivity) mContext).findViewById(R.id.tv_step_four);

        status_view_1 = ((RegistrationActivity) mContext).findViewById(R.id.status_view_1);
        status_view_2 = ((RegistrationActivity) mContext).findViewById(R.id.status_view_2);
        status_view_3 = ((RegistrationActivity) mContext).findViewById(R.id.status_view_3);

        TextView tv_verify_txt = view.findViewById(R.id.tv_verify_txt);
        String text = "<font color=" + ContextCompat.getColor(mContext, R.color.verify_photo_instruction_msg) + ">" + getString(R.string.verify_photo_instruction1) + "</font> <font color=" + ContextCompat.getColor(mContext, R.color.colorPrimary) + ">" + getString(R.string.app_name) + "</font> <font color=" + ContextCompat.getColor(mContext, R.color.verify_photo_instruction_msg) + ">" + getString(R.string.verify_photo_instruction2) + "</font>";
        tv_verify_txt.setText(Html.fromHtml(text));

        rl_upload_verify_photo = view.findViewById(R.id.rl_upload_verify_photo);
        iv_verify_photo = view.findViewById(R.id.iv_verify_photo);
        btn_submit_photo = view.findViewById(R.id.btn_submit_photo);
        btn_skip_verify_photo = view.findViewById(R.id.btn_skip_verify_photo);

        cv_registration = ((RegistrationActivity) mContext).findViewById(R.id.cv_registration);
        ly_no_network = ((RegistrationActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((RegistrationActivity) mContext).findViewById(R.id.btn_try_again);

        callGetVerifyStatus();
    }

    private void setSignUpVerifyPhotoFragmentActive() {
        iv_back.setVisibility(View.GONE);
        tv_registration_heading.setText(getString(R.string.verify_photo));

       /* // Step 1
        iv_step_one.setVisibility(View.VISIBLE);
        iv_step_one_bullet.setVisibility(View.GONE);

        // Step 2
        iv_step_two.setVisibility(View.GONE);
        iv_step_two_bullet.setVisibility(View.VISIBLE);
        tv_step_two.setVisibility(View.GONE);

        // Step 3
        iv_step_three.setVisibility(View.GONE);
        iv_step_three_bullet.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.VISIBLE);

        // Step 4
        iv_step_four_bullet.setVisibility(View.GONE);
        tv_step_four.setVisibility(View.VISIBLE);

        status_view_1.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        status_view_2.setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));
        status_view_3.setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));*/

        // Step 1
        iv_step_one.setVisibility(View.VISIBLE);
        iv_step_one_bullet.setVisibility(View.GONE);

        // Step 2
        iv_step_two.setVisibility(View.VISIBLE);
        iv_step_two_bullet.setVisibility(View.GONE);
        tv_step_two.setVisibility(View.GONE);

        // Step 3
        iv_step_three.setVisibility(View.GONE);
        iv_step_three_bullet.setVisibility(View.VISIBLE);
        tv_step_three.setVisibility(View.GONE);

        // Step 4
        iv_step_four_bullet.setVisibility(View.GONE);
        tv_step_four.setVisibility(View.VISIBLE);

        status_view_1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        status_view_2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        status_view_3.setBackgroundColor(getResources().getColor(R.color.gray));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.rl_upload_verify_photo:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        ImagePicker.pickImage(SignUpVerifyPhotoFragment.this);
                    }
                } else {
                    ImagePicker.pickImage(SignUpVerifyPhotoFragment.this);
                }
                break;

            case R.id.btn_submit_photo:
                if (verifyPhotoBitmap == null) {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.verify_photo_null));
                } else {
                    callVerifyPhotoUploadApi(verifyPhotoBitmap);
                }
                break;

            case R.id.btn_skip_verify_photo:// Skip Verify Photo fragment
                callSkipVerifyPhotoApi();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(SignUpVerifyPhotoFragment.this);
                } /*else {
                 //   cameraExternalPermissionDenied(getString(R.string.denied_external_storage_permission));

                    if (Build.VERSION.SDK_INT >= 23) {
                        if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(
                                    new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                }*/
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(SignUpVerifyPhotoFragment.this);
                } /*else {
                  //  cameraExternalPermissionDenied(getString(R.string.denied_camera_permission));
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(
                                    new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                }*/
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

                Glide.with(mContext).load(verifyPhotoBitmap).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext, R.drawable.placeholder_user))).into(iv_verify_photo);
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

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            RegistrationInfo registrationInfo = session.getRegistration();
                            registrationInfo.userDetail.profile_step = "3";
                            session.createRegistration(registrationInfo);
                            //  session.setScreen("UploadPhotosFragment");
                            //  ((RegistrationActivity) mContext).replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);

                            session.setScreen("SelectInterestFragment");
                            ((RegistrationActivity) mContext).replaceFragment(SelectInterestFragment.newInstance(), false, R.id.reg_fragment_place);

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
            api.callMultiPartApi("uploadVerifyPhoto", map, bitmapList);
        } else {

            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            cv_registration.setVisibility(View.GONE);
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

    // Skip Verify Photo fragment
    private void callSkipVerifyPhotoApi() {
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
                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            RegistrationInfo registrationInfo = session.getRegistration();
                            registrationInfo.userDetail.profile_step = "3";
                            session.createRegistration(registrationInfo);
                            // session.setScreen("UploadPhotosFragment");
                            // ((RegistrationActivity) mContext).replaceFragment(UploadPhotosFragment.newInstance(), false, R.id.reg_fragment_place);

                            session.setScreen("SelectInterestFragment");
                            ((RegistrationActivity) mContext).replaceFragment(SelectInterestFragment.newInstance(), false, R.id.reg_fragment_place);

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
            api.callApi("skipUploadPicture", Request.Method.GET, null);
        } else {
            //  CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.alert_no_network));

            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            cv_registration.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Preventing multiple clicks, using threshold of 1/2 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    callSkipVerifyPhotoApi();
                }
            });
        }
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

    private void callGetVerifyStatus() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        gesture_photo = js.getString("gesture_photo");

                        Glide.with(mContext).load(gesture_photo).apply(new RequestOptions().placeholder(ContextCompat.getDrawable(mContext,R.drawable.placeholder_user))).into(iv_verify_photo);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.went_wrong));
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("getVerifyStatus", Request.Method.GET, null);
        }
    }
}
