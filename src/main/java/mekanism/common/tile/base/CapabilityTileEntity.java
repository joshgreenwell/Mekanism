package mekanism.common.tile.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.IToggleableCapability;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class CapabilityTileEntity extends TileEntityUpdateable implements IToggleableCapability {

    private final CapabilityCache capabilityCache = new CapabilityCache();

    public CapabilityTileEntity(TileEntityType<?> type) {
        super(type);
    }

    protected final void addCapabilityResolver(ICapabilityResolver resolver) {
        capabilityCache.addCapabilityResolver(resolver);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Do not override this method if you are implementing {@link IToggleableCapability}, instead override {@link #getCapabilityIfEnabled(Capability,
     * Direction)}, calling this method is fine.
     */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //Due to TileEntity implementing ICapabilityProvider we have to manually copy the logic from IToggleableCapability
        // that reroutes getCapability to check if it is disabled and otherwise use getCapabilityIfEnabled
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        } else if (capabilityCache.canResolve(capability)) {
            return capabilityCache.getCapabilityUnchecked(capability, side);
        }
        return getCapabilityIfEnabled(capability, side);
        //Call to the TileEntity's Implementation of getCapability if we could not find a capability ourselves
        //return super.getCapability(capability, side);
    }

    //TODO: View usages and move them to our capability cache
    @Nonnull
    @Override
    @Deprecated
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //Call to the TileEntity's Implementation of getCapability if we could not find a capability ourselves
        return super.getCapability(capability, side);
    }
}