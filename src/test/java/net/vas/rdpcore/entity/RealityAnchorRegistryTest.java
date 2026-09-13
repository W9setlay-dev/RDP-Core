package net.vas.rdpcore.entity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.nbt.NBTTagCompound;

class RealityAnchorRegistryTest {
    @AfterEach
    void clearRegistry() {
        RealityAnchorRegistry.clear();
    }

    @Test
    void exactMetadataDefinitionOverridesWildcard() {
        RealityAnchorRegistry.register(new RealityAnchorDefinition(
            "example:anchor", -1, 32.0D, 0.5D,
            EnumSet.of(RealityAnchorCapability.REALITY_STABILIZATION)));
        RealityAnchorDefinition exact = new RealityAnchorDefinition(
            "example:anchor", 2, 64.0D, 1.0D,
            EnumSet.of(RealityAnchorCapability.ANOMALY_SUPPRESSION));
        RealityAnchorRegistry.register(exact);

        assertSame(exact, RealityAnchorRegistry.find("example:anchor", 2));
        assertEquals(0.5D, RealityAnchorRegistry.find("example:anchor", 7).getStrength());
    }

    @Test
    void definitionsClampUnsafeValues() {
        RealityAnchorDefinition definition = new RealityAnchorDefinition(
            "example:anchor", -1, -4.0D, 3.0D, null);

        assertEquals(0.0D, definition.getRadius());
        assertEquals(1.0D, definition.getStrength());
        assertTrue(definition.hasCapability(RealityAnchorCapability.REALITY_STABILIZATION));
    }

    @Test
    void influenceUsesConfiguredRadiusAndFallsOffLinearly() {
        RealityAnchorProfiles.register(new RealityAnchorProfile("fortified", 1.0D, 1.0D, null));
        RealityAnchorDefinition definition = new RealityAnchorDefinition("example:anchor", -1, 10.0D, 1.0D,
                EnumSet.of(RealityAnchorCapability.REALITY_STABILIZATION), "fortified");
        RealityAnchor anchor = new RealityAnchor(0, 0, 0, "test", definition);

        assertEquals(1.0D, anchor.getInfluenceAt(0, 0, 0), 0.00001D);
        assertEquals(0.5D, anchor.getInfluenceAt(5, 0, 0), 0.00001D);
        assertEquals(0.0D, anchor.getInfluenceAt(10, 0, 0), 0.00001D);
        assertEquals("fortified", definition.getProfile());
    }

    @Test
    void invalidDefinitionProducesDiagnostic() {
        assertEquals("metadata must be -1 (wildcard) or non-negative",
            RealityAnchorDefinition.validate("example:anchor", -2, 10.0D, 1.0D));
    }

    @Test
    void duplicateDefinitionsAreRejectedAndDiagnosed() {
        RealityAnchorDefinition definition = new RealityAnchorDefinition(
            "example:anchor", -1, 10.0D, 1.0D, null);
        RealityAnchorRegistry.register(definition);
        assertThrows(IllegalArgumentException.class, () -> RealityAnchorRegistry.register(definition));
        assertTrue(RealityAnchorRegistry.getDiagnostics().get(0).contains("duplicate"));
    }

    @Test
    void anchorRuntimeStateSurvivesPersistence() {
        RealityAnchor anchor = new RealityAnchor(3, 4, 5, "world",
            new RealityAnchorDefinition("example:anchor", -1, 12.0D, 0.75D,
                EnumSet.of(RealityAnchorCapability.ANOMALY_SUPPRESSION)));
        anchor.damage(0.2D);
        anchor.tick();
        NBTTagCompound tag = anchor.serializeNBT();
        RealityAnchor restored = new RealityAnchor(0, 0, 0, "");
        restored.deserializeNBT(tag);
        assertEquals(3, restored.getX());
        assertEquals(12.0D, restored.getEffectRadius(), 0.00001D);
        assertEquals(anchor.getDamageFraction(), restored.getDamageFraction(), 0.00001D);
        assertTrue(restored.hasCapability(RealityAnchorCapability.ANOMALY_SUPPRESSION));
        assertEquals(anchor.getAge(), restored.getAge());
    }

    @Test
    void unknownProfileCannotBeResolved() {
        assertThrows(IllegalArgumentException.class, () -> new RealityAnchorDefinition(
            "example:anchor", -1, 10.0D, 1.0D, null, "missing"));
    }
}
