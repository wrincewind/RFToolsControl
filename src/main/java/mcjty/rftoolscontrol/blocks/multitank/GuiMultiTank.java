package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalAlignment;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.PacketGetTankFluids;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;

import static mcjty.rftoolscontrol.blocks.multitank.MultiTankTileEntity.TANKS;

public class GuiMultiTank extends GenericGuiContainer<MultiTankTileEntity, EmptyContainer> {

    public static final int WIDTH = 180;
    public static final int HEIGHT = 87;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsControl.MODID, "textures/gui/tank.png");
    private int listDirty = 0;

    private BlockRender liquids[] = new BlockRender[TANKS];
    private Label labels[] = new Label[TANKS];

    public GuiMultiTank(MultiTankTileEntity te, EmptyContainer container, PlayerInventory inventory) {
        super(RFToolsControl.instance, te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/0, "tank");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setLayout(new PositionalLayout()).setBackground(iconLocation);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        for (int i = 0 ; i < TANKS ; i++) {
            liquids[i] = new BlockRender(minecraft, this)
                    .setLayoutHint(new PositionalLayout.PositionalHint(10, 9 + i * 18, 16, 16));
            toplevel.addChild(liquids[i]);
            labels[i] = new Label(minecraft, this);
            labels[i]
                    .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .setVerticalAlignment(VerticalAlignment.ALIGN_CENTER)
                    .setLayoutHint(new PositionalLayout.PositionalHint(32, 9 + i * 18, WIDTH-32-6, 16));
            toplevel.addChild(labels[i]);
        }

        window = new Window(this, toplevel);
    }

    private void requestLists() {
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetTankFluids(tileEntity.getPos()));
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestLists();
            listDirty = 10;
        }
    }
    private void updateLiquids() {
        requestListsIfNeeded();
        MultiTankFluidProperties[] properties = tileEntity.getProperties();
        for (int i = 0 ; i < TANKS ; i++) {
            if (i < properties.length && properties[i] != null) {
                FluidStack stack = properties[i].getContents();
                if (stack != null) {
                    liquids[i].setRenderItem(stack);
                    labels[i].setText(stack.getDisplayName().getFormattedText() + " (" + stack.getAmount() + "mb)");
                    continue;
                }
            }
            liquids[i].setRenderItem(null);
            labels[i].setText("");
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        updateLiquids();
        drawWindow();
    }
}
