package com.kinklink.modules.authentication.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.kinklink.R;
import com.kinklink.application.KinkLink;
import com.kinklink.helper.AppHelper;
import com.kinklink.helper.Constant;
import com.kinklink.helper.CustomToast;
import com.kinklink.helper.Progress;
import com.kinklink.modules.authentication.activity.ActivityEmailVarification;
import com.kinklink.modules.authentication.activity.EditProfileActivity;
import com.kinklink.modules.authentication.activity.RegistrationActivity;
import com.kinklink.modules.authentication.adapter.InterestsAdapter;
import com.kinklink.modules.authentication.listener.InterestIdListener;
import com.kinklink.modules.authentication.model.AdminModel;
import com.kinklink.modules.authentication.model.FirebaseUserModel;
import com.kinklink.modules.authentication.model.GetUserDetailModel;
import com.kinklink.modules.authentication.model.InterestsModel;
import com.kinklink.modules.authentication.model.RegistrationInfo;
import com.kinklink.modules.chat.model.Chat;
import com.kinklink.server_task.WebService;
import com.kinklink.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SelectInterestFragment extends Fragment implements View.OnClickListener {
    private ArrayList<InterestsModel> interestsList;
    private Context mContext;
    private ListView interest_listView;
    private String selected_interests = "";
    private Session session;

    private ImageView iv_back, iv_step_one, iv_step_two, iv_step_three, iv_step_one_bullet,
            iv_step_two_bullet, iv_step_three_bullet, iv_step_four_bullet;
    private TextView tv_registration_heading, tv_step_two, tv_step_three, tv_step_four;
    private View status_view_1, status_view_2, status_view_3;
    private TextView btn_sign_up, btn_try_again;

    private CardView cv_registration;
    private LinearLayout ly_no_network;

    private String chatNode;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUserModel otherUserInfo;
    private String myUId, otherUId, otherName, otherProfileImage;
    private Progress progress;
    // variable to track event time
    private long mLastClickTime = 0;

    public SelectInterestFragment() {
    }

    public static SelectInterestFragment newInstance() {
        Bundle args = new Bundle();
        SelectInterestFragment fragment = new SelectInterestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Get other user data from User table from firebase with help of UID
        //gettingDataFromUserTable("1");
        //addUserFirebaseDatabase();


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg_select_interests, container, false);
        init(view);
        progress = new Progress(mContext);
        session = new Session(mContext);
        session.setScreen("SelectInterestFragment");
        getAdminList();
        // Select Interest Fragment Active
        setSelectInterestFragmentActive();

        interestsList = new ArrayList<>();

        // Call getInterest List Api only if list size is 0, else only display list
        if (interestsList.size() == 0) {
            getInterestList();
        } else displayInterestsDialog(interestsList);

        btn_sign_up.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        interest_listView = view.findViewById(R.id.interest_listView);
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

        btn_sign_up = view.findViewById(R.id.btn_sign_up);

        cv_registration = ((RegistrationActivity) mContext).findViewById(R.id.cv_registration);
        ly_no_network = ((RegistrationActivity) mContext).findViewById(R.id.ly_no_network);
        btn_try_again = ((RegistrationActivity) mContext).findViewById(R.id.btn_try_again);
    }

    // Set Select Interest Fragment Active
    private void setSelectInterestFragmentActive() {
        iv_back.setVisibility(View.GONE);
        tv_registration_heading.setText(getString(R.string.reg_interests));

        // Step 1
        iv_step_one.setVisibility(View.VISIBLE);
        iv_step_one_bullet.setVisibility(View.GONE);

        // Step 2
        iv_step_two.setVisibility(View.VISIBLE);
        iv_step_two_bullet.setVisibility(View.GONE);
        tv_step_two.setVisibility(View.GONE);

        // Step 3
        iv_step_three.setVisibility(View.VISIBLE);
        iv_step_three_bullet.setVisibility(View.GONE);
        tv_step_three.setVisibility(View.GONE);

        // Step 4
        iv_step_four_bullet.setVisibility(View.VISIBLE);
        tv_step_four.setVisibility(View.GONE);

        status_view_1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        status_view_2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        status_view_3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Get Interests List
    private void getInterestList() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    interestsList.clear();
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            JSONArray interestsArray = js.getJSONArray("interest");

                            for (int i = 0; i < interestsArray.length(); i++) {
                                JSONObject object = interestsArray.getJSONObject(i);

                                InterestsModel interestsModel = new InterestsModel();
                                interestsModel.interestId = object.getString("interestId");
                                interestsModel.interest = object.getString("interest");

                                interestsList.add(interestsModel);
                            }
                            displayInterestsDialog(interestsList);    // Display Interests List

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("getInterestList", Request.Method.GET, null);
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
                    getInterestList();
                }
            });
        }
    }

    // Display Interests List
    private void displayInterestsDialog(ArrayList<InterestsModel> interestsList) {
        InterestsAdapter adapter = new InterestsAdapter(selected_interests, mContext, interestsList, new InterestIdListener() {
            @Override
            public void getInterest(String interest) {
                selected_interests = interest;
            }
        });

        interest_listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.btn_sign_up:      // SignUp button

                if (selected_interests.length() == 0) {
                    CustomToast.getInstance(mContext).showToast(mContext, getString(R.string.acc_interest_null));
                } else {
                    btn_sign_up.setEnabled(false);
                    saveInterestsApi(selected_interests);
                }
                break;
        }
    }

    // Save Selected Interest Api
    private void saveInterestsApi(final String selected_interests) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            final Map<String, String> map = new HashMap<>();
            map.put("interest", selected_interests);

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        Log.i("225255",""+js.toString());

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            String isEmailVarified = js.getString("isEmailVerified");
                            progress.dismiss();
                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            RegistrationInfo registrationInfo = session.getRegistration();

                            ArrayList<String> result = new ArrayList<>(Arrays.asList(selected_interests.split("\\s*,\\s*")));

                            for (int i = 0; i < interestsList.size(); i++) {
                                for (int j = 0; j < result.size(); j++) {
                                    if (interestsList.get(i).interestId.equals(result.get(j))) {
                                        RegistrationInfo.UserDetailBean.InterestsBean bean = new RegistrationInfo.UserDetailBean.InterestsBean();
                                        bean.interest = interestsList.get(i).interest;
                                        bean.interestId = interestsList.get(i).interestId;
                                        registrationInfo.userDetail.interests.add(bean);
                                        registrationInfo.userDetail.profile_step = "0";
                                        session.createRegistration(registrationInfo);
                                    }
                                }
                            }

                            if(isEmailVarified.equals("1")) {
                                RegistrationInfo registrationInfo_email = session.getRegistration();
                                registrationInfo_email.userDetail.isEmailVerified="1";
                                session.createRegistration(registrationInfo_email);
                                Intent intent = new Intent(mContext, EditProfileActivity.class);
                                startActivity(intent);
                                ((RegistrationActivity) mContext).finishAffinity();


                            }

                            else{
                                Intent intent = new Intent(mContext, ActivityEmailVarification.class);
                                startActivity(intent);
                                ((RegistrationActivity) mContext).finishAffinity();
                            }

                        } else {
                            progress.dismiss();
                            btn_sign_up.setEnabled(true);
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                        btn_sign_up.setEnabled(true);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                    btn_sign_up.setEnabled(true);
                }

            });
            api.callApi("saveInterest", Request.Method.POST, map);
        } else {
            View view = getView();
            if (view != null) {
                AppHelper.hideKeyboard(view, mContext);
            }

            btn_sign_up.setEnabled(true);
            cv_registration.setVisibility(View.GONE);
            ly_no_network.setVisibility(View.VISIBLE);
            btn_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveInterestsApi(selected_interests);
                }
            });

        }
    }

    // Write data to user table in firebase
    private void addUserFirebaseDatabase() {
        RegistrationInfo registrationInfo = new Session(mContext).getRegistration();
        String device_token = FirebaseInstanceId.getInstance().getToken();
        String name = registrationInfo.userDetail.full_name.toLowerCase();
        String uId = registrationInfo.userDetail.userId;

        String profilePic;
        if (registrationInfo.userDetail.images.size() > 0) {
            profilePic = registrationInfo.userDetail.images.get(0).image;
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
        userModel.isUserDeleted = 0;

        database.child(Constant.USER_TABLE).child(uId).setValue(userModel).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        RegistrationInfo registrationInfo = session.getRegistration();
                        myUId = registrationInfo.userDetail.userId;
                        //otherUId = "1";

                        //create note for chatroom
                        chatNode = gettingNotes();

                        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild(chatNode)) {
                                    sendMessage();

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
    }

    //create note for chatroom
    private String gettingNotes() {
        int myUid_ = Integer.parseInt(myUId);
        int otherUID_ = Integer.parseInt(otherUId);

        if (myUid_ < otherUID_) {
            chatNode = myUId + "_" + otherUId;
        } else {
            chatNode = otherUId + "_" + myUId;
        }

        return chatNode;
    }

    private void sendMessage() {
        String pushkey = firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).push().getKey();
        String msg = session.getRegistration().policy;

        Chat otherChat = new Chat();
        otherChat.deleteby = "1";
        otherChat.firebaseToken = "";


        otherChat.imageUrl = "";
        otherChat.message = msg;
        otherChat.image = 0;

        if (otherName != null) {
            otherChat.name = otherName.toLowerCase();
        } else {
            otherChat.name = "admin";
        }

        if (otherProfileImage != null) {
            otherChat.profilePic = otherProfileImage;
        } else {
            otherChat.profilePic = "";
        }

        otherChat.timeStamp = ServerValue.TIMESTAMP;
        otherChat.uid = otherUId;
        otherChat.lastMsg = otherUId;

        firebaseDatabase.getReference().child(Constant.CHAT_ROOMS_TABLE).child(chatNode).child(pushkey).setValue(otherChat);
        firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(myUId).child(otherUId).setValue(otherChat);
        //  firebaseDatabase.getReference().child(Constant.CHAT_HISTORY_TABLE).child(otherUId).child(myUId).setValue(otherChat);

        progress.dismiss();

    }

    // Get other user data from User table from firebase with help of UID
    private void gettingDataFromUserTable(String otherUId) {
        firebaseDatabase.getReference().child(Constant.USER_TABLE).child(otherUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherUserInfo = dataSnapshot.getValue(FirebaseUserModel.class);

                assert otherUserInfo != null;
                otherName = otherUserInfo.name;

                if (!otherUserInfo.profilePic.equals("")) {
                    otherProfileImage = otherUserInfo.profilePic;
                } else {
                    otherProfileImage = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


    // Get Interests List
    private void getAdminList() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.show();

            WebService api = new WebService(mContext, KinkLink.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    interestsList.clear();
                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String message = js.getString("message");

                        if (status.equals("success")) {
                            progress.dismiss();
                            cv_registration.setVisibility(View.VISIBLE);
                            ly_no_network.setVisibility(View.GONE);

                            String register_user_gender=session.getRegistration().userDetail.gender;
                            JSONArray adminArray = js.getJSONArray("data");

                            if(register_user_gender.equalsIgnoreCase("woman"))
                                register_user_gender="woman";
                            else register_user_gender="man";

                            for (int i = 0; i < adminArray.length(); i++) {
                                JSONObject object = adminArray.getJSONObject(i);

                                String admin_gender=object.getString("gender");
                                if(register_user_gender.equalsIgnoreCase(admin_gender)){
                                    otherUId=object.getString("id");
                                    otherName = object.getString("name");
                                    otherProfileImage=object.getString("profile_image");
                                    // Write data to user table in firebase
                                    addUserFirebaseDatabase();


                                }


                            }
                            displayInterestsDialog(interestsList);    // Display Interests List

                        } else {
                            progress.dismiss();
                            CustomToast.getInstance(mContext).showToast(mContext, message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.dismiss();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.dismiss();
                }

            });
            api.callApi("user/getAdminInfo", Request.Method.GET, null);
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
                    getInterestList();
                }
            });
        }
    }




}
