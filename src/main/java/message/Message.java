package message;

public class Message {
	protected String type;
	protected String id;
	protected String payload;

	public String getType() {
		return type;
	}

	public String getId() {
	    return id;
	}

	public String getPayload() {
	    return payload;
	}

	public void displayMessage(){
	    System.out.println("This is a " + getType() + " message. Its id is " + getId() + ", it contains: " + getPayload());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "This is a " + getType() + " message. Its id is " + getId() + ", it contains: " + getPayload());
		return sb.toString();
				
	}
}
