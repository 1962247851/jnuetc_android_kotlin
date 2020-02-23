package jn.mjz.aiot.jnuetc.kotlin.model.util;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * GsonUtil
 *
 * @author qq1962247851
 * @date 2020/2/17 20:04
 */
public class GsonUtil {

    private static Gson instance;

    public static Gson getInstance() {
        if (instance == null) {
            instance = new Gson();
        }
        return instance;
    }

    public static <T> void parseJsonArrayAdd2List(String result, List<T> objectList, Class<T> className, boolean clearAllFirst) {
        if (clearAllFirst) {
            objectList.clear();
        }
        //Json的解析类对象
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            objectList.add(getInstance().fromJson(c, className));
        }
    }

    public static <T> void parseJsonArrayAdd2List(String result, List<T> objectList, Class<T> className) {
        //Json的解析类对象
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            objectList.add(getInstance().fromJson(c, className));
        }
    }

    public static <T> void parseJsonArrayAdd2List(String result, List<T> objectList, Class<T> className, @Nullable IGsonListener iGsonListener) {
        int cnt = 0, oldCount = objectList.size();
        //Json的解析类对象
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            objectList.add(getInstance().fromJson(c, className));
            cnt++;
        }
        if (iGsonListener != null) {
            iGsonListener.onItemAdded(oldCount, cnt);
        }
    }

    public static <T> ArrayList<T> parseJsonArray2List(String result, Class<T> className) {
        ArrayList<T> list = new ArrayList<>();
        if (result == null || result.isEmpty()) {
            return list;
        }
        //Json的解析类对象
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            list.add(getInstance().fromJson(c, className));
        }
        return list;
    }

    public interface IGsonListener {
        void onItemAdded(int oldCount, int addedNumber);
    }

}
