# --- create a third-party/common.BUILD file to add non-managed
# --- third-party dependencies to this file.

# The following dependencies were calculated from:
# /home/morimekta/src/utils/android-util/pom.xml
# /home/morimekta/src/utils/console-util/pom.xml
# /home/morimekta/src/utils/io-util/pom.xml


java_library(
    name = "args4j_args4j",
    visibility = ["//visibility:public"],
    exports = [
        "@args4j_args4j//jar",
    ],
)
java_library(
    name = "junit_junit",
    visibility = ["//visibility:public"],
    testonly = 1,
    exports = [
        "@junit_junit//jar",
        "@org_hamcrest_hamcrest_core//jar",
    ],
)
java_library(
    name = "org_hamcrest_hamcrest_library",
    visibility = ["//visibility:public"],
    testonly = 1,
    exports = [
        "@org_hamcrest_hamcrest_library//jar",
        "@org_hamcrest_hamcrest_core//jar",
    ],
)
