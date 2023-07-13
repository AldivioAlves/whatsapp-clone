package com.estudos.whatsapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static  boolean validPermissions(String[] permissions, Activity activity, int requestCode){
        // validar se o usuario está usando uma versão maior que a Marsmellow

        if(Build.VERSION.SDK_INT >= 23){
            List<String> permissionsList = new ArrayList<>();

            for(String permission : permissions){
                boolean granted = ContextCompat.checkSelfPermission(activity,permission)== PackageManager.PERMISSION_GRANTED;
                if(!granted){
                    permissionsList.add(permission);
                }
            }

            if(permissionsList.isEmpty())return true;
                // converter a listagem para uma lista de strings
                String[] newPermissions = new String[permissionsList.size()];
                permissionsList.toArray(newPermissions);

            ActivityCompat.requestPermissions(activity,newPermissions,requestCode);

        }

        return true;
    }
}
