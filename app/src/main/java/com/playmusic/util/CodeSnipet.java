package com.playmusic.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class CodeSnipet {

    private CodeSnipet mCodeSnippet;


    public CodeSnipet getInstance() {
        if (mCodeSnippet == null)
            mCodeSnippet = new CodeSnipet();
        return mCodeSnippet;
    }


    public static void showLog(String pTag, String pMessage) {
        Log.e(pTag, pMessage);
    }

    public static void showToast(Context pContext, String pMessage) {
        Toast.makeText(pContext, pMessage, Toast.LENGTH_SHORT).show();
    }


}
