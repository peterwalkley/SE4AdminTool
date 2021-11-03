package tfa.se4.iplookup;

import tfa.se4.logger.LoggerInterface;

public interface IPLookupInterface
{
    /**
     * Get IP address information.
     *
     * @param ipAddress IP address
     * @param logger Logger for catching errors
     * @return IP address information.
     */
    public IPInformation getIPAddressInformation(String ipAddress, LoggerInterface logger);
}
