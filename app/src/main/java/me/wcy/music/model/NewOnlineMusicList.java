package me.wcy.music.model;

import java.util.ArrayList;
import java.util.List;

public class NewOnlineMusicList {


    /**
     * code : 1
     * data : {"name":"隔壁泰山","url":"http://music.163.com/song/media/outer/url?id=862101001.mp3","picurl":"http://p1.music.126.net/pbT0ag5PXJwYzFJ7YklMCA==/109951163386629013.jpg"}
     */

    private int code;
    private DataBean data;

    String albumName = "";
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * name : 隔壁泰山
         * url : http://music.163.com/song/media/outer/url?id=862101001.mp3
         * picurl : http://p1.music.126.net/pbT0ag5PXJwYzFJ7YklMCA==/109951163386629013.jpg
         */

        private String name;
        private String url;
        private String picurl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPicurl() {
            return picurl;
        }

        public void setPicurl(String picurl) {
            this.picurl = picurl;
        }
    }

    public List<OnlineMusic> getSong_list(){
        ArrayList<OnlineMusic> list = new ArrayList<>();
        OnlineMusic onlineMusic = new OnlineMusic();
        onlineMusic.setAlbum_title(data.getName());
        onlineMusic.setArtist_name(data.getName());
        onlineMusic.setSong_id("-1");
        onlineMusic.setPic_big(data.getPicurl());
        onlineMusic.setPic_small(data.getPicurl());
        onlineMusic.setLrclink("");
        onlineMusic.setTing_uid("");
        onlineMusic.setTitle(data.getName());
        onlineMusic.setDownload_url(data.url);
        list.add(onlineMusic);
        return list;
    }
}
