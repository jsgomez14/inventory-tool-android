package com.example.basicapp.model;

import android.view.View;

import java.util.ArrayList;

public class StockSummary {


    public static final ColumnHeader[] FIELDS = new ColumnHeader[]{new ColumnHeader("ID Producto"),
            new ColumnHeader("Nombre Producto"),
            new ColumnHeader("ID Proveedor"),
            new ColumnHeader("Nombre Proveedor"),
            new ColumnHeader("Stock"),
            new ColumnHeader("Fecha Creación"),
            new ColumnHeader("Fecha Actualización")};

    private int productId;

    private String productName;

    private int stock;

    private String createdAt;

    private String updatedAt;

    public StockSummary(int productId, String productName, int stock, String createdAt, String updatedAt) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAttribute(String s) {
        String resp = null;
        switch(s) {
            case "ID Producto":
                resp= String.valueOf(this.getProductId());
                break;
            case "Nombre Producto":
                resp= this.getProductName();
                break;
            case "Stock":
                resp= String.valueOf(this.getStock());
                break;
            case "Fecha Creación":
                resp= this.getCreatedAt();
                break;
            case "Fecha Actualización":
                resp= this.getUpdatedAt();
                break;
        }
        return resp;
    }
}
