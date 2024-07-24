package io.github.takahirom.robospec

fun <T> createBehaviors(root: TestNode.Describe<T>): DescribedBehaviors<T> {
    val checkNodes = collectCheckNodes(root)
    return DescribedBehaviors(checkNodes.map { createBehavior(it) })
}

private data class AncestryNode<T>(
    val node: TestNode<T>,
    val childIndex: Int,
)

private data class CheckNode<T>(
    val description: String,
    val parents: List<TestNode<T>>,
    val node: TestNode.ItShould<T>,
    val ancestry: List<AncestryNode<T>>,
)

/**
 * Collect all check nodes from the test tree
 */
private fun <T> collectCheckNodes(root: TestNode.Describe<T>): List<CheckNode<T>> {
    val checkNodes = mutableListOf<CheckNode<T>>()

    fun traverse(node: TestNode<T>, parents: List<TestNode<T>>, ancestry: List<AncestryNode<T>>) {
        when (node) {
            is TestNode.Describe -> {
                node.children.forEachIndexed { index, child ->
                    val currentAncestry = ancestry + AncestryNode(node, index)
                    traverse(
                        node = child,
                        parents = parents + node,
                        ancestry = currentAncestry
                    )
                }
            }

            is TestNode.ItShould -> {
                checkNodes.add(
                    CheckNode(
                        node.description,
                        parents = parents + node,
                        node = node,
                        ancestry = ancestry
                    )
                )
            }

            is TestNode.DoIt -> {}
        }
    }

    traverse(root, emptyList(), emptyList())
    return checkNodes
}

private fun <T> createBehavior(checkNode: CheckNode<T>): DescribedBehavior<T> {
    val steps = mutableListOf<TestNode<T>>()

    fun processNode(node: TestNode<T>, ancestry: List<TestNode<T>>, depth: Int) {
        when (node) {
            is TestNode.Describe -> {
                for (child in node.children) {
                    if (depth + 1 < checkNode.ancestry.size && child == checkNode.ancestry[depth + 1].node) {
                        processNode(child, ancestry + node, depth + 1)
                    } else if (child is TestNode.DoIt) {
                        steps.add(child)
                    } else if (child == checkNode.node) {
                        steps.add(child)
                    }
                }
            }

            is TestNode.DoIt -> {
                steps.add(node)
            }

            is TestNode.ItShould -> {
                if (node == checkNode.node) {
                    steps.add(node)
                }
            }
        }
    }

    processNode(checkNode.ancestry.first().node, emptyList(), 0)

    return DescribedBehavior(
        parents = TestNodes(checkNode.parents),
        steps = TestNodes(steps),
        targetCheckDescription = checkNode.description
    )
}
