package com.rokin.mobile.analytics.network;

/**
 * 
 * @author Sourav
 *
 */
public class Response 
{

	private Object data;
	private int requestType;
	private int dataType;
	private boolean status;
	
	private String message;
	private Exception exception;
	private String tag;
	private boolean serverHeader;
	
	public Response(Object data, int requestType, int dataType, boolean status, boolean hasServerTimeHeader)
	{
		this.serverHeader = hasServerTimeHeader;
		this.data = data;
		this.requestType = requestType;
		this.dataType = dataType;
		this.status = status;
	}

	public Response(Object data, int requestType, int dataType, boolean status)
	{
		this.data = data;
		this.requestType = requestType;
		this.dataType = dataType;
		this.status = status;
	}
	
	public Response(Object data, int requestType, int dataType, boolean status, String tag)
	{
		this.data = data;
		this.requestType = requestType;
		this.dataType = dataType;
		this.status = status;
		this.tag = tag;
	}
	
	public Response(Object data, int requestType, int dataType, boolean status, Exception exception, String message)
	{
		this(data, requestType, dataType, status);
		this.exception = exception;
		this.message = message;
	}
	
	public Response(Object data, int requestType, int dataType, boolean status, Exception exception, String message, boolean isServerHeader)
	{
		this(data, requestType, dataType, status);
		this.exception = exception;
		this.message = message;
		this.serverHeader = isServerHeader;
	}
	
	public Response(Object data, int requestType, int dataType, boolean status, Exception exception, String message, String tag)
	{
		this(data, requestType, dataType, status, tag);
		this.exception = exception;
		this.message = message;
	}
	
	public Object getData() 
	{
		return this.data;
	}
	
	public boolean getStatus()
	{
		return this.status;
	}
	
	public int getRequestType() 
	{
		return requestType;
	}
	
	public int getDataType() 
	{
		return dataType;
	}

	public String getMessage() 
	{
		return message;
	}
	
	public void setMessage(String message) 
	{
		this.message = message;
	}
	
	public Exception getException() 
	{
		return exception;
	}
	
	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public boolean isServerHeader()
	{
		return serverHeader;
	}
}
