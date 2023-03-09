package me.wcy.music.http;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.WebSettings;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.wcy.music.model.ArtistInfo;
import me.wcy.music.model.DownloadInfo;
import me.wcy.music.model.Lrc;
import me.wcy.music.model.NewDownloadInfo;
import me.wcy.music.model.NewOnlineMusicList;
import me.wcy.music.model.NewSearchMusicModel;
import me.wcy.music.model.SearchMusic;
import me.wcy.music.model.Splash;
import me.wcy.music.utils.ToastUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by hzwangchenyan on 2017/2/8.
 */
public class HttpClient {
    private static final String SPLASH_URL = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
    private static final String BASE_URL = "ttps://api.uomg.com/api/";
    private static final String METHOD_GET_MUSIC_LIST = "baidu.ting.billboard.billList";
    private static final String METHOD_DOWNLOAD_MUSIC = "baidu.ting.song.play";
    private static final String METHOD_ARTIST_INFO = "baidu.ting.artist.getInfo";
    private static final String METHOD_SEARCH_MUSIC = "baidu.ting.search.catalogSug";
    private static final String METHOD_LRC = "baidu.ting.song.lry";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_SONG_ID = "songid";
    private static final String PARAM_TING_UID = "tinguid";
    private static final String PARAM_QUERY = "query";

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    public static void getSplash(@NonNull final HttpCallback<Splash> callback) {
        OkHttpUtils.get().url(SPLASH_URL).build()
                .execute(new JsonCallback<Splash>(Splash.class) {
                    @Override
                    public void onResponse(Splash response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }

                    @Override
                    public Splash parseNetworkResponse(Response response, int id) throws Exception {
                        return null;
                    }
                });
    }

    public static void downloadFile(String url, String destFileDir, String destFileName, @Nullable final HttpCallback<File> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new FileCallBack(destFileDir, destFileName) {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                    }

                    @Override
                    public void onResponse(File file, int id) {
                        if (callback != null) {
                            callback.onSuccess(file);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (callback != null) {
                            callback.onFail(e);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        if (callback != null) {
                            callback.onFinish();
                        }
                    }
                });
    }

    public static void getSongListInfo(String type, int size, int offset, @NonNull final HttpCallback<NewOnlineMusicList> callback) {
        Class<NewOnlineMusicList> clazz = NewOnlineMusicList.class;
        Gson gson = new Gson();
        //热歌榜，新歌榜，飙升榜，抖音榜，电音榜
        String requestMessage = "热歌榜";
        switch (type) {
            case "0": {
                requestMessage = "热歌榜";
                break;
            }
            case "1": {
                requestMessage = "新歌榜";
                break;
            }
            case "2": {
                requestMessage = "飙升榜";
                break;
            }
            case "3": {
                requestMessage = "抖音榜";
                break;
            }
            case "4": {
                requestMessage = "电音榜";
                break;
            }
        }
        String s = "https://api.uomg.com/api/rand.music?sort=" + requestMessage + "&format=json";
        OkHttpUtils.get().url(s)
                .build()
                .execute(new JsonCallback<NewOnlineMusicList>(NewOnlineMusicList.class) {
                    @Override
                    public void onResponse(NewOnlineMusicList response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }

                    @Override
                    public NewOnlineMusicList parseNetworkResponse(Response response, int id) throws Exception {
                        try {
                            String jsonString = response.body().string();
                            return gson.fromJson(jsonString, clazz);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }

    public static void getMusicDownloadInfo(String songId, @NonNull final HttpCallback<DownloadInfo> callback) {
        Class<NewDownloadInfo> clazz = NewDownloadInfo.class;
        //dQWeJan4eZXFEhX9
        Gson gson = new Gson();
        OkHttpUtils.get().url("https://v2.alapi.cn/api/music/url?id="+ songId +"&format=json&token=dQWeJan4eZXFEhX9")
                .build()
                .execute(new JsonCallback<DownloadInfo>(DownloadInfo.class) {
                    @Override
                    public void onResponse(DownloadInfo response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }

                    @Override
                    public DownloadInfo parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        Log.e("string",string);
                        try {
                            NewDownloadInfo newDownloadInfo = gson.fromJson(string, clazz);
                            DownloadInfo downloadInfo = new DownloadInfo();
                            DownloadInfo.Bitrate bitrate = new DownloadInfo.Bitrate();
                            bitrate.setFile_duration(0);
                            bitrate.setFile_link(newDownloadInfo.getData().getUrl());
                            downloadInfo.setBitrate(bitrate);
                            return downloadInfo;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }

    public static void getBitmap(String url, @NonNull final HttpCallback<Bitmap> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        callback.onSuccess(bitmap);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getLrc(String songId, @NonNull final HttpCallback<Lrc> callback) {
//        OkHttpUtils.get().url(BASE_URL)
//                .addParams(PARAM_METHOD, METHOD_LRC)
//                .addParams(PARAM_SONG_ID, songId)
//                .build()
//                .execute(new JsonCallback<Lrc>(Lrc.class) {
//                    @Override
//                    public void onResponse(Lrc response, int id) {
//                        callback.onSuccess(response);
//                    }
//
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        callback.onFail(e);
//                    }
//
//                    @Override
//                    public void onAfter(int id) {
//                        callback.onFinish();
//                    }
//
//                    @Override
//                    public Lrc parseNetworkResponse(Response response, int id) throws Exception {
//                        return null;
//                    }
//                });
    }

    public static void searchMusic(String keyword, @NonNull final HttpCallback<NewSearchMusicModel> callback) {
        Class<NewSearchMusicModel> clazz = NewSearchMusicModel.class;
        Gson gson = new Gson();
        //LwExDtUWhF3rH5ib
        //dQWeJan4eZXFEhX9
        OkHttpUtils.get()
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                .addHeader("appver","1.5.2")
                .url("https://v2.alapi.cn/api/music/search?keyword="+ keyword +"&token=dQWeJan4eZXFEhX9")
                .build()
                .execute(new JsonCallback<NewSearchMusicModel>(NewSearchMusicModel.class) {
                    @Override
                    public void onResponse(NewSearchMusicModel response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }

                    @Override
                    public NewSearchMusicModel parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        Log.e("response","response is "+string);
                        try {
                            return gson.fromJson(string, clazz);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }

    public static void getArtistInfo(String tingUid, @NonNull final HttpCallback<ArtistInfo> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_ARTIST_INFO)
                .addParams(PARAM_TING_UID, tingUid)
                .build()
                .execute(new JsonCallback<ArtistInfo>(ArtistInfo.class) {
                    @Override
                    public void onResponse(ArtistInfo response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }

                    @Override
                    public ArtistInfo parseNetworkResponse(Response response, int id) throws Exception {
                        return null;
                    }
                });
    }
}
