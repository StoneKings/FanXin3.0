package com.fanxin.app.main.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.alibaba.fastjson.JSONObject;
import com.fanxin.app.DemoApplication;
import com.fanxin.app.R;
import com.fanxin.app.main.utils.OkHttpManager;
import com.fanxin.app.main.utils.Param;
import com.fanxin.app.main.widget.FXAlertDialog;
import com.fanxin.app.ui.BaseActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {
    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_fxid;
    private TextView tv_sex;
    private TextView tv_sign;

    private String imageName;
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final int UPDATE_FXID = 4;// 结果
    private static final int UPDATE_NICK = 5;// 结果
    private JSONObject jsonObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fx_activity_myinfo);
        jsonObject = DemoApplication.getInstance().getUserJson();
        initView();
    }

    private void initView() {
        this.findViewById(R.id.re_avatar).setOnClickListener(this);
        this.findViewById(R.id.re_name).setOnClickListener(this);
        this.findViewById(R.id.re_fxid).setOnClickListener(this);
        this.findViewById(R.id.re_sex).setOnClickListener(this);
        this.findViewById(R.id.re_region).setOnClickListener(this);
        // 头像
        iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);
        tv_name = (TextView) this.findViewById(R.id.tv_name);
        tv_fxid = (TextView) this.findViewById(R.id.tv_fxid);
        tv_sex = (TextView) this.findViewById(R.id.tv_sex);
        tv_sign = (TextView) this.findViewById(R.id.tv_sign);
        String nick = jsonObject.getString(FXConstant.JSON_KEY_NICK);
        String fxid = jsonObject.getString(FXConstant.JSON_KEY_FXID);
        String sex = jsonObject.getString(FXConstant.JSON_KEY_SEX);
        String sign = jsonObject.getString(FXConstant.JSON_KEY_SIGN);
        tv_name.setText(nick);

        if ("0".equals(fxid)) {
            tv_fxid.setText("未设置");

        } else {
            tv_fxid.setText(fxid);
        }
        if (("1").equals(sex)) {
            tv_sex.setText("男");

        } else if (("2").equals(sex)) {
            tv_sex.setText("女");

        } else {
            tv_sex.setText("");
        }
        if ("0".equals(sign)) {
            tv_sign.setText("未填写");
        } else {
            tv_sign.setText(sign);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_avatar:

                showPhotoDialog();

                break;
            case R.id.re_name:
                startActivityForResult(new Intent(ProfileActivity.this,
                        ProfileUpdateActivity.class), UPDATE_NICK);
                break;
            case R.id.re_fxid:
                if (jsonObject.getString(FXConstant.JSON_KEY_FXID)
                        .equals("0")) {
                    startActivityForResult(new Intent(ProfileActivity.this,
                            ProfileUpdateActivity.class), UPDATE_FXID);

                }
                break;
            case R.id.re_sex:
                showSexDialog();
                break;
            case R.id.re_region:

                break;

        }

    }


    private void showPhotoDialog() {

        List<String> items = new ArrayList<String>();
        items.add("拍照");
        items.add("相册");
        FXAlertDialog fxAlertDialog = new FXAlertDialog(ProfileActivity.this, null, items);
        fxAlertDialog.init(new FXAlertDialog.OnItemClickListner() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        imageName = getNowTime() + ".png";
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 指定调用相机拍照后照片的储存路径
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(new File(FXConstant.DIR_AVATAR, imageName)));
                        startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                        break;
                    case 1:
                        imageName = getNowTime() + ".png";
                        Intent intent2 = new Intent(Intent.ACTION_PICK, null);
                        intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent2, PHOTO_REQUEST_GALLERY);
                        break;
                }
            }
        });
    }

    private void showSexDialog() {
        String title = "性别";
        List<String> items = new ArrayList<String>();
        items.add("男");
        items.add("女");
        FXAlertDialog fxAlertDialog = new FXAlertDialog(ProfileActivity.this, title, items);
        fxAlertDialog.init(new FXAlertDialog.OnItemClickListner() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        if (!jsonObject.getString(FXConstant.JSON_KEY_SEX).equals("0")) {

                            updateInServer(FXConstant.JSON_KEY_SEX, "1");
                        }
                        break;
                    case 1:
                        if (!jsonObject.getString(FXConstant.JSON_KEY_SEX).equals("1")) {

                            updateInServer(FXConstant.JSON_KEY_SEX, "2");
                        }
                        break;
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_TAKEPHOTO:

                    startPhotoZoom(
                            Uri.fromFile(new File(FXConstant.DIR_AVATAR, imageName)),
                            480);
                    break;

                case PHOTO_REQUEST_GALLERY:
                    if (data != null)
                        startPhotoZoom(data.getData(), 480);
                    break;

                case PHOTO_REQUEST_CUT:
                    updateInServer(FXConstant.JSON_KEY_AVATAR, imageName);
                    break;

            }
            super.onActivityResult(requestCode, resultCode, data);

        }
    }

    private void startPhotoZoom(Uri uri1, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri1, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", false);

        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(FXConstant.DIR_AVATAR, imageName)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private String getNowTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS");
        return dateFormat.format(date);
    }


    private void updateInServer(final String key, final String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("key", key));
        params.add(new Param("value", value));
        params.add(new Param("hxid", jsonObject.getString(FXConstant.JSON_KEY_HXID)));
        List<File> files = new ArrayList<File>();
        if (key == FXConstant.JSON_KEY_AVATAR) {
            File file = new File(FXConstant.DIR_AVATAR, value);
            if (file.exists()) {
                files.add(file);
            }
        }
        OkHttpManager.getInstance().post(params, files, FXConstant.URL_UPDATE, new OkHttpManager.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int code = jsonObject.getIntValue("code");
                if (code == 1000) {
                    //update ui

                    Bitmap bitmap = BitmapFactory.decodeFile(FXConstant.DIR_AVATAR
                            + value);
                    iv_avatar.setImageBitmap(bitmap);
                    //
                    jsonObject.put(key, value);
                    DemoApplication.getInstance().setUserJson(jsonObject);
                } else {

                    Toast.makeText(getApplicationContext(), "更新失败,code:" + code, Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });


    }


}
