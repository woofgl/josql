package org.josql.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public final static Map<String, Object> mapIt(Object... objs) {
        HashMap<String, Object> m = new HashMap<String, Object>();

        for (int i = 0; i < objs.length; i += 2) {
            String key = objs[i].toString();
            if (i + 1 < objs.length) {
                Object value = objs[i + 1];
                m.put(key, value);
            } else {
                m.put(key, null);
            }
        }
        return m;
    }
}
