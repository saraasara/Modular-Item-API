package smartin.miapi.blueprint;

import com.redpxnda.nucleus.facet.FacetKey;
import net.minecraft.util.Identifier;

public class PlayerBlueprintFacet extends FacetKey<PlayerBoundBlueprint> {
    //TODO:player+item bound blueprint storage
    protected PlayerBlueprintFacet(Identifier id, Class<PlayerBoundBlueprint> cls) {
        super(id, cls);
    }
}