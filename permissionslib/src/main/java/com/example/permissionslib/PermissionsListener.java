package com.example.permissionslib;

public interface PermissionsListener {
    void onPermissionGranted(String permission);
    void onPermissionDenied(String permission);
    void onPermissionPermanentlyDenied(String permission);
}
