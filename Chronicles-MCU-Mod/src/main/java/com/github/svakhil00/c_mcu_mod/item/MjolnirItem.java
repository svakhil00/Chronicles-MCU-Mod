package com.github.svakhil00.c_mcu_mod.item;

import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MjolnirItem extends TieredItem {
	private final float ATTACKDAMAGE, ATTACKSPEED;

	public MjolnirItem(IItemTier tierIn, int attackDamageIn, float attackSpeedIn, Properties builder) {
		super(tierIn, builder);
		ATTACKDAMAGE = (float) attackDamageIn + tierIn.getAttackDamage();
		ATTACKSPEED = attackSpeedIn;
	}

	public float getAttackDamage() {
		return ATTACKDAMAGE;
	}

	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return !player.isCreative();
	}

	public float getDestroySpeed(ItemStack stack, BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.COBWEB) {
			return 15.0F;
		} else {
			Material material = state.getMaterial();
			return material != Material.PLANTS && material != Material.TALL_PLANTS && material != Material.CORAL
					&& !state.isIn(BlockTags.LEAVES) && material != Material.GOURD ? 1.0F : 1.5F;
		}
	}

	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damageItem(1, attacker, (p_220045_0_) -> {
			p_220045_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
		});
		return true;
	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack item = playerIn.getHeldItem(handIn);
		if (playerIn.isShiftKeyDown()) {
			Vec3d look = new Vec3d(0, 0, 0);
			boolean block = false;
			if (!playerIn.pick(100.0D, 1.0F, false).getType().equals(Type.MISS)) {
				look = playerIn.pick(100.D, 1.0F, false).getHitVec();
				block = true;
			}

			if (block) {
				LightningBoltEntity lightning = new LightningBoltEntity(worldIn, look.x, look.y, look.z, false);
				lightning.setGlowing(true);
				if (!worldIn.isRemote) {
					((ServerWorld) worldIn).addLightningBolt(lightning);
				}
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		} else {
			if (playerIn.onGround || playerIn.isInWaterOrBubbleColumn()) {
				float yaw = playerIn.rotationYaw;
				float pitch = playerIn.rotationPitch;

				int launchFactor = 3;

				float f1 = -MathHelper.sin(yaw * ((float) Math.PI / 180F))
						* MathHelper.cos(pitch * ((float) Math.PI / 180F));
				float f2 = -MathHelper.sin(pitch * ((float) Math.PI / 180F));
				;
				float f3 = MathHelper.cos(yaw * ((float) Math.PI / 180F))
						* MathHelper.cos(pitch * ((float) Math.PI / 180F));
				float f4 = MathHelper.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
				float f5 = 3.0F * ((1.0F + (float) launchFactor) / 4.0F);

				f1 = f1 * (f5 / f4);
				f2 = f2 * (f5 / f4);
				f3 = f3 * (f5 / f4);

				playerIn.addVelocity((double) f1, (double) f2, (double) f3);
				//playerIn.startSpinAttack(20);
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
			}else {
				return new ActionResult<ItemStack>(ActionResultType.FAIL, item);
			}
		}
	}

	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos,
			LivingEntity entityLiving) {
		if (state.getBlockHardness(worldIn, pos) != 0.0F) {
			stack.damageItem(2, entityLiving, (p_220044_0_) -> {
				p_220044_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
			});
		}

		return true;
	}

	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EquipmentSlotType.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER,
					"Weapon modifier", (double) this.ATTACKDAMAGE, AttributeModifier.Operation.ADDITION));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER,
					"Weapon modifier", (double) this.ATTACKSPEED, AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}
}
