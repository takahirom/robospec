# RoboSpec

## Motivation

I have been testing using [Robolectric](https://robolectric.org/) with [Roborazzi](https://github.com/takahirom/roborazzi).
The test names are always like `launch_article_list_shot()`. This makes it difficult to read and understand what the test is doing.
Also, if you use it for screenshot testing, you can't tell what the screenshot is about or whether it's correct.

Kotest provides [a more readable way](https://kotest.io/docs/framework/testing-styles.html#describe-spec) to write tests. It allows us to write tests using `describe` and `it`, similar to RSpec.
However, Robolectric doesn't currently support JUnit5 (although this may change in the future). Consequently, we can't use Kotest with Robolectric.

I discovered that we can use Robolectric's `ParameterizedRobolectricTestRunner` to structure our tests more descriptively.

## What is RoboSpec?

RoboSpec is a library that provides a way to write tests using `describe` and `itShould` blocks with Robolectric.

You can call `describeBehaviors` to create a list of `DescribedBehavior` objects and run each test with the `execute()` method.

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
@HiltAndroidTest
class ArticleListScreenTest(private val behavior: DescribedBehavior<ArticleListScreenRobot>) {

    // ArticleListScreenRobot is a utility class that provides functions to interact with the screen
    @Inject
    lateinit var articleListScreenRobot: ArticleListScreenRobot

    @Test
    fun test() {
        runRobot(articleListScreenRobot) {
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

This test creates three test cases:
1. `ArticleListScreenTest.test[when server is operational - it should show article items]`
2. `ArticleListScreenTest.test[when server is operational - click first article bookmark - it should show bookmarked article]`
3. `ArticleListScreenTest.test[when server is down - it should show error message]`

I believe this is more readable than test names like `launch_article_list_shot()`.

## How to Use

This is a very simple and pure Kotlin library. In fact, it doesn't even have a dependency on Robolectric. You can use it by adding the following dependency:

TODO: Please wait until the library is published to Maven Central.