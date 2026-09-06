package com.collegestore.unistore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;
    private Integer stock = 0;
    private String imageFile;
    private String category;
    @Column(length = 1000)
    private String description;

    public Product() {}

    public Product(String name, Double price, Integer stock, String imageFile) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageFile = imageFile;
    }

    public Product(String name, Double price, Integer stock, String imageFile, String category, String description) {
        this(name, price, stock, imageFile);
        this.category = category;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImageFile() { return imageFile; }
    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
