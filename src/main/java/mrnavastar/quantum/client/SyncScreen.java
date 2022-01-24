package mrnavastar.quantum.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class SyncScreen extends Screen {

    private final String message;

    public SyncScreen(String message) {
        super(new LiteralText("SyncScreen"));
        this.message = message;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        SyncScreen.drawCenteredText(matrices, this.textRenderer, new LiteralText("Syncing..."), this.width / 2, this.height / 2, 0xAAAAAA);
        SyncScreen.drawCenteredText(matrices, this.textRenderer, new LiteralText(message), this.width / 2, this.height / 2 + 12, 0xAAAAAA);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
