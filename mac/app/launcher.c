/*
 * FreeEed.app native stub — Contents/MacOS/FreeEed.
 *
 * WHY a compiled stub instead of a shell script as CFBundleExecutable:
 * notarization and Gatekeeper treat the main executable as code; a signed,
 * hardened-runtime Mach-O is the well-trodden path, while a script as the
 * bundle executable is fragile (signature stored out of line, "damaged app"
 * reports after copying). The stub does nothing but exec the real launcher,
 * Contents/Resources/freeeed-launcher.sh, with /bin/bash.
 *
 * Built universal (arm64 + x86_64) by release_freeeed_complete.sh:
 *   clang -arch arm64 -arch x86_64 -mmacosx-version-min=10.13 -O2 -o FreeEed launcher.c
 */
#include <libgen.h>
#include <limits.h>
#include <mach-o/dyld.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int main(void) {
    char exe[PATH_MAX];
    uint32_t size = sizeof(exe);
    if (_NSGetExecutablePath(exe, &size) != 0) {
        fprintf(stderr, "FreeEed: executable path too long\n");
        return 1;
    }
    char real[PATH_MAX];
    if (realpath(exe, real) == NULL) {
        perror("FreeEed: realpath");
        return 1;
    }
    /* .../FreeEed.app/Contents/MacOS/FreeEed -> .../FreeEed.app/Contents */
    char contents[PATH_MAX];
    strncpy(contents, dirname(dirname(real)), sizeof(contents) - 1);
    contents[sizeof(contents) - 1] = '\0';

    char script[PATH_MAX];
    if (snprintf(script, sizeof(script), "%s/Resources/freeeed-launcher.sh", contents) >= (int)sizeof(script)) {
        fprintf(stderr, "FreeEed: launcher path too long\n");
        return 1;
    }
    /* Finder may pass -psn_* arguments; the launcher takes none, so drop them. */
    char *const argv[] = {"bash", script, NULL};
    execv("/bin/bash", argv);
    perror("FreeEed: exec /bin/bash");
    return 1;
}
