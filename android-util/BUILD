java_library(
    name = "android-util",
    srcs = glob(["java/**/*.java"]),
    visibility = ['//visibility:public'],
)

java_test(
    name = "javatests",
    srcs = glob(["javatests/**/*.java"]),
    deps = [
        ":android-util",
        "//third-party:junit_junit",
        "//third-party:org_hamcrest_hamcrest_library",
    ],
    size = 'small',
)

test_suite(
    name = "tests",
    tests = [
        ":javatests",
    ]
)
