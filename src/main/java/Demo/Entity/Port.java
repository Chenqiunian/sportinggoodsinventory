package Demo.Entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.List;

public class Port {
    private Integer id;

    private Integer goodsid;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    private String image;

    private String name;

    private Integer number;

    private Double unitprice;

    private Double totalprice;

    private String operation;

    private String trader;

    private String details;

    private String state;

    private List<Port> portList;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Port> getPortList() {
        return portList;
    }

    public void setPortList(List<Port> portList) {
        this.portList = portList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoodsid() {
        return goodsid;
    }

    public void setGoodsid(Integer goodsid) {
        this.goodsid = goodsid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Double getUnitprice() {
        return unitprice;
    }

    public void setUnitprice(Double unitprice) {
        this.unitprice = unitprice;
    }

    public Double getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(Double totalprice) {
        this.totalprice = totalprice;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Port{" +
                "id=" + id +
                ", goodsid=" + goodsid +
                ", date=" + date +
                ", image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", number=" + number +
                ", unitprice=" + unitprice +
                ", totalprice=" + totalprice +
                ", operation='" + operation + '\'' +
                ", trader='" + trader + '\'' +
                ", details='" + details + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}