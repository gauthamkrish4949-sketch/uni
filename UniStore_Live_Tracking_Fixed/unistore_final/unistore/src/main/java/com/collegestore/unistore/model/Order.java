package com.collegestore.unistore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String studentName;
    private String userClass;
    private String orderType;
    @Column(length = 2000) private String details;
    private String documentFile;
    private Double totalAmount;
    private String status = "PLACED";
    private String paymentMethod = "PAY_AT_COUNTER";
    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {}
    public Long getId(){return id;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public String getStudentName(){return studentName;} public void setStudentName(String v){studentName=v;}
    public String getUserClass(){return userClass;} public void setUserClass(String v){userClass=v;}
    public String getOrderType(){return orderType;} public void setOrderType(String v){orderType=v;}
    public String getDetails(){return details;} public void setDetails(String v){details=v;}
    public String getDocumentFile(){return documentFile;} public void setDocumentFile(String v){documentFile=v;}
    public Double getTotalAmount(){return totalAmount;} public void setTotalAmount(Double v){totalAmount=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
