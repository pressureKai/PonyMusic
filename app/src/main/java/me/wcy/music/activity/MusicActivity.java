package me.wcy.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.DefaultUiListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import me.wcy.music.R;
import me.wcy.music.adapter.FragmentAdapter;
import me.wcy.music.constants.Extras;
import me.wcy.music.constants.Keys;
import me.wcy.music.executor.ControlPanel;
import me.wcy.music.executor.NaviMenuExecutor;
import me.wcy.music.executor.WeatherExecutor;
import me.wcy.music.fragment.LocalMusicFragment;
import me.wcy.music.fragment.PlayFragment;
import me.wcy.music.fragment.SheetListFragment;
import me.wcy.music.service.AudioPlayer;
import me.wcy.music.service.QuitTimer;
import me.wcy.music.storage.preference.Preferences;
import me.wcy.music.utils.PermissionReq;
import me.wcy.music.utils.SystemUtils;
import me.wcy.music.utils.ToastUtils;
import me.wcy.music.utils.binding.Bind;

public class MusicActivity extends BaseActivity implements View.OnClickListener, QuitTimer.OnTimerListener,
        NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    @Bind(R.id.drawer_layout)
    private DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view)
    private NavigationView navigationView;
    @Bind(R.id.iv_menu)
    private ImageView ivMenu;
    @Bind(R.id.iv_search)
    private ImageView ivSearch;
    @Bind(R.id.tv_local_music)
    private TextView tvLocalMusic;
    @Bind(R.id.tv_online_music)
    private TextView tvOnlineMusic;
    @Bind(R.id.viewpager)
    private ViewPager mViewPager;
    @Bind(R.id.fl_play_bar)
    private FrameLayout flPlayBar;

    private LinearLayout loginLayout;
    private ImageView mUserHeader;
    private TextView mUserName;

    private View vNavigationHeader;
    private LocalMusicFragment mLocalMusicFragment;
    private SheetListFragment mSheetListFragment;
    private PlayFragment mPlayFragment;
    private ControlPanel controlPanel;
    private NaviMenuExecutor naviMenuExecutor;
    private MenuItem timerItem;
    private boolean isPlayFragmentShow;


    //下面的msg.what的值，来自updateUserInfo()的DefaultUiListener回调接口
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                JSONObject response = (JSONObject) msg.obj;
                if (response.has("nickname")) { //提取QQ昵称参数
                    //成功就执行的操作
                    //mUserInfo.setVisibility(android.view.View.VISIBLE);设置用户名可见
                    //mUserInfo.setText(response.getString("nickname"));//设置QQ昵称刀TextView上
                }
            } else if(msg.what == 1){
                /** 需要更新的操作 */
                //Bitmap bitmap = (Bitmap)msg.obj; 更新ImageViwe为QQ头像
                //mUserLogo.setImageBitmap(bitmap);
                //mUserLogo.setVisibility(android.view.View.VISIBLE);
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
    }

    @Override
    protected void onServiceBound() {
        setupView();
        updateWeather();
        controlPanel = new ControlPanel(flPlayBar);
        naviMenuExecutor = new NaviMenuExecutor(this);
        AudioPlayer.get().addOnPlayEventListener(controlPanel);
        QuitTimer.get().setOnTimerListener(this);
        parseIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void setupView() {
        // add navigation header
        vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);
        mUserHeader = navigationView.findViewById(R.id.user_icon);
        mUserName = navigationView.findViewById(R.id.tv_user);
        loginLayout = navigationView.findViewById(R.id.login_layout);
        // setup view pager
        mLocalMusicFragment = new LocalMusicFragment();
        mSheetListFragment = new SheetListFragment();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(mLocalMusicFragment);
        adapter.addFragment(mSheetListFragment);
        mViewPager.setAdapter(adapter);
        tvLocalMusic.setSelected(true);

        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        tvLocalMusic.setOnClickListener(this);
        tvOnlineMusic.setOnClickListener(this);
        flPlayBar.setOnClickListener(this);
        loginLayout.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(this);
        navigationView.setNavigationItemSelectedListener(this);

        String name = Preferences.getPreferences().getString(Preferences.USER_NAME,"");
        String pictureUrl =Preferences.getPreferences().getString(Preferences.USER_PIC, "");


        if(!name.isEmpty()){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(MusicActivity.this).load(R.drawable.ic_launcher).into(mUserHeader);
                    mUserName.setText("音乐播放器 \n 退出登录");
                }
            },100);
        }

    }

    private void updateWeather() {
        PermissionReq.with(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .result(new PermissionReq.Result() {
                    @Override
                    public void onGranted() {
                        new WeatherExecutor(MusicActivity.this, vNavigationHeader).execute();
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.show(R.string.no_permission_location);
                    }
                })
                .request();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    @Override
    public void onTimer(long remain) {
        if (timerItem == null) {
            timerItem = navigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchMusicActivity.class));
                break;
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_online_music:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
            case R.id.login_layout:
                qqLogin();
                break;

        }
    }
    public static Tencent mTencent;
    public static String mAppid= "102043804";
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            /** 此处回调QQ用户数据处理 */
            initOpenidAndToken(values); //初始化Token
            //updateUserInfo(); 更新用户信息，头像，用户名
            //updateLoginButton(); 登录按钮更新为已登录
        }
    };

    public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch (Exception e) {
        }
    }

    private void qqLogin(){
        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppid, getApplication());
            mTencent.setIsPermissionGranted( true);
        }

        String name = Preferences.getPreferences().getString(Preferences.USER_NAME,"");
        String pictureUrl =Preferences.getPreferences().getString(Preferences.USER_PIC, "");

        if(name.isEmpty()){
            Log.e("qqLogin","qqLogin");
            mTencent.login(this, "all", loginListener);
            updateUserInfo();
        } else{
            Preferences.getPreferences().edit().putString(Preferences.USER_NAME,"").apply();
            Preferences.getPreferences().edit().putString(Preferences.USER_PIC, "").apply();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(MusicActivity.this).load(R.drawable.icon_qq).into(mUserHeader);
                    mUserName.setText("未登录");
                }
            },100);
            ToastUtils.show("退出登录成功");
        }

    }

    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new DefaultUiListener() {
                @Override
                public void onError(UiError e) {
                }
                @Override
                public void onComplete(final Object response) {
                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                    new Thread(){
                        @Override
                        public void run() {
                            JSONObject json = (JSONObject)response;
                            if(json.has("figureurl")){
                                Bitmap bitmap = null;
//                                try {
//                                    bitmap = Util.getbitmap(json.getString("figureurl_qq_2"));
//                                } catch (JSONException e) {
//                                   // SLog.e(TAG, "Util.getBitmap() jsonException : " + e.getMessage());
//                                }
                                Message msg = new Message();
                                msg.obj = bitmap;
                                msg.what = 1;
                                mHandler.sendMessage(msg);
                            }
                        }

                    }.start();
                }

                @Override
                public void onCancel() {

                }
            };
            UserInfo info = new UserInfo(this, mTencent.getQQToken());
            info.getUserInfo(listener);

        } else {
            /** 未登录状态 */
            //mUserInfo.setText("");
            //mUserInfo.setVisibility(android.view.View.GONE);
            //mUserLogo.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();
        handler.postDelayed(() -> item.setChecked(false), 500);
        return naviMenuExecutor.onNavigationItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            tvLocalMusic.setSelected(true);
            tvOnlineMusic.setSelected(false);
        } else {
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Keys.VIEW_PAGER_INDEX, mViewPager.getCurrentItem());
        mLocalMusicFragment.onSaveInstanceState(outState);
        mSheetListFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(() -> {
            mViewPager.setCurrentItem(savedInstanceState.getInt(Keys.VIEW_PAGER_INDEX), false);
            mLocalMusicFragment.onRestoreInstanceState(savedInstanceState);
            mSheetListFragment.onRestoreInstanceState(savedInstanceState);
        });
    }

    @Override
    protected void onDestroy() {
        AudioPlayer.get().removeOnPlayEventListener(controlPanel);
        QuitTimer.get().setOnTimerListener(null);
        super.onDestroy();
    }

    private class BaseUiListener extends DefaultUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {

                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (jsonResponse.length() == 0) {

                return;
            }


            doComplete((JSONObject) response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            Log.e("login","error is " + e);

        }

        @Override
        public void onCancel() {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG", "-->onActivityResult " + requestCode  + " resultCode=" + resultCode);
//        if (requestCode == Constants.REQUEST_LOGIN ||
//                requestCode == Constants.REQUEST_APPBAR) {
//            Tencent.onActivityResultData(requestCode,resultCode,data,loginListener);
//        } else {
//            Preferences.getPreferences().edit().putString(Preferences.USER_NAME,"666").apply();
//            Preferences.getPreferences().edit().putString(Preferences.USER_PIC, "音乐播放器").apply();
//
//
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Glide.with(MusicActivity.this).load(R.drawable.ic_launcher).into(mUserHeader);
//                    mUserName.setText("音乐播放器");
//                }
//            },2000);
//        }
        Preferences.getPreferences().edit().putString(Preferences.USER_NAME,"666").apply();
        Preferences.getPreferences().edit().putString(Preferences.USER_PIC, "音乐播放器").apply();


        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(MusicActivity.this).load(R.drawable.ic_launcher).into(mUserHeader);
                mUserName.setText("音乐播放器");
            }
        },2000);

        super.onActivityResult(requestCode, resultCode, data);
    }
}
