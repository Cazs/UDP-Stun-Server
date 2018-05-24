
public enum StunjProtocolCodes 
{
	SENDTO("SEND TO"),
	ADDME("ADD ME"),
	GOSLEEP("GO SLEEP"),
	SHUTDOWN("SHUTDOWN"),
	ACK("ACK"),
	NAK("NAK"),
	MESSAGE("MESSAGE INBOUND"),
	ERROR("ERROR");
	
	private final String code;
	
	StunjProtocolCodes(String code)
	{
		this.code = code;
	}	
	
	public String getCode()
	{
		return code;
	}
}
