package com.hawk.funday.support.sdk;

import android.text.TextUtils;

import com.hawk.funday.support.cache.FeaturedCacheUtility;
import com.hawk.funday.support.cache.NewCacheUtility;
import com.hawk.funday.support.cache.VideoCacheUtility;
import com.hawk.funday.support.sdk.bean.APILog;
import com.hawk.funday.support.sdk.bean.BaseBean;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.CommentsBean;
import com.hawk.funday.support.sdk.bean.APPConfsBean;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.sdk.bean.PostRequestBean;
import com.hawk.funday.support.sdk.bean.PostsBean;
import com.hawk.funday.support.sdk.bean.UUIDBean;
import com.hawk.funday.support.sdk.bean.UploadImageResultsBean;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.http.DefHttpUtility;
import org.aisen.android.network.http.OnFileProgress;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有服务端API
 *
 * Created by wangdan on 16/8/17.
 */
public class FundaySDK extends BaseSDK {

    private FundaySDK(CacheMode mode) {
        super(mode);
    }

    public static FundaySDK newInstance() {
        return newInstance(CacheMode.disable);
    }

    public static FundaySDK newInstance(CacheMode mode) {
        return new FundaySDK(mode);
    }

    /**
     * Featured列表
     *
     * @param refreshId
     * @return
     * @throws TaskException
     */
    public PostsBean getFeatured(String refreshId, String direction) throws TaskException {
        Setting action = newSetting("getFeatured", "/api/index/featured", "Feature列表");
        action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, FeaturedCacheUtility.class.getName(), ""));

        Params params = new Params();
        params.addParameter("direction", direction);
        if (!TextUtils.isEmpty(refreshId)) {
            params.addParameter("refreshId", refreshId);
        }

        return doGet(action, basicParams(params), PostsBean.class);
    }

    /**
     * New列表
     *
     * @param topId
     * @param bottomId
     * @return
     * @throws TaskException
     */
    public PostsBean getNew(long topId, long bottomId) throws TaskException {
        Setting action = newSetting("getNew", "/api/index/new", "New列表");
        action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, NewCacheUtility.class.getName(), ""));

        Params params = new Params();
        if (topId > 0) {
            params.addParameter("topId", topId + "");
        }
        else if (bottomId > 0) {
            params.addParameter("bottomId", bottomId + "");
        }

        return doGet(action, basicParams(params), PostsBean.class);
    }

    /**
     * 获取Video列表
     *
     * @param topId
     * @param bottomId
     * @return
     * @throws TaskException
     */
    public PostsBean getVideo(long topId, long bottomId) throws TaskException {
        Setting action = newSetting("getVideo", "/api/index/video", "Video列表");
        action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, VideoCacheUtility.class.getName(), ""));

        Params params = new Params();
        if (topId > 0) {
            params.addParameter("topId", topId + "");
        }
        else if (bottomId > 0) {
            params.addParameter("bottomId", bottomId + "");
        }

        return doGet(action, basicParams(params), PostsBean.class);
    }

    /**
     * 根据Token获取用户信息
     *
     * @param openId
     * @param token
     * @return
     * @throws TaskException
     */
    public FundayUserBean getUserByToken(String openId, String token) throws TaskException {
        Setting action = newSetting("getUserByToken", "/api/user/info", "根据Token获取用户信息");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, UserHttpUtility.class.getName(), ""));

        Params params = new Params();
        params.addParameter("openId", openId);
        params.addParameter("token", token);

        return doGet(action, basicParams(params), FundayUserBean.class);
    }

    /**
     * 上传字节流
     *
     * @param filebytes
     * @return
     * @throws TaskException
     */
    public UploadImageResultsBean uploadImage(byte[] filebytes, OnFileProgress onFileProgress) throws TaskException {
        Setting action = newSetting("uploadImage", "/api/upload", "上传文件");
        action.getExtras().put(HTTP_UTILITY, newSettingExtra(BASE_URL, DefHttpUtility.class.getName(), ""));
        /*action.setExtras(new HashMap<String, SettingExtra>());
        action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://10.115.10.100:80", ""));*/
        MultipartFile[] files = new MultipartFile[1];
        MultipartFile mulFile = new MultipartFile("image/jpge", "mfs", filebytes);
        if (onFileProgress != null) {
            mulFile.setOnProgress(onFileProgress);
        }
        files[0] = mulFile;

        return doPostFiles(configHttpConfig(), action, basicParams(null), null, files, UploadImageResultsBean.class);
    }

    /**
     * 上传文件
     *
     * @param file
     * @return
     * @throws TaskException
     */
    public UploadImageResultsBean uploadImage(File file, OnFileProgress onFileProgress) throws TaskException {
        Setting action = newSetting("uploadImage", "/api/upload", "上传文件");
        action.getExtras().put(HTTP_UTILITY, newSettingExtra(BASE_URL, DefHttpUtility.class.getName(), ""));
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, UploadImageHttpUtility.class.getName(), ""));

        MultipartFile[] files = new MultipartFile[1];
        MultipartFile mulFile = new MultipartFile("image/jpge", "mf", file);
        if (onFileProgress != null) {
            mulFile.setOnProgress(onFileProgress);
        }
        files[0] = mulFile;

        return doPostFiles(configHttpConfig(), action, basicParams(null), null, files, UploadImageResultsBean.class);
    }

    /**
      * @Description:  用户发表（布）内容

      * @return
      */

    public String doPublish(PostRequestBean bean) throws  TaskException{

        Setting action = newSetting("publish", "/api/ugc/post", "用户发表（布）内容");
       /* action.setExtras(new HashMap<String, SettingExtra>());
        action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://10.115.10.100:80", ""));*/

       /* Params params=new Params();
        params.addParameter("content",content);
        params.addParameter("title",title);
        params.addParameter("urls",urls);*/

        return doPost(configHttpConfig(),action,basicParams(null),null,bean,String.class);
    }
    /**
     * 获取评论列表
     *
     * @param resourceId 请求指定资源id的评论数据
     * @param resourceType 请求指定资源类型resourceType的评论数据
     * @param offset 评论分页的offset值
     * @return
     * @throws TaskException
     */
    public CommentsBean getComments(long resourceId, int resourceType, int offset) throws TaskException {
        Setting action = newSetting("getComments", "/api/comment/list", "获取评论列表");
        //action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, CommentsHttpUtility.class.getName(), ""));
        Params params = new Params();
        params.addParameter("resourceId", String.valueOf(resourceId));
        params.addParameter("resourceType", String.valueOf(resourceType));
        if (offset > 0)
            params.addParameter("offset", String.valueOf(offset));
        else
            params.addParameter("offset", "0");

        return doGet(action, basicParams(params), CommentsBean.class);
    }
    /**
      * @Description: 发表评论
      * @param  resourceId 评论的资源ID
      * @param  resourceType 评论类型
      * @param  content 评论的内容
      * @param  url  资源预览图
      * @param  parentId 父级评论
      * @return 评论提交结果
      */

    public CommentBean doSendComment(long resourceId,int resourceType,String content,String url,long parentId)throws  TaskException{
        Setting action = newSetting("sendComment", "/api/ugc/comment", "发表评论");
        Params params=new Params();
        params.addParameter("resourceId",String.valueOf(resourceId));
        params.addParameter("resourceType",String.valueOf(resourceType));
        params.addParameter("content",content);
        //params.addParameter("previewUrl",url);
        if (parentId>=0) {
            params.addParameter("parentId",String.valueOf(parentId));
        }
        return doPost(configHttpConfig(),action,basicParams(null),params,null,CommentBean.class);
    }

    /**
      * @Description:  用户删除自己本人发布的评论
      * @param commentID
      * @return  String
      */
    
    public BaseBean doCancelComment(long commentID) throws TaskException
    {
        Setting action=newSetting("cancelComment","/api/comment/del","删除用户本人发布的评论");
        Params params=new Params();
        params.addParameter("commentId",String.valueOf(commentID));
        return  doPost(configHttpConfig(),action,basicParams(null),params,null,BaseBean.class);
        //return  doGet(configHttpConfig(),action,params,String.class);
    }
    /**
      * @Description:
      * @param resourceId 资源ID
      * @param resourceType 资源类型
      * @param   type 举报类型
      * @return
      */
    public BaseBean doReportResource(long resourceId,int resourceType, String type) throws TaskException{
        Setting action = newSetting("reportComment", "/api/ugc/report/resource", "举报发布内容");

        Params params=new Params();
        params.addParameter("resourceId",String.valueOf(resourceId));
       // params.addParameter("resourceType",String.valueOf(resourceType));
        params.addParameter("type",type);

        return doGet(action,basicParams(params), BaseBean.class);
    }

    public BaseBean doCancelPost(long resourceId,int resourceType,String token,String  openId)throws TaskException{
        Setting action = newSetting("reportResource", "/api/profile/cancelpost", "用户删除已发布内容");

        Params params = new Params();
        params.addParameter("resourceId",String.valueOf(resourceId));
        params.addParameter("resourceType",String.valueOf(resourceType));
        params.addParameter("token",token);
        params.addParameter("openId",openId);

        return doGet(action, basicParams(params), BaseBean.class);
    }

    public BaseBean doReportComment(long commentId,String type)throws TaskException{
        Setting action=newSetting("ReportComment","/api/ugc/report/comment","用户举报评论");
        Params params=new Params();
        params.addParameter("commentId",String.valueOf(commentId));
        params.addParameter("type",type);
        return  doPost(configHttpConfig(),action,basicParams(null),params,null,BaseBean.class);
    }
    /**
      * @Description:  设置用户收藏
      * @params
      * @return
      */

    public  String doAddFavorite(long resourceId,int resourceType,String token,String  openId)throws TaskException{
        Setting action = newSetting("reportComment", "/api/user/favorite/add", "用户删除已发布内容");
        Params params=new Params();
        params.addParameter("resourceId",String.valueOf(resourceId));
        params.addParameter("resourceType",String.valueOf(resourceType));
        params.addParameter("token",token);
        params.addParameter("openId",openId);
        return doGet(action,basicParams(params),String.class);
    }
    /**
     * @Description:  取消用户收藏
     * @params
     * @return
     */

    public  String doCancelFavorite(long resourceId,int resourceType,String token,String  openId)throws TaskException{
        Setting action = newSetting("reportComment", "/api/user/favorite/cancel", "用户删除已发布内容");
        Params params=new Params();
        params.addParameter("resourceId",String.valueOf(resourceId));
        params.addParameter("resourceType",String.valueOf(resourceType));
        params.addParameter("token",token);
        params.addParameter("openId",openId);
        return doGet(action,basicParams(params),String.class);
    }

    /**
     * 修改用户头像
     */
    public BaseBean uploadAvatar(long userId, String token, String avatar) throws TaskException {
        Setting action = newSetting("uploadAvatar", "/api/user/modify/avatar", "根据open_id和Token修改用户头像");

//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, UploadAvatarHttpUtility.class.getName(), ""));

        Params params = new Params();
        params.addParameter("userId", "" + userId);
        params.addParameter("token", token);
        params.addParameter("avatar", avatar);

        return doPost(configHttpConfig(), action, basicParams(null), params, null, BaseBean.class);
    }


    /**
     * 获取用户发布的内容列表
     *
     * @param uid
     * @param offset
     * @return
     * @throws TaskException
     */
    public PostsBean getProfilePosted(long uid, int offset) throws TaskException {
        Setting action = newSetting("getProfilePosted", "/api/profile/posts", "获取用户发布的内容列表");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, ProfileCommentsHttpUtility.class.getName(), ""));

