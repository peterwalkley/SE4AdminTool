package tfa.se4;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public final class Protocol
{
	private static MessageDigest SHA1 = null;

	private static final byte[] EMPTY = new byte[] {};
	
	public static final char REQUEST_SEND_COMMAND = 57504;
	public static final char REQUEST_SET_WEB_STATUS_UPDATE_INTERVAL = 57520;
	public static final char REQUEST_INIT = 57584;
	public static final char REQUEST_SEND_PWD = 57586;
	
	public static final char REPLY_CMD_RESULT = 57505;
	public static final char REPLY_ASYNC_MSG = 57506;
	public static final char REPLY_WEB_STATUS_UPDATE = 57521;
	public static final char REPLY_COMMAND_LIST = 57522;
	public static final char REPLY_WEB_STATUS_MONITOR_UPDATE = 57523;
	public static final char REPLY_INIT = 57585;
	public static final char REPLY_CONNECTION_SUCCESS_1 = 57599;
	public static final char REPLY_CONNECTION_SUCCESS_2 = 61441;
	//public static final char REPLY_CONNECTION_FAILURE = 61442;
	
	/** Server status Pre-game i.e. in lobby. */
	public static final String PRE_GAME = "Pre-game";
	
	/** Server status starting game. */
	public static final String STARTING_GAME = "Starting game";
		
	/** Server status in game. */
	public static final String IN_GAME = "Starting game";
		
	
	public static final class ReplyMessage
	{
		public char messageId;
		public ByteBuffer payload;
	}
	
	private Protocol()
	{
		// utility class
	}
	
	/**
	 * Split raw reply into message id and optional payload.
	 * @param b bytes from SE4 server
	 * @return formatted reply
	 */
	public static ReplyMessage getReplyMessage(final byte[] b)
	{
		ReplyMessage result = new ReplyMessage();
    	ByteBuffer buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
    	result.messageId = buf.getChar(0);
		
    	if (b.length == 2)
    		result.payload = ByteBuffer.wrap(EMPTY);
    	else
    	{
    		final byte[] load = new byte[b.length - 2];
    		for (int i = 0; i < load.length; i++)
    			load[i] = b[i + 2];
    		result.payload = ByteBuffer.wrap(load);
    	}
    			
		return result;
	}
	
	/**
	 * Helper method to convert readable form to bytes.
	 * @param s Hex string of bytes e.g. 0a2fb3
	 * @return byte array
	 */
	public static byte[] hexStringToByteArray(String s)
	{
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	/**
	 * Helper to convert byte array to readable form.
	 * @param bytes Array of bytes
	 * @return Readable form e.g 0a2fb3
	 */
    public static String bytesToString(byte[] bytes)
    {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes)
        {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }
    
    /**
     * Build raw SE4 message.
     * @param messageId Message ID we are going to send.
     * @return ByteBuffer of bytes.
     */
    public static ByteBuffer buildMessage(final char messageId)
    {
        final ByteBuffer buf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        buf.putChar(messageId);
        buf.rewind();
        return buf;
    }
    
    /**
     * Build raw SE4 message.
     * @param messageId Message ID we are going to send.
     * @param payload payload of message
     * @return ByteBuffer of bytes.
     */
    public static ByteBuffer buildMessage(final char messageId, final byte[] payload)
    {
    	final ByteBuffer msgBuf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
    	msgBuf.putChar(messageId);
    	
        final ByteBuffer buf = ByteBuffer.allocate(2 + payload.length);
        buf.put(msgBuf.array()[0]);
        buf.put(msgBuf.array()[1]);
        
        for (final byte b : payload)
        	buf.put(b);
        buf.rewind();
        return buf;
    }
    
    /**
     * Get the SALT value from payload when the reply was a REPLY_INIT
     * @param buf reply payload (after message split)
     * @return salt value
     */
    public static byte[] getSaltValue(final ByteBuffer buf)
    {
		//TODO: check version and protocol
		final byte[] salt = new byte[buf.array().length - 8];
		buf.position(8);
		buf.get(salt, 0, salt.length);
		return salt;
    	
    }
    
    /**
     * Singleton for SHA1 digest
     * @return SHA1 digest
     */
    private static synchronized MessageDigest getSHA1()
    {
    	if (SHA1 == null)
    	{
    		try
    		{
    			SHA1 = MessageDigest.getInstance("SHA-1");
			}
    		catch (NoSuchAlgorithmException e)
    		{
    			// Fatal error and should never be possible !
    			throw new Error(e);
			}
    	}
    	
    	return SHA1;
    }
    
    /**
     * Build the salted password we need to return to SE4.
     * @param salt Password salt we've previously been sent by SE4 on a REPLY_INIT message.
     * @param password Server password.
     * @return salted and SHA1 digested password reply
     */
    public static byte[] buildSaltedPassword(final byte[] salt, final String password)
    {
    	final ArrayList<Byte> salted = new ArrayList<Byte>();
    	for (final byte b : salt)
    		salted.add(b);
    	
    	for (int i = 0; i < password.length(); i++)
    	{
    		char c = password.charAt(i);
    		if (c < 128)
        		salted.add((byte)c);
    		else if (c < 2048)
    		{
        		salted.add((byte)(192 | c >> 6));
        		salted.add((byte)(128 | 63 & c));
    		}
    		else if (c < 55296 || c >= 57344)
    		{
        		salted.add((byte)(224 | c >> 12));
        		salted.add((byte)(128 | c >> 6 & 63));
        		salted.add((byte)(128 | 63 & c));
    		}
    		else
    		{
    			c = (char) (65536 + ((1023 & c) << 10 | 1023 & password.charAt(++i)));
        		salted.add((byte)(240 | c >> 18));
        		salted.add((byte)(128 | c >> 12 & 63));
        		salted.add((byte)(128 | c >> 6 & 63));
        		salted.add((byte)(128 | 63 & c));
    		}
    	}
    	
    	final byte[] tmp = new byte[salted.size()];
    	for (int i = 0; i < salted.size(); i++)
    		tmp[i] = salted.get(i);
    	
    	final byte[] result = getSHA1().digest(tmp);
    	return result;
    }
    
    /**
     * Get unsigned int value from given offset in buffer.
     * @param buf buffer we're interpreting
     * @param offset offset to read from
     * @return uint value
     */
    public static long getUInt32(final ByteBuffer buf, int offset)
    {
    	byte[] bytes = buf.array();
	    long value = 
	        ((bytes[offset + 0] & 0xFF) <<  0) |
	        ((bytes[offset + 1] & 0xFF) <<  8) |
	        ((bytes[offset + 2] & 0xFF) << 16) |
	        ((bytes[offset + 3] & 0xFF) << 24);
	    return value;
    }
    
    /**
     * Encode a 32 bit unsigned as 4 bytes.
     * @param val Value to encode.
     * @return Bytes
     */
    public static byte[] uInt32ToBytes(final long val)
    {
		final byte[] result = new byte[4];
		result[0] = (byte)(val & 0xff);
		result[1] = (byte)((val >> 8) & 0xff);
		result[2] = (byte)((val >> 16) & 0xff);
		result[3] = (byte)((val >> 24) & 0xff);
		return result;
    }
    
    /**
     * Get floating point value from given offset in buffer.
     * @param buf buffer we're interpreting
     * @param offset offset to read from
     * @return float value
     */
    public static float getFloat(final ByteBuffer buf, int offset)
    {
    	byte[] floatBytes = new byte[4];
    	for (int i = 0; i < floatBytes.length; i++)
    		floatBytes[i] = buf.array()[i + offset];
    	
    	return ByteBuffer.wrap(floatBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
    
    /**
     * Interpret the supplied buffer as a UTF-8 encoded string and return the result.
     * @param buf buffer to interpret.
     * @return String
     */
    public static String payloadAsUTF8String(final ByteBuffer buf)
    {
		try
		{
			return new String(buf.array(), "UTF-8");
		}
		catch (final UnsupportedEncodingException e)
		{
			// Fatal error and should never be possible !
			throw new Error(e);
		}

    }
/*    
	public static void main(String[] args) throws Exception
	{
		final String fromWireshark = "f1e00000000025c91d85aaed704fcd6cbe09e8939f92b1b0594c1c1b7db5";
		final byte[] msg = hexStringToByteArray(fromWireshark);
//		System.out.println(fromWireshark);
//		System.out.println(bytesToString(msg));
		
		final ReplyMessage reply = getReplyMessage(msg);
		if (reply.messageId != Protocol.REPLY_INIT)
			throw new Exception("unexpected messge id: " + (int)reply.messageId);
		System.out.println("payload=" + bytesToString(reply.payload.array()));
		
		final byte[] salt = getSaltValue(reply.payload);
		System.out.println("salt=" + bytesToString(salt));
		final byte[] salted = buildSaltedPassword(salt, "wibble");
		final ByteBuffer toSend = buildMessage(Protocol.REQUEST_SEND_PWD, salted);
		System.out.println(bytesToString(toSend.array()));
		System.out.println("f2e0386e53e0f11247b4075bb0721a3d33990f95e314");
	}
*/	   
/*    
	public static void main(String[] args) throws Exception
	{
		final String tmp = "ee0500000b0800000dca6f42";
		final ByteBuffer b = ByteBuffer.wrap(hexStringToByteArray(tmp));
		
		long sent = getUInt32(b, 0);
		long received = getUInt32(b, 4);
		float fps = getFloat(b, 8);
		
		
		System.out.println("sent = " + sent);
		System.out.println("received = " + received);
		System.out.println("fps= " + fps);
		
		final byte[] test = new byte[4];
		test[0] = (byte)(received & 0xff);
		test[1] = (byte)((received >> 8) & 0xff);
		test[2] = (byte)((received >> 16) & 0xff);
		test[3] = (byte)((received >> 24) & 0xff);
		System.out.println("backToByete = " + bytesToString(test));
	}
*/	
}
