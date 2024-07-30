package io.github.takahirom.robospec

import kotlinx.coroutines.runBlocking
import org.junit.Test

class DSLBehaviorTest {
    @Test
    fun `when just using describe behavior with doIt it should not run`() {
        runBlocking {
            describeBehaviors<Unit>("root") {
                doIt {
                    error("should not be called")
                }
            }.forEach { it.execute(Unit) }
        }
    }

    @Test
    fun `when using describe behavior with doIt and itShould it should run`() {
        runBlocking {
            var doItCalled = false
            var itShouldCalled = false
            describeBehaviors<Unit>("root") {
                doIt {
                    doItCalled = true
                }
                itShould("itShould") {
                    itShouldCalled = true
                }
            }.forEach { it.execute(Unit) }

            assert(doItCalled)
            assert(itShouldCalled)
        }
    }

    @Test
    fun `when using two describe behavior and it should not run other doIt block`() {
        runBlocking {
            describeBehaviors<Unit>("root") {
                describe("describe1") {
                    doIt {
                        error("should not be called")
                    }
                }
                describe("describe2") {
                    itShould("itShould") {
                    }
                }
            }.forEach { it.execute(Unit) }
        }
    }

    @Test
    fun `when we have two itShould block it should run both`() {
        runBlocking {
            var itShould1Called = false
            var itShould2Called = false
            describeBehaviors<Unit>("root") {
                itShould("itShould1") {
                    itShould1Called = true
                }
                itShould("itShould2") {
                    itShould2Called = true
                }
            }.forEach { it.execute(Unit) }

            assert(itShould1Called)
            assert(itShould2Called)
        }
    }

    @Test
    fun `when we have two itShould block it should have two tests`() {
        runBlocking {
            val behaviors = describeBehaviors<Unit>("root") {
                itShould("itShould1") {
                }
                itShould("itShould2") {
                }
            }

            assert(behaviors.size == 2)
        }
    }

    @Test
    fun `when we have two describe block with itShould it should have two tests`() {
        runBlocking {
            val behaviors = describeBehaviors<Unit>("root") {
                describe("describe1") {
                    itShould("itShould1") {
                    }
                }
                describe("describe2") {
                    itShould("itShould2") {
                    }
                }
            }

            assert(behaviors.size == 2)
        }
    }

    @Test
    fun `when running second itShould block it should not run first doIt block`() {
        runBlocking {
            var itShould1Called = false
            var itShould2Called = false
            describeBehaviors<Unit>("root") {
                describe("describe1") {
                    doIt {
                        error("should not be called")
                    }
                    itShould("itShould1") {
                        itShould1Called = true
                    }
                }
                describe("describe2") {
                    itShould("itShould2") {
                        itShould2Called = true
                    }
                }
            }[1].execute(Unit)

            assert(!itShould1Called)
            assert(itShould2Called)
        }
    }

    @Test
    fun `when running nested describe block it should run root doIt`() {
        runBlocking {
            var doItCalled = false
            var itShould1Called = false
            var itShould2Called = false
            val behaviors = describeBehaviors<Unit>("root") {
                doIt("root doIt") {
                    doItCalled = true
                }
                describe("describe1") {
                    itShould("itShould1") {
                        itShould1Called = true
                    }
                }
                describe("describe2") {
                    itShould("itShould2") {
                        itShould2Called = true
                    }
                }
            }
            behaviors[0].execute(Unit)

            assert(behaviors[0].steps.fullDescription() == "do root doIt - it should itShould1")
            assert(behaviors[1].steps.fullDescription() == "do root doIt - it should itShould2")
            assert(doItCalled)
            assert(itShould1Called)
            assert(!itShould2Called)
        }
    }

    @Test
    fun `when has nested description it should have the name for it`() {
        runBlocking {
            var name = ""
            val describedBehaviors = describeBehaviors<Unit>("root") {
                describe("describe1") {
                    describe("describe1-1") {
                        itShould("itShould1-1") { behavior ->
                            name = behavior.toString()
                        }
                    }
                }
            }
            describedBehaviors[0].execute(Unit)

            assert(name == "root - describe1 - describe1-1 - it should itShould1-1")
            assert(describedBehaviors[0].toString() == "root - describe1 - describe1-1 - it should itShould1-1")
        }
    }

    @Test
    fun `when have two describe block with itShould it sholud be able get yaml`() {
        runBlocking {
            val behaviors = describeBehaviors<Unit>("root") {
                describe("describe1") {
                    doIt("describe1 doIt") { }
                    itShould("itShould1") {
                    }
                }
                describe("describe2") {
                    doIt("describe2 doIt") { }
                    itShould("itShould2") {
                    }
                }
            }
            val yaml = behaviors.toYamlString()
            println(yaml)
            val expected = """
               |- root:
               |  - describe1:
               |    - doIt: describe1 doIt
               |    - itShould: itShould1
               |  - describe2:
               |    - doIt: describe2 doIt
               |    - itShould: itShould2
            |""".trimMargin()
            println(expected)
            assert(yaml == expected)
        }
    }
}