//        if (FundayUtils.isLoginedUser(uid)) { // 如果是已登录用户，那么打开缓存
//            action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, ProfilePostedCacheUtility.class.getName(), ""));
//        }

        Params params = new Params();
        params.addParameter("userId", String.valueOf(uid));
        if (offset > 0) {
            params.addParameter("offset", String.valueOf(offset));
        } else {
            params.addParameter("offset", String.valueOf(0));
        }

        return doGet(action, basicParams(params), PostsBean.class);
    }


    /**
     * 获取用户发布的评论列表
     *
     * @param uid
     * @param offset
     * @return
     * @throws TaskException
     */
    public CommentsBean getProfileCmts(long uid, int offset) throws TaskException {
        Setting action = newSetting("getProfileCmts", "/api/profile/comments", "获取用户发布的评论列表");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, ProfileCommentsHttpUtility.class.getName(), ""));

//        if (FundayUtils.isLoginedUser(uid)) {
//            action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, ProfileCmtsCacheUtility.class.getName(), ""));
//        }

        Params params = new Params();
        params.addParameter("userId", String.valueOf(uid));
        if (offset > 0) {
            params.addParameter("offset", String.valueOf(offset));
        } else {
            params.addParameter("offset", String.valueOf(0));
        }

        return doGet(action, basicParams(params), CommentsBean.class);
    }


    /**
     * 获取用户收藏的内容列表
     *
     * @param uid
     * @param offset
     * @return
     * @throws TaskException
     */
    public PostsBean getProfileFavs(long uid, int offset) throws TaskException {
        Setting action = newSetting("getProfileFavs", "/api/profile/favorites", "获取用户收藏的内容列表");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, ProfileCommentsHttpUtility.class.getName(), ""));

