package io.adabox.dextreme.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import rest.koios.client.backend.api.asset.AssetService;
import rest.koios.client.backend.api.asset.model.AssetTokenRegistry;
import rest.koios.client.backend.api.base.Result;
import rest.koios.client.backend.api.base.exception.ApiException;
import rest.koios.client.backend.factory.BackendFactory;
import rest.koios.client.backend.factory.options.Offset;
import rest.koios.client.backend.factory.options.Options;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class TokenRegistry {

    private static TokenRegistry instance = null;
    private final AssetService assetService = BackendFactory.getKoiosMainnetService().getAssetService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public Map<String, AssetTokenRegistry> tokensRegistryMap = new HashMap<>();
    public Set<String> verifiedPolicies = new HashSet<>();

    public static synchronized TokenRegistry getInstance() {
        if (instance == null)
            instance = new TokenRegistry();

        return instance;
    }

    public void updateTokens() {
        try {
            log.info("Fetching Token Registry List.");
            tokensRegistryMap = fetchTokenRegistryList();
            log.info("Updated Token Registry List. Size: {}", tokensRegistryMap.size());
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            loadRegistryMapFromResourceFile().ifPresent(stringAssetTokenRegistryMap -> tokensRegistryMap = stringAssetTokenRegistryMap);
        }
        log.info("Fetching Verified Tokens List.");
        verifiedPolicies = fetchVerifiedPolicies();
        log.info("Updated Verified Tokens List. Size: {}", verifiedPolicies.size());
    }

    private Set<String> fetchVerifiedPolicies() {
        Set<String> set = new HashSet<>();
        try {
            objectMapper.readTree(URI.create("https://raw.githubusercontent.com/minswap/verified-tokens/main/tokens.json").toURL())
                    .fieldNames()
                    .forEachRemaining(set::add);
            return set;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    private Map<String, AssetTokenRegistry> fetchTokenRegistryList() throws ApiException {
        List<AssetTokenRegistry> result = new ArrayList<>();
        long page = 0;
        long count = 1000;
        List<AssetTokenRegistry> assetTokenRegistryList = new ArrayList<>();
        do {
            Options options = Options.builder().option(Offset.of(count * page++)).build();
            Result<List<AssetTokenRegistry>> assetTokenRegistryListResult = assetService.getAssetTokenRegistry(options);
            if (assetTokenRegistryListResult.isSuccessful()) {
                assetTokenRegistryList = assetTokenRegistryListResult.getValue();
                result.addAll(assetTokenRegistryList);
            }
        } while (!CollectionUtils.isEmpty(assetTokenRegistryList) && assetTokenRegistryList.size() == count);
        return result.stream().collect(
                Collectors.toMap(
                        assetTokenRegistry -> assetTokenRegistry.getPolicyId() + assetTokenRegistry.getAssetName(),
                        assetTokenRegistry -> assetTokenRegistry,
                        (element1, element2) -> element1));
    }

    private Optional<Map<String, AssetTokenRegistry>> loadRegistryMapFromResourceFile() {
        try {
            File file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("tokenRegistry.json")).getFile());
            List<AssetTokenRegistry> assetTokenRegistries = objectMapper.readValue(file, new TypeReference<>() {});
            return Optional.of(assetTokenRegistries.stream().collect(
                    Collectors.toMap(assetTokenRegistry -> assetTokenRegistry.getPolicyId() + assetTokenRegistry.getAssetName(),
                            assetTokenRegistry -> assetTokenRegistry, (element1, element2) -> element1)));
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }
}
