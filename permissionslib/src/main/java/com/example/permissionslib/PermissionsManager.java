package com.example.permissionslib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.examole.permissionslib.R;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.Map;

public class PermissionsManager implements PermissionsListener {

    private final Activity activity;
    private final Map<String, MaterialButton> permissionButtons = new HashMap<>();

    public PermissionsManager(Activity activity) {
        this.activity = activity;
    }

    public void setupPermissionsButton(MaterialButton button, String[] permissions, int requestCode) {
        button.setOnClickListener(v -> showPermissionsDialog(permissions, requestCode));
    }

    private void showPermissionsDialog(String[] permissions, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Request Permissions");

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);

        for (String permission : permissions) {
            MaterialButton permissionButton = new MaterialButton(activity);
            permissionButton.setText(getPermissionName(permission));
            permissionButton.setCornerRadius(24);

            if (PermissionsUtils.isPermissionGranted(activity, permission)) {
                permissionButton.setBackgroundColor(activity.getResources().getColor(R.color.green));
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                permissionButton.setBackgroundColor(activity.getResources().getColor(R.color.red));
            } else {
                permissionButton.setBackgroundColor(activity.getResources().getColor(R.color.blue));
            }

            permissionButton.setOnClickListener(v -> requestPermission(permission, requestCode));

            layout.addView(permissionButton);
            permissionButtons.put(permission, permissionButton);
        }

        builder.setView(layout);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void requestPermission(String permission, int requestCode) {
        if (PermissionsUtils.isPermissionGranted(activity, permission)) {
            onPermissionGranted(permission);
        } else if (PermissionsUtils.shouldShowRequestPermissionRationale(activity, permission)) {
            showPermissionRationaleDialog(permission, requestCode);
        } else {
            PermissionsUtils.requestPermission(activity, new String[]{permission}, requestCode);
        }
    }

    private void showPermissionRationaleDialog(String permission, int requestCode) {
        new AlertDialog.Builder(activity)
                .setTitle("Permission Required: " + getPermissionName(permission))
                .setMessage("This permission is required for the app to function properly. Please click 'Allow' to grant the permission.")
                .setPositiveButton("Allow", (dialog, which) -> PermissionsUtils.requestPermission(activity, new String[]{permission}, requestCode))
                .setNegativeButton("Deny", (dialog, which) -> {
                    dialog.dismiss();
                    onPermissionDenied(permission);
                })
                .show();
    }

    public void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permissions[i]);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                onPermissionDenied(permissions[i]);
            } else {
                onPermissionPermanentlyDenied(permissions[i]);
            }
        }
    }

    @Override
    public void onPermissionGranted(String permission) {
        Toast.makeText(activity, getPermissionName(permission) + " permission granted.", Toast.LENGTH_SHORT).show();
        updateButtonColor(permission, R.color.green);
    }

    @Override
    public void onPermissionDenied(String permission) {
        Toast.makeText(activity, getPermissionName(permission) + " permission denied.", Toast.LENGTH_SHORT).show();
        updateButtonColor(permission, R.color.red);
    }

    @Override
    public void onPermissionPermanentlyDenied(String permission) {
        showPermanentlyDeniedDialog(permission);
    }

    private void showPermanentlyDeniedDialog(String permission) {
        new AlertDialog.Builder(activity)
                .setTitle("Permission Required: " + getPermissionName(permission))
                .setMessage("The " + getPermissionName(permission) + " permission is required for this feature. Please enable it in the app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    private void updateButtonColor(String permission, @ColorRes int colorRes) {
        MaterialButton button = permissionButtons.get(permission);
        if (button != null) {
            button.setBackgroundColor(activity.getResources().getColor(colorRes));
        }
    }

    private String getPermissionName(String permission) {
        return permission.toLowerCase().substring(("android.permission.").length()).replace('_', ' ');
    }
}
