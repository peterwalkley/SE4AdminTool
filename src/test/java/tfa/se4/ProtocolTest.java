package tfa.se4;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Unit tests for Protocol class - Example of missing test coverage
 */
public class ProtocolTest {

    @Test
    public void testGetReplyMessageWithPayload() {
        // Arrange
        byte[] testData = {0x01, 0x02, 0x48, 0x65, 0x6C, 0x6C, 0x6F}; // Message ID + "Hello"
        
        // Act
        Protocol.ReplyMessage result = Protocol.getReplyMessage(testData);
        
        // Assert
        assertEquals(513, (int)result.messageId); // 0x0201 in little endian
        assertEquals("Hello", Protocol.payloadAsUTF8String(result.payload));
    }

    @Test
    public void testGetReplyMessageWithoutPayload() {
        // Arrange
        byte[] testData = {0x01, 0x02};
        
        // Act
        Protocol.ReplyMessage result = Protocol.getReplyMessage(testData);
        
        // Assert
        assertEquals(513, (int)result.messageId);
        assertEquals(0, result.payload.array().length);
    }

    @Test
    public void testHexStringToByteArray() {
        // Arrange
        String hexString = "48656c6c6f";
        
        // Act
        byte[] result = Protocol.hexStringToByteArray(hexString);
        
        // Assert
        assertArrayEquals(new byte[]{0x48, 0x65, 0x6c, 0x6c, 0x6f}, result);
        assertEquals("Hello", new String(result));
    }

    @Test
    public void testBytesToString() {
        // Arrange
        byte[] bytes = {0x48, 0x65, 0x6c, 0x6c, 0x6f};
        
        // Act
        String result = Protocol.bytesToString(bytes);
        
        // Assert
        assertEquals("48656c6c6f", result);
    }

    @Test
    public void testBuildMessageWithPayload() {
        // Arrange
        char messageId = 0x1234;
        byte[] payload = "test".getBytes();
        
        // Act
        ByteBuffer result = Protocol.buildMessage(messageId, payload);
        
        // Assert
        assertEquals(6, result.array().length); // 2 bytes for ID + 4 bytes for "test"
        
        // Verify message ID is correctly encoded in little endian
        ByteBuffer idBuffer = ByteBuffer.wrap(result.array(), 0, 2).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(messageId, idBuffer.getChar());
        
        // Verify payload
        byte[] actualPayload = new byte[4];
        System.arraycopy(result.array(), 2, actualPayload, 0, 4);
        assertArrayEquals(payload, actualPayload);
    }

    @Test
    public void testBuildSaltedPassword() {
        // Arrange
        byte[] salt = {0x01, 0x02, 0x03, 0x04};
        String password = "testpass";
        
        // Act
        byte[] result = Protocol.buildSaltedPassword(salt, password);
        
        // Assert
        assertNotNull(result);
        assertEquals(20, result.length); // SHA-1 produces 20 bytes
        
        // Verify deterministic - same input should produce same output
        byte[] result2 = Protocol.buildSaltedPassword(salt, password);
        assertArrayEquals(result, result2);
    }
}