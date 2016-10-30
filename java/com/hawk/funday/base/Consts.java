package com.hawk.funday.base;

/**
 * Created by wangdan on 16/8/18.
 */
final public class Consts {

    private Consts() {

    }

    public static final long REQUEST_DATA_DELAY = 500;

    public static final String FB_APP_NAME = "facebook";
    public static final String FB_URL = "https://www.facebook.com/FundayAPP";
    public static final String FB_APP_INTENT = "fb://page/171209516617967";
    public static final String INSTA_APP_NAME = "instagram";
    public static final String INSTA_APP_INTENT = "https://www.instagram.com/_u/fundayapp";
    public static final String INSTA_URL = "https://www.instagram.com/fundayapp";

    public static class Cache {

        public static final long time = 20 * 60 * 1000l;

    }

    public static class MediaType {

        public static final int image = 1;// 单张图片

        public static final int gif = 2;// GIF图片

        public static final int video = 3;// 视频

        public static final int ariticle = 4;// 文章

    }

    public static class Page {

        public static final String Page_main_featured = "page_main_featured";

        public static final String Page_main_new = "page_main_new";

        public static final String Page_main_video = "page_main_video";

        public static final String Page_comments = "page_comments";

        public static final String Page_profile_posted = "page_profile_posted";

        public static final String Page_profile_comments = "page_profile_comments";

        public static final String Page_profile_favorites = "page_profile_favoriates";

        public static final String Page_profile_modify = "page_profile_modify";

        public static final String Page_login_profile_modify = "page_login_profile_modify";

        public static final String Page_post_create = "page_post_create";

        public static final String Page_video_player_h5 = "page_video_player_h5";

        public static final String Page_video_player_native = "page_video_player_native";

    }

    /**
     * 日志记录，请同步修改每一个事件上报的额外参数
     *
     */
    public static class Event {

        public static final String Event_main_launch = "event_main_launch";// 首页启动次数

        public static final String Event_post_featured_loadmore = "event_post_featured_loadmore";// post列表featured加载更多次数

        public static final String Event_post_featured_refresh = "event_post_featured_refresh";// post列表featured下拉刷新次数

        public static final String Event_post_new_loadmore = "event_post_new_loadmore";// post列表new加载更多次数

        public static final String Event_post_new_refresh = "event_post_new_refresh";// post列表new下拉刷新次数

        public static final String Event_post_video_loadmore = "event_post_video_loadmore";// post列表video加载更多次数

        public static final String Event_post_video_refresh = "event_post_video_refresh";// post列表video下拉刷新次数

        public static final String Event_post_gif_play = "event_post_gif_play";// post的gif图播放次数

        public static final String Event_post_video_h5_play = "event_post_video_h5_play";// post的网页视频播放次数

        public static final String Event_post_video_native_play = "event_post_video_native_play";// post的原生视频播放次数

        public static final String Event_post_fav_create = "event_post_fav_create";// post添加收藏次数

        public static final String Event_post_fav_destory = "event_post_fav_destory";// post取消收藏次数

        public static final String Event_post_cmts_click = "event_post_cmts_click";// post列表项评论按钮点击次数

        public static final String Event_post_share_click = "event_post_share_click";// post列表项分享按钮点击次数

        public static final String Event_post_report = "event_post_report";// post举报次数

        public static final String Event_upload_pic = "event_upload_pic_success";// 发布静态图片成功次数

        public static final String Event_upload_gif = "event_upload_gif_success";// 发布GIF图片成功次数

        public static final String Event_upload_btn_click = "event_upload_btn_click";// 发布按钮点击次数

        public static final String Event_upload_draft_delete = "event_upload_draft_delete";// 发布草稿删除次数

        public static final String Event_upload_draft_retry = "event_upload_draft_retry";// 发布草稿重试次数

        public static final String Event_post_destory = "event_post_destory";// post删除次数

        public static final String Event_comment_create_success = "event_comment_create_success";// 评论成功次数

        public static final String Event_comment_create_faild = "event_comment_create_faild";// 评论失败次数

        public static final String Event_comment_report = "event_comment_report";// comment举报次数

        public static final String Event_comment_destory = "event_comment_destory";// comment删除次数

        public static final String Event_comment_profile_click = "event_comment_profile_click";// comment列表的用户头像点击次数

        public static final String Event_profile_main_click = "event_profile_main_click";// 用户信息首页按钮点击次数

        public static final String Event_profile_login_success = "event_profile_login_success";// 用户登陆成功次数

        public static final String Event_profile_login_faild = "event_profile_login_faild";// 用户登陆失败次数

        public static final String Event_profile_logout = "event_profile_logout";// 用户登出次数

        public static final String Event_profile_modify_click = "event_profile_modify_click";// 用户编辑信息按钮点击次数

        public static final String Event_profile_modify_avatar = "event_profile_modify_avatar";// 用户编辑头像次数

        public static final String Event_login_profile_modify_avatar = "event_login_profile_modify_avatar";// 用户进入到登陆后提示编辑信息页面，编辑头像次数

        public static final String Event_profile_modify_name = "event_profile_modify_name";// 用户编辑昵称次数

        public static final String Event_login_profile_modify_name = "event_login_profile_modify_name";// 用户进入到登陆后提示编辑信息页面，编辑昵称次数

        public static final String Event_login_profile_modify_done = "event_login_profile_modify_done";// 用户进入到登陆后提示编辑信息页面，点击完成按钮的次数

        public static final String Event_settings_facebook_click = "event_settings_follow_facebook_click";// 设置项facebook关注点击次数

        public static final String Event_settings_instagram_click = "event_settings_follow_instagram_click";// 设置项instagram关注点击次数

        public static final String Event_settings_feedback_click = "event_settings_follow_feedback_click";// 设置项feedback关注点击次数

        public static final String Event_settings_about_click = "event_settings_follow_about_click";// 设置项about关注点击次数

    }

}
