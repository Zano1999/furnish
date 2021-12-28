package io.github.wouink.furnish.block;

import io.github.wouink.furnish.block.util.PlacementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;

public class Cabinet extends InventoryFurniture {
	public static final BooleanProperty RIGHT = BooleanProperty.create("right");
	public Cabinet(Properties p, final RegistryObject<SoundEvent> sound) {
		super(p, sound);
		registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(RIGHT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RIGHT);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		return super.getStateForPlacement(ctx).setValue(RIGHT, PlacementHelper.placeRight(ctx));
	}
}
