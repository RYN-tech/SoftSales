package com.soft_sales.model;

import java.io.Serializable;
import java.util.List;

public class UserModel extends ResponseModel {
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data implements Serializable {
        private User user;
        private String access_token;
        private String token_type;
        private String expires_in;
        private SettingModel setting;
        private List<String> permissions;

        public User getUser() {
            return user;
        }

        public SettingModel getSetting() {
            return setting;
        }

        public String getAccess_token() {
            return access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public String getExpires_in() {
            return expires_in;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setSetting(SettingModel setting) {
            this.setting = setting;
        }

        public static class User implements Serializable {
            private String id;
            private String name;
            private String user_name;
            private String email_verified_at;
            private String photo;
            private String lang;
            private String is_login;


            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public String getUser_name() {
                return user_name;
            }

            public String getEmail_verified_at() {
                return email_verified_at;
            }

            public String getPhoto() {
                return photo;
            }

            public String getLang() {
                return lang;
            }

            public String getIs_login() {
                return is_login;
            }
        }

    }

}
