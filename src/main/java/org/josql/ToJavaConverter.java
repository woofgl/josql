package org.josql;

public abstract class ToJavaConverter<T> extends BaseConverter{

    public abstract Object toJava(T dbObj);
}
