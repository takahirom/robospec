# RoboSpec

RoboSpec is a library that provides a way to write tests using `describe` and `itShould` blocks with Robolectric.

## Motivation

I've been using [Robolectric](https://robolectric.org/) with [Roborazzi](https://github.com/takahirom/roborazzi) for testing.
My test names were always something like `launch_article_list_shot()`. These names make it hard to understand what each test does.
Also, when using this for screenshot testing, it's not clear what each screenshot shows or if it's correct.

Kotest provides [a more readable way](https://kotest.io/docs/framework/testing-styles.html#describe-spec) to write tests. It allows us to write tests using `describe` and `it`, similar to RSpec.
However, Robolectric doesn't currently support JUnit5 (although this may change in the future). Consequently, we can't use Kotest with Robolectric.

I discovered that we can use Robolectric's `ParameterizedRobolectricTestRunner` to structure our tests more descriptively.

## What is RoboSpec?

RoboSpec is a library that provides a way to write tests using `describe` and `itShould` blocks with Robolectric.

You can call `describeBehaviors` to create a list of `DescribedBehavior` objects and run each test with the `execute()` method.

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
// You don't need to use @HiltAndroidTest if you don't use Hilt
@HiltAndroidTest
class ArticleListScreenTest(
    private val behavior: DescribedBehavior<ArticleListScreenRobot>
) {

    // ArticleListScreenRobot is not a part of RoboSpec.
    // I recommend that you provide a Robot or PageObject for easy testing
    @Inject
    lateinit var articleListScreenRobot: ArticleListScreenRobot

    @Test
    fun test() {
        runTest {
            behavior.execute(articleListScreenRobot)
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun behaviors(): List<DescribedBehavior<ArticleListScreenRobot>> {
            // describeBehaviors is a function that creates a list of DescribedBehavior
            return describeBehaviors<ArticleListScreenRobot>(name = "ArticleListScreen") {
                describe("when server is operational") {
                    doIt {
                        // setupArticleListServer and setupArticleListScreenContent 
                        // are functions of ArticleListScreenRobot
                        setupArticleListServer(ServerStatus.Operational)
                        setupArticleListScreenContent()
                    }
                    itShould("show article items") {
                        // captureScreenWithChecks and checkArticleListItemsDisplayed
                        // are functions of ArticleListScreenRobot
                        captureScreenWithChecks(checks = {
                            checkArticleListItemsDisplayed()
                        })
                    }
                    describe("click first article bookmark") {
                        doIt {
                            clickFirstArticleBookmark()
                        }
                        itShould("show bookmarked article") {
                            captureScreenWithChecks {
                                checkFirstArticleBookmarked()
                            }
                        }
                    }
                }
                describe("when server is down") {
                    doIt {
                        setupArticleListServer(ServerStatus.Error)
                        setupArticleListScreenContent()
                    }
                    itShould("show error message") {
                        captureScreenWithChecks {
                            checkErrorMessageDisplayed()
                        }
                    }
                }
            }
        }
    }
}
```

This test creates three test cases as parameterized tests:
1. `ArticleListScreenTest.test[when server is operational - it should show article items]`
2. `ArticleListScreenTest.test[when server is operational - click first article bookmark - it should show bookmarked article]`
3. `ArticleListScreenTest.test[when server is down - it should show error message]`

I believe this is more readable than test names like `launch_article_list_shot()`.

## How to Use

This is a very simple and lightweight Kotlin library. In fact, it doesn't even depend on Robolectric. You can use it by adding the following dependency:

```kotlin
testImplementation("io.github.takahirom.robospec:robospec:[latest version]")
```

For version catalogs:

```kotlin
robospec = { module = "io.github.takahirom.robospec:robospec", version.ref = "robospec" }
```

## Advanced Usage

### You can generate any format you want by traversing the `DescribedBehaviors` object.

For example, you can create YAML files by using `toYamlString()`.

```kotlin
val behaviors = describeBehaviors<Unit>("root") {
    describe("describe1") {
        doIt("describe1 doIt") { }
        itShould("itShould1") { }
    }
    describe("describe2") {
        doIt("describe2 doIt") { }
        itShould("itShould2") { }
    }
}
val yaml = behaviors.toYamlString()
println(yaml)
```

```yaml
- root:
    - describe1:
        - doIt: describe1 doIt
        - itShould: itShould1
    - describe2:
        - doIt: describe2 doIt
        - itShould: itShould2
```
