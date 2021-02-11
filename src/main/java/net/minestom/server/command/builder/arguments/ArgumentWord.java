package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.validate.Check;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Represents a single word in the command.
 * <p>
 * You can specify the valid words with {@link #from(String...)} (do not abuse it or the client will not be able to join).
 * <p>
 * Example: hey
 */
public class ArgumentWord extends Argument<String> {

    public static final int SPACE_ERROR = 1;
    public static final int RESTRICTION_ERROR = 2;

    protected String[] restrictions;

    public ArgumentWord(String id) {
        super(id);
    }

    /**
     * Used to force the use of a few precise words instead of complete freedom.
     * <p>
     * WARNING: having an array too long would result in a packet too big or the client being stuck during login.
     *
     * @param restrictions the accepted words,
     *                     can be null but if an array is passed
     *                     you need to ensure that it is filled with non-null values
     * @return 'this' for chaining
     * @throws NullPointerException if {@code restrictions} is not null but contains null value(s)
     */
    @NotNull
    public ArgumentWord from(@Nullable String... restrictions) {
        if (restrictions != null) {
            for (String restriction : restrictions) {
                Check.notNull(restriction,
                        "ArgumentWord restriction cannot be null, you can pass 'null' instead of an empty array");
            }
        }

        this.restrictions = restrictions;
        return this;
    }

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Word cannot contain space character", input, SPACE_ERROR);

        // Check restrictions (acting as literal)
        if (hasRestrictions()) {
            for (String r : restrictions) {
                if (input.equalsIgnoreCase(r))
                    return input;
            }
            throw new ArgumentSyntaxException("Word needs to be in the restriction list", input, RESTRICTION_ERROR);
        }

        return input;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {

        // Add the single word properties + parser
        final Consumer<DeclareCommandsPacket.Node> wordConsumer = node -> {
            node.parser = "brigadier:string";
            node.properties = packetWriter -> {
                packetWriter.writeVarInt(0); // Single word
            };
        };

        final boolean hasRestriction = this.hasRestrictions();
        if (hasRestriction) {

            // Create a primitive array for mapping
            DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[this.getRestrictions().length];

            // Create a node for each restrictions as literal
            for (int i = 0; i < nodes.length; i++) {
                DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();

                argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                        executable, false, false);
                argumentNode.name = this.getRestrictions()[i];
                wordConsumer.accept(argumentNode);
                nodes[i] = argumentNode;

            }
            nodeMaker.setCurrentNodes(nodes);
        } else {
            // Can be any word, add only one argument node
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
            wordConsumer.accept(argumentNode);
            nodeMaker.setCurrentNodes(new DeclareCommandsPacket.Node[]{argumentNode});
        }
    }

    /**
     * Gets if this argument allow complete freedom in the word choice or if a list has been defined.
     *
     * @return true if the word selection is restricted
     */
    public boolean hasRestrictions() {
        return restrictions != null && restrictions.length > 0;
    }

    /**
     * Gets all the word restrictions.
     *
     * @return the word restrictions, can be null
     */
    @Nullable
    public String[] getRestrictions() {
        return restrictions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgumentWord that = (ArgumentWord) o;
        return Arrays.equals(restrictions, that.restrictions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(restrictions);
    }
}
