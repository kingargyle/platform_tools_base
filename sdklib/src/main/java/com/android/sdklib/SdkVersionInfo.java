/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.sdklib;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.base.Strings;

import java.util.Locale;

/** Information about available SDK Versions */
public class SdkVersionInfo {
    /**
     * The highest known API level. Note that the tools may also look at the
     * installed platforms to see if they can find more recently released
     * platforms, e.g. when the tools have not yet been updated for a new
     * release. This number is used as a baseline and any more recent platforms
     * found can be used to increase the highest known number.
     */
    public static final int HIGHEST_KNOWN_API = 21;

    /**
     * Like {@link #HIGHEST_KNOWN_API} but does not include preview platforms
     */
    public static final int HIGHEST_KNOWN_STABLE_API = 21;

   /**
    * The lowest active API level in the ecosystem. This number will change over time
    * as the distribution of older platforms decreases.
    */
    public static final int LOWEST_ACTIVE_API = 8;

    /**
     * Returns the Android version and code name of the given API level, or null
     * if not known. The highest number (inclusive) that is supported
     * is {@link SdkVersionInfo#HIGHEST_KNOWN_API}.
     *
     * @param api the api level
     * @return a suitable version display name
     */
    @Nullable
    public static String getAndroidName(int api) {
        // See http://source.android.com/source/build-numbers.html
        switch (api) {
            case 1:  return "API 1: Android 1.0";
            case 2:  return "API 2: Android 1.1";
            case 3:  return "API 3: Android 1.5 (Cupcake)";
            case 4:  return "API 4: Android 1.6 (Donut)";
            case 5:  return "API 5: Android 2.0 (Eclair)";
            case 6:  return "API 6: Android 2.0.1 (Eclair)";
            case 7:  return "API 7: Android 2.1 (Eclair)";
            case 8:  return "API 8: Android 2.2 (Froyo)";
            case 9:  return "API 9: Android 2.3 (Gingerbread)";
            case 10: return "API 10: Android 2.3.3 (Gingerbread)";
            case 11: return "API 11: Android 3.0 (Honeycomb)";
            case 12: return "API 12: Android 3.1 (Honeycomb)";
            case 13: return "API 13: Android 3.2 (Honeycomb)";
            case 14: return "API 14: Android 4.0 (IceCreamSandwich)";
            case 15: return "API 15: Android 4.0.3 (IceCreamSandwich)";
            case 16: return "API 16: Android 4.1 (Jelly Bean)";
            case 17: return "API 17: Android 4.2 (Jelly Bean)";
            case 18: return "API 18: Android 4.3 (Jelly Bean)";
            case 19: return "API 19: Android 4.4 (KitKat)";
            case 20: return "API 20: Android 4.4 (KitKat Wear)";
            case 21: return "API 21: Android 5.0 (Lollipop)";
            // If you add more versions here, also update #getBuildCodes and
            // #HIGHEST_KNOWN_API

            default: return null;
        }
    }

    @Nullable
    public static String getCodeName(int api) {
        String s = getAndroidName(api);
        if (s != null) {
            int start = s.indexOf('(');
            if (start != -1) {
                start++;
                int end = s.indexOf(')', start);
                if (end != -1) {
                    return s.substring(start, end);
                }
            }
        }

        return null;
    }

    /**
     * Returns the applicable build code (for
     * {@code android.os.Build.VERSION_CODES}) for the corresponding API level,
     * or null if it's unknown. The highest number (inclusive) that is supported
     * is {@link SdkVersionInfo#HIGHEST_KNOWN_API}.
     *
     * @param api the API level to look up a version code for
     * @return the corresponding build code field name, or null
     */
    @Nullable
    public static String getBuildCode(int api) {
        // See http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
        switch (api) {
            case 1:  return "BASE"; //$NON-NLS-1$
            case 2:  return "BASE_1_1"; //$NON-NLS-1$
            case 3:  return "CUPCAKE"; //$NON-NLS-1$
            case 4:  return "DONUT"; //$NON-NLS-1$
            case 5:  return "ECLAIR"; //$NON-NLS-1$
            case 6:  return "ECLAIR_0_1"; //$NON-NLS-1$
            case 7:  return "ECLAIR_MR1"; //$NON-NLS-1$
            case 8:  return "FROYO"; //$NON-NLS-1$
            case 9:  return "GINGERBREAD"; //$NON-NLS-1$
            case 10: return "GINGERBREAD_MR1"; //$NON-NLS-1$
            case 11: return "HONEYCOMB"; //$NON-NLS-1$
            case 12: return "HONEYCOMB_MR1"; //$NON-NLS-1$
            case 13: return "HONEYCOMB_MR2"; //$NON-NLS-1$
            case 14: return "ICE_CREAM_SANDWICH"; //$NON-NLS-1$
            case 15: return "ICE_CREAM_SANDWICH_MR1"; //$NON-NLS-1$
            case 16: return "JELLY_BEAN"; //$NON-NLS-1$
            case 17: return "JELLY_BEAN_MR1"; //$NON-NLS-1$
            case 18: return "JELLY_BEAN_MR2"; //$NON-NLS-1$
            case 19: return "KITKAT"; //$NON-NLS-1$
            case 20: return "KITKAT_WATCH"; //$NON-NLS-1$
            case 21: return "LOLLIPOP"; //$NON-NLS-1$
            // If you add more versions here, also update #getAndroidName and
            // #HIGHEST_KNOWN_API
        }

        return null;
    }

