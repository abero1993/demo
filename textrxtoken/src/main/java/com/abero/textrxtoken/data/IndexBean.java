package com.abero.testrxtoken.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by HuangJie on 2017/7/4.
 */

public class IndexBean implements Serializable {
    /**
     * mediaList : [{"image":"http://221.4.223.101:8000/thumb/7/thumb01.jpg","name":"Generation","description":"","mediaId":7},{"image":"http://221.4.223.101:8000/thumb/9/thumb01.jpg","name":"spark","description":"","mediaId":9},{"image":"http://221.4.223.101:8000/thumb/11/thumb02.jpg","name":"《小苹果》","description":"MV","mediaId":11},{"image":"http://221.4.223.101:8000/thumb/16/thumb04.jpg","name":"有点甜","description":"music MV","mediaId":16}]
     * name : Movies
     * logo : http://221.4.223.101:8000/categoryIcon/Movies.png
     * id : 4
     */

    private List<CategoryListBean> categoryList;
    /**
     * image : http://221.4.223.101:8000/adv/152.jpg
     * name : Thor: Ragnarok
     * mediaId : 152
     */

    private List<ADListBean> ADList;

    public List<CategoryListBean> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryListBean> categoryList) {
        this.categoryList = categoryList;
    }

    public List<ADListBean> getADList() {
        return ADList;
    }

    public void setADList(List<ADListBean> ADList) {
        this.ADList = ADList;
    }

    public static class CategoryListBean implements Serializable {
        private String name;
        private String logo;
        private int id;
        /**
         * image : http://221.4.223.101:8000/thumb/7/thumb01.jpg
         * name : Generation
         * description :
         * mediaId : 7
         */

        private List<MediaListBean> mediaList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<MediaListBean> getMediaList() {
            return mediaList;
        }

        public void setMediaList(List<MediaListBean> mediaList) {
            this.mediaList = mediaList;
        }

        public static class MediaListBean implements Serializable {
            private String image;
            private String name;
            private String description;
            private int mediaId;

            public String getImage() {
                return image;
            }

            public void setImage(String image) {
                this.image = image;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public int getMediaId() {
                return mediaId;
            }

            public void setMediaId(int mediaId) {
                this.mediaId = mediaId;
            }
        }
    }

    public static class ADListBean implements Serializable {
        private String image;
        private String name;
        private int mediaId;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMediaId() {
            return mediaId;
        }

        public void setMediaId(int mediaId) {
            this.mediaId = mediaId;
        }
    }
}
