public class Message 
{
	private String message					= "";
	private String dest						= null;
	private String src						= null;
	private String code						= "";
	
	public Message(String msg,String dest,String src,String code)
	{
		message = msg;
		this.dest = dest;
		this.src = src;
		this.code = code;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public void setCode(String code)
	{
		this.code = code;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public String getDestination()
	{
		return dest;
	}
	
	public String getSource()
	{
		return src;
	}
	
	public void setMessage(String msg)
	{
		message = msg;
	}
	
	public void setDestination(String dest)
	{
		this.dest = dest;
	}
	
	public void setSource(String src)
	{
		this.src = src;
	}
}