//        if (FundayUtils.isLoginedUser(uid)) {
//            action.getExtras().put(CACHE_UTILITY, newSettingExtra(CACHE_UTILITY, ProfileFavsCacheUtility.class.getName(), ""));
//        }

        Params params = new Params();
        params.addParameter("userId", String.valueOf(uid));
        if (offset > 0) {
            params.addParameter("offset", String.valueOf(offset));
        } else {
            params.addParameter("offset", String.valueOf(0));
        }

        return doGet(action, basicParams(params), PostsBean.class);
    }

    /**
     * 修改用户昵称
     */
    public BaseBean uploadUserName (String name, long userId, String token) throws TaskException {
        Setting action = newSetting("uploadUserName", "/api/user/modify/nick", "修改用户昵称");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, UploadAvatarHttpUtility.class.getName(), ""));

        Params params = new Params();
        params.addParameter("name", name);
        params.addParameter("userId", String.valueOf(userId));
        params.addParameter("token", token);

        return doGet(action, basicParams(params), BaseBean.class);
    }

    /**
     * 提交用户反馈
     */
    public BaseBean uploadFeedback (long userId, String content) throws TaskException {
        Setting action = newSetting("uploadFeedback", "/api/ugc/feedback", "提交用户反馈");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, UploadAvatarHttpUtility.class.getName(), ""));

        Params params = new Params();
        params.addParameter("userId", String.valueOf(userId));
        params.addParameter("content", content);

