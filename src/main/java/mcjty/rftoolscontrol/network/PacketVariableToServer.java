package mcjty.rftoolscontrol.network;


import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.ParameterTypeTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketVariableToServer {
    private BlockPos pos;
    private int varIndex;
    private CompoundNBT tagCompound;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(varIndex);
        buf.writeCompoundTag(tagCompound);
    }

    public PacketVariableToServer() {
    }

    public PacketVariableToServer(PacketBuffer buf) {
        pos = buf.readBlockPos();
        varIndex = buf.readInt();
        tagCompound = buf.readCompoundTag();
    }

    public PacketVariableToServer(BlockPos pos, int varIndex, CompoundNBT tagCompound) {
        this.pos = pos;
        this.varIndex = varIndex;
        this.tagCompound = tagCompound;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity playerEntity = ctx.getSender();
            TileEntity te = playerEntity.getEntityWorld().getTileEntity(pos);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                Parameter[] variables = processor.getVariableArray();
                if (varIndex < variables.length) {
                    Parameter parameter = variables[varIndex];
                    ParameterType type = parameter.getParameterType();
                    ParameterValue value = ParameterTypeTools.readFromNBT(tagCompound, type);
                    // Here we don't want to trigger the watch
                    variables[varIndex] = Parameter.builder()
                            .type(type)
                            .value(value)
                            .build();
                    processor.markDirty();
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}