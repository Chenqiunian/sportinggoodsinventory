package Demo.Entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.List;

public class UserOperation {
    private Integer id;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    private Integer userid;

    private String name;

    private String operation;

    private Integer number;

    private String operationobject;

    private String details;

    private List<UserOperation> operationList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getOperationobject() {
        return operationobject;
    }

    public void setOperationobject(String operationobject) {
        this.operationobject = operationobject;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<UserOperation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<UserOperation> operationList) {
        this.operationList = operationList;
    }

    @Override
    public String toString() {
        return "UserOperation{" +
                "id=" + id +
                ", date=" + date +
                ", userid=" + userid +
                ", name='" + name + '\'' +
                ", operation='" + operation + '\'' +
                ", number=" + number +
                ", operationobject='" + operationobject + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}