//        return doGet(action, basicParams(params), BaseBean.class);
        return doPost(configHttpConfig(), action, basicParams(null), params, null, BaseBean.class);
    }
    /**
     * 后台配置参数
     */
    public APPConfsBean setConf () throws TaskException {
        Setting action = newSetting("setConf", "/api/conf", "配置");
//        action.getExtras().put(HTTP_UTILITY, newSettingExtra(HTTP_UTILITY, UploadAvatarHttpUtility.class.getName(), ""));
        Params params = new Params();
        return doGet(action, basicParams(params), APPConfsBean.class);
    }
    /**
     *  上传加载时间
     */
    public BaseBean upElapse(List<APILog> logList) throws TaskException {
        if (logList == null || logList.size() == 0) {
            return null;
        }

        Setting action = newSetting("upElapse", "/api/log/elapse", "上传加载时间");

        List<Map<String, String>> uploadList = new ArrayList<>();
        for (APILog log : logList) {
            Map<String, String> logMap = new HashMap<>();
            logMap.put("api", log.getApi());
            logMap.put("time", "" + log.getDuration());

            uploadList.add(logMap);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("logs", uploadList);

        return doPost(configHttpConfig(), action, basicParams(null), null, map, BaseBean.class);
    }


    /**
     *  根据adi和did获取UUID
     */
    public UUIDBean getUUID(String aid, String did) throws TaskException {
        Setting action = newSetting("getUUID", "/api/uuid/find", "根据adi和did获取UUID");

        Params params = new Params();
        params.addParameter("aid", aid);
        params.addParameter("did", did);

        return doGet(action, basicParams(params), UUIDBean.class);
    }

}
