package io.github.wouink.furnish.block;

import io.github.wouink.furnish.block.util.PlacementHelper;
import io.github.wouink.furnish.block.util.VoxelShapeHelper;
import io.github.wouink.furnish.setup.FurnishBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class Shutter extends HorizontalBlock {
	private static final VoxelShape[] SHUTTER_CLOSED = VoxelShapeHelper.getRotatedShapes(Block.box(0, 0, 0, 2, 16, 16));
	private static final VoxelShape[] SHUTTER_HALF_OPENED = VoxelShapeHelper.getRotatedShapes(Block.box(0, 0, 14, 16, 16, 16));
	private static final VoxelShape[] SHUTTER_HALF_OPENED_R = VoxelShapeHelper.getRotatedShapes(Block.box(0, 0, 0, 16, 16, 2));
	private static final VoxelShape[] SHUTTER_OPENED = VoxelShapeHelper.getRotatedShapes(Block.box(0, 0, 14, 2, 16, 30));
	private static final VoxelShape[] SHUTTER_OPENED_R = VoxelShapeHelper.getRotatedShapes(Block.box(0, 0, -14, 2, 16, 2));

	private static final VoxelShape[] INTERACT_HALF = VoxelShapeHelper.getMergedShapes(SHUTTER_CLOSED, SHUTTER_HALF_OPENED);
	private static final VoxelShape[] INTERACT_HALF_R = VoxelShapeHelper.getMergedShapes(SHUTTER_CLOSED, SHUTTER_HALF_OPENED_R);
	private static final VoxelShape[] INTERACT_OPEN = VoxelShapeHelper.getMergedShapes(SHUTTER_CLOSED, SHUTTER_OPENED);
	private static final VoxelShape[] INTERACT_OPEN_R = VoxelShapeHelper.getMergedShapes(SHUTTER_CLOSED, SHUTTER_OPENED_R);

	public enum State implements IStringSerializable {
		CLOSED("closed"),
		HALF_OPEN("half_open"),
		OPEN("open");

		private final String name;
		private State(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final BooleanProperty RIGHT = FurnishBlocks.CustomProperties.RIGHT;
	public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

	public Shutter(Properties p) {
		super(p.noOcclusion());
		registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(STATE, State.CLOSED).setValue(RIGHT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, STATE, RIGHT);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(RIGHT, PlacementHelper.placeRight(ctx));
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
		if(world.setBlock(pos, state.cycle(STATE), 3)) {
			world.playSound(playerEntity, pos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
			// update shutters in the same column
			boolean rightProp = world.getBlockState(pos).getValue(RIGHT).booleanValue();
			BlockPos scan = pos.below();
			while(world.getBlockState(scan).getBlock() == this && world.getBlockState(scan).getValue(RIGHT) == rightProp) {
				world.setBlock(scan, world.getBlockState(pos), 3);
				scan = scan.below();
			}
			scan = pos.above();
			while(world.getBlockState(scan).getBlock() == this && world.getBlockState(scan).getValue(RIGHT) == rightProp) {
				world.setBlock(scan, world.getBlockState(pos), 3);
				scan = scan.above();
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
		int index = state.getValue(FACING).ordinal() - 2;
		if(state.getValue(STATE) == State.HALF_OPEN) {
			return state.getValue(RIGHT) ? INTERACT_HALF_R[index] : INTERACT_HALF[index];
		} else if(state.getValue(STATE) == State.OPEN) {
			return state.getValue(RIGHT) ? INTERACT_OPEN_R[index] : INTERACT_OPEN[index];
		} else {
			return SHUTTER_CLOSED[index];
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
		int index = state.getValue(FACING).ordinal() - 2;
		if(state.getValue(STATE) == State.HALF_OPEN) {
			return state.getValue(RIGHT) ? SHUTTER_HALF_OPENED_R[index] : SHUTTER_HALF_OPENED[index];
		} else if(state.getValue(STATE) == State.OPEN) {
			return state.getValue(RIGHT) ? SHUTTER_OPENED_R[index] : SHUTTER_OPENED[index];
		} else {
			return SHUTTER_CLOSED[index];
		}
	}

	// don't connect to fences
	@Override
	public VoxelShape getBlockSupportShape(BlockState p_230335_1_, IBlockReader p_230335_2_, BlockPos p_230335_3_) {
		return VoxelShapes.empty();
	}
}
