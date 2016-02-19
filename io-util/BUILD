java_library(
    name = "io-util",
    srcs = glob(["java/**/*.java"]),
    deps = [
    ],
    visibility = ['//visibility:public'],
)

java_test(
    name = "javatests",
    srcs = glob(["javatests/**/*.java"]),
    deps = [
        ":io-util",
        "//third-party:junit_junit",
        "//third-party:org_hamcrest_hamcrest_library",
        "//third-party:org_mockito_mockito_core",
    ],
    runtime_deps = [
        "//third-party:org_objenesis_objenesis",
    ],
    size = 'small',
)

test_suite(
    name = "tests",
    tests = [
        ":javatests",
    ]
)
