package org.josql;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class RSQLException extends RuntimeException {
    
    public enum Error{
        PARAM_VALUE_CANNOT_BE_NULL, 
        CANNOT_INSTANCIATE_LIST_RESOURCE_BUILDER;
    }
    
    private Error error; 

    public RSQLException(Error error, Throwable e){
        super(e);
        this.error = error;
    }
    
    public RSQLException(Error error){
        this.error = error;
    }
    
    public RSQLException(SQLException e){
        super(e);
    }
    
    public String getMessage(){
        StringBuilder sb = new StringBuilder();
        
        Throwable cause = this.getCause();
        if (error != null){
            sb.append(error.toString()).append(" ");
        }
        if (cause != null){
            sb.append(cause.getMessage());
        }
        
        String message = sb.toString();
        
        message = (message.length() == 0)?"no message":message;
        
        return message;
        
        
    }
}
