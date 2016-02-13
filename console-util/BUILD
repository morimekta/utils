java_library(
    name = "console-util",
    srcs = glob(["java/**/*.java"]),
    deps = [
        "//third-party:args4j_args4j",
    ],
    visibility = ['//visibility:public'],
)

java_test(
    name = "javatests",
    srcs = glob(["javatests/**/*.java"]),
    deps = [
        ":console-util",
        "//third-party:args4j_args4j",
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
