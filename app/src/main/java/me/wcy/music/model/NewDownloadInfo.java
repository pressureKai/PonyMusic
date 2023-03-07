package me.wcy.music.model;

public class NewDownloadInfo {


    /**
     * code : 200
     * msg : success
     * data : {"id":1957429426,"url":"http://m701.music.126.net/20230307140152/506c8e02a9a86af75af568bee6315e8c/jdymusic/obj/wo3DlMOGwrbDjj7DisKw/15130270112/1bf1/6761/5579/42c5b1d039a8c76a4f058a80c16f0c73.mp3","size":2243231,"md5":"42c5b1d039a8c76a4f058a80c16f0c73","encodeType":"mp3","urlSource":0}
     * time : 1678167413
     * log_id : 491603521199984640
     */

    private int code;
    private String msg;
    private DataBean data;
    private int time;
    private String log_id;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getLog_id() {
        return log_id;
    }

    public void setLog_id(String log_id) {
        this.log_id = log_id;
    }

    public static class DataBean {
        /**
         * id : 1957429426
         * url : http://m701.music.126.net/20230307140152/506c8e02a9a86af75af568bee6315e8c/jdymusic/obj/wo3DlMOGwrbDjj7DisKw/15130270112/1bf1/6761/5579/42c5b1d039a8c76a4f058a80c16f0c73.mp3
         * size : 2243231
         * md5 : 42c5b1d039a8c76a4f058a80c16f0c73
         * encodeType : mp3
         * urlSource : 0
         */

        private int id;
        private String url;
        private int size;
        private String md5;
        private String encodeType;
        private int urlSource;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public String getEncodeType() {
            return encodeType;
        }

        public void setEncodeType(String encodeType) {
            this.encodeType = encodeType;
        }

        public int getUrlSource() {
            return urlSource;
        }

        public void setUrlSource(int urlSource) {
            this.urlSource = urlSource;
        }
    }
}
