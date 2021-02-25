package com.yun.baselibrary.utils;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tencent.mmkv.BuildConfig;
import com.tencent.mmkv.MMKV;
import com.tencent.mmkv.MMKVLogLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: HOU
 * @Date: 2021/2/25 9:38 AM
 * @Desc mmkv映射存储，优于sp
 */
public class MMKVUtils {
    public static final String DEFAULT_FILE_NAME = "/MMKV_FILE";
    private MMKV mmkv;
    //保留Name与PersistUtil的映射
    private static SimpleArrayMap<String, MMKVUtils> PERSIST_UTILS_MAP = new SimpleArrayMap<>();

    /**
     * @param fileName 文件名
     */
    private MMKVUtils(String fileName) {
        mmkv = MMKV.mmkvWithID(fileName);
    }

    /**
     * @param fileName 文件名
     * @param multi    跨进程使用
     */
    private MMKVUtils(String fileName, int multi) {
        mmkv = MMKV.mmkvWithID(fileName, MMKV.MULTI_PROCESS_MODE);
    }

    /**
     * 保存对象
     *
     * @param name
     * @param value
     */
    public void putObject(String name, Object value) {
        mmkv.encode(name, toJsonString(value));
    }

    /**
     * 保存字符串
     *
     * @param name
     * @param value
     */
    public void putString(String name, String value) {
        mmkv.encode(name, value);
    }


    /**
     * 读取string
     *
     * @param name
     * @param defaultValue
     */
    public String getString(String name, @NonNull String defaultValue) {
        return mmkv.decodeString(name, defaultValue);
    }

    /**
     * 读取string
     *
     * @param name
     */
    public String getString(String name) {
        return mmkv.decodeString(name, "");
    }

    /**
     * 保存boolean
     *
     * @param name
     * @param value
     */
    public void putBoolean(String name, boolean value) {
        mmkv.encode(name, value);
    }

    /**
     * 读取boolean
     *
     * @param name
     * @param defaultValue
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        return mmkv.decodeBool(name, defaultValue);
    }

    /**
     * 读取boolean
     *
     * @param name
     */
    public boolean getBoolean(String name) {
        return mmkv.decodeBool(name, false);
    }

    /**
     * 保存int
     *
     * @param name
     * @param value
     */
    public void putInt(String name, int value) {
        //编码的时候
        mmkv.encode(name, value);
    }

    /**
     * 读取int
     *
     * @param name
     * @param defaultValue
     */
    public int getInt(String name, int defaultValue) {
        return mmkv.decodeInt(name, defaultValue);
    }

    /**
     * 读取int
     *
     * @param name
     */
    public int getInt(String name) {
        return mmkv.decodeInt(name, 0);
    }

    /**
     * 保存float
     *
     * @param name
     * @param value
     */
    public void putFloat(String name, float value) {
        mmkv.encode(name, value);
    }

    /**
     * 读取float，有默认值
     *
     * @param name
     * @param defaultValue
     */
    public float getFloat(String name, float defaultValue) {
        return mmkv.decodeFloat(name, defaultValue);
    }

    /**
     * 读取float
     *
     * @param name
     */
    public float getFloat(String name) {
        return mmkv.decodeFloat(name, 0f);
    }

    /**
     * 保存long
     *
     * @param name
     * @param value
     */
    public void putLong(String name, long value) {
        mmkv.encode(name, value);
    }

    /**
     * 读取long
     *
     * @param name
     * @param defaultValue
     */
    public float getLong(String name, long defaultValue) {
        return mmkv.decodeLong(name, defaultValue);
    }

    /**
     * 读取long
     *
     * @param name
     */
    public long getLong(String name) {
        return mmkv.decodeLong(name, 0L);
    }

    /**
     * 保存double
     *
     * @param name
     * @param value
     */
    public void putDouble(String name, double value) {
        mmkv.encode(name, value);
    }

    /**
     * 读取double
     *
     * @param name
     * @param defaultValue
     */
    public double getDouble(String name, double defaultValue) {
        return mmkv.decodeDouble(name, defaultValue);
    }

    /**
     * 读取double
     *
     * @param name
     */
    public double getDouble(String name) {
        return mmkv.decodeDouble(name, 0D);
    }

