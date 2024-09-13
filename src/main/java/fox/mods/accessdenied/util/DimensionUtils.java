package fox.mods.accessdenied.util;

import fox.mods.accessdenied.configuration.ADFileConfiguration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class DimensionUtils {
    public static Pair<String, String> getDimensionNamespaceAndPath(String dimension) {
        if (dimension == null || dimension.isEmpty()) {
            return Pair.of("", "");
        }
        String[] parts = dimension.split(":");
        return Pair.of(parts[0], parts[1]);
    }
    public static List<Pair<String, String>> getDisabledDimensions() {
        List<? extends String> dimensions = ADFileConfiguration.getDimensionsDisabled();
        return dimensions.stream()
                .map(dim -> {
                    String[] parts = dim.split(":");
                    return Pair.of(parts[0], parts[1]);
                })
                .collect(Collectors.toList());
    }
}




