package com.example.neurotrack.utils;

import android.net.Uri;
import com.example.neurotrack.models.Theme;

public class IconUrlBuilder {


    public static String buildIconUrl(Theme theme, String iconCode) {
        if (theme == null || iconCode == null || iconCode.isEmpty()) {
            return null;
        }

        if (theme.getIconBaseUrl() == null || theme.getIconSuffix() == null) {
            return null;
        }

        String fileName = iconCode + theme.getIconSuffix();
        String encodedFileName = Uri.encode(fileName);


        return theme.getIconBaseUrl() + encodedFileName + "?alt=media";
    }


    public static String buildStepUrl(Theme theme, String stepCode) {
        if (theme == null || stepCode == null || stepCode.isEmpty()) {
            return null;
        }

        if (theme.getStepBaseUrl() == null || theme.getStepSuffix() == null) {
            return null;
        }

        String fileName = stepCode + theme.getStepSuffix();
        String encodedFileName = Uri.encode(fileName);

        return theme.getStepBaseUrl() + encodedFileName + "?alt=media";
    }


    public static String buildBackgroundUrl(Theme theme) {
        if (theme == null || theme.getBgUrl() == null) {
            return null;
        }
        return theme.getBgUrl();
    }
}