    /**
     * 保存byte[]
     *
     * @param name
     * @param bytes
     */
    public void putBytes(String name, byte[] bytes) {
        mmkv.encode(name, bytes);
    }

    /**
     * 读取byte[]
     *
     * @param name
     * @return
     */
    public byte[] getBytes(String name) {
        return mmkv.decodeBytes(name);
    }

    /**
     * 读取byte[]
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public byte[] getBytes(String name, byte[] defaultValue) {
        return mmkv.decodeBytes(name, defaultValue);
    }

    /**
     * 获取object
     *
     * @param name
     * @param clazz
     */
    public <T> T getObject(String name, Class<T> clazz) {
        return stringToBean(mmkv.decodeString(name), clazz);

    }

    /**
     * 获取List
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String name, Class<T> clazz) {
        //最坏的情况也只是返回一个空的List 不会产生null
        return stringToList(mmkv.decodeString(name), clazz);
    }

    /**
     * 字符串转javaBean
     *
     * @param jsonResult
     * @param clz
     * @param <T>
     * @return
     */
    private <T> T stringToBean(String jsonResult, Class<T> clz) {
        Gson gson = new Gson();
        T t = null;
        try {
            t = gson.fromJson(jsonResult, clz);
        } catch (Exception e) {
            e.printStackTrace();
            return t;
        }
        return t;
    }

    /**
     * 字符串转javaList
     *
     * @param jsonResult
     * @param clz
     * @param <T>
     * @return
     */
    private <T> List<T> stringToList(String jsonResult, Class<T> clz) {
        List<T> list = new ArrayList<>();
        try {
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();
            JsonArray jsonarray = parser.parse(jsonResult).getAsJsonArray();
            for (JsonElement element : jsonarray) {
                list.add(gson.fromJson(element, clz));
            }
        } catch (Exception e) {
            list.clear();
            return list;
        }
        return list;
    }


    /**
     * 是否包含某个字段的key
     *
     * @param name
     * @return
     */
    public boolean containsKey(String name) {
        return mmkv.containsKey(name);
    }

    /**
     * 移除某个key及对应值
     *
     * @param name
     */
    public void removeValueForKey(String name) {
        mmkv.removeValueForKey(name);
    }

    /**
     * 移除某个key及对应值
     *
     * @param name
     */
    public void removeKeyAndValue(String name) {
        mmkv.remove(name).apply();
    }

    /**
     * 删除文件内内容
     */
    public void deleteFileContent() {
        mmkv.clearAll();
    }

    /**
     * 实现了序列化的的java对象或list转成String
     *
     * @param obj
     * @return
     */
    private String toJsonString(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    /**
     * 使用默认文件名
     *
     * @return
     */
    public static MMKVUtils getInstance() {
        return getInstance(null);
    }

    /**
     * 使用自定义文件名
     *
     * @param fileName
     * @return
     */
    public static MMKVUtils getInstance(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = DEFAULT_FILE_NAME;
        }
        MMKVUtils persistUtil = PERSIST_UTILS_MAP.get(fileName);
        if (persistUtil == null) {
            persistUtil = new MMKVUtils(fileName);
            PERSIST_UTILS_MAP.put(fileName, persistUtil);
        }
        return persistUtil;
    }

    public static void init(Application application) {
        init(application, null);
    }

    /**
     * @param application Application 一般为继承了Application的实例
     * @param pathName    不需要加"/"直接传个名字就行了 为/data/user/0/包名/files/下方的目录
     *                    由于考虑到不同目标平台对于额外存储权限的不同，这里使用的是私有存储类似于SP
     */
    public static void init(Application application, String pathName) {
        MMKVLogLevel logLevel = BuildConfig.DEBUG ? MMKVLogLevel.LevelDebug : MMKVLogLevel.LevelError;
        if (TextUtils.isEmpty(pathName)) {
            MMKV.initialize(application.getFilesDir().getAbsolutePath() + DEFAULT_FILE_NAME, null, logLevel);
        } else {
            if (!pathName.startsWith("/")) {
                pathName = new StringBuilder().append("/").append(pathName).toString();
            }
            MMKV.initialize(application.getFilesDir().getAbsolutePath() + pathName, null, logLevel);

        }
    }
}
