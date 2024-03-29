package net.ccbluex.liquidbounce.managers

import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class CombatManager: MinecraftInstance(), Listenable {
	var inCombat = false
	var target: EntityLivingBase? = null
	private val attackedEntityList = mutableListOf<EntityLivingBase>()

	private val attackTimer = MSTimer()

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		mc.thePlayer ?: return
		attackedEntityList.map{it}.forEach {
			if (it.isDead) {
				LiquidBounce.eventManager.callEvent(EntityKilledEvent(it))
				attackedEntityList.remove(it)
			}
		}

		inCombat = false

		if (!attackTimer.hasTimePassed(250)) {
			inCombat = true
			return
		}

		if (target != null) {
			if (mc.thePlayer.getDistanceToEntity(target) > 7 || !inCombat || target!!.isDead) {
				target = null
			} else {
				inCombat = true
			}
		}
	}

	@EventTarget
	fun onAttack(event: AttackEvent) {
		val target = event.targetEntity

		if (target is EntityLivingBase && EntityUtils.isSelected(target, true)) {
			this.target = target
			if (!attackedEntityList.contains(target)) attackedEntityList.add(target)
		}

		attackTimer.reset()
	}

	@EventTarget
	fun onWorld(event: WorldEvent) {
		attackedEntityList.clear()
		target = null
		inCombat = false
	}

	fun getNearByEntity(radius: Float): EntityLivingBase? {
		return try {
			mc.theWorld.loadedEntityList.filter {
				mc.thePlayer.getDistanceToEntity(it) < radius && EntityUtils.isSelected(it, true)
			}.minByOrNull { it.getDistanceToEntity(mc.thePlayer) } as EntityLivingBase
		} catch (e: Exception) {
			null
		}
	}

	override fun handleEvents() = true

}
