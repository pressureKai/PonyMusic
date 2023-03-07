package me.wcy.music.executor;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import me.wcy.music.R;
import me.wcy.music.http.HttpCallback;
import me.wcy.music.http.HttpClient;
import me.wcy.music.model.DownloadInfo;
import me.wcy.music.model.Music;
import me.wcy.music.model.OnlineMusic;
import me.wcy.music.utils.FileUtils;
import me.wcy.music.utils.MusicUtils;
import me.wcy.music.utils.ToastUtils;

/**
 * 播放在线音乐
 * Created by wcy on 2016/1/3.
 */
public abstract class PlayOnlineMusic extends PlayMusic {
    private OnlineMusic mOnlineMusic;
    public PlayOnlineMusic(Activity activity, OnlineMusic onlineMusic) {
        super(activity, 3);
        mOnlineMusic = onlineMusic;
    }

    @Override
    protected void getPlayInfo() {
        String artist = mOnlineMusic.getArtist_name();
        String title = mOnlineMusic.getTitle();

        music = new Music();
        music.setType(Music.Type.ONLINE);
        music.setTitle(title);
        music.setArtist(artist);
        music.setAlbum(mOnlineMusic.getAlbum_title());

        // 下载歌词
        String lrcFileName = FileUtils.getLrcFileName(artist, title);
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!lrcFile.exists() && !TextUtils.isEmpty(mOnlineMusic.getLrclink())) {
            downloadLrc(mOnlineMusic.getLrclink(), lrcFileName);
        } else {
            mCounter++;
        }

        // 下载封面
        String albumFileName = FileUtils.getAlbumFileName(artist, title);
        File albumFile = new File(FileUtils.getAlbumDir(), albumFileName);
        String picUrl = mOnlineMusic.getPic_big();
        if (TextUtils.isEmpty(picUrl)) {
            picUrl = mOnlineMusic.getPic_small();
        }
        if (!albumFile.exists() && !TextUtils.isEmpty(picUrl)) {
            downloadAlbum(picUrl, albumFileName);
        } else {
            mCounter++;
        }
        music.setCoverPath(albumFile.getPath());

        Log.e("DOWNLOAD PATH IS",mOnlineMusic.getDownload_url());
        music.setDuration(MusicUtils.getDurationInMilliseconds(mOnlineMusic.getDownload_url()));

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                music.setPath(mOnlineMusic.getDownload_url());
                checkCounter();
            }
        }, 3000);




    }

    private void downloadLrc(String url, String fileName) {
        HttpClient.downloadFile(url, FileUtils.getLrcDir(), fileName, new HttpCallback<File>() {
            @Override
            public void onSuccess(File file) {
            }

            @Override
            public void onFail(Exception e) {
            }

            @Override
            public void onFinish() {
                checkCounter();
            }
        });
    }

    private void downloadAlbum(String picUrl, String fileName) {
        HttpClient.downloadFile(picUrl, FileUtils.getAlbumDir(), fileName, new HttpCallback<File>() {
            @Override
            public void onSuccess(File file) {
            }

            @Override
            public void onFail(Exception e) {
            }

            @Override
            public void onFinish() {
                checkCounter();
            }
        });
    }

}
