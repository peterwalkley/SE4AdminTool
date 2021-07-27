package tfa.se4.ipstack;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import tfa.se4.ipstack.json.IPStack;
import tfa.se4.logger.LoggerInterface;
import tfa.se4.ipstack.json.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holder class for anything we do to talk to IPStack.
 */
public final class IPStackAPI
{
    /** IPStack API key. */
    private final String m_APIkey;

    /** Lookup cache to reduce hits to IP stack. */
    private static final Map<String, IPStack> s_cache = new HashMap<>();

    /**
     * Initialise and remember steam API key.
     *
     * @param apiKey Steam API key.
     */
    public IPStackAPI(final String apiKey)
    {
        m_APIkey = apiKey;
    }

    /**
     * Get IP information.
     *
     * @param ip     IP address
     * @param logger Logger for catching errors
     * @return IP address information.
     */
    public IPStack getIPAddressInfo(final String ip, final LoggerInterface logger)
    {
        if (s_cache.get(ip) != null)
        {
            return s_cache.get(ip);

        }
        logger.log(LoggerInterface.LogLevel.DEBUG, LoggerInterface.LogType.IPSTACK, "Fetching IP address information for %s", ip);
        final String urlTemplate = "http://api.ipstack.com/##IP##?access_key=##API_KEY##";

        final String url = urlTemplate
                .replace("##IP##", ip)
                .replace("##API_KEY##", m_APIkey);

        try
        {
            final IPStack result = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            s_cache.put(ip, result);
            logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.IPSTACK, "IP location for %s is %s", ip, toLocation(result));
            return result;
        }
        catch (final IOException e)
        {
            logger.log(LoggerInterface.LogLevel.ERROR, LoggerInterface.LogType.IPSTACK, e, "Unable to fetch IP info for %s", ip);
        }
        return null;
    }

    /**
     * Get location in form City, State, Country
     *
     * @param ip     IP address
     * @param logger Logger for issues
     * @return String
     */
    public String getLocation(final String ip, final LoggerInterface logger)
    {
        return toLocation(getIPAddressInfo(ip, logger));
    }

    /**
     * Get location information as a string.
     * @param ipInfo IP info returned from IPStack
     * @return City, State, Country as a string
     */
    private String toLocation(final IPStack ipInfo)
    {
        if (ipInfo == null)
        {
            return "N/A";
        }

        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(ipInfo.getCity()))
        {
            sb.append(ipInfo.getCity());
            sb.append(',');
        }
        if (StringUtils.isNotBlank(ipInfo.getRegionName()))
        {
            sb.append(ipInfo.getRegionName());
            sb.append(',');
        }
        sb.append(ipInfo.getCountryName());
        return sb.toString();
    }
}

