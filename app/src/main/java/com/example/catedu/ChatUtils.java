//package com.example.catedu;
//
//import java.util.UUID;
//
//import cn.jiguang.imui.commons.models.IMessage;
//import cn.jiguang.imui.commons.models.IUser;
//
//public class ChatUtils {
//    public class MyMessage implements IMessage {
//
//        private long id;
//        private String text;
//        private String timeString;
//        private MessageType type;
//        private IUser user;
//        private String contentFile;
//        private long duration;
//
//        public MyMessage(String text, MessageType type) {
//            this.text = text;
//            this.type = type;
//            this.id = UUID.randomUUID().getLeastSignificantBits();
//        }
//
//        @Override
//        public String getMsgId() {
//            return String.valueOf(id);
//        }
//
//        @Override
//        public IUser getFromUser() {
//            if (user == null) {
//                return new DefaultUser("0", "user1", null);
//            }
//            return user;
//        }
//
//        public void setUserInfo(IUser user) {
//            this.user = user;
//        }
//
//        public void setMediaFilePath(String path) {
//            this.contentFile = path;
//        }
//
//        public void setDuration(long duration) {
//            this.duration = duration;
//        }
//
//        @Override
//        public long getDuration() {
//            return duration;
//        }
//
//        public void setTimeString(String timeString) {
//            this.timeString = timeString;
//        }
//
//        @Override
//        public String getTimeString() {
//            return timeString;
//        }
//
//        @Override
//        public MessageType getType() {
//            return type;
//        }
//
//        @Override
//        public String getText() {
//            return text;
//        }
//
//        @Override
//        public String getMediaFilePath() {
//            return contentFile;
//        }
//    }
//
//
//
//    public class DefaultUser implements IUser {
//
//        private String id;
//        private String displayName;
//        private String avatar;
//
//        public DefaultUser(String id, String displayName, String avatar) {
//            this.id = id;
//            this.displayName = displayName;
//            this.avatar = avatar;
//        }
//
//        @Override
//        public String getId() {
//            return id;
//        }
//
//        @Override
//        public String getDisplayName() {
//            return displayName;
//        }
//
//        @Override
//        public String getAvatarFilePath() {
//            return avatar;
//        }
//    }
//}
