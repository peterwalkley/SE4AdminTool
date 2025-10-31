package tfa.se4.iplookup.ipapi;

import org.apache.http.client.fluent.Request;
import tfa.se4.Utils;
import tfa.se4.iplookup.IPInformation;
import tfa.se4.iplookup.IPLookupInterface;
import tfa.se4.logger.LoggerInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holder class for anything we do to talk to IP API:  https://ip-api.com/
 */
public class IPAPI implements IPLookupInterface {
    /** For rate limit. */
    private static long lastLookup = 0;

    /** Lookup cache to reduce hits to IP stack. */
    private static final Map<String, IPInformation> sCache = new HashMap<>();

    /**
     * Initialise and remember steam API key.
     *
     */
    public IPAPI()
    {
        // Nothing needed
    }

    /**
     * Ensure we don't try and do more than 45 requests per minute.
     */
    private static void applyRateLimit() {

        long diff = System.currentTimeMillis() - lastLookup;
        if (diff < 1500) {
            Utils.sleep(1500 - diff);
        }
        lastLookup = System.currentTimeMillis();
    }

    @Override
    public IPInformation getIPAddressInformation(final String ip, final LoggerInterface logger, final boolean waitForResult)
    {
        if (sCache.get(ip) != null)
            return sCache.get(ip);

        if (!waitForResult) {
            return IPInformation.
                    create().
                    setIpAddress(ip);
        }

        logger.log(LoggerInterface.LogLevel.DEBUG, LoggerInterface.LogType.IPINFO, "Fetching IP address information for %s", ip);
        applyRateLimit();
        if (sCache.get(ip) != null) // check cache again in case a different thread got the answer during the rate limit check
            return sCache.get(ip);

        final String urlTemplate = "http://ip-api.com/json/##IP##";

        final String url = urlTemplate.replace("##IP##", ip);

        try
        {
            final IPAPIResponse result = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            if (result != null && "success".equals(result.getStatus())) {
                final IPInformation info = IPInformation.
                        create().
                        setIpAddress(ip).
                        setCity(result.getCity()).
                        setCountry(result.getCountry()).
                        setRegion(result.getRegion()).
                        setLatitude(result.getLat()).
                        setLongitude(result.getLon());
                sCache.put(ip, info);
                logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.IPINFO, "IP location for %s is %s", ip, info.toString());
                return info;
            }
        }
        catch (final IOException e)
        {
            logger.log(LoggerInterface.LogLevel.ERROR, LoggerInterface.LogType.IPINFO, e, "Unable to fetch IP info for %s", ip);
        }
        return IPInformation.
                create().
                setIpAddress(ip);
    }
}
