package io.adabox.dextreme.dex.api;

import io.adabox.dextreme.dex.Minswap;
import io.adabox.dextreme.dex.base.Dex;
import io.adabox.dextreme.model.Asset;
import io.adabox.dextreme.model.LiquidityPool;
import io.adabox.dextreme.model.Ohlcv;
import io.adabox.dextreme.model.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.adabox.dextreme.model.AssetType.*;

public class MinSwapApiTest {
    private final Dex minswap = new Minswap();

    @Test
    public void minswapGetAllLiquidityPools() {
        Map<String, LiquidityPool> liquidityPoolList = minswap.getLiquidityPoolMap();
        System.out.println(liquidityPoolList);
    }

    @Test
    public void minswapGetLiquidityPoolByTokenPairs() {
        Asset assetA = ADA.getAsset();
        Asset assetB = iBTC.getAsset();
        List<LiquidityPool> liquidityPoolList = minswap.getLiquidityPools(assetA, assetB);
        System.out.println(liquidityPoolList);
    }

    @Test
    public void minswapTokens() {
        List<Token> tokens = minswap.getTokens(true).values().stream().toList();
        System.out.println(tokens);
    }

    @Test
    public void minswapGeroPriceChart() {
        Asset assetA = ADA.getAsset();
        Asset assetB = GERO.getAsset();
        long currentTime = System.currentTimeMillis();
        long timeFrom = currentTime - (7 * 1000 * 60 * 60 * 24);
        List<Ohlcv> ohlcvChart = minswap.getPriceChart(assetA, assetB, timeFrom);
        Assertions.assertFalse(ohlcvChart.isEmpty());
    }
}
