/****************************************************************************
Copyright (c) 2015 Chukong Technologies Inc.

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ****************************************************************************/
package com.chukong.cocosplay.client;

import android.app.Activity;
import android.util.Log;

public class CocosPlayClient {
    private static final String TAG = CocosPlayClient.class.getName();

    public static boolean init(Activity activity, boolean isDemo) {
        Log.i(TAG,"==== init ====");
        return false;
    }
    
    public static boolean isEnabled() {
        Log.i(TAG,"==== isEnabled ====");
        return false;
    }
    
    public static boolean isDemo() {
        Log.i(TAG,"==== isDemo ====");
        return false;
    }
    
    public static boolean isNotifyFileLoadedEnabled() {
        Log.i(TAG,"==== isNotifyFileLoadedEnabled ====");
        return false;
    }
    
    public static void notifyFileLoaded(String filePath) {
        Log.i(TAG,"==== notifyFileLoaded ====");
        
    }
    
    public static void updateAssets(String filePath) {
        Log.i(TAG,"==== updateAssets ====");
        
    }
    
    public static String getGameRoot() {
        Log.i(TAG,"==== getGameRoot ====");
        return "";
    }
    
    public static native String[] getSearchPaths();
}
