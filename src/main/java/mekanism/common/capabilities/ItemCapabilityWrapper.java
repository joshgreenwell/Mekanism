package mekanism.common.capabilities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class ItemCapabilityWrapper implements ICapabilityProvider {

    protected final ItemStack itemStack;
    private List<ItemCapability> capabilities = new ArrayList<>();

    public ItemCapabilityWrapper(ItemStack stack, ItemCapability... caps) {
        itemStack = stack;
        for (ItemCapability c : caps) {
            c.wrapper = this;
            c.init();
            capabilities.add(c);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //Note: The capability can technically be null if it is for a mod that is not loaded and another mod
        // tries to check if we have it, so we just safety check to ensure that it is not so we don't have
        // issues when caching our lazy optionals
        if (capability != null && !itemStack.isEmpty()) {
            //Only provide capabilities if we are not empty
            for (ItemCapability cap : capabilities) {
                if (cap.canProcess(capability)) {
                    //Make sure that we load any data the cap needs from the stack, as it doesn't have any NBT set when it is initially initialized
                    // This also allows us to update to any direct changes on the NBT of the stack that someone may have made
                    cap.load();
                    return cap.getMatchingCapability(capability);
                }
            }
        }
        return LazyOptional.empty();
    }

    public static abstract class ItemCapability {

        private final CapabilityCache capabilityCache = new CapabilityCache();
        private ItemCapabilityWrapper wrapper;

        protected ItemCapability() {
            addCapabilityResolvers(capabilityCache);
        }

        protected abstract void addCapabilityResolvers(CapabilityCache capabilityCache);

        public boolean canProcess(Capability<?> capability) {
            return capabilityCache.canResolve(capability);
        }

        protected void init() {
        }

        protected void load() {
        }

        public ItemStack getStack() {
            return wrapper.itemStack;
        }

        /**
         * Note: it is expected that canProcess is called before this
         */
        public <T> LazyOptional<T> getMatchingCapability(@Nonnull Capability<T> capability) {
            return capabilityCache.getCapabilityUnchecked(capability, null);
        }
    }
}