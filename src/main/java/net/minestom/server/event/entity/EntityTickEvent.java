package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity ticks itself.
 * Same event instance used for all tick events for the same entity.
 */
public class EntityTickEvent extends Event implements EntityEvent {

    private final Entity entity;

    public EntityTickEvent(@NotNull Entity entity) {
        this.entity = entity;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }
}
