package com.example.android.eventtimerfinish;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class RiderContract {

    private RiderContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.eventtimerfinish";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_RIDER = "rider";

    public static final class RiderEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_RIDER);

        public static final  String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RIDER;

        public static final  String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RIDER;

        public static final String TABLE_NAME = "rider";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_RIDER_NUM = "number";
        public static final String COLUMN_FENCE_NUM = "fenceNumber";
        public static final String COLUMN_RIDER_START = "start";
        public static final String COLUMN_RIDER_FINISH = "finish";

    }
}
