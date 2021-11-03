package tfa.se4.iplookup.extremeip;

import org.apache.http.client.fluent.Request;
import tfa.se4.iplookup.IPInformation;
import tfa.se4.iplookup.IPLookupInterface;
import tfa.se4.logger.LoggerInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holder class for anything we do to talk to Extreme IP.
 */
public final class ExtremeIPAPI implements IPLookupInterface
{
    /** For rate limit. */
    private static long lastLookup = 0;

    /** IPStack API key. */
    private final String m_APIkey;

    /** Lookup cache to reduce hits to IP stack. */
    private static final Map<String, IPInformation> s_cache = new HashMap<>();

    /**
     * Initialise and remember steam API key.
     *
     * @param apiKey Steam API key.
     */
    public ExtremeIPAPI(final String apiKey)
    {
        m_APIkey = apiKey;
    }

    /**
     * Ensure we don't try and do more than 20 requests per minute.
     */
    private static synchronized void applyRateLimit() {

        long diff = System.currentTimeMillis() - lastLookup;
        if (diff < 3500) {
            try {
                Thread.sleep(3500 - diff);
            }
            catch (final InterruptedException e) {
                // ignore
            }
        }
        lastLookup = System.currentTimeMillis();
    }

    @Override
    public IPInformation getIPAddressInformation(final String ip, final LoggerInterface logger)
    {
        if (s_cache.get(ip) != null)
            return s_cache.get(ip);

        logger.log(LoggerInterface.LogLevel.DEBUG, LoggerInterface.LogType.IPINFO, "Fetching IP address information for %s", ip);
        applyRateLimit();
        if (s_cache.get(ip) != null) // check cache again in case a different thread got the answer during the rate limit check
            return s_cache.get(ip);

        final String urlTemplate = "https://extreme-ip-lookup.com/json/##IP##?key=##API_KEY##";

        final String url = urlTemplate
                .replace("##IP##", ip)
                .replace("##API_KEY##", m_APIkey);

        try
        {
            final ExtremeIPResponse result = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            final IPInformation info = IPInformation.
                    create().
                    setIpAddress(ip).
                    setCity(result.getCity()).
                    setCountry(result.getCountry()).
                    setRegion(result.getRegion()).
                    setLatitude(result.getLat()).
                    setLongitude(result.getLon());
            s_cache.put(ip, info);
            logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.IPINFO, "IP location for %s is %s", ip, info.toString());
            return info;
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
