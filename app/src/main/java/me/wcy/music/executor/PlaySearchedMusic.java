package me.wcy.music.executor;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import me.wcy.music.http.HttpCallback;
import me.wcy.music.http.HttpClient;
import me.wcy.music.model.DownloadInfo;
import me.wcy.music.model.Lrc;
import me.wcy.music.model.NewSearchMusicModel;
import me.wcy.music.model.SearchMusic;
import me.wcy.music.model.Music;
import me.wcy.music.utils.FileUtils;
import me.wcy.music.utils.MusicUtils;

/**
 * 播放搜索的音乐
 * Created by hzwangchenyan on 2016/1/13.
 */
public abstract class PlaySearchedMusic extends PlayMusic {
    private NewSearchMusicModel.DataBean.SongsBean mSong;

    public PlaySearchedMusic(Activity activity, NewSearchMusicModel.DataBean.SongsBean song) {
        super(activity, 2);
        mSong = song;
    }

    @Override
    protected void getPlayInfo() {


        String lrcFileName = FileUtils.getLrcFileName(mSong.getArtists().get(0).getName(), mSong.getName());
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!lrcFile.exists()) {
            downloadLrc(lrcFile.getPath());
        } else {
            mCounter++;
        }

        music = new Music();
        music.setType(Music.Type.ONLINE);
        String artist =mSong.getName();
        String title = mSong.getArtists().get(0).getName();
        // 下载封面
        String albumFileName = FileUtils.getAlbumFileName(artist, title);
        File albumFile = new File(FileUtils.getAlbumDir(), albumFileName);
        String picUrl = mSong.getArtists().get(0).getImg1v1Url();
        if (TextUtils.isEmpty(picUrl)) {
            picUrl = mSong.getArtists().get(0).getImg1v1Url();
        }
        if (!albumFile.exists() && !TextUtils.isEmpty(picUrl)) {
            downloadAlbum(picUrl, albumFileName);
        } else {
            mCounter++;
        }
        music.setCoverPath(albumFile.getPath());
        music.setTitle(mSong.getName());
        music.setArtist(mSong.getArtists().get(0).getName());

        // 获取歌曲播放链接
        HttpClient.getMusicDownloadInfo(mSong.getId()+"", new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo response) {
                if (response == null || response.getBitrate() == null) {
                    onFail(null);
                    return;
                }

                Log.e("getMusicDownloadInfo","ready to play");
                music.setPath(response.getBitrate().getFile_link());
                music.setDuration(MusicUtils.getDurationInMilliseconds(response.getBitrate().getFile_link()));
                checkCounter();
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    }

    @Override
    protected void checkCounter() {
        onExecuteSuccess(music);
    }

    private void downloadLrc(final String filePath) {
        HttpClient.getLrc(mSong.getId()+"", new HttpCallback<Lrc>() {
            @Override
            public void onSuccess(Lrc response) {
                if (response == null || TextUtils.isEmpty(response.getLrcContent())) {
                    return;
                }

                FileUtils.saveLrcFile(filePath, response.getLrcContent());
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
