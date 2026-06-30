package io.github.kimovoid.polished.client.event;

import net.ornithemc.osl.core.api.events.Event;

import java.util.List;
import java.util.function.Consumer;

public class RenderEvents {

    /**
     * Invoked when a side of the modern debug screen is rendered.
     */
    public static final Event<Consumer<DebugRenderEvent>> RENDER_DEBUG = Event.consumer();

    public enum DebugSide {
        LEFT,
        RIGHT;
    }

    public static class DebugRenderEvent {
        private final DebugSide side;
        private final List<String> lines;

        public DebugRenderEvent(DebugSide side, List<String> lines) {
            this.side = side;
            this.lines = lines;
        }

        public DebugSide getSide() {
            return this.side;
        }

        public List<String> getLines() {
            return this.lines;
        }
    }
}
