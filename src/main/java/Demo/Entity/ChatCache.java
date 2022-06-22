package Demo.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class ChatCache {
    private Integer id;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date sendtime;

    private String message;

    private Integer senduserid;

    private Integer acceptuserid;

    private String sendusername;

    private List<ChatCache> chatCacheList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getSendtime() {
        return sendtime;
    }

    public void setSendtime(Date sendtime) {
        this.sendtime = sendtime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getSenduserid() {
        return senduserid;
    }

    public void setSenduserid(Integer senduserid) {
        this.senduserid = senduserid;
    }

    public Integer getAcceptuserid() {
        return acceptuserid;
    }

    public void setAcceptuserid(Integer acceptuserid) {
        this.acceptuserid = acceptuserid;
    }

    public String getSendusername() {
        return sendusername;
    }

    public void setSendusername(String sendusername) {
        this.sendusername = sendusername;
    }

    public List<ChatCache> getChatCacheList() {
        return chatCacheList;
    }

    public void setChatCacheList(List<ChatCache> chatCacheList) {
        this.chatCacheList = chatCacheList;
    }

    @Override
    public String toString() {
        return "ChatCache{" +
                "id=" + id +
                ", sendtime=" + sendtime +
                ", message='" + message + '\'' +
                ", senduserid=" + senduserid +
                ", acceptuserid=" + acceptuserid +
                ", sendusername='" + sendusername + '\'' +
                '}';
    }
}