
public class AddressMap 
{
	private String alias		= "";
	private String ip			= "";
	private int port			= 0;
	
	public AddressMap(String ip,int port,String alias)
	{
		this.alias = alias;
		this.ip = ip;
		this.port = port;
	}
	
	public String getAlias()
	{
		return alias;
	}
	
	public String getIP()
	{
		return ip;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public void setIP(String ip)
	{
		this.ip = ip;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}
}
