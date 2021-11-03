package tfa.se4.iplookup.ipstack;

import org.apache.http.client.fluent.Request;
import tfa.se4.iplookup.IPInformation;
import tfa.se4.iplookup.IPLookupInterface;
import tfa.se4.iplookup.ipstack.json.IPStack;
import tfa.se4.logger.LoggerInterface;
import tfa.se4.iplookup.ipstack.json.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holder class for anything we do to talk to IPStack.
 */
public final class IPStackAPI implements IPLookupInterface
{
    /** IPStack API key. */
    private final String m_APIkey;

    /** Lookup cache to reduce hits to IP stack. */
    private static final Map<String, IPInformation> s_cache = new HashMap<>();

    /**
     * Initialise and remember steam API key.
     *
     * @param apiKey Steam API key.
     */
    public IPStackAPI(final String apiKey)
    {
        m_APIkey = apiKey;
    }

    @Override
    public IPInformation getIPAddressInformation(final String ip, final LoggerInterface logger)
    {
        if (s_cache.get(ip) != null)
        {
            return s_cache.get(ip);

        }
        logger.log(LoggerInterface.LogLevel.DEBUG, LoggerInterface.LogType.IPINFO, "Fetching IP address information for %s", ip);
        final String urlTemplate = "http://api.ipstack.com/##IP##?access_key=##API_KEY##";

        final String url = urlTemplate
                .replace("##IP##", ip)
                .replace("##API_KEY##", m_APIkey);

        try
        {
            final IPStack result = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            final IPInformation info = IPInformation.
                    create().
                    setIpAddress(ip).
                    setCity(result.getCity()).
                    setCountry(result.getCountryName()).
                    setRegion(result.getRegionName()).
                    setLatitude(result.getLatitude().toString()).
                    setLongitude(result.getLongitude().toString());
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

