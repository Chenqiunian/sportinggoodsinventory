package Demo.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
@Component
public class LoginRecord {
    private Integer id;

    private String username;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date datetime;

    private String ip;

    private String address;

    private String client;

    private Integer sliderrequesttimes;

    private Integer sliderfailtimes;

    private Integer pwfailtimes;

    private String state;

    private List<LoginRecord> loginRecordList;

    public List<LoginRecord> getLoginRecordList() {
        return loginRecordList;
    }

    public void setLoginRecordList(List<LoginRecord> loginRecordList) {
        this.loginRecordList = loginRecordList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Integer getSliderrequesttimes() {
        return sliderrequesttimes;
    }

    public void setSliderrequesttimes(Integer sliderrequesttimes) {
        this.sliderrequesttimes = sliderrequesttimes;
    }

    public Integer getSliderfailtimes() {
        return sliderfailtimes;
    }

    public void setSliderfailtimes(Integer sliderfailtimes) {
        this.sliderfailtimes = sliderfailtimes;
    }

    public Integer getPwfailtimes() {
        return pwfailtimes;
    }

    public void setPwfailtimes(Integer pwfailtimes) {
        this.pwfailtimes = pwfailtimes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "LoginRecord{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", datetime=" + datetime +
                ", ip='" + ip + '\'' +
                ", address='" + address + '\'' +
                ", client='" + client + '\'' +
                ", sliderrequesttimes=" + sliderrequesttimes +
                ", sliderfailtimes=" + sliderfailtimes +
                ", pwfailtimes=" + pwfailtimes +
                ", state='" + state + '\'' +
                '}';
    }
}