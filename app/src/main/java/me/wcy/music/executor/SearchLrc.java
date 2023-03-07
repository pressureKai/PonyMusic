package me.wcy.music.executor;

import android.text.TextUtils;

import me.wcy.music.http.HttpCallback;
import me.wcy.music.http.HttpClient;
import me.wcy.music.model.Lrc;
import me.wcy.music.model.NewSearchMusicModel;
import me.wcy.music.utils.FileUtils;

/**
 * 如果本地歌曲没有歌词则从网络搜索歌词
 * Created by wcy on 2016/4/26.
 */
public abstract class SearchLrc implements IExecutor<String> {
    private String artist;
    private String title;

    public SearchLrc(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    @Override
    public void execute() {
        onPrepare();
        searchLrc();
    }

    private void searchLrc() {
        HttpClient.searchMusic(title + "-" + artist, new HttpCallback<NewSearchMusicModel>() {
            @Override
            public void onSuccess(NewSearchMusicModel response) {
                try {
                    if (response == null) {
                        onFail(null);
                        return;
                    }

                    downloadLrc(response.getData().getSongs().get(0).getId()+"");
                }catch (Exception e){

                }
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    }

    private void downloadLrc(String songId) {
        HttpClient.getLrc(songId, new HttpCallback<Lrc>() {
            @Override
            public void onSuccess(Lrc response) {
                if (response == null || TextUtils.isEmpty(response.getLrcContent())) {
                    onFail(null);
                    return;
                }

                String filePath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(artist, title);
                FileUtils.saveLrcFile(filePath, response.getLrcContent());
                onExecuteSuccess(filePath);
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    }
}
