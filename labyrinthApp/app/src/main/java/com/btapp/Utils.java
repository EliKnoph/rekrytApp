package com.btapp;

public class Utils {

    static String convertToJSON(String key1, String value1, String key2, String value2, String key3, String value3){

        //'{ "name":"John", "age":30, "city":"New York"}'
        //'{ "x":"1.3915063", "y":"2.0190015", "z":"9.680669"}'
        //'{ "x":-1.7974621, "y":4.132606, "z":10.656639}'
        String json = ("'{ " + "\"" + key1 + "\":" + value1 + "," + " \"" + key2 + "\":"  + value2 + "," + " \"" + key3 + "\":"  + value3 + "}'");
        System.out.println(json);
        return json;

    }
}
