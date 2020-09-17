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
 * Holder class for anything we use to talk to IPStack.
 */
public final class IPStackAPI
{
    private String m_APIkey;

    private static Map<String, IPStack> s_cache = new HashMap<>();
    /**
     * Initialise and remember steam API key.
     * @param apiKey Steam API key.
     */
    public IPStackAPI(final String apiKey)
    {
        m_APIkey = apiKey;
    }

    /**
     * Get IP information.
     * @param ip IP address
     * @param logger Logger for catching errors
     * @return IP address information.
     */
    public IPStack getIPAddressInfo(final String ip, final LoggerInterface logger)
    {
        if (s_cache.get(ip) != null)
        {
            return s_cache.get(ip);

        }
        logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.IPSTACK, "Fetching IP address information for %s", ip);
        final String urltemplate = "http://api.ipstack.com/##IP##?access_key=##API_KEY##";

        final String url = urltemplate
                .replace("##IP##", ip)
                .replace("##API_KEY##", m_APIkey);

        try
        {
            final IPStack result = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            s_cache.put(ip, result);
            return result;
        }
        catch (IOException e)
        {
            logger.log(LoggerInterface.LogLevel.ERROR, LoggerInterface.LogType.IPSTACK, e,"Unable to fetch IP info for %s", ip);
        }
        return null;
    }

    /**
     * Get location in form City, State, Country
     * @param ip IP address
     * @param logger Logger for issues
     * @return String
     */
    public String getLocation(final String ip, final LoggerInterface logger)
    {
        IPStack ipInfo = getIPAddressInfo(ip, logger);
        if (ipInfo != null) {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(ipInfo.getCity())) {
                sb.append(ipInfo.getCity());
                sb.append(',');
            }
            if (StringUtils.isNotBlank(ipInfo.getRegionName())) {
                sb.append(ipInfo.getRegionName());
                sb.append(',');
            }
            sb.append(ipInfo.getCountryName());
            return sb.toString();
        }
        return "N/A";
    }
}

