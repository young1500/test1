package com.hawk.funday.support.sdk.bean;

import org.aisen.android.component.orm.annotation.AutoIncrementPrimaryKey;

import java.io.Serializable;

/**
 * @Description: UploadBean 上传发布草稿记录对象
 * @author  qiangtai.huang
 * @date  2016/8/30
 * @copyright TCL-HAWK
 */
public class UploadBean extends Object implements Serializable{
    private static final long serialVersionUID = 9672211595049820L;
    @AutoIncrementPrimaryKey(column = "id")
    private int id;
    private String filePath;

    private String fileUrl;
    private String title;
    private int state; /////状态，默认为0，上传完文件为1，上传完url地址及附属信息为2
    private int resourceType;  /*resourceType=1表示为静态图片Picture资源，
                                    resourceType=2表示为动态图片Gif资源，
                                    resourceType=3表示为视频Video资源，
                                    resourceType=4表示为文章Ariticle资源,
                                    resourceType=20160906为发送失败的资源*/
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }
}
