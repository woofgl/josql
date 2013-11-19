package org.josql;

public abstract class ToDbConverter<T>  extends BaseConverter {

    public abstract Object toDb(T javaObj);
}
