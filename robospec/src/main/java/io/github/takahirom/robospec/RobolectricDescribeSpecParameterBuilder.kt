package io.github.takahirom.robospec

var DEBUG = false

inline fun <reified T> describeBehaviors(
    name: String,
    block: BehaviorsTreeBuilder<T>.() -> Unit,
): List<DescribedBehavior<T>> {
    val builder = BehaviorsTreeBuilder<T>(name)
    builder.block()
    val root = builder.build(name = name)
    return createBehaviors(root)
}

suspend fun <T> DescribedBehavior<T>.execute(robot: T) {
    for ((index, step) in steps.withIndex()) {
        if (DEBUG) println("Executing step: $index (${parents.fullDescription()})")
        when (step) {
            is TestNode.DoIt -> step.action(robot)
            is TestNode.ItShould -> {
                if (step.description == targetCheckDescription) {
                    step.action(robot, this)
                }
            }

            is TestNode.Describe -> {}
        }
        if (DEBUG) println("Step executed: $index (${parents.fullDescription()})")
    }
}

class TestNodes<T>(nodes: List<TestNode<T>>) : List<TestNode<T>> by nodes {
    fun fullDescription(): String {
        return joinToString(" - ") { node ->
            when (node) {
                is TestNode.Describe -> node.description
                is TestNode.ItShould -> "it should ${node.description}"
                is TestNode.DoIt -> "do " + node.description.ifEmpty { "no description" }
            }
        }
    }
}

sealed interface TestNode<T> {
    data class Describe<T>(override val description: String, val children: List<TestNode<T>>) :
        TestNode<T>

    data class DoIt<T>(override val description: String, val action: suspend T.() -> Unit) :
        TestNode<T>

    data class ItShould<T>(
        override val description: String,
        val action: suspend T.(describedBehavior: DescribedBehavior<T>) -> Unit
    ) : TestNode<T>

    val description: String
}

data class DescribedBehavior<T>(
    val parents: TestNodes<T>,
    val steps: TestNodes<T>,
    val targetCheckDescription: String,
) {
    override fun toString(): String = parents.fullDescription()
}


class BehaviorsTreeBuilder<T>(private val parentDescription: String = "") {
    private val children = mutableListOf<TestNode<T>>()

    fun describe(description: String, block: BehaviorsTreeBuilder<T>.() -> Unit) {
        val builder = BehaviorsTreeBuilder<T>(parentDescription = description)
        builder.block()
        children.add(TestNode.Describe(description, builder.children))
    }

    fun doIt(description: String = parentDescription, action: suspend T.() -> Unit) {
        children.add(TestNode.DoIt(description) { action() })
    }

    fun itShould(
        description: String,
        action: suspend T.(describedBehavior: DescribedBehavior<T>) -> Unit
    ) {
        children.add(TestNode.ItShould(description) { describedBehavior -> action(describedBehavior) })
    }

    fun build(name: String): TestNode.Describe<T> = TestNode.Describe(name, children)
}

