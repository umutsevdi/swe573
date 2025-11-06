package com.hive.config;

import jakarta.servlet.ServletException;

public class AssetNotFoundException extends ServletException {

    public AssetNotFoundException() {
        super("Asset not found!");
    }
}
