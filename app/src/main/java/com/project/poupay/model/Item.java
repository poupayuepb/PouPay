package com.project.poupay.model;

public class Item {

    private String description;
    private int icon;
    private Double price;
    private String paymentType;

    public Item(){}

    public Item(String description, int icon, Double price, String paymentType) {
        this.description = description;
        this.icon = icon;
        this.price = price;
        this.paymentType = paymentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
