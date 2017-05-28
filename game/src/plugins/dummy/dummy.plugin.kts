import org.apollo.game.action.DistancedAction
import org.apollo.game.message.impl.ObjectActionMessage
import org.apollo.game.model.Animation
import org.apollo.game.model.Position
import org.apollo.game.model.entity.Player
import org.apollo.game.model.entity.Skill
import org.apollo.net.message.Message

val DUMMY_ID = 823
val DUMMY_SIZE = 1

/**
 * A list of [ObjectDefinition] identifiers which are training dummies.
 */
val DUMMY_IDS = setOf<Int>(823)

on { ObjectActionMessage::class }
        .where { option == 2 && id in DUMMY_IDS }
        .then { DummyAction.start(this, it, position) }

class DummyAction(val player: Player, position: Position) : DistancedAction<Player>(0, true, player, position, DISTANCE) {

    companion object {

        /**
         * The maximum level a player can be before the dummy stops giving XP.
         */
        const val LEVEL_THRESHOLD = 8

        /**
         * The number of experience points per hit.
         */
        const val EXP_PER_HIT = 5.0

        /**
         * The minimum distance a player can be from the dummy.
         */
        const val DISTANCE = 1

        /**
         * The [Animation] played when a player hits a dummy.
         */
        val PUNCH_ANIMATION = Animation(422)

        /**
         * Starts a [DummyAction] for the specified [Player], terminating the [Message] that triggered it.
         */
        fun start(message: Message, player: Player, position: Position) {
            player.startAction(DummyAction(player, position))
            message.terminate()
        }

    }

    var started = false

    override fun executeAction() {
        if (started) {
            val skills = player.skillSet

            if (skills.getSkill(Skill.ATTACK).maximumLevel >= LEVEL_THRESHOLD) {
                player.sendMessage("There is nothing more you can learn from hitting a dummy.")
            } else {
                skills.addExperience(Skill.ATTACK, EXP_PER_HIT)
            }

            stop()
        } else {
            mob.sendMessage("You hit the dummy.")
            mob.turnTo(this.position)
            mob.playAnimation(PUNCH_ANIMATION)

            started = true
        }
    }

}
