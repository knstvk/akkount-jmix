package akkount.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class CurrencyRatesService {

    private static final String API_URL_TEMPLATE =
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/%s.json";

    private final Cache<String, Optional<Map<String, BigDecimal>>> ratesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public Optional<Map<String, BigDecimal>> getRates(String baseCode) {
        String normalizedBase = baseCode.toLowerCase();
        try {
            return ratesCache.get(normalizedBase, () -> fetchRates(normalizedBase));
        } catch (ExecutionException e) {
            return Optional.empty();
        }
    }

    private Optional<Map<String, BigDecimal>> fetchRates(String baseCode) {
        String url = String.format(API_URL_TEMPLATE, baseCode);
        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(response);
            JsonNode ratesNode = root.get(baseCode);
            if (ratesNode == null || !ratesNode.isObject()) {
                return Optional.empty();
            }

            Map<String, BigDecimal> rates = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = ratesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getValue().isNumber()) {
                    rates.put(entry.getKey().toLowerCase(), new BigDecimal(entry.getValue().asText()));
                }
            }
            return rates.isEmpty() ? Optional.empty() : Optional.of(rates);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