    /**
     * Returns the API level of the given build code (e.g. JELLY_BEAN_MR1 => 17), or -1 if not
     * recognized
     *
     * @param buildCode         the build code name (not case sensitive)
     * @param recognizeUnknowns if true, treat an unrecognized code name as a newly released
     *                          platform the tools are not yet aware of, and set its API level to
     *                          some higher number than all the currently known API versions
     * @return the API level, or -1 if not recognized (unless recognizeUnknowns is true, in which
     * {@link #HIGHEST_KNOWN_API} plus one is returned
     */
    public static int getApiByBuildCode(@NonNull String buildCode, boolean recognizeUnknowns) {
        for (int api = 1; api <= HIGHEST_KNOWN_API; api++) {
            String code = getBuildCode(api);
            if (code != null && code.equalsIgnoreCase(buildCode)) {
                return api;
            }
        }

        if (buildCode.equalsIgnoreCase("L")) {
            return 21; // For now the Build class also provides this as an alias to Lollipop
        }

        return recognizeUnknowns ? HIGHEST_KNOWN_API + 1 : -1;
    }

    /**
     * Returns the API level of the given preview code name (e.g. JellyBeanMR2 => 17), or -1 if not
     * recognized
     *
     * @param previewName       the preview name (not case sensitive)
     * @param recognizeUnknowns if true, treat an unrecognized code name as a newly released
     *                          platform the tools are not yet aware of, and set its API level to
     *                          some higher number than all the currently known API versions
     * @return the API level, or -1 if not recognized (unless recognizeUnknowns is true, in which
     * {@link #HIGHEST_KNOWN_API} plus one is returned
     */
    public static int getApiByPreviewName(@NonNull String previewName, boolean recognizeUnknowns) {
        // JellyBean => JELLY_BEAN
        String codeName = camelCaseToUnderlines(previewName).toUpperCase(Locale.US);
        return getApiByBuildCode(codeName, recognizeUnknowns);
    }

    /**
     * Converts a CamelCase word into an underlined_word
     *
     * @param string the CamelCase version of the word
     * @return the underlined version of the word
     */
    @NonNull
    public static String camelCaseToUnderlines(@NonNull String string) {
        if (string.isEmpty()) {
            return string;
        }

        StringBuilder sb = new StringBuilder(2 * string.length());
        int n = string.length();
        boolean lastWasUpperCase = Character.isUpperCase(string.charAt(0));
        for (int i = 0; i < n; i++) {
            char c = string.charAt(i);
            boolean isUpperCase = Character.isUpperCase(c);
            if (isUpperCase && !lastWasUpperCase) {
                sb.append('_');
            }
            lastWasUpperCase = isUpperCase;
            c = Character.toLowerCase(c);
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Converts an underlined_word into a CamelCase word
     *
     * @param string the underlined word to convert
     * @return the CamelCase version of the word
     */
    @NonNull
    public static String underlinesToCamelCase(@NonNull String string) {
        StringBuilder sb = new StringBuilder(string.length());
        int n = string.length();

        int i = 0;
        @SuppressWarnings("SpellCheckingInspection")
        boolean upcaseNext = true;
        for (; i < n; i++) {
            char c = string.charAt(i);
            if (c == '_') {
                upcaseNext = true;
            } else {
                if (upcaseNext) {
                    c = Character.toUpperCase(c);
                }
                upcaseNext = false;
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Returns the {@link AndroidVersion} for a given version string, which is typically an API
     * level number, but can also be a codename for a <b>preview</b> platform. Note: This should
     * <b>not</b> be used to look up version names for build codes; for that, use {@link
     * #getApiByBuildCode(String, boolean)}. The primary difference between this method is that
     * {@link #getApiByBuildCode(String, boolean)} will return the final API number for a platform
     * (e.g. for "KITKAT" it will return 19) whereas this method will return the API number for the
     * codename as a preview platform (e.g. 18).
     *
     * @param apiOrPreviewName the version string
     * @param targets          an optional array of installed targets, if available. If the version
     *                         string corresponds to a code name, this is used to search for a
     *                         corresponding API level.
     * @return an {@link com.android.sdklib.AndroidVersion}, or null if the version could not be
     * determined (e.g. an empty or invalid API number or an unknown code name)
     */
    @Nullable
    public static AndroidVersion getVersion(
            @Nullable String apiOrPreviewName,
            @Nullable IAndroidTarget[] targets) {
        if (Strings.isNullOrEmpty(apiOrPreviewName)) {
            return null;
        }

        if (Character.isDigit(apiOrPreviewName.charAt(0))) {
            try {
                int api = Integer.parseInt(apiOrPreviewName);
                if (api >= 1) {
                    return new AndroidVersion(api, null);
                }
                return null;
            } catch (NumberFormatException e) {
                // Invalid version string
                return null;
            }
        }

        // Codename
        if (targets != null) {
            for (int i = targets.length - 1; i >= 0; i--) {
                IAndroidTarget target = targets[i];
                if (target.isPlatform()) {
                    AndroidVersion version = target.getVersion();
                    if (version.isPreview() && apiOrPreviewName.equalsIgnoreCase(version.getCodename())) {
                        return new AndroidVersion(version.getApiLevel(), version.getCodename());
                    }
                }
            }
        }

        int api = getApiByPreviewName(apiOrPreviewName, false);
        if (api != -1) {
            return new AndroidVersion(api - 1, apiOrPreviewName);
        }

        // Must be a future SDK platform
        return new AndroidVersion(HIGHEST_KNOWN_API, apiOrPreviewName);
    }
}
