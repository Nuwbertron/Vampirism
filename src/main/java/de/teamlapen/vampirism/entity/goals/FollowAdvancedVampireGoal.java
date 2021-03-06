package de.teamlapen.vampirism.entity.goals;

import de.teamlapen.vampirism.entity.vampire.AdvancedVampireEntity;
import de.teamlapen.vampirism.entity.vampire.BasicVampireEntity;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * Makes the basic hunter follow a nearby advanced vampires
 */
public class FollowAdvancedVampireGoal extends Goal {

    protected final BasicVampireEntity entity;
    protected final double speed;
    /**
     * Maximum distance before the entity starts following the advanced vampire
     */
    private final int DIST = 20;
    private int delayCounter;

    public FollowAdvancedVampireGoal(BasicVampireEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.entity.getAdvancedLeader() == null) {
            return false;
        } else {
            double d0 = this.entity.getDistanceSq(this.entity.getAdvancedLeader());
            return d0 >= DIST && d0 <= 256.0D;
        }
    }

    @Override
    public boolean shouldExecute() {

        AdvancedVampireEntity leader = entity.getAdvancedLeader();
        if (leader != null) {
            return leader.isAlive() && this.entity.getDistanceSq(leader) > DIST;
        }

        List<AdvancedVampireEntity> list = this.entity.getEntityWorld().getEntitiesWithinAABB(AdvancedVampireEntity.class, this.entity.getBoundingBox().grow(8, 4, 8));

        double d0 = Double.MAX_VALUE;

        for (AdvancedVampireEntity entity1 : list) {
            if (entity1.isAlive() && entity1.getFollowingCount() < entity1.getMaxFollowerCount()) {
                double d1 = this.entity.getDistanceSq(entity1);

                if (d1 <= d0) {
                    d0 = d1;
                    leader = entity1;
                }
            }
        }

        if (leader == null) return false;
        else {
            entity.setAdvancedLeader(leader);
            leader.increaseFollowerCount();
            return this.entity.getDistanceSq(leader) > DIST;
        }
    }

    @Override
    public void startExecuting() {
        delayCounter = 0;
    }

    @Override
    public void tick() {
        if (--this.delayCounter <= 0 && entity.getAdvancedLeader() != null) {
            this.delayCounter = 10;
            this.entity.getNavigator().tryMoveToEntityLiving(this.entity.getAdvancedLeader(), this.speed);
            this.entity.lookAt(EntityAnchorArgument.Type.EYES, this.entity.getAdvancedLeader().getPositionVector().add(0, this.entity.getAdvancedLeader().getEyeHeight(), 0));
        }
    }
}
